package org.pageseeder.sdk.model.search;

import java.util.Objects;

/**
 * A counted term in a returned search facet.
 *
 * @param text        the term text
 * @param cardinality the number of matching results
 */
public record SearchFacetTerm(String text, int cardinality) {

  /**
   * Creates a search facet term.
   *
   * @param text        the term text
   * @param cardinality the number of matching results
   */
  public SearchFacetTerm {
    Objects.requireNonNull(text, "text");
  }
}
