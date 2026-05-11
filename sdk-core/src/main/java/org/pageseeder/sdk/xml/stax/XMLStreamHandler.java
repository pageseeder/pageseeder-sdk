package org.pageseeder.sdk.xml.stax;

import org.jspecify.annotations.Nullable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Contract for pull-based XML handlers.
 *
 * @param <T> The type of object produced by this handler.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public interface XMLStreamHandler<T> {

  /**
   * Moves the stream reader to the next item this handler can parse.
   *
   * @param xml the stream reader
   * @return {@code true} when a matching item is available
   * @throws XMLStreamException if the stream cannot be read
   */
  boolean find(XMLStreamReader xml) throws XMLStreamException;

  /**
   * Parses the current item from the stream reader.
   *
   * @param xml the stream reader
   * @return the parsed item, or {@code null} if the current item should be skipped
   * @throws XMLStreamException if the stream cannot be read
   */
  @Nullable T get(XMLStreamReader xml) throws XMLStreamException;

  /**
   * Finds and parses the next available item.
   *
   * @param xml the stream reader
   * @return the next parsed item, or {@code null} if none is available
   * @throws XMLStreamException if the stream cannot be read
   */
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
