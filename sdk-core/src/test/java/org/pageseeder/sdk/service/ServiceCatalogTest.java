package org.pageseeder.sdk.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ServiceCatalogTest {

  @Test
  void shouldExposeDocBackedServiceCatalog() {
    assertTrue(ServiceCatalog.all().size() >= 300);
    assertTrue(ServiceCatalog.contains("GET", "/version"));
    assertTrue(ServiceCatalog.contains("get", "/version"));
    assertTrue(ServiceCatalog.contains("POST", "/groups/{group}/members"));
    assertTrue(ServiceCatalog.contains("GET", "/clients/{client}/webhooks"));
    assertTrue(ServiceCatalog.contains("POST", "/members/{member}/groups/{group}/uris/{uri}/versions"));
    assertNotNull(ServiceCatalog.find("GET", "/uri/{uri}"));
  }

  @Test
  void shouldReturnCanonicalEndpointInstances() {
    ServiceEndpoint endpoint = ServiceCatalog.endpoint("GET", "/version");

    assertEquals(endpoint, ServiceCatalog.find("get", "/version"));
    assertEquals(ServiceCatalog.VERSION, endpoint);
  }

  @Test
  void shouldExposeGroupRelationshipEndpoints() {
    assertEquals(ServiceCatalog.endpoint("GET", "/groups/{group}/subgroups"), ServiceCatalog.GROUP_SUBGROUPS);
    assertEquals(ServiceCatalog.endpoint("GET", "/groups/{group}/supergroups"), ServiceCatalog.GROUP_SUPERGROUPS);
  }

  @Test
  void shouldExposeWebhookEndpoints() {
    assertEquals(ServiceCatalog.endpoint("GET", "/clients/{client}/webhooks"), ServiceCatalog.CLIENT_WEBHOOKS);
    assertEquals(ServiceCatalog.endpoint("GET", "/clients/{client}/webhooks/{webhook}"), ServiceCatalog.CLIENT_WEBHOOK);
  }

  @Test
  void shouldExposeDocumentVersionEndpoints() {
    assertEquals(ServiceCatalog.endpoint("GET", "/groups/{group}/uris/{uri}/versions"),
        ServiceCatalog.GROUP_URI_VERSIONS);
    assertEquals(ServiceCatalog.endpoint("GET", "/groups/{group}/versions"), ServiceCatalog.GROUP_VERSIONS);
  }

  @Test
  void shouldExposeWorkflowEndpoints() {
    assertEquals(ServiceCatalog.endpoint("GET", "/groups/{group}/uris/{uri}/workflow"),
        ServiceCatalog.GROUP_URI_WORKFLOW);
    assertEquals(ServiceCatalog.endpoint("GET", "/members/{member}/groups/{group}/uris/{uri}/workflow"),
        ServiceCatalog.MEMBER_GROUP_URI_WORKFLOW);
  }

  @Test
  void shouldExposeMemberDataEndpoints() {
    assertEquals(ServiceCatalog.endpoint("GET", "/member-data/{member}/data/{data}"), ServiceCatalog.MEMBER_DATA);
    assertEquals(ServiceCatalog.endpoint("GET", "/members/{member}/data"), ServiceCatalog.MEMBER_DATA_LIST);
  }

  @Test
  void shouldExposeAuthenticatorEndpoints() {
    assertEquals(ServiceCatalog.endpoint("GET", "/authenticators/{authenticator}"), ServiceCatalog.AUTHENTICATOR);
    assertEquals(ServiceCatalog.endpoint("GET", "/members/{member}/authenticators"),
        ServiceCatalog.MEMBER_AUTHENTICATORS);
    assertEquals(ServiceCatalog.endpoint("GET", "/self/authenticators"), ServiceCatalog.SELF_AUTHENTICATORS);
  }
}
