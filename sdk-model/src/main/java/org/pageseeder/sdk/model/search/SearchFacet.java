package org.pageseeder.sdk.model.search;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A facet returned by a search response.
 *
 * @param name       the facet field name
 * @param type       the facet type
 * @param flexible   whether this facet was computed as a flexible facet
 * @param hasResults whether the facet has matching results
 * @param totalTerms the total number of terms for the facet
 * @param dataType   the field data type, when returned
 * @param terms      the returned facet terms
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record SearchFacet(String name, String type, boolean flexible, boolean hasResults, int totalTerms,
                          @Nullable String dataType, List<SearchFacetTerm> terms) {

  /**
   * Creates a returned search facet.
   *
   * @param name       the facet field name
   * @param type       the facet type
   * @param flexible   whether this facet was computed as a flexible facet
   * @param hasResults whether the facet has matching results
   * @param totalTerms the total number of terms for the facet
   * @param dataType   the field data type, when returned
   * @param terms      the returned facet terms
   */
  public SearchFacet {
    Objects.requireNonNull(name, "name");
    type = Objects.toString(type, "");
    terms = terms == null ? List.of() : List.copyOf(terms);
  }

}
