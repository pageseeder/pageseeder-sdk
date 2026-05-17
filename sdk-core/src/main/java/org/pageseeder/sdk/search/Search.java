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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
   * Format the date time as a string for use in search filters and ranges.
   *
   * <p>The date time string is serialised using ISO 8601 in universal time (UTC).</p>
   *
   * @param datetime The datetime instance to format
   *
   * @return The corresponding string format.
   */
  public static String format(LocalDateTime datetime) {
    return datetime.atZone(ZoneId.systemDefault())
        .withZoneSameInstant(ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.SECONDS)
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";
  }
}
