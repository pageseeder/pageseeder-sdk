package org.pageseeder.sdk.xml.sax;

import org.jspecify.annotations.Nullable;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;

/**
 * SAX handler able to expose parsed objects directly.
 *
 * @param <T> The type of object produced by this handler.
 */
public abstract class Handler<T> extends DefaultHandler {

  /**
   * Returns all parsed items.
   *
   * @return the parsed items
   */
  public abstract List<T> list();

  /**
   * Returns the current or last parsed item.
   *
   * @return the current or last parsed item, or {@code null} if none is available
   */
  public abstract @Nullable T get();
}
