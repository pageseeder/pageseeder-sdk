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
}
