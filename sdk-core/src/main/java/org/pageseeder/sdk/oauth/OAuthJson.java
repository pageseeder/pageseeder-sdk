package org.pageseeder.sdk.oauth;

import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal JSON support for OAuth token payloads.
 */
final class OAuthJson {

  private OAuthJson() {
  }

  /**
   * Scans a flat JSON object into string values.
   *
   * <p>This intentionally avoids a JSON dependency in {@code sdk-core}. It handles the scalar
   * values used in OAuth responses and stores nested objects or arrays as raw JSON.
   */
  static Map<String, String> parseFlatObject(String json) {
    return new FlatObjectParser(json).parse();
  }

  static Map<String, String> parseBase64UrlFlatObject(String segment) {
    try {
      byte[] bytes = Base64.getUrlDecoder().decode(segment);
      return parseFlatObject(new String(bytes, StandardCharsets.UTF_8));
    } catch (IllegalArgumentException ex) {
      return Map.of();
    }
  }

  private static final class FlatObjectParser {

    private final String json;
    private int offset;

    private FlatObjectParser(String json) {
      this.json = json;
    }

    private Map<String, String> parse() {
      Map<String, String> values = new LinkedHashMap<>();
      skipWhitespace();
      if (!consume('{')) {
        return values;
      }
      while (hasMorePairs()) {
        String key = readString();
        if (key == null || !consumeNameSeparator()) {
          break;
        }
        String value = readValue();
        if (value != null) {
          values.put(key, value);
        }
      }
      return values;
    }

    private boolean hasMorePairs() {
      skipWhitespace();
      return !consume('}') && current() == '"';
    }

    private boolean consumeNameSeparator() {
      skipWhitespace();
      if (!consume(':')) {
        return false;
      }
      skipWhitespace();
      return true;
    }

    private @Nullable String readValue() {
      char current = current();
      if (current == '"') {
        return readString();
      }
      if (current == '{' || current == '[') {
        return readNestedValue();
      }
      return readScalarValue();
    }

    private @Nullable String readScalarValue() {
      int start = this.offset;
      while (this.offset < this.json.length() && this.json.charAt(this.offset) != ','
          && this.json.charAt(this.offset) != '}') {
        this.offset++;
      }
      String value = this.json.substring(start, this.offset).trim();
      consumeValueSeparator();
      return "null".equals(value) ? null : value;
    }

    @SuppressWarnings("java:S3776") // Better to keep this method for readability
    private @Nullable String readNestedValue() {
      int start = this.offset;
      int depth = 0;
      boolean inString = false;
      boolean escaped = false;
      while (this.offset < this.json.length()) {
        char c = this.json.charAt(this.offset++);
        if (escaped) {
          escaped = false;
        } else if (c == '\\' && inString) {
          escaped = true;
        } else if (c == '"') {
          inString = !inString;
        } else if (!inString) {
          depth += opensNestedValue(c) ? 1 : 0;
          depth -= closesNestedValue(c) ? 1 : 0;
          if (depth == 0) {
            String value = this.json.substring(start, this.offset);
            consumeValueSeparator();
            return value;
          }
        }
      }
      return null;
    }

    private static boolean opensNestedValue(char c) {
      return c == '{' || c == '[';
    }

    private static boolean closesNestedValue(char c) {
      return c == '}' || c == ']';
    }

    private void consumeValueSeparator() {
      skipWhitespace();
      consume(',');
    }

    private @Nullable String readString() {
      if (!consume('"')) {
        return null;
      }
      StringBuilder value = new StringBuilder();
      while (this.offset < this.json.length()) {
        char c = this.json.charAt(this.offset++);
        if (c == '"') {
          consumeValueSeparator();
          return value.toString();
        }
        appendStringCharacter(value, c);
      }
      return null;
    }

    private void appendStringCharacter(StringBuilder value, char c) {
      if (c != '\\' || this.offset >= this.json.length()) {
        value.append(c);
        return;
      }
      appendEscapedCharacter(value, this.json.charAt(this.offset++));
    }

    private void appendEscapedCharacter(StringBuilder value, char c) {
      switch (c) {
        case '"' -> value.append('"');
        case '\\' -> value.append('\\');
        case '/' -> value.append('/');
        case 'n' -> value.append('\n');
        case 'r' -> value.append('\r');
        case 't' -> value.append('\t');
        case 'b' -> value.append('\b');
        case 'f' -> value.append('\f');
        case 'u' -> appendUnicodeCharacter(value);
        default -> {
          value.append('\\');
          value.append(c);
        }
      }
    }

    private void appendUnicodeCharacter(StringBuilder value) {
      if (this.offset + 4 > this.json.length()) {
        value.append("\\u");
        return;
      }
      try {
        value.append((char) Integer.parseInt(this.json.substring(this.offset, this.offset + 4), 16));
        this.offset += 4;
      } catch (NumberFormatException ex) {
        value.append("\\u");
      }
    }

    private boolean consume(char expected) {
      if (current() != expected) {
        return false;
      }
      this.offset++;
      return true;
    }

    private char current() {
      return this.offset < this.json.length() ? this.json.charAt(this.offset) : '\0';
    }

    private void skipWhitespace() {
      while (this.offset < this.json.length() && Character.isWhitespace(this.json.charAt(this.offset))) {
        this.offset++;
      }
    }
  }
}
