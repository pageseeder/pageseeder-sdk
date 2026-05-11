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

  public int getPosition() {
    return this.position;
  }

  public String getName() {
    return this.name;
  }

  public String getValue() {
    return this.value;
  }

  public boolean isEditable() {
    return this.editable;
  }

  public String getTitle() {
    return this.title;
  }

  public String getType() {
    return this.type;
  }
}
