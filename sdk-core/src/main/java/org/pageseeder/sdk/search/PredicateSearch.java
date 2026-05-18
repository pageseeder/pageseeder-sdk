/*
 * Copyright 2018 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.sdk.search;

import org.pageseeder.sdk.service.ServiceCall;
import org.pageseeder.sdk.service.ServiceCatalog;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable query for Lucene predicate-based searches.
 *
 * <p>The predicate is passed verbatim to the PageSeeder search engine as a Lucene
 * query string. Build a query with the fluent methods, then bind it to a {@link SearchScope}
 * to get a {@link ServiceCall} ready to execute:</p>
 * <pre>{@code
 * ServiceCall call = PredicateSearch.of("title:report AND pstype:document")
 *     .facet("psstatus")
 *     .pageSize(50)
 *     .toServiceCall(SearchScope.project("my-project", "jdoe"));
 * }</pre>
 *
 * @see <a href="https://dev.pageseeder.com/api/web_services/services/group-search-predicate-GET.html">/groups/{group}/search/predicate [GET]</a>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class PredicateSearch implements Serializable {

  @SuppressWarnings("java:S2057")
  private static final long serialVersionUID = 20260518L;

  private final Predicate predicate;
  private final FacetList facets;
  private final Page page;
  private final List<String> sortFields;

  private PredicateSearch(Predicate predicate, FacetList facets, Page page, List<String> sortFields) {
    this.predicate = predicate;
    this.facets = facets;
    this.page = page;
    this.sortFields = sortFields;
  }

  /** @return An empty predicate search with no criteria set. */
  public static PredicateSearch create() {
    return new PredicateSearch(Predicate.EMPTY, FacetList.EMPTY, Page.DEFAULT_PAGE, List.of());
  }

  /** @return A predicate search with the specified Lucene predicate string. */
  public static PredicateSearch of(String predicate) {
    return create().predicate(predicate);
  }

  // Query components
  // --------------------------------------------------------------------------

  /**
   * @param predicate The Lucene predicate string.
   * @return A new {@code PredicateSearch} with the updated predicate.
   */
  public PredicateSearch predicate(String predicate) {
    return new PredicateSearch(new Predicate(predicate, this.predicate.defaultField()), facets, page, sortFields);
  }

  /**
   * @param predicate The predicate, including the default field configuration.
   * @return A new {@code PredicateSearch} with the updated predicate.
   */
  public PredicateSearch predicate(Predicate predicate) {
    return new PredicateSearch(predicate, facets, page, sortFields);
  }

  /** @return The current predicate. */
  public Predicate predicate() {
    return predicate;
  }

  /**
   * @param defaultField The default field to use when the predicate contains no field qualifier.
   * @return A new {@code PredicateSearch} with the updated default field.
   */
  public PredicateSearch defaultField(String defaultField) {
    return new PredicateSearch(new Predicate(predicate.predicate(), defaultField), facets, page, sortFields);
  }

  /**
   * @param facets The facets to compute in the search results.
   * @return A new {@code PredicateSearch} with the specified facets.
   */
  public PredicateSearch facets(List<Facet> facets) {
    return new PredicateSearch(predicate, new FacetList(facets, this.facets.facetSize()), page, sortFields);
  }

  /**
   * @param facetSize The maximum number of facet values to load (max 1000).
   * @return A new {@code PredicateSearch} with the updated facet size.
   */
  public PredicateSearch facetSize(int facetSize) {
    return new PredicateSearch(predicate, facets.facetSize(facetSize), page, sortFields);
  }

  /**
   * @param facet The facet to add.
   * @return A new {@code PredicateSearch} with this additional facet.
   */
  public PredicateSearch facet(Facet facet) {
    return new PredicateSearch(predicate, facets.facet(facet), page, sortFields);
  }

  /**
   * @param field The index field name to facet on.
   * @return A new {@code PredicateSearch} with a standard facet on the given field.
   */
  public PredicateSearch facet(String field) {
    return facet(Facet.of(field));
  }

  /**
   * @param field    The index field name to facet on.
   * @param flexible {@code true} for a flexible facet.
   * @return A new {@code PredicateSearch} with the specified facet.
   */
  public PredicateSearch facet(String field, boolean flexible) {
    return facet(Facet.of(field, flexible));
  }

  /** @return The current facets. */
  public List<Facet> facets() {
    return facets.facets();
  }

  /** @return The maximum number of facet values to load; negative means server default. */
  public int facetSize() {
    return facets.facetSize();
  }

  /**
   * @param page The page to request.
   * @return A new {@code PredicateSearch} with the updated page.
   */
  public PredicateSearch page(Page page) {
    return new PredicateSearch(predicate, facets, page, sortFields);
  }

  /**
   * @param page The 1-based page number.
   * @return A new {@code PredicateSearch} with the updated page number.
   */
  public PredicateSearch page(int page) {
    return new PredicateSearch(predicate, facets, this.page.number(page), sortFields);
  }

  /**
   * @param pageSize The number of results per page.
   * @return A new {@code PredicateSearch} with the updated page size.
   */
  public PredicateSearch pageSize(int pageSize) {
    return new PredicateSearch(predicate, facets, page.size(pageSize), sortFields);
  }

  /** @return The current page. */
  public Page page() {
    return page;
  }

  /**
   * @param field The field to add to the sort order.
   * @return A new {@code PredicateSearch} with this additional sort field.
   */
  public PredicateSearch sortField(String field) {
    return new PredicateSearch(predicate, facets, page, Search.listWith(sortFields, field));
  }

  /**
   * @param fields The fields to sort results by.
   * @return A new {@code PredicateSearch} with the updated sort fields.
   */
  public PredicateSearch sortFields(String... fields) {
    return new PredicateSearch(predicate, facets, page, List.of(fields));
  }

  /**
   * @param sortFields The list of fields to sort results by.
   * @return A new {@code PredicateSearch} with the updated sort fields.
   */
  public PredicateSearch sortFields(List<String> sortFields) {
    return new PredicateSearch(predicate, facets, page, List.copyOf(sortFields));
  }

  /** @return The current sort fields. */
  public List<String> sortFields() {
    return sortFields;
  }

  // Scope binding
  // --------------------------------------------------------------------------

  /**
   * Bind this query to a scope and return a ready-to-execute {@link ServiceCall}.
   *
   * @param scope The scope defining where to execute this search.
   * @return A fully configured {@code ServiceCall}.
   */
  public ServiceCall toServiceCall(SearchScope scope) {
    return Search.buildCall(scope,
        ServiceCatalog.GROUP_SEARCH_PREDICATE,
        ServiceCatalog.MEMBER_PROJECT_SEARCH_PREDICATE,
        ServiceCatalog.MEMBER_SEARCH_PREDICATE,
        toParameters());
  }

  Map<String, String> toParameters() {
    Map<String, String> parameters = new LinkedHashMap<>();
    predicate.toParameters(parameters);
    facets.toParameters(parameters);
    page.toParameters(parameters);
    if (!sortFields.isEmpty())
      parameters.put("sortfields", String.join(",", sortFields));
    return parameters;
  }

  @Override
  public String toString() {
    return predicate.toString();
  }
}
