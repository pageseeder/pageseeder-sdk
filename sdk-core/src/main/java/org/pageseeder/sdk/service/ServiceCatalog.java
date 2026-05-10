package org.pageseeder.sdk.service;

import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * PageSeeder service catalog based on the official services index.
 *
 * <p>The endpoint list in this catalog is loaded from
 * {@code /org/pageseeder/sdk/service/services.txt}, sourced from
 * <a href="https://dev.pageseeder.com/api/services.html">https://dev.pageseeder.com/api/services.html</a>,
 * last edited on 12 August 2025.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ServiceCatalog {

  private static final String RESOURCE_PATH = "/org/pageseeder/sdk/service/services.txt";

  private static final Set<ServiceEndpoint> ENDPOINTS = build();

  public static final ServiceEndpoint MEMBER = endpoint("GET", "/members/{member}");
  public static final ServiceEndpoint MEMBER_MEMBERSHIPS = endpoint("GET", "/members/{member}/memberships");
  public static final ServiceEndpoint GROUP = endpoint("GET", "/groups/{group}");
  public static final ServiceEndpoint RESOURCE_URI = endpoint("GET", "/uri/{uri}");
  public static final ServiceEndpoint VERSION = endpoint("GET", "/version");

  private ServiceCatalog() {
  }

  /**
   * Retrieves the {@code ServiceEndpoint} for the specified HTTP method and URI path.
   * If no matching endpoint is found, an {@link IllegalArgumentException} is thrown.
   *
   * @param method The HTTP method (e.g., "GET", "POST", etc.). Must not be null.
   * @param path   The URI path for the endpoint. Must not be null.
   *
   * @return The {@code ServiceEndpoint} corresponding to the specified method and path.
   *
   * @throws IllegalArgumentException If the specified method or path does not match any existing service endpoint.
   */
  public static ServiceEndpoint endpoint(String method, String path) {
    ServiceEndpoint endpoint = find(method, path);
    if (endpoint == null) {
      throw new IllegalArgumentException("Unknown PageSeeder service: " + method + " " + path);
    }
    return endpoint;
  }

  /**
   * Finds and returns the {@code ServiceEndpoint} associated with the specified HTTP method and URI path.
   * If no match is found, this method returns {@code null}.
   *
   * @param method The HTTP method (e.g., "GET", "POST", etc.). Must not be null.
   * @param path   The URI path for the endpoint. Must not be null.
   *
   * @return The {@code ServiceEndpoint} corresponding to the specified method and path, or {@code null} if no match is found.
   */
  public static @Nullable ServiceEndpoint find(String method, String path) {
    ServiceEndpoint lookup = lookup(method, path);
    for (ServiceEndpoint endpoint : ENDPOINTS) {
      if (endpoint.equals(lookup)) {
        return endpoint;
      }
    }
    return null;
  }

  /**
   * Checks whether the specified HTTP method and URI path are associated with an existing service endpoint.
   *
   * @param method The HTTP method (e.g., "GET", "POST", etc.). Must not be null.
   * @param path   The URI path for the endpoint. Must not be null.
   * @return {@code true} if the specified method and path correspond to an existing endpoint, {@code false} otherwise.
   */
  public static boolean contains(String method, String path) {
    return ENDPOINTS.contains(lookup(method, path));
  }

  /**
   * Returns an unmodifiable collection of all known service endpoints.
   * @return An unmodifiable collection of all known service endpoints.
   */
  public static Collection<ServiceEndpoint> all() {
    return ENDPOINTS;
  }

  private static Set<ServiceEndpoint> build() {
    Set<ServiceEndpoint> endpoints = new LinkedHashSet<>();
    try (InputStream in = ServiceCatalog.class.getResourceAsStream(RESOURCE_PATH)) {
      if (in == null) {
        throw new IllegalStateException("Missing PageSeeder service catalog resource: " + RESOURCE_PATH);
      }
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isBlank() || line.startsWith("#")) {
            continue;
          }
          String[] parts = line.trim().split("\\s+", 2);
          if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalStateException("Invalid PageSeeder service catalog entry: " + line);
          }
          String method = parts[0];
          String path = parts[1];
          put(endpoints, method, path);
        }
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to load PageSeeder service catalog", ex);
    }
    return Collections.unmodifiableSet(endpoints);
  }

  private static void put(Set<ServiceEndpoint> endpoints, String method, String path) {
    endpoints.add(lookup(method, path));
  }

  private static ServiceEndpoint lookup(String method, String path) {
    return ServiceEndpoint.of(normalizeMethod(method), Objects.requireNonNull(path, "path"));
  }

  private static String normalizeMethod(String method) {
    return Objects.requireNonNull(method, "method").toUpperCase(Locale.ROOT);
  }
}
