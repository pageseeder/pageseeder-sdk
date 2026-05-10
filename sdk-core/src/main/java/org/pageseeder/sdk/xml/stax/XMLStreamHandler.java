package org.pageseeder.sdk.xml.stax;

import org.jspecify.annotations.Nullable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Contract for pull-based XML handlers.
 *
 * @param <T> The type of object produced by this handler.
 */
public interface XMLStreamHandler<T> {

  boolean find(XMLStreamReader xml) throws XMLStreamException;

  @Nullable T get(XMLStreamReader xml) throws XMLStreamException;

  default @Nullable T next(XMLStreamReader xml) throws XMLStreamException {
    while (find(xml)) {
      T item = get(xml);
      if (item != null) {
        return item;
      }
    }
    return null;
  }
}
