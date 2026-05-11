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
    assertEquals(ServiceCatalog.GROUP_SUBGROUPS, ServiceCatalog.endpoint("GET", "/groups/{group}/subgroups"));
    assertEquals(ServiceCatalog.GROUP_SUPERGROUPS, ServiceCatalog.endpoint("GET", "/groups/{group}/supergroups"));
  }

  @Test
  void shouldExposeWebhookEndpoints() {
    assertEquals(ServiceCatalog.CLIENT_WEBHOOKS, ServiceCatalog.endpoint("GET", "/clients/{client}/webhooks"));
    assertEquals(ServiceCatalog.CLIENT_WEBHOOK, ServiceCatalog.endpoint("GET", "/clients/{client}/webhooks/{webhook}"));
  }

  @Test
  void shouldExposeDocumentVersionEndpoints() {
    assertEquals(ServiceCatalog.GROUP_URI_VERSIONS, ServiceCatalog.endpoint("GET", "/groups/{group}/uris/{uri}/versions"));
    assertEquals(ServiceCatalog.GROUP_VERSIONS, ServiceCatalog.endpoint("GET", "/groups/{group}/versions"));
  }

  @Test
  void shouldExposeWorkflowEndpoints() {
    assertEquals(ServiceCatalog.GROUP_URI_WORKFLOW, ServiceCatalog.endpoint("GET", "/groups/{group}/uris/{uri}/workflow"));
    assertEquals(ServiceCatalog.MEMBER_GROUP_URI_WORKFLOW, ServiceCatalog.endpoint("GET", "/members/{member}/groups/{group}/uris/{uri}/workflow"));
  }

  @Test
  void shouldExposeMemberDataEndpoints() {
    assertEquals(ServiceCatalog.MEMBER_DATA, ServiceCatalog.endpoint("GET", "/member-data/{member}/data/{data}"));
    assertEquals(ServiceCatalog.MEMBER_DATA_LIST, ServiceCatalog.endpoint("GET", "/members/{member}/data"));
  }

  @Test
  void shouldExposeAuthenticatorEndpoints() {
    assertEquals(ServiceCatalog.AUTHENTICATOR, ServiceCatalog.endpoint("GET", "/authenticators/{authenticator}"));
    assertEquals(ServiceCatalog.MEMBER_AUTHENTICATORS, ServiceCatalog.endpoint("GET", "/members/{member}/authenticators"));
    assertEquals(ServiceCatalog.SELF_AUTHENTICATORS, ServiceCatalog.endpoint("GET", "/self/authenticators"));
  }
}
