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
import org.pageseeder.sdk.service.ServiceEndpoint;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility methods shared across PageSeeder search services.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Search {

  private Search() {}

  /**
   * Returns a new unmodifiable list with {@code element} appended to {@code list}.
   */
  static <E> List<E> listWith(List<E> list, E element) {
    if (list.isEmpty()) return List.of(element);
    var updated = new ArrayList<>(list);
    updated.add(element);
    return List.copyOf(updated);
  }

  /**
   * Escape a value embedded inside a PageSeeder search list parameter.
   *
   * <p>The search API uses comma, semicolon, and pipe as parameter-level separators
   * in facets, filters, and ranges. A literal backslash must be escaped first so it
   * cannot accidentally escape the next character.</p>
   */
  static String escapeParameterValue(String value) {
    return value
        .replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace("|", "\\|");
  }

  /**
   * Dispatch a scope to the correct endpoint and apply parameters.
   *
   * <p>Selects the group, project, or global endpoint based on the scope type, wires the
   * required path variables, injects a {@code groups} query parameter for project-scoped
   * searches when the group list is non-empty, and applies all remaining parameters.</p>
   */
  static ServiceCall buildCall(SearchScope scope,
                               ServiceEndpoint groupEndpoint,
                               ServiceEndpoint projectEndpoint,
                               ServiceEndpoint globalEndpoint,
                               Map<String, String> params) {
    ServiceCall call;
    if (scope instanceof SearchScope.Group g) {
      call = ServiceCall.of(groupEndpoint)
          .pathVariable("group", g.group());
    } else if (scope instanceof SearchScope.Project p) {
      if (!p.groups().isEmpty())
        params.put("groups", String.join(",", p.groups()));
      call = ServiceCall.of(projectEndpoint)
          .pathVariable("member", p.member())
          .pathVariable("project", p.project());
    } else if (scope instanceof SearchScope.Global gl) {
      call = ServiceCall.of(globalEndpoint)
          .pathVariable("member", gl.member());
    } else {
      throw new IllegalArgumentException("Unknown scope type: " + scope.getClass().getName());
    }
    params.forEach(call::query);
    return call;
  }

  /**
   * Format a datetime as a PageSeeder range value string.
   *
   * <p>Use this method when constructing a {@link Range} with raw string bounds rather than
   * the {@code LocalDateTime} factory methods ({@link Range#from}, {@link Range#to},
   * {@link Range#between}). The output is ISO 8601 in UTC, truncated to seconds —
   * the format PageSeeder expects for datetime range parameters.</p>
   *
   * <p>Example: {@code 2024-06-01T00:00:00} in the system timezone becomes
   * {@code 2024-05-31T14:00:00Z} if the system is UTC+10.</p>
   *
   * @param datetime The datetime to format (interpreted in the system default timezone).
   * @return The corresponding UTC string, e.g. {@code "2024-06-01T00:00:00Z"}.
   */
  public static String format(LocalDateTime datetime) {
    return datetime.atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.SECONDS)
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
  }
}
