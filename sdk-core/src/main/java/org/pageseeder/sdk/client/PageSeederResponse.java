package org.pageseeder.sdk.client;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.xml.sax.Handler;
import org.pageseeder.sdk.xml.stax.XMLStreamHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * SDK response with raw body access and XML decoding helpers.
 *
 * <p>For JSON decoding or automatic mapping to model types, use
 * {@link #as(BodyDecoder)} with decoders from {@code sdk-model}.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@SuppressWarnings("java:S6206") // Response is a behavior-rich SDK facade; record identity would include byte[] by reference.
public final class PageSeederResponse {

  private final int statusCode;
  private final Map<String, List<String>> headers;
  private final byte[] body;
  private final @Nullable String mediaType;

  /**
   * Creates an SDK response wrapper.
   *
   * @param statusCode the HTTP status code
   * @param headers    the response headers
   * @param body       the raw response body
   * @param mediaType  the response media type, or {@code null} if unknown
   */
  public PageSeederResponse(int statusCode, Map<String, List<String>> headers, byte[] body,
                            @Nullable String mediaType) {
    this.statusCode = statusCode;
    this.headers = headers;
    this.body = body;
    this.mediaType = mediaType;
  }

  /**
   * Returns the HTTP status code.
   *
   * @return the HTTP status code
   */
  public int statusCode() {
    return this.statusCode;
  }

  /**
   * Returns the response headers.
   *
   * @return the response headers
   */
  public Map<String, List<String>> headers() {
    return this.headers;
  }

  /**
   * Returns the raw response body.
   *
   * @return the response body bytes
   */
  public byte[] body() {
    return this.body;
  }

  /**
   * Returns the response media type.
   *
   * @return the media type, or {@code null} if unknown
   */
  public @Nullable String mediaType() {
    return this.mediaType;
  }

  /**
   * Decodes the response body as text using the charset from the media type, defaulting to UTF-8.
   *
   * @return the response body as text
   */
  public String bodyAsString() {
    return new String(this.body, resolveCharset());
  }

  /**
   * Decodes the response body using the supplied decoder.
   *
   * <p>Use {@code Decoders} from {@code sdk-model} for standard PageSeeder model types,
   * or supply any {@link BodyDecoder} implementation for custom mapping.
   *
   * @param decoder The decoder to apply.
   * @param <T>     The result type.
   * @return The decoded value.
   */
  public <T> T as(BodyDecoder<T> decoder) {
    return decoder.decode(this.body, this.mediaType);
  }

  /**
   * @return XML-specific decoding helpers for this response body.
   */
  public XmlResponseBody xml() {
    return new XmlResponseBody(this.body, this.mediaType);
  }

  // --- XML convenience delegates ---

  /**
   * Parses the response body as XML and forwards SAX events to the supplied handler.
   *
   * @param handler The SAX handler to invoke.
   */
  public void consumeXml(DefaultHandler handler) {
    xml().sax(handler);
  }

  /**
   * Parses the response body as XML using a SAX handler and returns the last item produced.
   *
   * @param handler The handler to invoke.
   * @param <T>     The item type.
   * @return The last item produced, or {@code null} if none.
   */
  public <T> @Nullable T decodeXmlItem(Handler<T> handler) {
    return xml().saxItem(handler);
  }

  /**
   * Parses the response body as XML using a SAX handler and returns all items produced.
   *
   * @param handler The handler to invoke.
   * @param <T>     The item type.
   * @return All items produced by the handler.
   */
  public <T> List<T> decodeXmlList(Handler<T> handler) {
    return xml().saxList(handler);
  }

  /**
   * Parses the response body as XML using a StAX handler and returns the first item produced.
   *
   * @param handler The StAX handler to invoke.
   * @param <T>     The item type.
   * @return The first item produced, or {@code null} if none.
   */
  public <T> @Nullable T decodeXmlItem(XMLStreamHandler<T> handler) {
    return xml().staxItem(handler);
  }

  /**
   * Parses the response body as XML using a StAX handler and returns all items produced.
   *
   * @param handler The StAX handler to invoke.
   * @param <T>     The item type.
   * @return All items produced by the handler.
   */
  public <T> List<T> decodeXmlList(XMLStreamHandler<T> handler) {
    return xml().staxList(handler);
  }

  /**
   * Parses the response body as a DOM document and maps it using the supplied function.
   *
   * @param decoder The DOM mapping function.
   * @param <T>     The return type.
   * @return The mapped value.
   */
  public <T> T decodeXmlDocument(Function<Document, T> decoder) {
    return xml().document(decoder);
  }

  /**
   * Parses the response body as a DOM document and maps all matching elements.
   *
   * @param tagName The element name to select.
   * @param decoder The element mapping function.
   * @param <T>     The item type.
   * @return All mapped elements.
   */
  public <T> List<T> decodeXmlElements(String tagName, Function<Element, T> decoder) {
    return xml().elements(tagName, decoder);
  }

  private Charset resolveCharset() {
    if (this.mediaType == null) {
      return StandardCharsets.UTF_8;
    }
    for (String part : this.mediaType.split(";")) {
      String trimmed = part.trim();
      if (trimmed.startsWith("charset=")) {
        return Charset.forName(trimmed.substring("charset=".length()));
      }
    }
    return StandardCharsets.UTF_8;
  }
}
