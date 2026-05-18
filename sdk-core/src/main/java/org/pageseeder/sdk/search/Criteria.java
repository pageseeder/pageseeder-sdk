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

import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Package-private carrier for the search constraints shared by {@link QuestionSearch} and {@link FacetSearch}.
 *
 * <p>Holds the question, filters, and range filters — the "what to search for" — and owns all
 * serialisation and manipulation logic for those three components. Facet configuration is excluded:
 * it shapes the response rather than the query, and belongs at the same level as pagination.</p>
 */
record Criteria(Question question, List<Filter> filters, List<RangeFilter> ranges) {

  public Criteria {
    filters = List.copyOf(filters);
    ranges  = List.copyOf(ranges);
  }

  static final Criteria EMPTY = new Criteria(Question.EMPTY, List.of(), List.of());

  // Question
  // --------------------------------------------------------------------------

  Criteria question(String q) {
    return new Criteria(new Question(q, question.fields(), question.suggestSize()), filters, ranges);
  }

  Criteria question(Question q) {
    return new Criteria(q, filters, ranges);
  }

  // Filters
  // --------------------------------------------------------------------------

  Criteria filters(List<Filter> list) {
    return new Criteria(question, list, ranges);
  }

  Criteria filter(Filter f) {
    return new Criteria(question, Search.listWith(filters, f), ranges);
  }

  Criteria filter(String field, String value) {
    return filter(new Filter(field, value));
  }

  Criteria filter(String field, String value, Filter.Occur occur) {
    return filter(new Filter(field, value, occur));
  }

  // Ranges
  // --------------------------------------------------------------------------

  Criteria ranges(List<RangeFilter> list) {
    return new Criteria(question, filters, list);
  }

  /** Add or replace a range filter; at most one range per field is kept. */
  Criteria range(String field, Range range) {
    List<RangeFilter> without = ranges.stream().filter(f -> !f.field().equals(field)).toList();
    return new Criteria(question, filters, Search.listWith(without, new RangeFilter(field, range)));
  }

  // Named filter shorthands
  // --------------------------------------------------------------------------

  Criteria withType(String type)           { return filter("pstype", type); }
  Criteria withStatus(String status)       { return filter("psstatus", status); }
  Criteria withPriority(String priority)   { return filter("pspriority", priority); }
  Criteria withMediaType(String mediaType) { return filter("psmediatype", mediaType); }
  Criteria withAssignedTo(String member)   { return filter("psassignedto", member); }
  Criteria withFolder(String folder)       { return filter("psfolder", folder); }
  Criteria withDocumentType(String type)   { return filter("psdocumenttype", type); }

  Criteria withProperty(String property, String value) {
    return filter("psproperty-" + property, value);
  }

  Criteria withMetadata(String property, String value) {
    return filter("psmetadata-" + property, value);
  }

  // Named range shorthands
  // --------------------------------------------------------------------------

  /** Updates only the lower bound of the {@code psmodifieddate} range, preserving any existing upper bound. */
  Criteria withFrom(LocalDateTime from) {
    Range existing = rangeOf("psmodifieddate");
    Range range = existing != null ? existing.min(Search.format(from), true) : Range.from(from, true);
    return range("psmodifieddate", range);
  }

  /** Updates only the upper bound of the {@code psmodifieddate} range, preserving any existing lower bound. */
  Criteria withTo(LocalDateTime to) {
    Range existing = rangeOf("psmodifieddate");
    Range range = existing != null ? existing.max(Search.format(to), true) : Range.to(to, true);
    return range("psmodifieddate", range);
  }

  /** Replaces any existing {@code psmodifieddate} range entirely. */
  Criteria withBetween(LocalDateTime from, LocalDateTime to) {
    return range("psmodifieddate", Range.between(from, to, true, true));
  }

  private @Nullable Range rangeOf(String field) {
    for (RangeFilter f : ranges)
      if (f.field().equals(field)) return f.range();
    return null;
  }

  // Serialisation
  // --------------------------------------------------------------------------

  void toParameters(Map<String, String> params) {
    question.toParameters(params);
    if (!filters.isEmpty()) params.put("filters", filtersString());
    if (!ranges.isEmpty())  params.put("ranges",  rangesString());
  }

  private String filtersString() {
    StringBuilder out = new StringBuilder();
    for (Filter f : filters) {
      if (!out.isEmpty()) out.append(',');
      out.append(f.occur()).append(f.field()).append(':').append(f.value().replace(",", "\\,"));
    }
    return out.toString();
  }

  private String rangesString() {
    StringBuilder out = new StringBuilder();
    for (RangeFilter f : ranges) {
      if (!out.isEmpty()) out.append(',');
      out.append(f.field()).append(':').append(f.range().toString().replace(",", "\\,"));
    }
    return out.toString();
  }

  @Override
  public String toString() {
    return question.question() + filtersString() + rangesString();
  }
}
