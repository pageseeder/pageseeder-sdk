package org.pageseeder.sdk.model.search;

import java.util.Objects;

/**
 * A counted term in a returned search facet.
 *
 * @param text        the term text
 * @param cardinality the number of matching results
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
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
