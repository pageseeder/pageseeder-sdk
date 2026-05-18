package org.pageseeder.sdk.model.search;

/**
 * The kind of field returned for a search hit.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
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
