package org.pageseeder.sdk.xml.sax;

import org.jspecify.annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Convenience SAX handler with basic parsing state.
 *
 * @param <T> The type of object produced by this handler.
 */
public abstract class BasicHandler<T> extends Handler<T> {

  private final List<T> list = new ArrayList<>();
  private final List<String> ancestorOrSelf = new ArrayList<>();
  private @Nullable StringBuilder buffer = null;
  private @Nullable Locator locator = null;

  public void startElement(String element, Attributes atts) {
  }

  public void endElement(String element) {
  }

  @Override
  @SuppressWarnings("java:S1075") // XPath uses '/'
  public final void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    String element = localName.isEmpty() ? qName : localName;
    this.ancestorOrSelf.add(element);
    try {
      startElement(element, attributes);
    } catch (AttributeException ex) {
      String xpath = "/" + String.join("/", this.ancestorOrSelf) + "/@" + ex.getAttributeName();
      throw new SAXParseException(ex.getMessage() + " XPath=" + xpath, this.locator);
    }
  }

  @Override
  public final void endElement(String uri, String localName, String qName) {
    String element = localName.isEmpty() ? qName : localName;
    endElement(element);
    if (!this.ancestorOrSelf.isEmpty()) {
      this.ancestorOrSelf.remove(this.ancestorOrSelf.size() - 1);
    }
  }

  @Override
  public final void characters(char[] ch, int start, int length) {
    StringBuilder b = this.buffer;
    if (b != null) {
      b.append(ch, start, length);
    }
  }

  @Override
  public void setDocumentLocator(Locator locator) {
    this.locator = locator;
  }

  protected final void newBuffer() {
    this.buffer = new StringBuilder();
  }

  protected final @Nullable String buffer() {
    StringBuilder b = this.buffer;
    return b != null ? b.toString() : null;
  }

  protected final @Nullable String buffer(boolean clear) {
    String text = buffer();
    if (clear) {
      this.buffer = null;
    }
    return text;
  }

  protected final void append(String s) {
    StringBuilder b = this.buffer;
    if (b != null) {
      b.append(s);
    }
  }

  protected void clearBuffer() {
    this.buffer = null;
  }

  protected final void add(T item) {
    Objects.requireNonNull(item, "Cannot add null item to list");
    this.list.add(item);
  }

  protected final @Nullable String element() {
    return this.ancestorOrSelf.isEmpty() ? null : this.ancestorOrSelf.get(this.ancestorOrSelf.size() - 1);
  }

  protected final @Nullable String parent() {
    return this.ancestorOrSelf.size() > 1 ? this.ancestorOrSelf.get(this.ancestorOrSelf.size() - 2) : null;
  }

  protected final boolean isElement(String element) {
    return element.equals(element());
  }

  protected final boolean isParent(String parent) {
    return parent.equals(parent());
  }

  protected final boolean hasAncestor(String ancestor) {
    if (this.ancestorOrSelf.size() <= 1) {
      return false;
    }
    for (int i = this.ancestorOrSelf.size() - 2; i >= 0; i--) {
      if (ancestor.equals(this.ancestorOrSelf.get(i))) {
        return true;
      }
    }
    return false;
  }

  protected final boolean isAny(String... elements) {
    String current = element();
    if (current != null) {
      for (String e : elements) {
        if (e.equals(current)) {
          return true;
        }
      }
    }
    return false;
  }

  public static Long getLong(Attributes atts, String name) {
    String value = atts.getValue(name);
    if (value == null) {
      throw new MissingAttributeException(name);
    }
    return toLong(value, name);
  }

  public static Long getLong(Attributes atts, String name, Long fallback) {
    String value = atts.getValue(name);
    return value != null ? toLong(value, name) : fallback;
  }

  public static @Nullable Long getOptionalLong(Attributes atts, String name) {
    String value = atts.getValue(name);
    if (value == null) {
      return null;
    }
    return toLong(value, name);
  }

  public static String getString(Attributes atts, String name) {
    String value = atts.getValue(name);
    if (value == null) {
      throw new MissingAttributeException(name);
    }
    return value;
  }

  public static String getString(Attributes atts, String name, String fallback) {
    String value = atts.getValue(name);
    return value != null ? value : fallback;
  }

  public static int getInt(Attributes atts, String name) {
    String value = atts.getValue(name);
    if (value == null) {
      throw new MissingAttributeException(name);
    }
    return toInt(value, name);
  }

  public static int getInt(Attributes atts, String name, int fallback) {
    String value = atts.getValue(name);
    return value != null ? toInt(value, name) : fallback;
  }

  public static @Nullable String getOptionalString(Attributes atts, String name) {
    return atts.getValue(name);
  }

  private static Long toLong(String value, String name) {
    try {
      return Long.valueOf(value);
    } catch (NumberFormatException ex) {
      throw new InvalidAttributeException(name, ex);
    }
  }

  private static int toInt(String value, String name) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      throw new InvalidAttributeException(name, ex);
    }
  }

  @Override
  public List<T> list() {
    return this.list;
  }

  @Override
  public @Nullable T get() {
    return this.list.isEmpty() ? null : this.list.get(this.list.size() - 1);
  }
}
