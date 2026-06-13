package org.pageseeder.sdk.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A single executable fetch of a PageSeeder website resource.
 *
 * <p>Unlike {@link ServiceCall}, which targets the {@code /api/} namespace, a resource call
 * resolves against the website root and appends no format extension. It covers the servlet
 * patterns that PageSeeder maps outside the API:
 *
 * <ul>
 *   <li>{@code /docid/{docid}} — document by document-ID string</li>
 *   <li>{@code /uri/{uriid}} — document by numeric URI ID</li>
 *   <li>{@code /servlet/thumbnail/{uriid}} — thumbnail image</li>
 *   <li>{@code /servlet/image/{uriid}} — resized image</li>
 * </ul>
 *
 * <p>Use the named factory methods for the patterns above, or {@link #of(String)} for any other
 * path under the website root.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ResourceCall {

  private static final Pattern DOC_ID = Pattern.compile("[a-zA-Z0-9_-]+");
  private static final String URIID = "uriid";

  private static final PathTemplate DOCID_TEMPLATE     = new PathTemplate("/docid/{docid}");
  private static final PathTemplate URI_TEMPLATE       = new PathTemplate("/uri/{uriid}");
  private static final PathTemplate THUMBNAIL_TEMPLATE = new PathTemplate("/servlet/thumbnail/{uriid}");
  private static final PathTemplate IMAGE_TEMPLATE     = new PathTemplate("/servlet/image/{uriid}");

  private final PathTemplate pathTemplate;
  private final Map<String, Object> pathVariables = new LinkedHashMap<>();
  private final QueryParameters query = new QueryParameters();
  private final Map<String, String> headers = new LinkedHashMap<>();

  private ResourceCall(PathTemplate pathTemplate) {
    this.pathTemplate = Objects.requireNonNull(pathTemplate, "pathTemplate");
  }

  /**
   * Creates a resource call for a document by its document ID.
   *
   * @param docId the document ID; must match {@code [a-zA-Z0-9_-]+}
   * @return a new resource call
   * @throws IllegalArgumentException if {@code docId} is blank or contains invalid characters
   */
  public static ResourceCall docId(String docId) {
    Objects.requireNonNull(docId, "docId");
    if (!DOC_ID.matcher(docId).matches()) {
      throw new IllegalArgumentException("docId must match [a-zA-Z0-9_-]+: " + docId);
    }
    ResourceCall call = new ResourceCall(DOCID_TEMPLATE);
    call.pathVariables.put("docid", docId);
    return call;
  }

  /**
   * Creates a resource call for a document by its numeric URI ID.
   *
   * @param uriId the URI ID
   * @return a new resource call
   */
  public static ResourceCall uri(long uriId) {
    ResourceCall call = new ResourceCall(URI_TEMPLATE);
    call.pathVariables.put(URIID, uriId);
    return call;
  }

  /**
   * Creates a resource call for a thumbnail image by its numeric URI ID.
   *
   * @param uriId the URI ID
   * @return a new resource call
   */
  public static ResourceCall thumbnail(long uriId) {
    ResourceCall call = new ResourceCall(THUMBNAIL_TEMPLATE);
    call.pathVariables.put(URIID, uriId);
    return call;
  }

  /**
   * Creates a resource call for a resized image by its numeric URI ID.
   *
   * @param uriId the URI ID
   * @return a new resource call
   */
  public static ResourceCall image(long uriId) {
    ResourceCall call = new ResourceCall(IMAGE_TEMPLATE);
    call.pathVariables.put(URIID, uriId);
    return call;
  }

  /**
   * Creates a resource call with an explicit path template, for patterns not covered by the
   * named factory methods.
   *
   * @param pathTemplate the path template; must start with {@code /}
   * @return a new resource call
   */
  public static ResourceCall of(PathTemplate pathTemplate) {
    return new ResourceCall(pathTemplate);
  }

  /**
   * Creates a resource call with an explicit path template string, for patterns not covered by
   * the named factory methods.
   *
   * @param pathTemplate the path template string; must start with {@code /}
   * @return a new resource call
   */
  public static ResourceCall of(String pathTemplate) {
    return new ResourceCall(new PathTemplate(pathTemplate));
  }

  /**
   * Sets a path variable to be substituted into the path template.
   *
   * @param name  the variable name (must match a {@code {name}} placeholder in the path)
   * @param value the value; will be percent-encoded when the path is resolved
   * @return {@code this} for chaining
   */
  public ResourceCall pathVariable(String name, Object value) {
    this.pathVariables.put(name, value);
    return this;
  }

  /**
   * Appends a query parameter to this call.
   *
   * @param name  the parameter name
   * @param value the parameter value
   * @return {@code this} for chaining
   */
  public ResourceCall query(String name, String value) {
    this.query.add(name, value);
    return this;
  }

  /**
   * Sets an additional request header.
   *
   * @param name  the header name
   * @param value the header value
   * @return {@code this} for chaining
   */
  public ResourceCall header(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  /** @return the path template for this resource call */
  public PathTemplate pathTemplate() {
    return this.pathTemplate;
  }

  /** @return an immutable snapshot of the current path variables */
  public Map<String, Object> pathVariables() {
    return Map.copyOf(this.pathVariables);
  }

  /** @return the query parameters accumulated for this call */
  public QueryParameters queryParameters() {
    return this.query;
  }

  /** @return an immutable snapshot of the additional request headers */
  public Map<String, String> headers() {
    return Map.copyOf(this.headers);
  }
}
