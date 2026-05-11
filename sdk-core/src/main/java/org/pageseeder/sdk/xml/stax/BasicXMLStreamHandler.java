package org.pageseeder.sdk.xml.stax;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.xml.sax.InvalidAttributeException;
import org.pageseeder.sdk.xml.sax.MissingAttributeException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Convenience base class for StAX handlers.
 *
 * @param <T> The type of object produced by this handler.
 */
public abstract class BasicXMLStreamHandler<T> implements XMLStreamHandler<T> {

  /**
   * Advances the stream reader to the next start element.
   *
   * @param xml the stream reader
   * @throws XMLStreamException if the stream cannot be advanced
   */
  public static void skipToAnyStartElement(XMLStreamReader xml) throws XMLStreamException {
    do {
      xml.next();
    } while (!xml.isStartElement());
  }

  /**
   * Advances the stream reader to the end element with the supplied name.
   *
   * @param xml  the stream reader
   * @param name the end element name
   * @throws XMLStreamException if the stream cannot be advanced
   */
  public static void skipToEndElement(XMLStreamReader xml, String name) throws XMLStreamException {
    do {
      xml.next();
    } while (!(xml.isEndElement() && xml.getLocalName().equals(name)));
  }

  /**
   * Reads a required attribute from the current element.
   *
   * @param xml  the stream reader
   * @param name the attribute name
   * @return the attribute value
   */
  public static String attribute(XMLStreamReader xml, String name) {
    String value = optionalAttribute(xml, name);
    if (value == null) {
      throw new MissingAttributeException(name);
    }
    return value;
  }

  /**
   * Reads an optional attribute from the current element with a fallback value.
   *
   * @param xml      the stream reader
   * @param name     the attribute name
   * @param fallback the value to return when the attribute is absent
   * @return the attribute value or fallback
   */
  public static String attribute(XMLStreamReader xml, String name, String fallback) {
    String value = optionalAttribute(xml, name);
    return value == null ? fallback : value;
  }

  /**
   * Reads an optional long attribute from the current element with a fallback value.
   *
   * @param xml      the stream reader
   * @param name     the attribute name
   * @param fallback the value to return when the attribute is absent
   * @return the parsed long value or fallback
   */
  public static long attribute(XMLStreamReader xml, String name, long fallback) {
    String value = optionalAttribute(xml, name);
    if (value == null) {
      return fallback;
    }
    try {
      return Long.parseLong(value);
    } catch (IllegalArgumentException ex) {
      throw new InvalidAttributeException(name, ex);
    }
  }

  /**
   * Reads an optional boolean attribute from the current element with a fallback value.
   *
   * @param xml      the stream reader
   * @param name     the attribute name
   * @param fallback the value to return when the attribute is absent
   * @return the parsed boolean value or fallback
   */
  public static boolean attribute(XMLStreamReader xml, String name, boolean fallback) {
    String value = optionalAttribute(xml, name);
    if (value == null) {
      return fallback;
    }
    return "true".equals(value);
  }

  /**
   * Reads an optional attribute from the current element.
   *
   * @param xml  the stream reader
   * @param name the attribute name
   * @return the attribute value, or {@code null} if absent
   */
  public static @Nullable String optionalAttribute(XMLStreamReader xml, String name) {
    for (int i = 0; i < xml.getAttributeCount(); i++) {
      if (name.equals(xml.getAttributeLocalName(i))) {
        return xml.getAttributeValue(i);
      }
    }
    return null;
  }
}
