package org.pageseeder.sdk.legacy;

import org.pageseeder.bridge.PSConfig;
import org.pageseeder.sdk.PageSeederInstance;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utility methods for working with {@link PSConfig} instances.
 */
public final class PSConfigs {

  private PSConfigs() {}

  /**
   * Converts a {@link PSConfig} to a {@link PageSeederInstance}.
   *
   * <p>The three URL origins (website, API, document) and the site prefix are mapped directly.
   * Default ports (80 for HTTP, 443 for HTTPS) are omitted from the resulting URIs.
   *
   * @param config The PSConfig to convert.
   * @return The equivalent PageSeeder instance.
   * @throws IllegalArgumentException if any origin URL cannot be converted to a valid URI.
   */
  public static PageSeederInstance toInstance(PSConfig config) {
    URI website  = toOrigin(config.getScheme(),         config.getHost(),         config.getPort());
    URI api      = toOrigin(config.getAPIScheme(),      config.getAPIHost(),      config.getAPIPort());
    URI document = toOrigin(config.getDocumentScheme(), config.getDocumentHost(), config.getDocumentPort());
    return PageSeederInstance.builder()
        .websiteOrigin(website)
        .apiOrigin(api)
        .documentOrigin(document)
        .sitePrefix(config.getSitePrefix())
        .build();
  }

  private static URI toOrigin(String scheme, String host, int port) {
    int p = isDefaultPort(scheme, port) ? -1 : port;
    try {
      return new URI(scheme, null, host, p, null, null, null);
    } catch (URISyntaxException ex) {
      throw new IllegalArgumentException("Cannot build origin URI from PSConfig: " + ex.getMessage(), ex);
    }
  }

  private static boolean isDefaultPort(String scheme, int port) {
    return ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);
  }
}
