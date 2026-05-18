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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An immutable set of facets for use in a search request.
 *
 * @param facets    The list of facets to compute.
 * @param facetSize The maximum number of facet values to load; negative means server default.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
record FacetList(List<Facet> facets, int facetSize) {

  /**
   * An empty list of facets.
   */
  public static final FacetList EMPTY = new FacetList(List.of(), -1);

  public FacetList {
    facets = List.copyOf(facets);
  }

  /**
   * Create a facet list for the specified fields.
   *
   * @param fields An array of field names.
   * @return A new {@code FacetList} for those fields.
   */
  public static FacetList of(String... fields) {
    return new FacetList(Arrays.stream(fields).map(Facet::of).toList(), -1);
  }

  /**
   * Create a flexible facet list for the specified fields.
   *
   * @param fields An array of field names.
   * @return A new flexible {@code FacetList} for those fields.
   */
  public static FacetList flexible(String... fields) {
    return new FacetList(Arrays.stream(fields).map(f -> Facet.of(f, true)).toList(), -1);
  }

  /** @return {@code true} if no facets are set. */
  public boolean isEmpty() {
    return facets.isEmpty();
  }

  /**
   * Add or replace a facet for the specified index field.
   *
   * @param field    The name of the index field.
   * @param flexible {@code true} if the facet should be flexible.
   * @return A new {@code FacetList} including the specified facet.
   */
  public FacetList facet(String field, boolean flexible) {
    return facet(Facet.of(field, flexible));
  }

  /**
   * Add or replace a facet; any existing facet for the same field is replaced.
   *
   * @param facet The facet to add.
   * @return A new {@code FacetList} including the specified facet.
   */
  public FacetList facet(Facet facet) {
    List<Facet> without = facets.stream().filter(f -> !f.field().equals(facet.field())).toList();
    return new FacetList(Search.listWith(without, facet), this.facetSize);
  }

  /**
   * @param facetSize The max number of facet values to load (max 1000).
   * @return A new {@code FacetList} with the updated facet size.
   */
  public FacetList facetSize(int facetSize) {
    if (facetSize == this.facetSize) return this;
    return new FacetList(this.facets, facetSize);
  }

  /**
   * Update the specified parameters to include the facets in this list.
   *
   * <p>Adds the {@code facets}, {@code flexiblefacets}, and optionally {@code facetsize} parameters.</p>
   *
   * @param parameters The parameters to update.
   * @return The updated parameters.
   */
  void toParameters(Map<String, String> parameters) {
    if (!isEmpty()) {
      String regular = facets.stream().filter(f -> !f.flexible()).map(Facet::definition).collect(Collectors.joining(","));
      if (!regular.isEmpty()) parameters.put("facets", regular);
      String flex = facets.stream().filter(Facet::flexible).map(Facet::definition).collect(Collectors.joining(","));
      if (!flex.isEmpty()) parameters.put("flexiblefacets", flex);
    }
    if (this.facetSize > 0) parameters.put("facetsize", String.valueOf(this.facetSize));
  }
}
