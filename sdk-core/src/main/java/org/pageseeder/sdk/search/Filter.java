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
 * A search filter on a PageSeeder group index.
 *
 * <p>A filter is a field name–value pair that results must match (or not match)
 * depending on the {@link Occur} requirement. Multiple filters on the same field
 * with {@link Occur#DEFAULT} occurrence are combined with OR; filters on different
 * fields are combined with AND.</p>
 *
 * @param field The name of the index field.
 * @param value The value the field must match.
 * @param occur The occurrence requirement for this filter.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record Filter(String field, String value, Occur occur) {

  /**
   * The requirement on the occurrence of the name-value pair in the index.
   */
  public enum Occur {

    /**
     * Results that match the field name will be included if they match this value
     * or any other DEFAULT value from the same field (OR within the field, AND across fields).
     */
    DEFAULT(""),

    /**
     * Only results that match the field name and value pair will be included (AND).
     */
    MUST("+"),

    /**
     * Only results that do not match the field name and value pair will be included.
     */
    MUST_NOT("-");

    private final String symbol;

    Occur(String symbol) {
      this.symbol = symbol;
    }

    @Override
    public String toString() {
      return this.symbol;
    }
  }

  /**
   * @param field The name of the index field.
   * @param value The value the field must match.
   * @param occur The occurrence requirement for this filter.
   * @throws NullPointerException if any component is null.
   */
  public Filter {
    Objects.requireNonNull(field, "The field name must be specified");
    Objects.requireNonNull(value, "The value to filter must be specified");
    Objects.requireNonNull(occur);
  }

  /**
   * Create a new filter with DEFAULT occurrence.
   *
   * @param field The name of the field
   * @param value The value of the field to match
   *
   * @throws NullPointerException If any argument is {@code null}.
   */
  public Filter(String field, String value) {
    this(field, value, Occur.DEFAULT);
  }

  @Override
  public String toString() {
    return occur + field + ":" + value;
  }
}
