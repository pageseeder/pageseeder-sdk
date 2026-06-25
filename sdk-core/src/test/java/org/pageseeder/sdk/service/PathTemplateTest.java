package org.pageseeder.sdk.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

final class PathTemplateTest {

  @Test
  void shouldResolveValidTemplate() {
    PathTemplate template = new PathTemplate("/members/{member}/groups/{group}");
    Map<String, Object> variables = new LinkedHashMap<>();
    variables.put("member", "john smith");
    variables.put("group", "Editors");

    assertEquals("/members/john%20smith/groups/Editors", template.resolve(variables));
  }

  @Test
  void shouldPreserveAtSignInPathVariable() {
    PathTemplate template = new PathTemplate("/members/{member}/memberships");
    String resolved = template.resolve(PathTemplate.variables("member", "user@example.com"));
    assertEquals("/members/user@example.com/memberships", resolved);
  }

  @Test
  void shouldPreserveTildeInPathVariable() {
    PathTemplate template = new PathTemplate("/members/{member}/memberships");
    String resolved = template.resolve(PathTemplate.variables("member", "~jsmith"));
    assertEquals("/members/~jsmith/memberships", resolved);
  }

  @Test
  void shouldEncodeSlashInPathVariable() {
    PathTemplate template = new PathTemplate("/groups/{group}");
    String resolved = template.resolve(PathTemplate.variables("group", "a/b"));
    assertEquals("/groups/a%2Fb", resolved);
  }

  @Test
  void shouldEncodeSpaceAsPercent20() {
    PathTemplate template = new PathTemplate("/groups/{group}");
    String resolved = template.resolve(PathTemplate.variables("group", "my group"));
    assertEquals("/groups/my%20group", resolved);
  }

  @Test
  void shouldPreserveSubDelimsAndColonInPathVariable() {
    PathTemplate template = new PathTemplate("/path/{value}");
    String resolved = template.resolve(PathTemplate.variables("value", "a:b@c!d$e"));
    assertEquals("/path/a:b@c!d$e", resolved);
  }

  @Test
  void shouldEncodeNonAsciiCharacters() {
    PathTemplate template = new PathTemplate("/path/{value}");
    String resolved = template.resolve(PathTemplate.variables("value", "café"));
    assertEquals("/path/caf%C3%A9", resolved);
  }

  // ~ prefix tests

  @Test
  void shouldPrefixTildeForNumericStringValue() {
    PathTemplate template = new PathTemplate("/members/{member}");
    String resolved = template.resolve(PathTemplate.variables("member", "12345"));
    assertEquals("/members/~12345", resolved);
  }

  @Test
  void shouldNotPrefixTildeForNumericTypeValue() {
    PathTemplate template = new PathTemplate("/members/{member}");
    String resolved = template.resolve(PathTemplate.variables("member", 12345L));
    assertEquals("/members/12345", resolved);
  }

  @Test
  void shouldNotPrefixTildeForNonNumericString() {
    PathTemplate template = new PathTemplate("/members/{member}");
    String resolved = template.resolve(PathTemplate.variables("member", "jsmith"));
    assertEquals("/members/jsmith", resolved);
  }

  @Test
  void shouldNotPrefixTildeForLeadingZeroString() {
    PathTemplate template = new PathTemplate("/members/{member}");
    String resolved = template.resolve(PathTemplate.variables("member", "007"));
    assertEquals("/members/007", resolved);
  }

  @Test
  void shouldNotPrefixTildeForEmailAddress() {
    PathTemplate template = new PathTemplate("/members/{member}");
    String resolved = template.resolve(PathTemplate.variables("member", "user@example.com"));
    assertEquals("/members/user@example.com", resolved);
  }

  @Test
  void shouldNotPrefixTildeForIntegerTypeValue() {
    PathTemplate template = new PathTemplate("/members/{member}");
    String resolved = template.resolve(PathTemplate.variables("member", 42));
    assertEquals("/members/42", resolved);
  }

  // encodePathSegment (RFC 3986 pchar) tests

  @Test
  void shouldNotEncodeUnreservedCharacters() {
    assertEquals("azAZ09", PathTemplate.encodePathSegment("azAZ09"));
    assertEquals("-._~", PathTemplate.encodePathSegment("-._~"));
  }

  @Test
  void shouldNotEncodeSubDelimiters() {
    assertEquals("!$&'()*+,;=", PathTemplate.encodePathSegment("!$&'()*+,;="));
  }

  @Test
  void shouldNotEncodeColonOrAtSign() {
    assertEquals(":@", PathTemplate.encodePathSegment(":@"));
  }

  @Test
  void shouldEncodeSpace() {
    assertEquals("hello%20world", PathTemplate.encodePathSegment("hello world"));
  }

  @Test
  void shouldEncodeSlash() {
    assertEquals("a%2Fb", PathTemplate.encodePathSegment("a/b"));
  }

  @Test
  void shouldEncodeQuestionMarkAndHash() {
    assertEquals("%3F%23", PathTemplate.encodePathSegment("?#"));
  }

  @Test
  void shouldEncodeSquareBrackets() {
    assertEquals("%5B%5D", PathTemplate.encodePathSegment("[]"));
  }

  @Test
  void shouldEncodePercent() {
    assertEquals("%25", PathTemplate.encodePathSegment("%"));
  }

  @Test
  void shouldEncodeMultiByteUtf8() {
    assertEquals("%C3%A9", PathTemplate.encodePathSegment("é"));
    assertEquals("%E4%B8%AD", PathTemplate.encodePathSegment("中"));
    assertEquals("%F0%9F%98%80", PathTemplate.encodePathSegment("😀"));
  }

  @Test
  void shouldReturnEmptyStringForEmptyInput() {
    assertEquals("", PathTemplate.encodePathSegment(""));
  }

  @Test
  void shouldEncodeOnlyUnsafeCharactersInMixedInput() {
    assertEquals("user@example.com", PathTemplate.encodePathSegment("user@example.com"));
    assertEquals("hello%20world%2Fpath", PathTemplate.encodePathSegment("hello world/path"));
    assertEquals("100%25%20done", PathTemplate.encodePathSegment("100% done"));
  }

  // Template resolution tests

  @Test
  void shouldRejectInvalidVariableNameInTemplate() {
    assertIllegalArgument("Path variable name must match \\w+: +fragment",
        () -> new PathTemplate("/uris/{uri}/fragments/{+fragment}"));
  }

  @Test
  void shouldRejectInvalidUriPathTemplate() {
    assertIllegalArgument("Path template must not contain query or fragment components.",
        () -> new PathTemplate("/members/{member}?format=json"));
  }

  @Test
  void shouldRejectInvalidVariableNameWhenResolving() {
    PathTemplate template = new PathTemplate("/members/{member}");

    assertIllegalArgument("Path variable name must match \\w+: member-id",
        () -> template.resolve(PathTemplate.variables("member-id", "jsmith")));
  }

  @Test
  void shouldRejectUnexpectedClosingBrace() {
    assertIllegalArgument("Unexpected closing brace in /members/member}",
        () -> new PathTemplate("/members/member}"));
  }

  @Test
  void shouldRejectVariableNamesLongerThan255Characters() {
    String longName = "a".repeat(256);

    assertIllegalArgument("Path variable name must not exceed 255 characters.",
        () -> new PathTemplate("/members/{" + longName + "}"));
  }

  @Test
  void shouldRejectPathTemplatesLongerThan2048Characters() {
    String longTemplate = "/" + "a".repeat(2048);

    assertIllegalArgument("Path template must not exceed 2048 characters.",
        () -> new PathTemplate(longTemplate));
  }

  @Test
  void shouldRejectUnresolvedVariablesOnResolve() {
    PathTemplate template = new PathTemplate("/members/{member}");

    assertIllegalArgument("Unresolved path template variables in /members/{member}",
        () -> template.resolve(new java.util.LinkedHashMap<>()));
  }

  @Test
  void shouldRejectTemplateNotStartingWithSlash() {
    assertIllegalArgument("Path template must start with '/'.",
        () -> new PathTemplate("members/{member}"));
  }

  private static void assertIllegalArgument(String message, Runnable runnable) {
    try {
      runnable.run();
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException ex) {
      assertEquals(message, ex.getMessage());
    }
  }
}
