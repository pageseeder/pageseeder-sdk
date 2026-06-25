package org.pageseeder.sdk.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * URI path template helper for PageSeeder services.
 *
 * <p>A path template represents only the path component of a URI. It must start
 * with {@code /} and must not include query or fragment components such as
 * {@code ?format=json} or {@code #section}. Variables are written between braces
 * and their names must match {@code \w+}; in practice, use letters, digits, and
 * underscores only.
 *
 * <p>Examples of valid templates:
 *
 * <pre>
 * PathTemplate member = new PathTemplate("/members/&#123;member&#125;");
 * PathTemplate group = new PathTemplate("/members/&#123;member&#125;/groups/&#123;group&#125;");
 * PathTemplate document = new PathTemplate("/groups/&#123;group&#125;/documents/&#123;document_id&#125;");
 * </pre>
 *
 * <p>Examples of invalid templates:
 *
 * <pre>
 * new PathTemplate("members/&#123;member&#125;");          // must start with '/'
 * new PathTemplate("/members/&#123;member&#125;?format=json"); // no query component
 * new PathTemplate("/members/&#123;member-id&#125;");     // '-' is not valid in a variable name
 * new PathTemplate("/members/&#123;member");         // unclosed variable
 * </pre>
 *
 * <p>Resolve a template by passing all variables used by the path. Values are
 * encoded as URI path segments, so spaces and other reserved characters are
 * escaped before the final path is returned.
 *
 * <pre>
 * PathTemplate template = new PathTemplate("/members/&#123;member&#125;/groups/&#123;group&#125;");
 * Map&lt;String, Object&gt; variables = new LinkedHashMap&lt;&gt;();
 * variables.put("member", "john smith");
 * variables.put("group", "Editors");
 *
 * String path = template.resolve(variables);
 * // path is "/members/john%20smith/groups/Editors"
 * </pre>
 *
 * @param template the URI path template
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record PathTemplate(String template) {

  private static final int MAX_TEMPLATE_LENGTH = 2048;
  private static final int MAX_VARIABLE_NAME_LENGTH = 255;
  private static final Pattern VARIABLE_NAME = Pattern.compile("\\w+");

  /**
   * Creates a new path template.
   *
   * <p>The supplied template is valid when it:
   * <ul>
   *   <li>is not blank;</li>
   *   <li>starts with {@code /};</li>
   *   <li>is at most 2048 characters long;</li>
   *   <li>does not contain query or fragment components;</li>
   *   <li>contains only balanced path variables such as {@code {member}};</li>
   *   <li>uses variable names matching {@code \w+} and at most 255 characters long;</li>
   *   <li>is a valid URI path once variables are replaced by a placeholder.</li>
   * </ul>
   *
   * @param template The path template.
   *
   * @throws NullPointerException     if the template is {@code null}
   * @throws IllegalArgumentException if the template is not a valid path template
   */
  public PathTemplate {
    String pathTemplate = Objects.requireNonNull(template, "Path template must not be null.");
    if (pathTemplate.isBlank() || pathTemplate.charAt(0) != '/') {
      throw new IllegalArgumentException("Path template must start with '/'.");
    }
    if (pathTemplate.length() > MAX_TEMPLATE_LENGTH) {
      throw new IllegalArgumentException("Path template must not exceed " + MAX_TEMPLATE_LENGTH + " characters.");
    }
    validateTemplate(pathTemplate);
    template = pathTemplate;
  }

  /**
   * Resolves the path template with the specified variables.
   *
   * <p>Every variable in the template must be provided. Variable names passed to
   * this method are validated with the same rules as variable names declared in
   * the template. Values must not be {@code null}.
   *
   * @param variables The variables to resolve.
   *
   * @return The resolved path template.
   *
   * @throws NullPointerException     if a variable value is {@code null}
   * @throws IllegalArgumentException if a variable name is invalid or if any
   *                                  template variable remains unresolved
   */
  public String resolve(Map<String, ?> variables) {
    String resolved = this.template;
    for (Map.Entry<String, ?> entry : variables.entrySet()) {
      String name = validateVariableName(entry.getKey());
      String key = "{" + name + "}";
      Object value = Objects.requireNonNull(entry.getValue(), "Path variable " + name);
      String segment = toPathSegment(value);
      resolved = resolved.replace(key, encodePathSegment(segment));
    }
    if (resolved.contains("{")) {
      throw new IllegalArgumentException("Unresolved path template variables in " + resolved);
    }
    return resolved;
  }

  /**
   * Convenience factory for a single-variable map, for use with {@link #resolve(Map)}.
   *
   * @param key   The variable name.
   * @param value The variable value.
   * @return A mutable map pre-populated with the single entry.
   *
   * @throws NullPointerException     if the variable name is {@code null}
   * @throws IllegalArgumentException if the variable name is invalid
   */
  public static Map<String, Object> variables(String key, Object value) {
    Map<String, Object> variables = new LinkedHashMap<>();
    variables.put(validateVariableName(key), value);
    return variables;
  }

  @Override
  public String toString() {
    return this.template;
  }

  /**
   * Characters allowed unencoded in a URI path segment (RFC 3986 {@code pchar}).
   *
   * <pre>pchar = unreserved / pct-encoded / sub-delims / ":" / "@"</pre>
   */
  private static final BitSet PCHAR_SAFE = new BitSet(128);
  static {
    for (int i = 'a'; i <= 'z'; i++) PCHAR_SAFE.set(i);
    for (int i = 'A'; i <= 'Z'; i++) PCHAR_SAFE.set(i);
    for (int i = '0'; i <= '9'; i++) PCHAR_SAFE.set(i);
    "-._~!$&'()*+,;=:@".chars().forEach(PCHAR_SAFE::set);
  }

  /**
   * Converts a path variable value to its string representation, prepending the {@code ~} prefix
   * when needed to prevent PageSeeder from interpreting a string identifier as a numeric ID.
   *
   * <p>The prefix is added when the value is a {@link String} that looks like a numeric ID
   * (all digits, no leading zero). {@link Number} values are always treated as IDs and are
   * never prefixed.
   */
  static String toPathSegment(Object value) {
    if (value instanceof Number) return value.toString();
    String s = String.valueOf(value);
    return isMaybeID(s) ? "~" + s : s;
  }

  private static boolean isMaybeID(String value) {
    if (value.isEmpty() || value.charAt(0) == '0') return false;
    for (int i = 0; i < value.length(); i++) {
      if (!Character.isDigit(value.charAt(i))) return false;
    }
    return true;
  }

  static String encodePathSegment(String value) {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder(bytes.length);
    for (byte b : bytes) {
      int c = b & 0xFF;
      if (c < 128 && PCHAR_SAFE.get(c)) {
        sb.append((char) c);
      } else {
        sb.append('%');
        sb.append(Character.toUpperCase(Character.forDigit(c >> 4, 16)));
        sb.append(Character.toUpperCase(Character.forDigit(c & 0xF, 16)));
      }
    }
    return sb.toString();
  }

  private static void validateTemplate(String template) {
    if (template.indexOf('?') >= 0 || template.indexOf('#') >= 0) {
      throw new IllegalArgumentException("Path template must not contain query or fragment components.");
    }
    StringBuilder normalized = new StringBuilder(template.length());
    int i = 0;
    int length = template.length();
    while (i < length) {
      char current = template.charAt(i);
      if (current == '{') {
        int end = template.indexOf('}', i + 1);
        if (end < 0) {
          throw new IllegalArgumentException("Unclosed path variable in " + template);
        }
        String name = template.substring(i + 1, end);
        validateVariableName(name);
        normalized.append('x');
        i = end + 1;
      } else if (current == '}') {
        throw new IllegalArgumentException("Unexpected closing brace in " + template);
      } else {
        normalized.append(current);
        i++;
      }
    }
    try {
      URI uri = new URI(null, null, normalized.toString(), null);
      if (!normalized.toString().equals(uri.getRawPath())) {
        throw new IllegalArgumentException("Invalid path template: " + template);
      }
    } catch (Exception ex) {
      throw new IllegalArgumentException("Invalid path template: " + template, ex);
    }
  }

  private static String validateVariableName(String name) {
    String variableName = Objects.requireNonNull(name, "Path variable name must not be null.");
    if (variableName.length() > MAX_VARIABLE_NAME_LENGTH) {
      throw new IllegalArgumentException("Path variable name must not exceed " + MAX_VARIABLE_NAME_LENGTH + " characters.");
    }
    if (!VARIABLE_NAME.matcher(variableName).matches()) {
      throw new IllegalArgumentException("Path variable name must match \\w+: " + variableName);
    }
    return variableName;
  }
}
