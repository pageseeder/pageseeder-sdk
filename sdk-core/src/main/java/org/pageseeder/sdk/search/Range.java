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

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A range of values for use in a range filter.
 *
 * <p>The string representation uses PageSeeder range notation:
 * square brackets ({@code [}, {@code ]}) for inclusive bounds and
 * curly braces ({@code \{}, {@code \}}) for exclusive bounds, with a semicolon
 * separator. For example, {@code [2020-01-01T00:00:00Z;2021-01-01T00:00:00Z]}
 * is a fully inclusive date range. An empty string for {@code min} or {@code max}
 * leaves the corresponding bound open.</p>
 *
 * @param min          The minimum value of the range; empty string for an open lower bound.
 * @param minInclusive {@code true} if the lower bound is inclusive.
 * @param max          The maximum value of the range; empty string for an open upper bound.
 * @param maxInclusive {@code true} if the upper bound is inclusive.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record Range(String min, boolean minInclusive, String max, boolean maxInclusive) {

  /**
   * @param min          The minimum value of the range.
   * @param minInclusive {@code true} if the lower bound is inclusive.
   * @param max          The maximum value of the range.
   * @param maxInclusive {@code true} if the upper bound is inclusive.
   * @throws NullPointerException if {@code min} or {@code max} is null.
   */
  public Range {
    Objects.requireNonNull(min);
    Objects.requireNonNull(max);
  }

  /**
   * @param min       The new minimum value
   * @param inclusive Whether the minimum is inclusive
   * @return A new {@code Range} with the updated minimum.
   */
  public Range min(String min, boolean inclusive) {
    return new Range(min, inclusive, max, maxInclusive);
  }

  /**
   * @param max       The new maximum value
   * @param inclusive Whether the maximum is inclusive
   * @return A new {@code Range} with the updated maximum.
   */
  public Range max(String max, boolean inclusive) {
    return new Range(min, minInclusive, max, inclusive);
  }

  /**
   * @param from      The lower bound value.
   * @param inclusive Whether the lower bound is inclusive
   * @return A new open-ended {@code Range} starting at {@code from}.
   */
  public static Range from(String from, boolean inclusive) {
    return new Range(from, inclusive, "", false);
  }

  /**
   * @param to        The upper bound value.
   * @param inclusive Whether the upper bound is inclusive
   * @return A new open-ended {@code Range} ending at {@code to}.
   */
  public static Range to(String to, boolean inclusive) {
    return new Range("", false, to, inclusive);
  }

  /**
   * @param from          The lower bound value.
   * @param to            The upper bound value.
   * @param fromInclusive Whether the lower bound is inclusive
   * @param toInclusive   Whether the upper bound is inclusive
   * @return A new {@code Range} between {@code from} and {@code to}.
   */
  public static Range between(String from, String to, boolean fromInclusive, boolean toInclusive) {
    return new Range(from, fromInclusive, to, toInclusive);
  }

  // Date range factories
  // --------------------------------------------------------------------------

  /**
   * @param from      The lower bound for a date range search.
   * @param inclusive Whether the lower bound is inclusive
   * @return A new open-ended {@code Range} starting at {@code from}.
   */
  public static Range from(LocalDateTime from, boolean inclusive) {
    return new Range(Search.format(from), inclusive, "", false);
  }

  /**
   * @param to        The upper bound for a date range search.
   * @param inclusive Whether the upper bound is inclusive
   * @return A new open-ended {@code Range} ending at {@code to}.
   */
  public static Range to(LocalDateTime to, boolean inclusive) {
    return new Range("", false, Search.format(to), inclusive);
  }

  /**
   * @param from          The lower bound value.
   * @param to            The upper bound value.
   * @param fromInclusive Whether the lower bound is inclusive
   * @param toInclusive   Whether the upper bound is inclusive
   * @return A new {@code Range} between {@code from} and {@code to}.
   */
  public static Range between(LocalDateTime from, LocalDateTime to, boolean fromInclusive, boolean toInclusive) {
    return new Range(Search.format(from), fromInclusive, Search.format(to), toInclusive);
  }

  @Override
  public String toString() {
    return (minInclusive ? "[" : "{") + min + ";" + max + (maxInclusive ? "]" : "}");
  }

  String toParameterValue() {
    return (minInclusive ? "[" : "{")
        + Search.escapeParameterValue(min)
        + ";"
        + Search.escapeParameterValue(max)
        + (maxInclusive ? "]" : "}");
  }
}
