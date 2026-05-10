package org.pageseeder.sdk.legacy;

import org.jspecify.annotations.Nullable;
import org.pageseeder.bridge.xml.Handler;
import org.pageseeder.bridge.xml.stax.XMLStreamHandler;
import org.pageseeder.sdk.client.BodyDecoder;
import org.pageseeder.sdk.exception.ParsingException;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
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

/**
 * Factory for {@link BodyDecoder} implementations backed by legacy Bridge {@link Handler} instances.
 *
 * <p>Allows existing {@link org.pageseeder.bridge.xml.BasicHandler} subclasses to be used with the
 * SDK's typed execution path during migration from PageSeeder Bridge to the SDK:
 *
 * <pre>{@code
 * Member member = client.execute(call, LegacyHandlers.item(new MyMemberHandler()));
 * List<Membership> list = client.execute(call, LegacyHandlers.list(new MyMembershipHandler()));
 * }</pre>
 */
public final class LegacyHandlers {

  private LegacyHandlers() {}

  /**
   * Returns a decoder that parses the response XML using the supplied Bridge handler and returns
   * the last item it produced.
   *
   * @param handler The Bridge SAX handler.
   * @param <T>     The type of object produced by the handler.
   * @return A decoder wrapping the handler; the decoded value may be {@code null} when the handler
   *         produced no items.
   */
  public static <T> BodyDecoder<T> item(Handler<T> handler) {
    return (body, mediaType) -> {
      parse(handler, body);
      return handler.get();
    };
  }

  /**
   * Returns a decoder that parses the response XML using the supplied Bridge handler and returns
   * all items it produced.
   *
   * @param handler The Bridge SAX handler.
   * @param <T>     The item type produced by the handler.
   * @return A decoder wrapping the handler; never {@code null}, may be empty.
   */
  public static <T> BodyDecoder<List<T>> list(Handler<T> handler) {
    return (body, mediaType) -> {
      parse(handler, body);
      return handler.list();
    };
  }

  /**
   * Returns a decoder that parses the response XML using the supplied Bridge StAX handler and
   * returns the first item it produced.
   *
   * @param handler The Bridge StAX handler.
   * @param <T>     The type of object produced by the handler.
   * @return A decoder wrapping the handler; the decoded value may be {@code null} when the handler
   *         produced no items.
   */
  public static <T> BodyDecoder<@Nullable T> item(XMLStreamHandler<T> handler) {
    return (body, mediaType) -> {
      List<T> items = staxList(handler, body, mediaType);
      return items.isEmpty() ? null : items.get(0);
    };
  }

  /**
   * Returns a decoder that parses the response XML using the supplied Bridge StAX handler and
   * returns all items it produced.
   *
   * @param handler The Bridge StAX handler.
   * @param <T>     The item type produced by the handler.
   * @return A decoder wrapping the handler; never {@code null}, may be empty.
   */
  public static <T> BodyDecoder<List<T>> list(XMLStreamHandler<T> handler) {
    return (body, mediaType) -> staxList(handler, body, mediaType);
  }

  private static <T> List<T> staxList(XMLStreamHandler<T> handler, byte[] body, @Nullable String mediaType) {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    factory.setProperty(XMLInputFactory.IS_COALESCING, true);
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
    List<T> items = new ArrayList<>();
    try (ByteArrayInputStream in = new ByteArrayInputStream(body)) {
      XMLStreamReader reader = factory.createXMLStreamReader(in, resolveCharset(mediaType).name());
      while (handler.find(reader)) {
        T item = handler.get(reader);
        if (item != null) {
          items.add(item);
        }
      }
      reader.close();
      return items;
    } catch (XMLStreamException | IOException ex) {
      throw new ParsingException("Unable to parse PageSeeder XML payload", ex);
    }
  }

  private static Charset resolveCharset(@Nullable String mediaType) {
    if (mediaType != null) {
      for (String part : mediaType.split(";")) {
        String trimmed = part.trim();
        if (trimmed.startsWith("charset=")) {
          return Charset.forName(trimmed.substring("charset=".length()));
        }
      }
    }
    return StandardCharsets.UTF_8;
  }

  private static void parse(DefaultHandler handler, byte[] body) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setValidating(false);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.newSAXParser().parse(new ByteArrayInputStream(body), handler);
    } catch (IOException | SAXException | ParserConfigurationException ex) {
      throw new ParsingException("Unable to parse PageSeeder XML payload", ex);
    }
  }
}
