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

import java.util.Objects;

/**
 * A facet to be computed and returned in the search results.
 *
 * <p>A simple facet is identified by a plain field name (e.g. {@code pstype}).
 * Range and interval facets embed their configuration in the definition string
 * via {@link #rangeFacet(String, String...)} or {@link #intervalFacet(String, boolean, boolean, String, String, String)}. Flexible facets count
 * hits independently of the active filters for that field, allowing the UI to
 * show how many results each value would yield if selected.</p>
 *
 * @param definition The field name or full facet definition string (range or interval).
 * @param flexible   {@code true} if this is a flexible facet.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record Facet(String definition, boolean flexible) {

  /**
   * @param definition The field name or full facet definition string.
   * @param flexible   {@code true} if this is a flexible facet.
   * @throws NullPointerException if {@code definition} is null.
   */
  public Facet {
    Objects.requireNonNull(definition);
  }

  /**
   * @param flexible whether this facet should be flexible
   * @return A new facet with the same definition but the updated flexible flag.
   */
  public Facet flexible(boolean flexible) {
    return new Facet(definition, flexible);
  }

  /**
   * Return the name of the field for this facet.
   *
   * <p>If the definition includes a colon, this is the substring that precedes it.</p>
   *
   * @return The name of the field for the facet
   */
  public String field() {
    int colon = definition.indexOf(':');
    return colon < 0 ? definition : definition.substring(0, colon);
  }

  // Experimental facets
  // ---------------------------------------------------------------------------------------------

  /**
   * Create a fully inclusive range facet for the specified field and boundary values.
   *
   * @param field  The name of the index field.
   * @param values The boundary values that define the range buckets.
   * @return A new range {@code Facet} for that field.
   */
  public static Facet rangeFacet(String field, String... values) {
    return rangeFacet(field, true, true, values);
  }

  /**
   * Create a range facet for the specified field, boundary values, and inclusivity.
   *
   * @param field        The name of the index field.
   * @param minInclusive {@code true} if the lower bound of each bucket is inclusive.
   * @param maxInclusive {@code true} if the upper bound of each bucket is inclusive.
   * @param values       The boundary values that define the range buckets.
   * @return A new range {@code Facet} for that field.
   */
  public static Facet rangeFacet(String field, boolean minInclusive, boolean maxInclusive, String... values) {
    String definition = field + ':' + (minInclusive ? '[' : '{') + String.join(",", values) + (maxInclusive ? ']' : '}');
    return new Facet(definition, false);
  }

  /**
   * Create an interval facet that groups values into fixed-width buckets.
   *
   * @param field        The name of the index field.
   * @param minInclusive {@code true} if the lower bound is inclusive.
   * @param maxInclusive {@code true} if the upper bound is inclusive.
   * @param from         The start of the overall range.
   * @param to           The end of the overall range.
   * @param interval     The size of each bucket.
   * @return A new interval {@code Facet} for that field.
   */
  public static Facet intervalFacet(String field, boolean minInclusive, boolean maxInclusive, String from, String to, String interval) {
    String definition = field + ':' + (minInclusive ? '[' : '{') + from + ";" + to + "|" + interval + (maxInclusive ? ']' : '}');
    return new Facet(definition, false);
  }
}
