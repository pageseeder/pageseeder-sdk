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
 */
public final class SearchFacet {

  private final String name;
  private final String type;
  private final boolean flexible;
  private final boolean hasResults;
  private final int totalTerms;
  private final @Nullable String dataType;
  private final List<SearchFacetTerm> terms;

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
  public SearchFacet(String name, String type, boolean flexible, boolean hasResults, int totalTerms,
                     @Nullable String dataType, List<SearchFacetTerm> terms) {
    this(name, type, flexible, hasResults, totalTerms, dataType, terms, false);
  }

  private SearchFacet(String name, String type, boolean flexible, boolean hasResults, int totalTerms,
                      @Nullable String dataType, List<SearchFacetTerm> terms, boolean trusted) {
    this.name = Objects.requireNonNull(name, "name");
    this.type = Objects.toString(type, "");
    this.flexible = flexible;
    this.hasResults = hasResults;
    this.totalTerms = totalTerms;
    this.dataType = dataType;
    this.terms = trusted ? SearchLists.trusted(terms) : SearchLists.copy(terms);
  }

  static SearchFacet trusted(String name, String type, boolean flexible, boolean hasResults, int totalTerms,
                             @Nullable String dataType, List<SearchFacetTerm> terms) {
    return new SearchFacet(name, type, flexible, hasResults, totalTerms, dataType, terms, true);
  }

  public String name() {
    return this.name;
  }

  public String type() {
    return this.type;
  }

  public boolean flexible() {
    return this.flexible;
  }

  public boolean hasResults() {
    return this.hasResults;
  }

  public int totalTerms() {
    return this.totalTerms;
  }

  public @Nullable String dataType() {
    return this.dataType;
  }

  public List<SearchFacetTerm> terms() {
    return this.terms;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof SearchFacet that)) {
      return false;
    }
    return this.flexible == that.flexible
        && this.hasResults == that.hasResults
        && this.totalTerms == that.totalTerms
        && this.name.equals(that.name)
        && this.type.equals(that.type)
        && Objects.equals(this.dataType, that.dataType)
        && this.terms.equals(that.terms);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.name, this.type, this.flexible, this.hasResults, this.totalTerms, this.dataType, this.terms);
  }

  @Override
  public String toString() {
    return "SearchFacet[name=" + this.name
        + ", type=" + this.type
        + ", flexible=" + this.flexible
        + ", hasResults=" + this.hasResults
        + ", totalTerms=" + this.totalTerms
        + ", dataType=" + this.dataType
        + ", terms=" + this.terms + ']';
  }
}
