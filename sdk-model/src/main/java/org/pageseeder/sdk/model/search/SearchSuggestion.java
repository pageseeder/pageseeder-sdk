package org.pageseeder.sdk.model.search;

import java.util.Objects;

/**
 * A suggested search question.
 *
 * @param question    the suggested question text
 * @param cardinality the number of matching results
 */
public record SearchSuggestion(String question, int cardinality) {

  /**
   * Creates a search suggestion.
   *
   * @param question    the suggested question text
   * @param cardinality the number of matching results
   */
  public SearchSuggestion {
    Objects.requireNonNull(question, "question");
  }
}
