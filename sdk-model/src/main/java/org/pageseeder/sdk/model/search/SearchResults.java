package org.pageseeder.sdk.model.search;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * The paged hit section of a search response.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SearchResults {

  /**
   * Empty search results.
   */
  public static final SearchResults EMPTY = new SearchResults(0, 0, 0, 0, 0, 0, List.of());

  private final int page;
  private final int pageSize;
  private final int totalPages;
  private final int totalResults;
  private final int firstResult;
  private final int lastResult;
  private final List<SearchHit> hits;

  /**
   * Creates search results.
   *
   * @param page         the current page number
   * @param pageSize     the requested page size
   * @param totalPages   the total number of pages
   * @param totalResults the total number of matching results
   * @param firstResult  the first result index in this page
   * @param lastResult   the last result index in this page
   * @param hits         the returned hits
   */
  public SearchResults(int page, int pageSize, int totalPages, int totalResults, int firstResult,
                       int lastResult, List<SearchHit> hits) {
    this(page, pageSize, totalPages, totalResults, firstResult, lastResult, hits, false);
  }

  private SearchResults(int page, int pageSize, int totalPages, int totalResults, int firstResult,
                        int lastResult, List<SearchHit> hits, boolean trusted) {
    this.page = page;
    this.pageSize = pageSize;
    this.totalPages = totalPages;
    this.totalResults = totalResults;
    this.firstResult = firstResult;
    this.lastResult = lastResult;
    this.hits = trusted ? SearchLists.trusted(hits) : SearchLists.copy(hits);
  }

  static SearchResults trusted(int page, int pageSize, int totalPages, int totalResults, int firstResult,
                               int lastResult, List<SearchHit> hits) {
    return new SearchResults(page, pageSize, totalPages, totalResults, firstResult, lastResult, hits, true);
  }

  /**
   * @return the current page number
   */
  public int page() {
    return this.page;
  }

  /**
   * @return the requested page size
   */
  public int pageSize() {
    return this.pageSize;
  }

  /**
   * @return the total number of pages
   */
  public int totalPages() {
    return this.totalPages;
  }

  /**
   * @return the total number of matching results
   */
  public int totalResults() {
    return this.totalResults;
  }

  /**
   * @return the first result index in this page
   */
  public int firstResult() {
    return this.firstResult;
  }

  /**
   * @return the last result index in this page
   */
  public int lastResult() {
    return this.lastResult;
  }

  /**
   * @return the returned search hits
   */
  public List<SearchHit> hits() {
    return this.hits;
  }

  @Override
  public boolean equals(@Nullable Object object) {
    if (this == object) {
      return true;
    }
    if (!(object instanceof SearchResults that)) {
      return false;
    }
    return this.page == that.page
        && this.pageSize == that.pageSize
        && this.totalPages == that.totalPages
        && this.totalResults == that.totalResults
        && this.firstResult == that.firstResult
        && this.lastResult == that.lastResult
        && this.hits.equals(that.hits);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.page, this.pageSize, this.totalPages, this.totalResults, this.firstResult, this.lastResult, this.hits);
  }

  @Override
  public String toString() {
    return "SearchResults[page=" + this.page
        + ", pageSize=" + this.pageSize
        + ", totalPages=" + this.totalPages
        + ", totalResults=" + this.totalResults
        + ", firstResult=" + this.firstResult
        + ", lastResult=" + this.lastResult
        + ", hits=" + this.hits + ']';
  }
}
