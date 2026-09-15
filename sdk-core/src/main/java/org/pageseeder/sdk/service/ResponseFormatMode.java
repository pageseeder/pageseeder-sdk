package org.pageseeder.sdk.service;

/**
 * Controls how a PageSeeder service call selects its response representation.
 *
 * <p>The default is {@link #PATH_SUFFIX}, which preserves the conventional PageSeeder API
 * behaviour of appending a format extension to the service path. Calls to endpoints mapped only
 * to their exact path can instead use content negotiation or leave the response format
 * unspecified.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ResponseFormatMode {

  /**
   * Selects the representation using a path extension and an {@code Accept} header.
   *
   * <p>For example, JSON format produces {@code /version.json} with
   * {@code Accept: application/json}.
   */
  PATH_SUFFIX,

  /**
   * Selects the representation using an {@code Accept} header without modifying the path.
   *
   * <p>For example, JSON format produces {@code /upload} with
   * {@code Accept: application/json}.
   */
  CONTENT_NEGOTIATION,

  /**
   * Uses the exact endpoint path without applying the client's default response format.
   *
   * <p>No {@code Accept} header is generated unless the call explicitly selects a format or
   * supplies the header itself. This mode is suitable for endpoints returning arbitrary binary
   * content.
   */
  UNSPECIFIED
}
