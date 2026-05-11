package org.pageseeder.sdk.service;

import java.util.Objects;

/**
 * A typed PageSeeder endpoint definition.
 *
 * @param method       the HTTP method for this endpoint
 * @param pathTemplate the URI path template for this endpoint
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record ServiceEndpoint(String method, PathTemplate pathTemplate) {

  /**
   * Constructs a new {@code ServiceEndpoint} instance with the specified HTTP method and path template.
   *
   * @param method       The HTTP method for this endpoint (e.g., "GET", "POST"). Must not be null.
   * @param pathTemplate The URI path template for this endpoint. Must start with a forward slash ('/') and must not be null.
   *                     Variables in the template can be defined using curly braces (e.g., "/path/{variable}").
   */
  public ServiceEndpoint(String method, String pathTemplate) {
    this(method, new PathTemplate(pathTemplate));
  }

  public ServiceEndpoint {
    method = Objects.requireNonNull(method, "method");
    pathTemplate = Objects.requireNonNull(pathTemplate, "pathTemplate");
  }

  /**
   * Creates a new {@code ServiceEndpoint} with the specified HTTP method and path template.
   *
   * @param method       The HTTP method for this endpoint (e.g., "GET", "POST"). Must not be null.
   * @param pathTemplate The URI path template for this endpoint. Must start with a forward slash ('/')
   *                     and must not be null. Variables in the template can be defined using curly braces
   *                     (e.g., "/path/{variable}").
   * @return A {@code ServiceEndpoint} instance configured with the specified method and path template.
   */
  public static ServiceEndpoint of(String method, String pathTemplate) {
    return new ServiceEndpoint(method, pathTemplate);
  }

  /**
   * Creates a new {@code ServiceEndpoint} configured for the HTTP GET method with the specified path template.
   *
   * @param pathTemplate The URI path template for the endpoint.
   * @return A {@code ServiceEndpoint} instance configured with the HTTP GET method and the specified path template.
   */
  public static ServiceEndpoint get(String pathTemplate) {
    return new ServiceEndpoint("GET", pathTemplate);
  }

  /**
   * Creates a new {@code ServiceEndpoint} configured for the HTTP HEAD method with the specified path template.
   *
   * @param pathTemplate The URI path template for the endpoint.
   * @return A {@code ServiceEndpoint} instance configured with the HTTP HEAD method and the specified path template.
   */
  public static ServiceEndpoint head(String pathTemplate) {
    return new ServiceEndpoint("HEAD", pathTemplate);
  }

  /**
   * Creates a new {@code ServiceEndpoint} configured for the HTTP POST method with the specified path template.
   *
   * @param pathTemplate The URI path template for the endpoint.
   * @return A {@code ServiceEndpoint} instance configured with the HTTP POST method and the specified path template.
   */
  public static ServiceEndpoint post(String pathTemplate) {
    return new ServiceEndpoint("POST", pathTemplate);
  }

  /**
   * Creates a new {@code ServiceEndpoint} configured for the HTTP PATCH method with the specified path template.
   *
   * @param pathTemplate The URI path template for the endpoint.
   * @return A {@code ServiceEndpoint} instance configured with the HTTP PATCH method and the specified path template.
   */
  public static ServiceEndpoint patch(String pathTemplate) {
    return new ServiceEndpoint("PATCH", pathTemplate);
  }

  /**
   * Creates a new {@code ServiceEndpoint} configured for the HTTP PUT method with the specified path template.
   *
   * @param pathTemplate The URI path template for the endpoint.
   * @return A {@code ServiceEndpoint} instance configured with the HTTP PUT method and the specified path template.
   */
  public static ServiceEndpoint put(String pathTemplate) {
    return new ServiceEndpoint("PUT", pathTemplate);
  }

  /**
   * Creates a new {@code ServiceEndpoint} configured for the HTTP DELETE method with the specified path template.
   *
   * @param pathTemplate The URI path template for the endpoint.
   * @return A {@code ServiceEndpoint} instance configured with the HTTP DELETE method and the specified path template.
   */
  public static ServiceEndpoint delete(String pathTemplate) {
    return new ServiceEndpoint("DELETE", pathTemplate);
  }

  /**
   * The HTTP method for this endpoint.
   *
   * @return The HTTP method for this endpoint.
   */
  public String method() {
    return this.method;
  }

  /**
   * The URI path template for this endpoint.
   *
   * @return The URI path template for this endpoint.
   */
  public PathTemplate pathTemplate() {
    return this.pathTemplate;
  }

  @Override
  public String toString() {
    return this.method + " " + this.pathTemplate;
  }
}
