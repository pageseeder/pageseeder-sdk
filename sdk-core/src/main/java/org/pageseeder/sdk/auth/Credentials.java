package org.pageseeder.sdk.auth;

import java.net.http.HttpRequest;

/**
 * Authentication strategy for PageSeeder requests.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Credentials {

  /**
   * Apply authentication details to the request builder.
   *
   * @param builder the HTTP request builder.
   */
  void apply(HttpRequest.Builder builder);
}
