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
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A full-text search question for use in question-based and facet searches.
 *
 * <p>The question text is processed by PageSeeder as a free-text query. When
 * {@code fields} is empty, the server's default field set is searched. A negative
 * {@code suggestSize} disables the suggestion feature entirely.</p>
 *
 * @param question    The question text for full-text search; empty string if not set.
 * @param fields      The list of field names to search; empty list to use the server default.
 * @param suggestSize The maximum number of suggestions to return; negative to disable.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record Question(String question, List<String> fields, int suggestSize) implements Serializable {

  /**
   * An empty question (no search terms).
   */
  public static final Question EMPTY = new Question("", List.of(), -1);

  /**
   * @param question    The question text for full-text search.
   * @param fields      The list of field names to search; defensively copied.
   * @param suggestSize The maximum number of suggestions to return.
   * @throws NullPointerException if {@code question} or {@code fields} is null.
   */
  public Question {
    Objects.requireNonNull(question);
    fields = List.copyOf(Objects.requireNonNull(fields));
  }

  void toParameters(Map<String, String> parameters) {
    if (!question.isEmpty()) {
      parameters.put("question", question);
      if (!fields.isEmpty()) {
        parameters.put("questionfields", String.join(",", fields));
      }
      if (suggestSize >= 0) {
        parameters.put("suggestsize", Integer.toString(suggestSize));
      }
    }
  }
}
