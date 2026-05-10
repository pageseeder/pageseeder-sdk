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

  public static void skipToAnyStartElement(XMLStreamReader xml) throws XMLStreamException {
    do {
      xml.next();
    } while (!xml.isStartElement());
  }

  public static void skipToEndElement(XMLStreamReader xml, String name) throws XMLStreamException {
    do {
      xml.next();
    } while (!(xml.isEndElement() && xml.getLocalName().equals(name)));
  }

  public static String attribute(XMLStreamReader xml, String name) {
    String value = optionalAttribute(xml, name);
    if (value == null) {
      throw new MissingAttributeException(name);
    }
    return value;
  }

  public static String attribute(XMLStreamReader xml, String name, String fallback) {
    String value = optionalAttribute(xml, name);
    return value == null ? fallback : value;
  }

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

  public static boolean attribute(XMLStreamReader xml, String name, boolean fallback) {
    String value = optionalAttribute(xml, name);
    if (value == null) {
      return fallback;
    }
    return "true".equals(value);
  }

  public static @Nullable String optionalAttribute(XMLStreamReader xml, String name) {
    for (int i = 0; i < xml.getAttributeCount(); i++) {
      if (name.equals(xml.getAttributeLocalName(i))) {
        return xml.getAttributeValue(i);
      }
    }
    return null;
  }
}
