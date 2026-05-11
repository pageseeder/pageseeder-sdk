package org.pageseeder.sdk.client;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.exception.ParsingException;
import org.pageseeder.sdk.xml.sax.Handler;
import org.pageseeder.sdk.xml.stax.XMLStreamHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * XML-specific decoding helpers for a response body.
 */
public final class XmlResponseBody {

  // XMLInputFactory is thread-safe for createXMLStreamReader per the StAX spec
  private static final XMLInputFactory STAX_FACTORY;
  static {
    XMLInputFactory f = XMLInputFactory.newInstance();
    f.setProperty(XMLInputFactory.IS_COALESCING, true);
    f.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
    f.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    f.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
    STAX_FACTORY = f;
  }

  private final byte[] body;
  private final @Nullable String mediaType;

  /**
   * Creates XML-specific decoding helpers.
   *
   * @param body      the raw response body
   * @param mediaType the response media type, or {@code null} if unknown
   */
  XmlResponseBody(byte[] body, @Nullable String mediaType) {
    this.body = body;
    this.mediaType = mediaType;
  }

  /**
   * Parses the response body as XML and forwards SAX events to the supplied handler.
   *
   * @param handler The SAX handler to invoke.
   */
  public void sax(DefaultHandler handler) {
    try {
      SAXParser parser = newSaxParser();
      parser.parse(new ByteArrayInputStream(this.body), handler);
    } catch (IOException | org.xml.sax.SAXException | ParserConfigurationException ex) {
      throw new ParsingException("Unable to parse PageSeeder XML payload", ex);
    }
  }

  /**
   * Parses the response body as XML using a SAX handler and returns the last item it produced.
   *
   * @param handler The handler to invoke.
   * @param <T> The type of item returned by the handler.
   *
   * @return The last item produced by the handler, or {@code null} when the handler produced none.
   */
  public <T> @Nullable T saxItem(Handler<T> handler) {
    sax(handler);
    return handler.get();
  }

  /**
   * Parses the response body as XML using a SAX handler and returns all items it produced.
   *
   * @param handler The handler to invoke.
   * @param <T> The type of item returned by the handler.
   *
   * @return The items produced by the handler.
   */
  public <T> List<T> saxList(Handler<T> handler) {
    sax(handler);
    return handler.list();
  }

  /**
   * Parses the response body as XML using a StAX handler and returns the first item it produced.
   *
   * @param handler The StAX handler to invoke.
   * @param <T> The type of item returned by the handler.
   *
   * @return The first item produced by the handler, or {@code null} when the handler produced none.
   */
  public <T> @Nullable T staxItem(XMLStreamHandler<T> handler) {
    List<T> items = staxList(handler);
    return items.isEmpty() ? null : items.get(0);
  }

  /**
   * Parses the response body as XML using a StAX handler and returns all items it produced.
   *
   * @param handler The StAX handler to invoke.
   * @param <T> The type of item returned by the handler.
   *
   * @return The items produced by the handler.
   */
  public <T> List<T> staxList(XMLStreamHandler<T> handler) {
    List<T> items = new ArrayList<>();
    try (ByteArrayInputStream in = new ByteArrayInputStream(this.body)) {
      XMLStreamReader reader = STAX_FACTORY.createXMLStreamReader(in, resolveCharset().name());
      while (handler.find(reader)) {
        T item = handler.get(reader);
        if (item != null) {
          items.add(item);
        }
      }
      reader.close();
      return items;
    } catch (XMLStreamException | RuntimeException | IOException ex) {
      throw new ParsingException("Unable to parse PageSeeder XML payload", ex);
    }
  }

  /**
   * Parses the response body as a DOM document and lets the caller map it.
   *
   * @param decoder The DOM decoder.
   * @param <T> The type returned by the decoder.
   *
   * @return The decoded value.
   */
  public <T> T document(Function<Document, T> decoder) {
    return decoder.apply(document());
  }

  /**
   * Parses the response body as XML and maps all matching elements using the supplied decoder.
   *
   * @param tagName The element name to select.
   * @param decoder The element decoder.
   * @param <T> The item type.
   *
   * @return The decoded items.
   */
  public <T> List<T> elements(String tagName, Function<Element, T> decoder) {
    NodeList nodes = document().getElementsByTagName(tagName);
    if (nodes.getLength() == 0) {
      return List.of();
    }
    List<T> items = new ArrayList<>(nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++) {
      items.add(decoder.apply((Element) nodes.item(i)));
    }
    return items;
  }

  /**
   * Parses the response body as a DOM document and returns it.
   *
   * @return The parsed DOM document.
   */
  public Document document() {
    try {
      DocumentBuilder builder = newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(this.body));
    } catch (IOException | org.xml.sax.SAXException | ParserConfigurationException ex) {
      throw new ParsingException("Unable to parse PageSeeder XML payload", ex);
    }
  }

  private Charset resolveCharset() {
    if (this.mediaType == null) {
      return StandardCharsets.UTF_8;
    }
    String[] parts = this.mediaType.split(";");
    for (String part : parts) {
      String trimmed = part.trim();
      if (trimmed.startsWith("charset=")) {
        return Charset.forName(trimmed.substring("charset=".length()));
      }
    }
    return StandardCharsets.UTF_8;
  }

  private static SAXParser newSaxParser() throws ParserConfigurationException, org.xml.sax.SAXException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    return factory.newSAXParser();
  }

  private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(false);
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    return factory.newDocumentBuilder();
  }
}
