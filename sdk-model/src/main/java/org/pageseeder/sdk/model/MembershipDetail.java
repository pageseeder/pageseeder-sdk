package org.pageseeder.sdk.model;

/**
 * Immutable membership detail field.
 */
public final class MembershipDetail {

  private final int position;
  private final String name;
  private final String value;
  private final boolean editable;
  private final String title;
  private final String type;

  public MembershipDetail(int position, String name, String value, boolean editable, String title, String type) {
    this.position = position;
    this.name = name;
    this.value = value;
    this.editable = editable;
    this.title = title;
    this.type = type;
  }

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
