package org.pageseeder.sdk.model;

/**
 * Immutable membership detail field.
 *
 * @param position the display position
 * @param name     the detail field name
 * @param value    the detail field value
 * @param editable whether the field is editable
 * @param title    the display title
 * @param type     the detail field type
 */
public record MembershipDetail(int position, String name, String value, boolean editable, String title, String type) {

  /**
   * Returns the display position.
   *
   * @return the display position
   */
  public int getPosition() {
    return this.position;
  }

  /**
   * Returns the detail field name.
   *
   * @return the detail field name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the detail field value.
   *
   * @return the detail field value
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Indicates whether the detail field is editable.
   *
   * @return {@code true} when the field is editable
   */
  public boolean isEditable() {
    return this.editable;
  }

  /**
   * Returns the display title.
   *
   * @return the display title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Returns the detail field type.
   *
   * @return the detail field type
   */
  public String getType() {
    return this.type;
  }
}
