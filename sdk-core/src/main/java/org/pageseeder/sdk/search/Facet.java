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
 * <p>{@link Field} facets are identified by a plain field name (e.g. {@code pstype}).
 * {@link Range} facets embed their bucket configuration in an encoded definition
 * via {@link #rangeFacet(String, String...)} or {@link #intervalFacet(String, boolean, boolean, String, String, String)}.
 * Flexible facets count hits independently of the active filters for that field.</p>
 *
 * <p>Create instances using the static factory methods:</p>
 * <pre>{@code
 * Facet simple   = Facet.of("psstatus");
 * Facet flexible = Facet.of("psstatus", true);
 * Facet range    = Facet.rangeFacet("psdate", "2020", "2021", "2022");
 * }</pre>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public sealed interface Facet permits Facet.Field, Facet.Range {

  /**
   * The name of the index field this facet operates on.
   *
   * <p>For {@link Range} facets this is the substring that precedes the first colon
   * in the definition.</p>
   */
  String field();

  /**
   * The full definition of this facet as expected by the PageSeeder search API.
   *
   * <p>For {@link Field} facets this is the plain field name; for {@link Complex}
   * facets this is the encoded range or interval specification.</p>
   */
  String definition();

  /** Whether this is a flexible facet. */
  boolean flexible();

  /** @return A copy of this facet with the flexible flag updated. */
  Facet flexible(boolean flexible);

  // Factory methods
  // --------------------------------------------------------------------------

  /** @return A standard non-flexible facet on the specified field. */
  static Facet of(String field) {
    return new Field(field, false);
  }

  /** @return A facet on the specified field with the given flexibility. */
  static Facet of(String field, boolean flexible) {
    return new Field(field, flexible);
  }

  // Permitted implementations
  // --------------------------------------------------------------------------

  /**
   * A standard facet identified by a single index field name.
   *
   * @param field    The name of the index field.
   * @param flexible {@code true} if this is a flexible facet.
   */
  record Field(String field, boolean flexible) implements Facet {

    public Field {
      Objects.requireNonNull(field);
    }

    @Override
    public String definition() {
      return field;
    }

    @Override
    public Facet flexible(boolean flexible) {
      return new Field(field, flexible);
    }
  }

  /**
   * A facet with an encoded definition for range or interval bucketing.
   *
   * <p>The definition encodes the field name followed by a colon and the bucket
   * specification. Use {@link Facet#rangeFacet(String, String...)} or
   * {@link Facet#intervalFacet(String, boolean, boolean, String, String, String)}
   * to construct instances of this type.</p>
   *
   * @param definition The full facet definition string (field name + encoded bucket spec).
   * @param flexible   {@code true} if this is a flexible facet.
   */
  record Range(String definition, boolean flexible) implements Facet {

    public Range {
      Objects.requireNonNull(definition);
    }

    @Override
    public String field() {
      int colon = definition.indexOf(':');
      return colon < 0 ? definition : definition.substring(0, colon);
    }

    @Override
    public Facet flexible(boolean flexible) {
      return new Range(definition, flexible);
    }
  }

  // Experimental facets
  // --------------------------------------------------------------------------

  /**
   * Create a fully inclusive range facet for the specified field and boundary values.
   *
   * @param field  The name of the index field.
   * @param values The boundary values that define the range buckets.
   * @return A new range {@code Facet} for that field.
   */
  static Facet rangeFacet(String field, String... values) {
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
  static Facet rangeFacet(String field, boolean minInclusive, boolean maxInclusive, String... values) {
    String definition = field + ':' + (minInclusive ? '[' : '{') + String.join(",", values) + (maxInclusive ? ']' : '}');
    return new Range(definition, false);
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
  static Facet intervalFacet(String field, boolean minInclusive, boolean maxInclusive, String from, String to, String interval) {
    String definition = field + ':' + (minInclusive ? '[' : '{') + from + ";" + to + "|" + interval + (maxInclusive ? ']' : '}');
    return new Range(definition, false);
  }
}
