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
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable query for full-text question-based searches.
 *
 * <p>Build a query with the fluent methods, then bind it to a {@link SearchScope}
 * to get a {@link ServiceCall} ready to execute:</p>
 * <pre>{@code
 * ServiceCall call = QuestionSearch.of("annual report")
 *     .withType("document")
 *     .withStatus("Approved")
 *     .facet("psstatus")
 *     .page(2)
 *     .toServiceCall(SearchScope.group("my-group"));
 * }</pre>
 *
 * @see <a href="https://dev.pageseeder.com/api/web_services/services/group-search-GET.html">/groups/{group}/search [GET]</a>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class QuestionSearch implements Serializable {

  @SuppressWarnings("java:S2057")
  private static final long serialVersionUID = 20260518L;

  private final Criteria criteria;
  private final FacetList facets;
  private final Page page;
  private final List<String> sortFields;

  private QuestionSearch(Criteria criteria, FacetList facets, Page page, List<String> sortFields) {
    this.criteria = criteria;
    this.facets = facets;
    this.page = page;
    this.sortFields = sortFields;
  }

  /** @return An empty question search with no criteria set. */
  public static QuestionSearch create() {
    return new QuestionSearch(Criteria.EMPTY, FacetList.EMPTY, Page.DEFAULT_PAGE, List.of());
  }

  /** @return A question search with the specified question text. */
  public static QuestionSearch of(String question) {
    return create().question(question);
  }

  // Question
  // --------------------------------------------------------------------------

  /**
   * @param question The question text for full-text search.
   * @return A new {@code QuestionSearch} with the updated question text.
   */
  public QuestionSearch question(String question) {
    return new QuestionSearch(criteria.question(question), facets, page, sortFields);
  }

  /**
   * @param question The question for full-text search, including field and suggest configuration.
   * @return A new {@code QuestionSearch} with the updated question.
   */
  public QuestionSearch question(Question question) {
    return new QuestionSearch(criteria.question(question), facets, page, sortFields);
  }

  /** @return The question text; empty string if not set. */
  public String question() {
    return criteria.question().question();
  }

  // Facets
  // --------------------------------------------------------------------------

  /**
   * @param facets The facets to compute in the search results.
   * @return A new {@code QuestionSearch} with the specified facets.
   */
  public QuestionSearch facets(List<Facet> facets) {
    return new QuestionSearch(criteria, new FacetList(facets, this.facets.facetSize()), page, sortFields);
  }

  /**
   * @param facet The facet to add.
   * @return A new {@code QuestionSearch} with this additional facet.
   */
  public QuestionSearch facet(Facet facet) {
    return new QuestionSearch(criteria, facets.facet(facet), page, sortFields);
  }

  /**
   * @param field The index field name to facet on.
   * @return A new {@code QuestionSearch} with a standard facet on the given field.
   */
  public QuestionSearch facet(String field) {
    return facet(new Facet(field, false));
  }

  /**
   * @param field    The index field name to facet on.
   * @param flexible {@code true} for a flexible facet.
   * @return A new {@code QuestionSearch} with the specified facet.
   */
  public QuestionSearch facet(String field, boolean flexible) {
    return facet(new Facet(field, flexible));
  }

  /**
   * @param facetSize The maximum number of facet values to load (max 1000).
   * @return A new {@code QuestionSearch} with the updated facet size.
   */
  public QuestionSearch facetSize(int facetSize) {
    return new QuestionSearch(criteria, facets.facetSize(facetSize), page, sortFields);
  }

  /** @return The current facets. */
  public List<Facet> facets() {
    return facets.facets();
  }

  /** @return The maximum number of facet values to load; negative means server default. */
  public int facetSize() {
    return facets.facetSize();
  }

  // Filters
  // --------------------------------------------------------------------------

  /**
   * @param filters The filters to apply.
   * @return A new {@code QuestionSearch} with the specified filters.
   */
  public QuestionSearch filters(List<Filter> filters) {
    return new QuestionSearch(criteria.filters(filters), facets, page, sortFields);
  }

  /**
   * @param filter The filter to add.
   * @return A new {@code QuestionSearch} with this additional filter.
   */
  public QuestionSearch filter(Filter filter) {
    return new QuestionSearch(criteria.filter(filter), facets, page, sortFields);
  }

  /**
   * @param field The index field name.
   * @param value The value to match.
   * @return A new {@code QuestionSearch} with this additional filter (DEFAULT occurrence).
   */
  public QuestionSearch filter(String field, String value) {
    return new QuestionSearch(criteria.filter(field, value), facets, page, sortFields);
  }

  /**
   * @param field The index field name.
   * @param value The value to match.
   * @param occur The occurrence requirement.
   * @return A new {@code QuestionSearch} with this additional filter.
   */
  public QuestionSearch filter(String field, String value, Filter.Occur occur) {
    return new QuestionSearch(criteria.filter(field, value, occur), facets, page, sortFields);
  }

  /** @return The current filters. */
  public List<Filter> filters() {
    return criteria.filters();
  }

  // Ranges
  // --------------------------------------------------------------------------

  /**
   * Add or replace a range filter for the specified field.
   *
   * @param field The index field name.
   * @param range The range for that field.
   * @return A new {@code QuestionSearch} with the updated range filter.
   */
  public QuestionSearch range(String field, Range range) {
    return new QuestionSearch(criteria.range(field, range), facets, page, sortFields);
  }

  /**
   * @param ranges The range filters to apply.
   * @return A new {@code QuestionSearch} with the specified range filters.
   */
  public QuestionSearch ranges(List<RangeFilter> ranges) {
    return new QuestionSearch(criteria.ranges(ranges), facets, page, sortFields);
  }

  /** @return The current range filters. */
  public List<RangeFilter> ranges() {
    return criteria.ranges();
  }

  // Page
  // --------------------------------------------------------------------------

  /**
   * @param page The page to request.
   * @return A new {@code QuestionSearch} with the updated page.
   */
  public QuestionSearch page(Page page) {
    return new QuestionSearch(criteria, facets, page, sortFields);
  }

  /**
   * @param page The 1-based page number.
   * @return A new {@code QuestionSearch} with the updated page number.
   */
  public QuestionSearch page(int page) {
    return new QuestionSearch(criteria, facets, this.page.number(page), sortFields);
  }

  /**
   * @param pageSize The number of results per page.
   * @return A new {@code QuestionSearch} with the updated page size.
   */
  public QuestionSearch pageSize(int pageSize) {
    return new QuestionSearch(criteria, facets, page.size(pageSize), sortFields);
  }

  /** @return The current page. */
  public Page page() {
    return page;
  }

  // Sort fields
  // --------------------------------------------------------------------------

  /**
   * @param fields The fields to sort results by.
   * @return A new {@code QuestionSearch} with the updated sort fields.
   */
  public QuestionSearch sortFields(String... fields) {
    return new QuestionSearch(criteria, facets, page, List.of(fields));
  }

  /**
   * @param sortFields The list of fields to sort results by.
   * @return A new {@code QuestionSearch} with the updated sort fields.
   */
  public QuestionSearch sortFields(List<String> sortFields) {
    return new QuestionSearch(criteria, facets, page, List.copyOf(sortFields));
  }

  /** @return The current sort fields. */
  public List<String> sortFields() {
    return sortFields;
  }

  // Named filter shorthands (with prefix)
  // --------------------------------------------------------------------------

  /** @return A new search with an additional filter on {@code pstype}. */
  public QuestionSearch withType(String type) { return new QuestionSearch(criteria.withType(type), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code psstatus}. */
  public QuestionSearch withStatus(String status) { return new QuestionSearch(criteria.withStatus(status), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code pspriority}. */
  public QuestionSearch withPriority(String priority) { return new QuestionSearch(criteria.withPriority(priority), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code psmediatype}. */
  public QuestionSearch withMediaType(String mediaType) { return new QuestionSearch(criteria.withMediaType(mediaType), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code psassignedto}. */
  public QuestionSearch withAssignedTo(String assignedTo) { return new QuestionSearch(criteria.withAssignedTo(assignedTo), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code psfolder}. */
  public QuestionSearch withFolder(String folder) { return new QuestionSearch(criteria.withFolder(folder), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code psdocumenttype}. */
  public QuestionSearch withDocumentType(String documentType) { return new QuestionSearch(criteria.withDocumentType(documentType), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code psproperty-<property>}. */
  public QuestionSearch withProperty(String property, String value) { return new QuestionSearch(criteria.withProperty(property, value), facets, page, sortFields); }

  /** @return A new search with an additional filter on {@code psmetadata-<property>}. */
  public QuestionSearch withMetadata(String property, String value) { return new QuestionSearch(criteria.withMetadata(property, value), facets, page, sortFields); }

  // Named range shorthands (with prefix)
  // --------------------------------------------------------------------------

  /**
   * Set the lower bound for a date range filter on the modified date.
   *
   * @param from The lower bound (inclusive).
   * @return A new {@code QuestionSearch} with the updated date range.
   */
  public QuestionSearch withFrom(LocalDateTime from) { return new QuestionSearch(criteria.withFrom(from), facets, page, sortFields); }

  /**
   * Set the upper bound for a date range filter on the modified date.
   *
   * @param to The upper bound (inclusive).
   * @return A new {@code QuestionSearch} with the updated date range.
   */
  public QuestionSearch withTo(LocalDateTime to) { return new QuestionSearch(criteria.withTo(to), facets, page, sortFields); }

  /**
   * Set a date range filter on the modified date.
   *
   * @param from The lower bound (inclusive).
   * @param to   The upper bound (inclusive).
   * @return A new {@code QuestionSearch} with the updated date range.
   */
  public QuestionSearch withBetween(LocalDateTime from, LocalDateTime to) { return new QuestionSearch(criteria.withBetween(from, to), facets, page, sortFields); }

  // Scope binding
  // --------------------------------------------------------------------------

  /**
   * Bind this query to a scope and return a ready-to-execute {@link ServiceCall}.
   *
   * @param scope The scope defining where to execute this search.
   * @return A fully configured {@code ServiceCall}.
   */
  public ServiceCall toServiceCall(SearchScope scope) {
    Map<String, String> params = toParameters();
    ServiceCall call;
    if (scope instanceof SearchScope.Group g) {
      call = ServiceCall.of(ServiceCatalog.GROUP_SEARCH)
          .pathVariable("group", g.group());
    } else if (scope instanceof SearchScope.Project p) {
      if (!p.groups().isEmpty())
        params.put("groups", String.join(",", p.groups()));
      call = ServiceCall.of(ServiceCatalog.MEMBER_PROJECT_SEARCH)
          .pathVariable("member", p.member())
          .pathVariable("project", p.project());
    } else if (scope instanceof SearchScope.Global gl) {
      call = ServiceCall.of(ServiceCatalog.MEMBER_SEARCH)
          .pathVariable("member", gl.member());
    } else {
      throw new IllegalArgumentException("Unknown scope type: " + scope.getClass().getName());
    }
    params.forEach(call::query);
    return call;
  }

  Map<String, String> toParameters() {
    Map<String, String> parameters = new LinkedHashMap<>();
    criteria.toParameters(parameters);
    facets.toParameters(parameters);
    page.toParameters(parameters);
    if (!sortFields.isEmpty())
      parameters.put("sortfields", String.join(",", sortFields));
    return parameters;
  }

  @Override
  public String toString() {
    return criteria.toString();
  }
}
