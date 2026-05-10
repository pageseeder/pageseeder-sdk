package org.pageseeder.sdk.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ServiceCatalogTest {

  @Test
  public void shouldExposeDocBackedServiceCatalog() {
    assertTrue(ServiceCatalog.all().size() >= 300);
    assertTrue(ServiceCatalog.contains("GET", "/version"));
    assertTrue(ServiceCatalog.contains("get", "/version"));
    assertTrue(ServiceCatalog.contains("POST", "/groups/{group}/members"));
    assertTrue(ServiceCatalog.contains("GET", "/clients/{client}/webhooks"));
    assertTrue(ServiceCatalog.contains("POST", "/members/{member}/groups/{group}/uris/{uri}/versions"));
    assertNotNull(ServiceCatalog.find("GET", "/uri/{uri}"));
  }

  @Test
  public void shouldReturnCanonicalEndpointInstances() {
    ServiceEndpoint endpoint = ServiceCatalog.endpoint("GET", "/version");

    assertEquals(endpoint, ServiceCatalog.find("get", "/version"));
    assertEquals(endpoint, ServiceCatalog.VERSION);
  }
}
