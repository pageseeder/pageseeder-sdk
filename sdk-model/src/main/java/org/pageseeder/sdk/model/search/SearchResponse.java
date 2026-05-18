package org.pageseeder.sdk.model.search;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * A PageSeeder search response.
 *
 * @param indexes     the index identifiers listed by the response
 * @param reindexing  whether the response indicated that reindexing is in progress
 * @param warning     the response warning, when present
 * @param suggestions the suggested questions
 * @param facets      the returned facets
 * @param results     the paged search results
 */
public final class SearchResponse {

  private final List<String> indexes;
  private final boolean reindexing;
  private final @Nullable String warning;
  private final List<SearchSuggestion> suggestions;
  private final List<SearchFacet> facets;
  private final SearchResults results;

  /**
   * Creates a search response.
   *
   * @param indexes     the index identifiers listed by the response
   * @param reindexing  whether the response indicated that reindexing is in progress
   * @param warning     the response warning, when present
   * @param suggestions the suggested questions
   * @param facets      the returned facets
   * @param results     the paged search results
   */
  public SearchResponse(List<String> indexes, boolean reindexing, @Nullable String warning,
                        List<SearchSuggestion> suggestions, List<SearchFacet> facets,
                        SearchResults results) {
    this(indexes, reindexing, warning, suggestions, facets, results, false);
  }

  private SearchResponse(List<String> indexes, boolean reindexing, @Nullable String warning,
                         List<SearchSuggestion> suggestions, List<SearchFacet> facets,
                         SearchResults results, boolean trusted) {
    this.indexes = trusted ? SearchLists.trusted(indexes) : SearchLists.copy(indexes);
    this.reindexing = reindexing;
    this.warning = warning;
    this.suggestions = trusted ? SearchLists.trusted(suggestions) : SearchLists.copy(suggestions);
    this.facets = trusted ? SearchLists.trusted(facets) : SearchLists.copy(facets);
    this.results = results == null ? SearchResults.EMPTY : results;
  }

  static SearchResponse trusted(List<String> indexes, boolean reindexing, @Nullable String warning,
                                List<SearchSuggestion> suggestions, List<SearchFacet> facets,
                                SearchResults results) {
    return new SearchResponse(indexes, reindexing, warning, suggestions, facets, results, true);
  }

  public List<String> indexes() {
    return this.indexes;
  }

  public boolean reindexing() {
    return this.reindexing;
  }

  public @Nullable String warning() {
    return this.warning;
  }

  public List<SearchSuggestion> suggestions() {
    return this.suggestions;
  }

  public List<SearchFacet> facets() {
    return this.facets;
  }

  public SearchResults results() {
    return this.results;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof SearchResponse that)) {
      return false;
    }
    return this.reindexing == that.reindexing
        && this.indexes.equals(that.indexes)
        && Objects.equals(this.warning, that.warning)
        && this.suggestions.equals(that.suggestions)
        && this.facets.equals(that.facets)
        && this.results.equals(that.results);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.indexes, this.reindexing, this.warning, this.suggestions, this.facets, this.results);
  }

  @Override
  public String toString() {
    return "SearchResponse[indexes=" + this.indexes
        + ", reindexing=" + this.reindexing
        + ", warning=" + this.warning
        + ", suggestions=" + this.suggestions
        + ", facets=" + this.facets
        + ", results=" + this.results + ']';
  }
}
