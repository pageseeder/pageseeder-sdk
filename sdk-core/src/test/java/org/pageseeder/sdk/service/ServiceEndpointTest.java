package org.pageseeder.sdk.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class ServiceEndpointTest {

  @Test
  void shouldUseMethodAndPathTemplateForEquality() {
    ServiceEndpoint endpoint = ServiceEndpoint.of("GET", "/members/{member}");
    ServiceEndpoint same = ServiceEndpoint.of("GET", "/members/{member}");
    ServiceEndpoint differentMethod = ServiceEndpoint.of("POST", "/members/{member}");
    ServiceEndpoint differentPath = ServiceEndpoint.of("GET", "/groups/{group}");

    assertEquals(endpoint, same);
    assertEquals(endpoint.hashCode(), same.hashCode());
    assertNotEquals(endpoint, differentMethod);
    assertNotEquals(endpoint, differentPath);
  }

  @Test
  void shouldProvideReadableStringRepresentation() {
    ServiceEndpoint endpoint = ServiceEndpoint.of("GET", "/members/{member}");

    assertEquals("GET /members/{member}", endpoint.toString());
  }
}
