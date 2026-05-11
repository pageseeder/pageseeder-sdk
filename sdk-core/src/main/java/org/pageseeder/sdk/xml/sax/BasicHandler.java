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
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class BasicHandler<T> extends Handler<T> {

  private final List<T> list = new ArrayList<>();
  private final List<String> ancestorOrSelf = new ArrayList<>();
  private @Nullable StringBuilder buffer = null;
  private @Nullable Locator locator = null;

  /**
   * Handles the start of an XML element using its local name.
   *
   * @param element the local element name
   * @param atts    the element attributes
   */
  public void startElement(String element, Attributes atts) {
  }

  /**
   * Handles the end of an XML element using its local name.
   *
   * @param element the local element name
   */
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

  /**
   * Starts buffering character content.
   */
  protected final void newBuffer() {
    this.buffer = new StringBuilder();
  }

  /**
   * Returns the current buffered character content.
   *
   * @return the buffered text, or {@code null} if no buffer is active
   */
  protected final @Nullable String buffer() {
    StringBuilder b = this.buffer;
    return b != null ? b.toString() : null;
  }

  /**
   * Returns the current buffered character content, optionally clearing the buffer.
   *
   * @param clear whether to clear the buffer after reading it
   * @return the buffered text, or {@code null} if no buffer is active
   */
  protected final @Nullable String buffer(boolean clear) {
    String text = buffer();
    if (clear) {
      this.buffer = null;
    }
    return text;
  }

  /**
   * Appends text to the current buffer if one is active.
   *
   * @param s the text to append
   */
  protected final void append(String s) {
    StringBuilder b = this.buffer;
    if (b != null) {
      b.append(s);
    }
  }

  /**
   * Clears the current character buffer.
   */
  protected void clearBuffer() {
    this.buffer = null;
  }

  /**
   * Adds a parsed item to this handler's result list.
   *
   * @param item the parsed item
   */
  protected final void add(T item) {
    Objects.requireNonNull(item, "Cannot add null item to list");
    this.list.add(item);
  }

  /**
   * Returns the current element name.
   *
   * @return the current element name, or {@code null} before parsing starts
   */
  protected final @Nullable String element() {
    return this.ancestorOrSelf.isEmpty() ? null : this.ancestorOrSelf.get(this.ancestorOrSelf.size() - 1);
  }

  /**
   * Returns the parent element name.
   *
   * @return the parent element name, or {@code null} if there is no parent
   */
  protected final @Nullable String parent() {
    return this.ancestorOrSelf.size() > 1 ? this.ancestorOrSelf.get(this.ancestorOrSelf.size() - 2) : null;
  }

  /**
   * Tests whether the current element has the supplied name.
   *
   * @param element the element name to compare
   * @return {@code true} when the current element has that name
   */
  protected final boolean isElement(String element) {
    return element.equals(element());
  }

  /**
   * Tests whether the parent element has the supplied name.
   *
   * @param parent the parent element name to compare
   * @return {@code true} when the parent element has that name
   */
  protected final boolean isParent(String parent) {
    return parent.equals(parent());
  }

  /**
   * Tests whether the current element has the supplied ancestor.
   *
   * @param ancestor the ancestor element name to compare
   * @return {@code true} when the current element is below that ancestor
   */
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

  /**
   * Tests whether the current element matches any supplied name.
   *
   * @param elements the element names to compare
   * @return {@code true} when the current element matches one of the supplied names
   */
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

  /**
   * Reads a required long attribute.
   *
   * @param atts the SAX attributes
   * @param name the attribute name
   * @return the parsed long value
   */
  public static Long getLong(Attributes atts, String name) {
    String value = getString(atts, name);
    return toLong(value, name);
  }

  /**
   * Reads an optional long attribute with a fallback value.
   *
   * @param atts     the SAX attributes
   * @param name     the attribute name
   * @param fallback the value to return when the attribute is absent
   * @return the parsed long value or fallback
   */
  public static Long getLong(Attributes atts, String name, Long fallback) {
    String value = atts.getValue(name);
    return value != null ? toLong(value, name) : fallback;
  }

  /**
   * Reads an optional long attribute.
   *
   * @param atts the SAX attributes
   * @param name the attribute name
   * @return the parsed long value, or {@code null} if absent
   */
  public static @Nullable Long getOptionalLong(Attributes atts, String name) {
    String value = atts.getValue(name);
    if (value == null) {
      return null;
    }
    return toLong(value, name);
  }

  /**
   * Reads a required string attribute.
   *
   * @param atts the SAX attributes
   * @param name the attribute name
   * @return the attribute value
   */
  public static String getString(Attributes atts, String name) {
    String value = atts.getValue(name);
    if (value == null) {
      throw new MissingAttributeException(name);
    }
    return value;
  }

  /**
   * Reads an optional string attribute with a fallback value.
   *
   * @param atts     the SAX attributes
   * @param name     the attribute name
   * @param fallback the value to return when the attribute is absent
   * @return the attribute value or fallback
   */
  public static String getString(Attributes atts, String name, String fallback) {
    String value = atts.getValue(name);
    return value != null ? value : fallback;
  }

  /**
   * Reads a required integer attribute.
   *
   * @param atts the SAX attributes
   * @param name the attribute name
   * @return the parsed integer value
   */
  public static int getInt(Attributes atts, String name) {
    String value = getString(atts, name);
    return toInt(value, name);
  }

  /**
   * Reads an optional integer attribute with a fallback value.
   *
   * @param atts     the SAX attributes
   * @param name     the attribute name
   * @param fallback the value to return when the attribute is absent
   * @return the parsed integer value or fallback
   */
  public static int getInt(Attributes atts, String name, int fallback) {
    String value = atts.getValue(name);
    return value != null ? toInt(value, name) : fallback;
  }

  /**
   * Reads an optional string attribute.
   *
   * @param atts the SAX attributes
   * @param name the attribute name
   * @return the attribute value, or {@code null} if absent
   */
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
