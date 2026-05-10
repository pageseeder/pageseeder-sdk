package org.pageseeder.sdk.service;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class PathTemplateTest {

  @Test
  public void shouldResolveValidTemplate() {
    PathTemplate template = new PathTemplate("/members/{member}/groups/{group}");
    Map<String, Object> variables = new LinkedHashMap<>();
    variables.put("member", "john smith");
    variables.put("group", "Editors");

    assertEquals("/members/john%20smith/groups/Editors", template.resolve(variables));
  }

  @Test
  public void shouldRejectInvalidVariableNameInTemplate() {
    assertIllegalArgument("Path variable name must match \\w+: +fragment",
        () -> new PathTemplate("/uris/{uri}/fragments/{+fragment}"));
  }

  @Test
  public void shouldRejectInvalidUriPathTemplate() {
    assertIllegalArgument("Path template must not contain query or fragment components.",
        () -> new PathTemplate("/members/{member}?format=json"));
  }

  @Test
  public void shouldRejectInvalidVariableNameWhenResolving() {
    PathTemplate template = new PathTemplate("/members/{member}");

    assertIllegalArgument("Path variable name must match \\w+: member-id",
        () -> template.resolve(PathTemplate.variables("member-id", "jsmith")));
  }

  @Test
  public void shouldRejectUnexpectedClosingBrace() {
    assertIllegalArgument("Unexpected closing brace in /members/member}",
        () -> new PathTemplate("/members/member}"));
  }

  @Test
  public void shouldRejectVariableNamesLongerThan255Characters() {
    String longName = "a".repeat(256);

    assertIllegalArgument("Path variable name must not exceed 255 characters.",
        () -> new PathTemplate("/members/{" + longName + "}"));
  }

  @Test
  public void shouldRejectPathTemplatesLongerThan2048Characters() {
    String longTemplate = "/" + "a".repeat(2048);

    assertIllegalArgument("Path template must not exceed 2048 characters.",
        () -> new PathTemplate(longTemplate));
  }

  @Test
  public void shouldRejectUnresolvedVariablesOnResolve() {
    PathTemplate template = new PathTemplate("/members/{member}");

    assertIllegalArgument("Unresolved path template variables in /members/{member}",
        () -> template.resolve(new java.util.LinkedHashMap<>()));
  }

  @Test
  public void shouldRejectTemplateNotStartingWithSlash() {
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
