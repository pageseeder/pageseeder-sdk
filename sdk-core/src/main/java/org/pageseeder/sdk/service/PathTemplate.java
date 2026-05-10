package org.pageseeder.sdk.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * URI template helper for PageSeeder services.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PathTemplate {

  private static final int MAX_TEMPLATE_LENGTH = 2048;
  private static final int MAX_VARIABLE_NAME_LENGTH = 255;
  private static final Pattern VARIABLE_NAME = Pattern.compile("\\w+");

  private final String template;

  /**
   * Creates a new path template.
   *
   * @param template The path template.
   */
  public PathTemplate(String template) {
    String pathTemplate = Objects.requireNonNull(template, "Path template must not be null.");
    if (pathTemplate.isBlank() || pathTemplate.charAt(0) != '/') {
      throw new IllegalArgumentException("Path template must start with '/'.");
    }
    if (pathTemplate.length() > MAX_TEMPLATE_LENGTH) {
      throw new IllegalArgumentException("Path template must not exceed " + MAX_TEMPLATE_LENGTH + " characters.");
    }
    validateTemplate(pathTemplate);
    this.template = pathTemplate;
  }

  /**
   * Resolves the path template with the specified variables.
   *
   * @param variables The variables to resolve.
   *
   * @return The resolved path template.
   */
  public String resolve(Map<String, ?> variables) {
    String resolved = this.template;
    for (Map.Entry<String, ?> entry : variables.entrySet()) {
      String name = validateVariableName(entry.getKey());
      String key = "{" + name + "}";
      Object value = Objects.requireNonNull(entry.getValue(), "Path variable " + name);
      resolved = resolved.replace(key, encodePathSegment(String.valueOf(value)));
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
   */
  public static Map<String, Object> variables(String key, Object value) {
    Map<String, Object> variables = new LinkedHashMap<>();
    variables.put(validateVariableName(key), value);
    return variables;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PathTemplate)) return false;
    PathTemplate that = (PathTemplate) o;
    return this.template.equals(that.template);
  }

  @Override
  public int hashCode() {
    return this.template.hashCode();
  }

  @Override
  public String toString() {
    return this.template;
  }

  private static String encodePathSegment(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }

  private static void validateTemplate(String template) {
    if (template.indexOf('?') >= 0 || template.indexOf('#') >= 0) {
      throw new IllegalArgumentException("Path template must not contain query or fragment components.");
    }
    StringBuilder normalized = new StringBuilder(template.length());
    for (int i = 0; i < template.length(); i++) {
      char current = template.charAt(i);
      if (current == '{') {
        int end = template.indexOf('}', i + 1);
        if (end < 0) {
          throw new IllegalArgumentException("Unclosed path variable in " + template);
        }
        String name = template.substring(i + 1, end);
        validateVariableName(name);
        normalized.append('x');
        i = end;
      } else if (current == '}') {
        throw new IllegalArgumentException("Unexpected closing brace in " + template);
      } else {
        normalized.append(current);
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
