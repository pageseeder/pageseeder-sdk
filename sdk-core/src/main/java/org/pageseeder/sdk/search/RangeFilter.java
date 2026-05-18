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
 * A range-based search filter on a PageSeeder group index.
 *
 * <p>Associates an index field with a {@link Range}, restricting results to
 * documents whose field value falls within that range. When added through
 * the fluent search builders, only one range per field is kept.</p>
 *
 * @param field The name of the index field.
 * @param range The range of values the field must fall within.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record RangeFilter(String field, Range range) {

  /**
   * @param field The name of the index field.
   * @param range The range of values the field must fall within.
   * @throws NullPointerException if either argument is null.
   */
  public RangeFilter {
    Objects.requireNonNull(field);
    Objects.requireNonNull(range);
  }

  @Override
  public String toString() {
    return field + ":" + range;
  }
}
