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
 * An immutable query for facet-extraction searches.
 *
 * <p>Returns facet counts for the specified fields without paginated results.
 * Build a query with the fluent methods, then bind it to a {@link SearchScope}
 * to get a {@link ServiceCall} ready to execute:</p>
 * <pre>{@code
 * ServiceCall call = FacetSearch.of("annual report")
 *     .withType("document")
 *     .facet("psstatus")
 *     .facet("pspriority")
 *     .toServiceCall(SearchScope.group("my-group"));
 * }</pre>
 *
 * @see <a href="https://dev.pageseeder.com/api/web_services/services/group-search-facets-GET.html">/groups/{group}/search/facets [GET]</a>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class FacetSearch implements Serializable {

  @SuppressWarnings("java:S2057")
  private static final long serialVersionUID = 20260518L;

  private final Criteria criteria;
  private final FacetList facets;

  private FacetSearch(Criteria criteria, FacetList facets) {
    this.criteria = criteria;
    this.facets = facets;
  }

  /** @return An empty facet search with no criteria set. */
  public static FacetSearch create() {
    return new FacetSearch(Criteria.EMPTY, FacetList.EMPTY);
  }

  /** @return A facet search with the specified question text. */
  public static FacetSearch of(String question) {
    return create().question(question);
  }

  // Question
  // --------------------------------------------------------------------------

  /**
   * @param question The question text for full-text search.
   * @return A new {@code FacetSearch} with the updated question text.
   */
  public FacetSearch question(String question) {
    return new FacetSearch(criteria.question(question), facets);
  }

  /**
   * @param question The question for full-text search, including field and suggest configuration.
   * @return A new {@code FacetSearch} with the updated question.
   */
  public FacetSearch question(Question question) {
    return new FacetSearch(criteria.question(question), facets);
  }

  /** @return The question text; empty string if not set. */
  public String question() {
    return criteria.question().question();
  }

  // Facets
  // --------------------------------------------------------------------------

  /**
   * @param facets The facets to compute.
   * @return A new {@code FacetSearch} with the specified facets.
   */
  public FacetSearch facets(List<Facet> facets) {
    return new FacetSearch(criteria, new FacetList(facets, this.facets.facetSize()));
  }

  /**
   * @param facet The facet to add.
   * @return A new {@code FacetSearch} with this additional facet.
   */
  public FacetSearch facet(Facet facet) {
    return new FacetSearch(criteria, facets.facet(facet));
  }

  /**
   * @param field The index field name to facet on.
   * @return A new {@code FacetSearch} with a standard facet on the given field.
   */
  public FacetSearch facet(String field) {
    return facet(new Facet(field, false));
  }

  /**
   * @param field    The index field name to facet on.
   * @param flexible {@code true} for a flexible facet.
   * @return A new {@code FacetSearch} with the specified facet.
   */
  public FacetSearch facet(String field, boolean flexible) {
    return facet(new Facet(field, flexible));
  }

  /**
   * @param facetSize The maximum number of facet values to load (max 1000).
   * @return A new {@code FacetSearch} with the updated facet size.
   */
  public FacetSearch facetSize(int facetSize) {
    return new FacetSearch(criteria, facets.facetSize(facetSize));
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
   * @return A new {@code FacetSearch} with the specified filters.
   */
  public FacetSearch filters(List<Filter> filters) {
    return new FacetSearch(criteria.filters(filters), facets);
  }

  /**
   * @param filter The filter to add.
   * @return A new {@code FacetSearch} with this additional filter.
   */
  public FacetSearch filter(Filter filter) {
    return new FacetSearch(criteria.filter(filter), facets);
  }

  /**
   * @param field The index field name.
   * @param value The value to match.
   * @return A new {@code FacetSearch} with this additional filter (DEFAULT occurrence).
   */
  public FacetSearch filter(String field, String value) {
    return new FacetSearch(criteria.filter(field, value), facets);
  }

  /**
   * @param field The index field name.
   * @param value The value to match.
   * @param occur The occurrence requirement.
   * @return A new {@code FacetSearch} with this additional filter.
   */
  public FacetSearch filter(String field, String value, Filter.Occur occur) {
    return new FacetSearch(criteria.filter(field, value, occur), facets);
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
   * @return A new {@code FacetSearch} with the updated range filter.
   */
  public FacetSearch range(String field, Range range) {
    return new FacetSearch(criteria.range(field, range), facets);
  }

  /**
   * @param ranges The range filters to apply.
   * @return A new {@code FacetSearch} with the specified range filters.
   */
  public FacetSearch ranges(List<RangeFilter> ranges) {
    return new FacetSearch(criteria.ranges(ranges), facets);
  }

  /** @return The current range filters. */
  public List<RangeFilter> ranges() {
    return criteria.ranges();
  }

  // Named filter shorthands (with prefix)
  // --------------------------------------------------------------------------

  /** @return A new search with an additional filter on {@code pstype}. */
  public FacetSearch withType(String type) { return new FacetSearch(criteria.withType(type), facets); }

  /** @return A new search with an additional filter on {@code psstatus}. */
  public FacetSearch withStatus(String status) { return new FacetSearch(criteria.withStatus(status), facets); }

  /** @return A new search with an additional filter on {@code pspriority}. */
  public FacetSearch withPriority(String priority) { return new FacetSearch(criteria.withPriority(priority), facets); }

  /** @return A new search with an additional filter on {@code psmediatype}. */
  public FacetSearch withMediaType(String mediaType) { return new FacetSearch(criteria.withMediaType(mediaType), facets); }

  /** @return A new search with an additional filter on {@code psassignedto}. */
  public FacetSearch withAssignedTo(String assignedTo) { return new FacetSearch(criteria.withAssignedTo(assignedTo), facets); }

  /** @return A new search with an additional filter on {@code psfolder}. */
  public FacetSearch withFolder(String folder) { return new FacetSearch(criteria.withFolder(folder), facets); }

  /** @return A new search with an additional filter on {@code psdocumenttype}. */
  public FacetSearch withDocumentType(String documentType) { return new FacetSearch(criteria.withDocumentType(documentType), facets); }

  /** @return A new search with an additional filter on {@code psproperty-<property>}. */
  public FacetSearch withProperty(String property, String value) { return new FacetSearch(criteria.withProperty(property, value), facets); }

  /** @return A new search with an additional filter on {@code psmetadata-<property>}. */
  public FacetSearch withMetadata(String property, String value) { return new FacetSearch(criteria.withMetadata(property, value), facets); }

  // Named range shorthands (with prefix)
  // --------------------------------------------------------------------------

  /**
   * Set the lower bound for a date range filter on the modified date.
   *
   * @param from The lower bound (inclusive).
   * @return A new {@code FacetSearch} with the updated date range.
   */
  public FacetSearch withFrom(LocalDateTime from) { return new FacetSearch(criteria.withFrom(from), facets); }

  /**
   * Set the upper bound for a date range filter on the modified date.
   *
   * @param to The upper bound (inclusive).
   * @return A new {@code FacetSearch} with the updated date range.
   */
  public FacetSearch withTo(LocalDateTime to) { return new FacetSearch(criteria.withTo(to), facets); }

  /**
   * Set a date range filter on the modified date.
   *
   * @param from The lower bound (inclusive).
   * @param to   The upper bound (inclusive).
   * @return A new {@code FacetSearch} with the updated date range.
   */
  public FacetSearch withBetween(LocalDateTime from, LocalDateTime to) { return new FacetSearch(criteria.withBetween(from, to), facets); }

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
      call = ServiceCall.of(ServiceCatalog.GROUP_SEARCH_FACETS)
          .pathVariable("group", g.group());
    } else if (scope instanceof SearchScope.Project p) {
      if (!p.groups().isEmpty())
        params.put("groups", String.join(",", p.groups()));
      call = ServiceCall.of(ServiceCatalog.MEMBER_PROJECT_SEARCH_FACETS)
          .pathVariable("member", p.member())
          .pathVariable("project", p.project());
    } else if (scope instanceof SearchScope.Global gl) {
      call = ServiceCall.of(ServiceCatalog.MEMBER_SEARCH_FACETS)
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
    return parameters;
  }

  @Override
  public String toString() {
    return criteria.toString();
  }
}
