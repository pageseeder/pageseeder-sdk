package org.pageseeder.sdk.legacy;

import org.junit.jupiter.api.Test;
import org.pageseeder.bridge.xml.BasicHandler;
import org.pageseeder.bridge.xml.stax.BasicXMLStreamHandler;
import org.pageseeder.sdk.client.BodyDecoder;
import org.xml.sax.Attributes;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LegacyHandlersTest {

  private static final String XML_ONE =
      "<root><item name=\"alpha\"/></root>";

  private static final String XML_MANY =
      "<root><item name=\"alpha\"/><item name=\"beta\"/></root>";

  private static final String XML_EMPTY =
      "<root/>";

  /** Minimal Bridge BasicHandler subclass that collects item names as strings. */
  private static class ItemNameHandler extends BasicHandler<String> {
    @Override
    public void startElement(String element, Attributes atts) {
      if ("item".equals(element)) {
        add(atts.getValue("name"));
      }
    }
  }

  @Test
  public void item_returnsSingleResult() {
    BodyDecoder<String> decoder = LegacyHandlers.item(new ItemNameHandler());
    String result = decoder.decode(bytes(XML_ONE), "application/xml");
    assertEquals("alpha", result);
  }

  @Test
  public void item_returnsLastWhenMultiple() {
    BodyDecoder<String> decoder = LegacyHandlers.item(new ItemNameHandler());
    String result = decoder.decode(bytes(XML_MANY), "application/xml");
    assertEquals("beta", result);
  }

  @Test
  public void item_returnsNullWhenNoneFound() {
    BodyDecoder<String> decoder = LegacyHandlers.item(new ItemNameHandler());
    String result = decoder.decode(bytes(XML_EMPTY), "application/xml");
    assertNull(result);
  }

  @Test
  public void list_returnsAllItems() {
    BodyDecoder<List<String>> decoder = LegacyHandlers.list(new ItemNameHandler());
    List<String> result = decoder.decode(bytes(XML_MANY), "application/xml");
    assertEquals(List.of("alpha", "beta"), result);
  }

  @Test
  public void list_returnsEmptyListWhenNoneFound() {
    BodyDecoder<List<String>> decoder = LegacyHandlers.list(new ItemNameHandler());
    List<String> result = decoder.decode(bytes(XML_EMPTY), "application/xml");
    assertTrue(result.isEmpty());
  }

  // StAX handler tests
  // ---------------------------------------------------------------------------

  /** Minimal Bridge BasicXMLStreamHandler subclass that collects item names as strings. */
  private static class StaxItemNameHandler extends BasicXMLStreamHandler<String> {
    @Override
    public boolean find(XMLStreamReader xml) throws XMLStreamException {
      while (xml.hasNext()) {
        xml.next();
        if (xml.isStartElement() && "item".equals(xml.getLocalName())) {
          return true;
        }
      }
      return false;
    }

    @Override
    public String get(XMLStreamReader xml) {
      return xml.getAttributeValue(null, "name");
    }
  }

  @Test
  public void stax_item_returnsSingleResult() {
    BodyDecoder<String> decoder = LegacyHandlers.item(new StaxItemNameHandler());
    String result = decoder.decode(bytes(XML_ONE), "application/xml");
    assertEquals("alpha", result);
  }

  @Test
  public void stax_item_returnsFirstWhenMultiple() {
    BodyDecoder<String> decoder = LegacyHandlers.item(new StaxItemNameHandler());
    String result = decoder.decode(bytes(XML_MANY), "application/xml");
    assertEquals("alpha", result);
  }

  @Test
  public void stax_item_returnsNullWhenNoneFound() {
    BodyDecoder<String> decoder = LegacyHandlers.item(new StaxItemNameHandler());
    String result = decoder.decode(bytes(XML_EMPTY), "application/xml");
    assertNull(result);
  }

  @Test
  public void stax_list_returnsAllItems() {
    BodyDecoder<List<String>> decoder = LegacyHandlers.list(new StaxItemNameHandler());
    List<String> result = decoder.decode(bytes(XML_MANY), "application/xml");
    assertEquals(List.of("alpha", "beta"), result);
  }

  @Test
  public void stax_list_returnsEmptyListWhenNoneFound() {
    BodyDecoder<List<String>> decoder = LegacyHandlers.list(new StaxItemNameHandler());
    List<String> result = decoder.decode(bytes(XML_EMPTY), "application/xml");
    assertTrue(result.isEmpty());
  }

  private static byte[] bytes(String xml) {
    return xml.getBytes(StandardCharsets.UTF_8);
  }
}
