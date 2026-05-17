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

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * A Lucene predicate query for use in predicate-based searches.
 *
 * <p>The predicate is passed verbatim to the PageSeeder search engine as a Lucene
 * query string. An empty predicate produces no search parameters. The default field
 * is used when no field prefix is present in the predicate; leave it empty to use
 * the server's configured default.</p>
 *
 * @param predicate    The Lucene predicate string; empty string if not set.
 * @param defaultField The default field to apply when the predicate contains no field
 *                     qualifier; empty string to use the server default.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record Predicate(String predicate, String defaultField) implements Serializable {

  @SuppressWarnings("java:S2057")
  private static final long serialVersionUID = 20260418L;

  /**
   * An empty predicate with no search terms and no default field.
   */
  public static final Predicate EMPTY = new Predicate("", "");

  /**
   * @param predicate    The Lucene predicate string.
   * @param defaultField The default field qualifier.
   * @throws NullPointerException if either argument is null.
   */
  public Predicate {
    Objects.requireNonNull(predicate);
    Objects.requireNonNull(defaultField);
  }

  void toParameters(Map<String, String> parameters) {
    if (!predicate.isEmpty()) {
      parameters.put("predicate", predicate);
      if (!defaultField.isEmpty()) {
        parameters.put("defaultfield", defaultField);
      }
    }
  }
}
