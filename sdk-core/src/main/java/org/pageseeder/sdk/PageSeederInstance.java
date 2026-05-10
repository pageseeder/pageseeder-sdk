package org.pageseeder.sdk;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Objects;

/**
 * Immutable definition of a PageSeeder deployment.
 *
 * <p>A PageSeeder instance is described using three origins plus a site prefix:
 * <ul>
 *   <li><strong>website origin</strong>: the public authority used by users to access the site,
 *   typically {@code https://example.com}</li>
 *   <li><strong>API origin</strong>: the authority used for API and OAuth services, often the same
 *   as the website origin but not always</li>
 *   <li><strong>document origin</strong>: the canonical authority used when constructing document
 *   references, historically often using {@code http} and port {@code 80}</li>
 *   <li><strong>site prefix</strong>: the deployed application path, usually {@code /ps}</li>
 * </ul>
 *
 * <p>The SDK derives the concrete roots it needs from those values:
 * <ul>
 *   <li>{@code websiteRoot()} = {@code [websiteOrigin][sitePrefix]/}</li>
 *   <li>{@code apiRoot()} = {@code [apiOrigin][sitePrefix]/api/}</li>
 *   <li>{@code oauthRoot()} = {@code [apiOrigin][sitePrefix]/oauth/}</li>
 * </ul>
 *
 * <p>Example:
 * <pre>{@code
 * PageSeederInstance instance = PageSeederInstance.builder()
 *     .websiteOrigin(URI.create("https://example.com"))
 *     .apiOrigin(URI.create("https://api.example.com"))
 *     .documentOrigin(URI.create("http://example.com"))
 *     .sitePrefix("/ps")
 *     .build();
 *
 * instance.websiteRoot(); // https://example.com/ps/
 * instance.apiRoot();     // https://api.example.com/ps/api/
 * instance.oauthRoot();   // https://api.example.com/ps/oauth/
 * }</pre>
 *
 * <p>Use {@link #of(URI)}, {@link #of(URI, URI)}, {@link #of(String)}, or
 * {@link #of(String, String)} for the common cases where the site prefix is the default
 * {@code /ps} and the document origin can be derived automatically. Use {@link #builder()} when
 * all three origins or the site prefix must be specified explicitly.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PageSeederInstance {

  private static final String WEBSITE_ORIGIN_NAME = "websiteOrigin";
  private static final String API_ORIGIN_NAME = "apiOrigin";
  private static final String DOCUMENT_ORIGIN_NAME = "documentOrigin";

  private static final String DEFAULT_SITE_PREFIX = "/ps";

  private final URI websiteOrigin;
  private final URI apiOrigin;
  private final URI documentOrigin;
  private final String sitePrefix;

  private PageSeederInstance(URI websiteOrigin, URI apiOrigin, URI documentOrigin, String sitePrefix) {
    this.websiteOrigin = validateOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME);
    this.apiOrigin = validateOrigin(apiOrigin, API_ORIGIN_NAME);
    this.documentOrigin = validateOrigin(documentOrigin, DOCUMENT_ORIGIN_NAME);
    this.sitePrefix = validateSitePrefix(sitePrefix);
  }

  /**
   * Creates an instance from the website origin.
   *
   * <p>The API origin defaults to the website origin, the document origin defaults to
   * {@link #defaultDocumentOrigin(URI)}, and the site prefix defaults to {@code /ps}.
   *
   * @param websiteOrigin The public website origin.
   *
   * @return A new PageSeeder instance using the standard defaults.
   */
  public static PageSeederInstance of(URI websiteOrigin) {
    URI normalizedWebsite = validateOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME);
    return new PageSeederInstance(normalizedWebsite, normalizedWebsite, defaultDocumentOrigin(normalizedWebsite), DEFAULT_SITE_PREFIX);
  }

  /**
   * Creates an instance from the website origin string.
   *
   * <p>This is a convenience overload for configuration sources such as properties files.
   *
   * @param websiteOrigin The public website origin.
   *
   * @return A new PageSeeder instance using the standard defaults.
   */
  public static PageSeederInstance of(String websiteOrigin) {
    return of(parseOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME));
  }

  /**
   * Creates an instance from separate website and API origins.
   *
   * <p>The document origin defaults to {@link #defaultDocumentOrigin(URI)} derived from the website
   * origin, and the site prefix defaults to {@code /ps}.
   *
   * @param websiteOrigin The public website origin.
   * @param apiOrigin     The API and OAuth origin.
   *
   * @return A new PageSeeder instance using the standard defaults.
   */
  public static PageSeederInstance of(URI websiteOrigin, URI apiOrigin) {
    URI normalizedWebsite = validateOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME);
    URI normalizedApi = validateOrigin(apiOrigin, API_ORIGIN_NAME);
    return new PageSeederInstance(normalizedWebsite, normalizedApi, defaultDocumentOrigin(normalizedWebsite), DEFAULT_SITE_PREFIX);
  }

  /**
   * Creates an instance from separate website and API origin strings.
   *
   * <p>This is a convenience overload for configuration sources such as properties files.
   *
   * @param websiteOrigin The public website origin.
   * @param apiOrigin     The API and OAuth origin.
   *
   * @return A new PageSeeder instance using the standard defaults.
   */
  public static PageSeederInstance of(String websiteOrigin, String apiOrigin) {
    return of(parseOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME), parseOrigin(apiOrigin, API_ORIGIN_NAME));
  }

  /**
   * Returns a builder for fully custom PageSeeder instance definitions.
   *
   * <p>The builder is useful when the document origin or site prefix cannot be derived from the
   * defaults.
   *
   * @return A new builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * @return The public website origin.
   */
  public URI websiteOrigin() {
    return this.websiteOrigin;
  }

  /**
   * @return The API and OAuth origin.
   */
  public URI apiOrigin() {
    return this.apiOrigin;
  }

  /**
   * @return The canonical origin used for document references.
   */
  public URI documentOrigin() {
    return this.documentOrigin;
  }

  /**
   * @return The deployed site prefix, usually {@code /ps}.
   */
  public String sitePrefix() {
    return this.sitePrefix;
  }

  /**
   * @return The website root, combining the website origin and site prefix.
   */
  public URI websiteRoot() {
    return appendPath(this.websiteOrigin, this.sitePrefix + "/");
  }

  /**
   * @return The API root, combining the API origin, site prefix, and {@code /api}.
   */
  public URI apiRoot() {
    return appendPath(this.apiOrigin, this.sitePrefix + "/api/");
  }

  /**
   * @return The OAuth root, combining the API origin, site prefix, and {@code /oauth}.
   */
  public URI oauthRoot() {
    return appendPath(this.apiOrigin, this.sitePrefix + "/oauth/");
  }

  /**
   * Computes the default document origin for a website origin.
   *
   * <p>This preserves the host information from the website origin while forcing the scheme to
   * {@code http} and the port to {@code 80}, matching the historical document URL convention used
   * by PageSeeder.
   *
   * @param websiteOrigin The website origin to derive from.
   * @return The default document origin.
   */
  public static URI defaultDocumentOrigin(URI websiteOrigin) {
    URI normalizedWebsite = validateOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME);
    return URI.create(new URIBuilder(normalizedWebsite).scheme("http").port(80).build());
  }

  /**
   * Computes the default document origin from a website origin string.
   *
   * @param websiteOrigin The website origin to derive from.
   * @return The default document origin.
   */
  public static URI defaultDocumentOrigin(String websiteOrigin) {
    return defaultDocumentOrigin(parseOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME));
  }

  private static URI parseOrigin(String value, String name) {
    Objects.requireNonNull(value, name);
    try {
      return URI.create(value.trim());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException(name + " must be a valid URI", ex);
    }
  }

  private static URI validateOrigin(URI uri, String name) {
    Objects.requireNonNull(uri, name);
    if (!uri.isAbsolute()) {
      throw new IllegalArgumentException(name + " must be absolute");
    }
    if (uri.getHost() == null || uri.getHost().isBlank()) {
      throw new IllegalArgumentException(name + " must include a host");
    }
    if (uri.getQuery() != null || uri.getFragment() != null) {
      throw new IllegalArgumentException(name + " must not include query or fragment");
    }
    return uri;
  }

  private static String validateSitePrefix(String sitePrefix) {
    String normalized = Objects.requireNonNull(sitePrefix, "sitePrefix").trim();
    if (normalized.isEmpty() || normalized.charAt(0) != '/') {
      throw new IllegalArgumentException("sitePrefix must start with '/'");
    }
    if (normalized.indexOf('?') >= 0 || normalized.indexOf('#') >= 0) {
      throw new IllegalArgumentException("sitePrefix must not include query or fragment");
    }
    if (normalized.length() > 1 && normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  private static URI appendPath(URI base, String suffix) {
    String basePath = base.getPath() == null ? "" : base.getPath();
    if ("/".equals(basePath)) {
      basePath = "";
    } else if (basePath.endsWith("/")) {
      basePath = basePath.substring(0, basePath.length() - 1);
    }
    String path = basePath + suffix;
    try {
      return new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), path, null, null);
    } catch (Exception ex) {
      throw new IllegalArgumentException("Unable to build PageSeeder URI from " + base + " and " + suffix, ex);
    }
  }

  public static final class Builder {

    private @Nullable URI websiteOrigin;
    private @Nullable URI apiOrigin;
    private @Nullable URI documentOrigin;
    private String sitePrefix = DEFAULT_SITE_PREFIX;

    private Builder() {
    }

    /**
     * Sets the public website origin.
     *
     * @param websiteOrigin The public website origin.
     * @return This builder.
     */
    public Builder websiteOrigin(URI websiteOrigin) {
      this.websiteOrigin = websiteOrigin;
      return this;
    }

    /**
     * Sets the public website origin from a string.
     *
     * @param websiteOrigin The public website origin.
     * @return This builder.
     */
    public Builder websiteOrigin(String websiteOrigin) {
      return websiteOrigin(parseOrigin(websiteOrigin, WEBSITE_ORIGIN_NAME));
    }

    /**
     * Sets the API and OAuth origin.
     *
     * @param apiOrigin The API and OAuth origin.
     * @return This builder.
     */
    public Builder apiOrigin(URI apiOrigin) {
      this.apiOrigin = apiOrigin;
      return this;
    }

    /**
     * Sets the API and OAuth origin from a string.
     *
     * @param apiOrigin The API and OAuth origin.
     * @return This builder.
     */
    public Builder apiOrigin(String apiOrigin) {
      return apiOrigin(parseOrigin(apiOrigin, API_ORIGIN_NAME));
    }

    /**
     * Sets the canonical document origin.
     *
     * @param documentOrigin The document origin.
     * @return This builder.
     */
    public Builder documentOrigin(URI documentOrigin) {
      this.documentOrigin = documentOrigin;
      return this;
    }

    /**
     * Sets the canonical document origin from a string.
     *
     * @param documentOrigin The document origin.
     * @return This builder.
     */
    public Builder documentOrigin(String documentOrigin) {
      return documentOrigin(parseOrigin(documentOrigin, DOCUMENT_ORIGIN_NAME));
    }

    /**
     * Sets the deployed site prefix.
     *
     * @param sitePrefix The site prefix, usually {@code /ps}.
     * @return This builder.
     */
    public Builder sitePrefix(String sitePrefix) {
      this.sitePrefix = sitePrefix;
      return this;
    }

    /**
     * Builds the PageSeeder instance.
     *
     * <p>If only one of website origin or API origin is provided, the missing one defaults to the
     * other. If the document origin is omitted, it defaults to
     * {@link PageSeederInstance#defaultDocumentOrigin(URI)} derived from the website origin.
     *
     * @return A new immutable PageSeeder instance.
     */
    public PageSeederInstance build() {
      if (this.websiteOrigin == null && this.apiOrigin == null) {
        throw new IllegalStateException("websiteOrigin or apiOrigin is required");
      }
      URI website = this.websiteOrigin != null ? validateOrigin(this.websiteOrigin, WEBSITE_ORIGIN_NAME)
          : validateOrigin(this.apiOrigin, API_ORIGIN_NAME);
      URI api = this.apiOrigin != null ? validateOrigin(this.apiOrigin, API_ORIGIN_NAME) : website;
      URI document = this.documentOrigin != null ? validateOrigin(this.documentOrigin, DOCUMENT_ORIGIN_NAME)
          : defaultDocumentOrigin(website);
      return new PageSeederInstance(website, api, document, this.sitePrefix);
    }
  }

  /**
   * Small helper to rebuild the URI origin while preserving host details.
   */
  private static final class URIBuilder {

    private final URI source;
    private String scheme;
    private int port;

    private URIBuilder(URI source) {
      this.source = source;
      this.scheme = source.getScheme();
      this.port = source.getPort();
    }

    private URIBuilder scheme(String scheme) {
      this.scheme = scheme;
      return this;
    }

    private URIBuilder port(int port) {
      this.port = port;
      return this;
    }

    private String build() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.scheme).append("://");
      if (this.source.getUserInfo() != null && !this.source.getUserInfo().isBlank()) {
        builder.append(this.source.getUserInfo()).append('@');
      }
      builder.append(this.source.getHost());
      if (this.port >= 0) {
        builder.append(':').append(this.port);
      }
      String path = this.source.getPath();
      if (path != null && !path.isBlank()) {
        builder.append(path);
      }
      return builder.toString();
    }
  }
}
