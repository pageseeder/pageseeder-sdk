package org.pageseeder.sdk.model.search;

/**
 * The kind of field returned for a search hit.
 */
public enum SearchFieldKind {

  /**
   * A stored index field.
   */
  FIELD,

  /**
   * A text extract returned by the search service.
   */
  EXTRACT
}
