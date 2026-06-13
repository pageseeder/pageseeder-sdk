package org.pageseeder.sdk.client;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.PageSeederInstance;
import org.pageseeder.sdk.auth.Credentials;
import org.pageseeder.sdk.exception.HttpStatusException;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.exception.ServiceErrorException;
import org.pageseeder.sdk.exception.TransportException;
import org.pageseeder.sdk.service.PayloadFormat;
import org.pageseeder.sdk.service.ResourceCall;
import org.pageseeder.sdk.service.ServiceCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * PageSeeder SDK client built on Java 11 {@link HttpClient}.
 *
 * <p>Use {@link #execute(ServiceCall)} to obtain a {@link PageSeederResponse} for full control,
 * or {@link #execute(ServiceCall, BodyDecoder)} with a decoder from {@code sdk-model}'s
 * {@code Decoders} for convenient typed results.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PageSeederClient {

  private static final String ERROR_ID = "id";
  private static final String ERROR_MESSAGE = "message";
  private static final String ERROR_ELEMENT = "error";

  private static final Logger LOGGER = LoggerFactory.getLogger(PageSeederClient.class);

  // XMLInputFactory is thread-safe for createXMLStreamReader per the StAX spec
  private static final XMLInputFactory ERROR_XML_FACTORY;
  static {
    XMLInputFactory f = XMLInputFactory.newFactory();
    f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    f.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    ERROR_XML_FACTORY = f;
  }

  private final PageSeederInstance instance;
  private final Duration timeout;
  private final PayloadFormat defaultFormat;
  private final @Nullable Credentials credentials;
  private final boolean gzipEnabled;
  private final HttpClient httpClient;

  /**
   * Creates a client for the supplied PageSeeder instance.
   *
   * @param instance the PageSeeder instance
   */
  public PageSeederClient(PageSeederInstance instance) {
    this(instance, null, null, null, true);
  }

  private PageSeederClient(PageSeederInstance instance,
                           @Nullable Duration timeout,
                           @Nullable PayloadFormat defaultFormat,
                           @Nullable Credentials credentials,
                           boolean gzipEnabled) {
    this.instance = Objects.requireNonNull(instance, "instance");
    this.timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    this.defaultFormat = defaultFormat == null ? PayloadFormat.XML : defaultFormat;
    this.credentials = credentials;
    this.gzipEnabled = gzipEnabled;
    this.httpClient = HttpClient.newBuilder().connectTimeout(this.timeout).build();
  }

  /**
   * Returns a builder for configuring a PageSeeder client.
   *
   * @return a new client builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the PageSeeder instance used by this client.
   *
   * @return the PageSeeder instance
   */
  public PageSeederInstance instance() {
    return this.instance;
  }

  /**
   * Returns the API root URI used by this client.
   *
   * @return the API root URI
   */
  public URI apiRoot() {
    return this.instance.apiRoot();
  }

  /**
   * Returns a copy of this client with the supplied request timeout.
   *
   * @param timeout the request timeout
   * @return the updated client
   */
  public PageSeederClient withTimeout(Duration timeout) {
    return new PageSeederClient(this.instance, timeout, this.defaultFormat, this.credentials, this.gzipEnabled);
  }

  /**
   * Returns a copy of this client with the supplied default response format.
   *
   * @param defaultFormat the default payload format
   * @return the updated client
   */
  public PageSeederClient withDefaultFormat(PayloadFormat defaultFormat) {
    return new PageSeederClient(this.instance, this.timeout, defaultFormat, this.credentials, this.gzipEnabled);
  }

  /**
   * Returns a copy of this client with the supplied default credentials.
   *
   * @param credentials the default credentials, or {@code null} for none
   * @return the updated client
   */
  public PageSeederClient withCredentials(@Nullable Credentials credentials) {
    return new PageSeederClient(this.instance, this.timeout, this.defaultFormat, credentials, this.gzipEnabled);
  }

  /**
   * Returns a copy of this client with gzip response handling enabled or disabled.
   *
   * @param gzipEnabled whether to request and decode gzip responses
   * @return the updated client
   */
  public PageSeederClient withGzipEnabled(boolean gzipEnabled) {
    return new PageSeederClient(this.instance, this.timeout, this.defaultFormat, this.credentials, gzipEnabled);
  }

  /**
   * Executes a service call and returns the raw response.
   *
   * @param call The service call to execute.
   * @return The response.
   */
  public PageSeederResponse execute(ServiceCall call) {
    return execute(call, this.credentials);
  }

  /**
   * Executes a service call with explicit credentials, overriding any client-level credentials.
   *
   * @param call        The service call to execute.
   * @param credentials The credentials to use.
   * @return The response.
   */
  public PageSeederResponse execute(ServiceCall call, @Nullable Credentials credentials) {
    PageSeederRequest request = toRequest(call);
    HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri()).timeout(this.timeout);
    request.headers().forEach(builder::header);
    if (request.contentType() != null) {
      builder.header("Content-Type", request.contentType());
    }
    if (credentials != null) {
      credentials.apply(builder);
    }
    if (this.gzipEnabled) {
      builder.header("Accept-Encoding", "gzip");
    }
    byte [] body = request.body();
    if (body == null || body.length == 0) {
      builder.method(request.method(), HttpRequest.BodyPublishers.noBody());
    } else {
      builder.method(request.method(), HttpRequest.BodyPublishers.ofByteArray(body));
    }

    try {
      HttpResponse<byte[]> response = this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
      byte[] responseBody = decodeBody(response);
      String mediaType = response.headers().firstValue("Content-Type").orElse(null);
      if (response.statusCode() >= 400) {
        throwServiceException(response.statusCode(), responseBody, mediaType);
      }
      return new PageSeederResponse(response.statusCode(), response.headers().map(), responseBody, mediaType);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new TransportException("Unable to execute PageSeeder request", ex);
    } catch (IOException ex) {
      throw new TransportException("Unable to execute PageSeeder request", ex);
    }
  }

  /**
   * Executes a service call and decodes the response using the supplied decoder.
   *
   * <p>Use {@code Decoders} from {@code sdk-model} for standard PageSeeder model types.
   *
   * @param call    The service call to execute.
   * @param decoder The decoder to apply to the response body.
   * @param <T>     The result type.
   * @return The decoded result.
   */
  public <T> T execute(ServiceCall call, BodyDecoder<T> decoder) {
    PageSeederResponse response = execute(call);
    return response.as(decoder);
  }

  /**
   * Executes a service call with explicit credentials and decodes the response.
   *
   * @param call        The service call to execute.
   * @param credentials The credentials to use.
   * @param decoder     The decoder to apply to the response body.
   * @param <T>         The result type.
   * @return The decoded result.
   */
  public <T> T execute(ServiceCall call, @Nullable Credentials credentials, BodyDecoder<T> decoder) {
    PageSeederResponse response = execute(call, credentials);
    return response.as(decoder);
  }

  /**
   * Converts a service call to a low-level HTTP request without executing it.
   *
   * <p>Resolves path variables, merges query parameters, encodes form parameters,
   * and sets the {@code Accept} and {@code Content-Type} headers.
   *
   * @param call The service call to convert.
   * @return The corresponding HTTP request descriptor.
   */
  public PageSeederRequest toRequest(ServiceCall call) {
    PayloadFormat format = call.format() == null ? this.defaultFormat : call.format();
    String path = call.endpoint().pathTemplate().resolve(call.pathVariables()) + format.extension();
    URI uri = withQuery(this.instance.apiRoot().resolve(relativePath(path)), call.queryParameters().toFormUrlEncoded());
    Map<String, String> headers = new LinkedHashMap<>(call.headers());
    headers.putIfAbsent("Accept", format.mediaType());
    byte[] reqBody = null;
    String contentType = call.contentType();
    if (!call.formParameters().isEmpty()) {
      reqBody = call.formParameters().toFormUrlEncoded().getBytes(StandardCharsets.UTF_8);
      contentType = "application/x-www-form-urlencoded; charset=UTF-8";
    } else if (call.rawBody() != null) {
      reqBody = call.rawBody();
    }
    return new PageSeederRequest(call.endpoint().method(), uri, headers, reqBody, contentType, format);
  }

  /**
   * Fetches a website resource and returns the raw response.
   *
   * <p>Resolves the resource path against {@code websiteRoot()} with no format extension appended.
   * Use this for document, thumbnail, and image servlet paths that live outside the {@code /api/}
   * namespace.
   *
   * @param call the resource call to execute
   * @return the response
   */
  public PageSeederResponse fetch(ResourceCall call) {
    return fetch(call, this.credentials);
  }

  /**
   * Fetches a website resource with explicit credentials, overriding any client-level credentials.
   *
   * @param call        the resource call to execute
   * @param credentials the credentials to use
   * @return the response
   */
  public PageSeederResponse fetch(ResourceCall call, @Nullable Credentials credentials) {
    PageSeederRequest request = toRequest(call);
    HttpRequest.Builder builder = HttpRequest.newBuilder(request.uri()).timeout(this.timeout);
    request.headers().forEach(builder::header);
    if (credentials != null) {
      credentials.apply(builder);
    }
    if (this.gzipEnabled) {
      builder.header("Accept-Encoding", "gzip");
    }
    builder.GET();
    try {
      HttpResponse<byte[]> response = this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
      byte[] responseBody = decodeBody(response);
      String mediaType = response.headers().firstValue("Content-Type").orElse(null);
      if (response.statusCode() >= 400) {
        throwServiceException(response.statusCode(), responseBody, mediaType);
      }
      return new PageSeederResponse(response.statusCode(), response.headers().map(), responseBody, mediaType);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new TransportException("Unable to fetch PageSeeder resource", ex);
    } catch (IOException ex) {
      throw new TransportException("Unable to fetch PageSeeder resource", ex);
    }
  }

  /**
   * Fetches a website resource and decodes the response using the supplied decoder.
   *
   * @param call    the resource call to execute
   * @param decoder the decoder to apply to the response body
   * @param <T>     the result type
   * @return the decoded result
   */
  public <T> T fetch(ResourceCall call, BodyDecoder<T> decoder) {
    return fetch(call, this.credentials, decoder);
  }

  /**
   * Fetches a website resource with explicit credentials and decodes the response.
   *
   * @param call        the resource call to execute
   * @param credentials the credentials to use
   * @param decoder     the decoder to apply to the response body
   * @param <T>         the result type
   * @return the decoded result
   */
  public <T> T fetch(ResourceCall call, @Nullable Credentials credentials, BodyDecoder<T> decoder) {
    PageSeederResponse response = fetch(call, credentials);
    return response.as(decoder);
  }

  /**
   * Converts a resource call to a low-level HTTP request without executing it.
   *
   * <p>Resolves path variables and merges query parameters. The resulting URI is relative to
   * {@code websiteRoot()}; no format extension is appended and no {@code Accept} header is set.
   *
   * @param call the resource call to convert
   * @return the corresponding HTTP request descriptor
   */
  public PageSeederRequest toRequest(ResourceCall call) {
    String path = call.pathTemplate().resolve(call.pathVariables());
    URI uri = withQuery(this.instance.websiteRoot().resolve(relativePath(path)), call.queryParameters().toFormUrlEncoded());
    Map<String, String> headers = new LinkedHashMap<>(call.headers());
    return new PageSeederRequest("GET", uri, headers, null, null, null);
  }

  private URI withQuery(URI base, @Nullable String query) {
    if (query == null || query.isBlank()) {
      return base;
    }
    String existing = base.getQuery();
    String combined = existing == null || existing.isBlank() ? query : existing + "&" + query;
    return URI.create(base.getScheme() + "://" + base.getAuthority() + base.getPath() + "?" + combined);
  }

  private static String relativePath(String path) {
    return path.startsWith("/") ? path.substring(1) : path;
  }

  private byte[] decodeBody(HttpResponse<byte[]> response) throws IOException {
    if (!"gzip".equalsIgnoreCase(response.headers().firstValue("Content-Encoding").orElse(null))) {
      return response.body();
    }
    try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(response.body()))) {
      return gzip.readAllBytes();
    }
  }

  private void throwServiceException(int statusCode, byte[] body, @Nullable String mediaType) {
    if (mediaType != null && mediaType.toLowerCase().contains("xml")) {
      ServiceError error = parseXmlError(body);
      if (error != null) {
        throw new ServiceErrorException(statusCode, error);
      }
    }
    if (mediaType != null && mediaType.toLowerCase().contains("json")) {
      ServiceError error = parseJsonError(body);
      if (error != null) {
        throw new ServiceErrorException(statusCode, error);
      }
    }
    throw new HttpStatusException(statusCode, "PageSeeder returned HTTP " + statusCode);
  }

  private static @Nullable ServiceError parseXmlError(byte[] body) {
    try (XmlErrorReader xml = XmlErrorReader.open(body)) {
      return readXmlError(xml.reader());
    } catch (XMLStreamException ex) {
      LOGGER.debug("Unable to parse XML service error", ex);
    }
    return null;
  }

  private static @Nullable ServiceError readXmlError(XMLStreamReader xml) throws XMLStreamException {
    XmlError error = new XmlError();
    while (xml.hasNext() && !error.isComplete()) {
      error.read(xml.next(), xml);
    }
    return error.toServiceError();
  }

  @SuppressWarnings("java:S6206") // Resource owner, not transparent data; close() is the primary contract.
  private static final class XmlErrorReader implements AutoCloseable {

    private final XMLStreamReader reader;

    private XmlErrorReader(XMLStreamReader reader) {
      this.reader = reader;
    }

    private static XmlErrorReader open(byte[] body) throws XMLStreamException {
      return new XmlErrorReader(ERROR_XML_FACTORY.createXMLStreamReader(new ByteArrayInputStream(body)));
    }

    private XMLStreamReader reader() {
      return this.reader;
    }

    @Override
    public void close() {
      try {
        this.reader.close();
      } catch (XMLStreamException ex) {
        LOGGER.debug("Unable to close XML service error reader", ex);
      }
    }
  }

  private static final class XmlError {

    private @Nullable String id;
    private @Nullable String message;
    private boolean inError;
    private boolean inMessage;
    private boolean complete;

    private void read(int event, XMLStreamReader xml) {
      if (event == XMLStreamConstants.START_ELEMENT) {
        readStart(xml);
      } else if (event == XMLStreamConstants.CHARACTERS) {
        readCharacters(xml);
      } else if (event == XMLStreamConstants.END_ELEMENT) {
        readEnd(xml);
      }
    }

    private void readStart(XMLStreamReader xml) {
      String element = xml.getLocalName();
      if (ERROR_ELEMENT.equals(element)) {
        this.inError = true;
        this.id = xml.getAttributeValue(null, ERROR_ID);
        this.message = xml.getAttributeValue(null, ERROR_MESSAGE);
      } else if (this.inError && ERROR_MESSAGE.equals(element)) {
        this.inMessage = true;
      }
    }

    private void readCharacters(XMLStreamReader xml) {
      if (this.inMessage) {
        this.message = xml.getText();
      }
    }

    private void readEnd(XMLStreamReader xml) {
      String element = xml.getLocalName();
      if (ERROR_MESSAGE.equals(element)) {
        this.inMessage = false;
      } else if (ERROR_ELEMENT.equals(element)) {
        this.complete = true;
      }
    }

    private boolean isComplete() {
      return this.complete;
    }

    private @Nullable ServiceError toServiceError() {
      if (this.message == null) {
        return null;
      }
      return new ServiceError(this.id != null ? this.id : "", this.message);
    }
  }

  private static @Nullable ServiceError parseJsonError(byte[] body) {
    // Minimal JSON scan without Jackson: look for "id" and "message" string values.
    // This avoids a Jackson dependency in core. Covers the standard PageSeeder error format.
    try {
      String json = new String(body, StandardCharsets.UTF_8);
      String id = extractJsonString(json, ERROR_ID);
      String message = extractJsonString(json, ERROR_MESSAGE);
      if (message == null) {
        message = extractJsonString(json, "description");
      }
      if (message != null) {
        return new ServiceError(id != null ? id : "", message);
      }
    } catch (RuntimeException ex) {
      LOGGER.debug("Unable to parse JSON service error", ex);
    }
    return null;
  }

  private static @Nullable String extractJsonString(String json, String key) {
    String search = "\"" + key + "\"";
    int pos = json.indexOf(search);
    if (pos < 0) {
      return null;
    }
    pos += search.length();
    while (pos < json.length() && (json.charAt(pos) == ' ' || json.charAt(pos) == ':')) {
      pos++;
    }
    if (pos >= json.length() || json.charAt(pos) != '"') {
      return null;
    }
    pos++;
    StringBuilder sb = new StringBuilder();
    while (pos < json.length() && json.charAt(pos) != '"') {
      if (json.charAt(pos) == '\\' && pos + 1 < json.length()) {
        pos++;
        sb.append(json.charAt(pos));
      } else {
        sb.append(json.charAt(pos));
      }
      pos++;
    }
    return sb.toString();
  }

  /**
   * Builder for {@link PageSeederClient}.
   */
  public static final class Builder {

    private @Nullable PageSeederInstance instance;
    private @Nullable URI apiOrigin;
    private Duration timeout = Duration.ofSeconds(30);
    private PayloadFormat defaultFormat = PayloadFormat.XML;
    private @Nullable Credentials credentials;
    private boolean gzipEnabled = true;

    private Builder() {
    }

    /**
     * Sets the PageSeeder instance.
     *
     * @param instance the PageSeeder instance
     * @return this builder
     */
    public Builder instance(PageSeederInstance instance) {
      this.instance = instance;
      return this;
    }

    /**
     * Sets the API origin used to create a default PageSeeder instance.
     *
     * @param apiOrigin the API origin
     * @return this builder
     */
    public Builder apiOrigin(URI apiOrigin) {
      this.apiOrigin = apiOrigin;
      return this;
    }

    /**
     * Sets the request timeout.
     *
     * @param timeout the request timeout
     * @return this builder
     */
    public Builder timeout(Duration timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * Sets the default response payload format.
     *
     * @param defaultFormat the default payload format
     * @return this builder
     */
    public Builder defaultFormat(PayloadFormat defaultFormat) {
      this.defaultFormat = defaultFormat;
      return this;
    }

    /**
     * Sets the default credentials.
     *
     * @param credentials the default credentials
     * @return this builder
     */
    public Builder credentials(Credentials credentials) {
      this.credentials = credentials;
      return this;
    }

    /**
     * Enables or disables gzip response handling.
     *
     * @param gzipEnabled whether gzip response handling is enabled
     * @return this builder
     */
    public Builder gzipEnabled(boolean gzipEnabled) {
      this.gzipEnabled = gzipEnabled;
      return this;
    }

    /**
     * Builds the configured client.
     *
     * @return a PageSeeder client
     */
    public PageSeederClient build() {
      PageSeederInstance resolved = this.instance != null ? this.instance
          : PageSeederInstance.of(Objects.requireNonNull(this.apiOrigin, "instance or apiOrigin is required"));
      return new PageSeederClient(resolved, this.timeout, this.defaultFormat, this.credentials, this.gzipEnabled);
    }
  }
}
