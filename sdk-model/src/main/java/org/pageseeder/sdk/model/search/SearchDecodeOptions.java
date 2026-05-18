package org.pageseeder.sdk.model.search;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Options for decoding search responses.
 */
public final class SearchDecodeOptions {

  private static final SearchDecodeOptions DEFAULT = new SearchDecodeOptions(Set.of(), Set.of());

  private final Set<String> allowFields;
  private final Set<String> denyFields;

  private SearchDecodeOptions(Set<String> allowFields, Set<String> denyFields) {
    this.allowFields = Set.copyOf(allowFields);
    this.denyFields = Set.copyOf(denyFields);
  }

  /**
   * @return default search decoding options
   */
  public static SearchDecodeOptions defaults() {
    return DEFAULT;
  }

  /**
   * @return a search decode options builder
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * @return the allowed field names, or an empty set to allow all fields
   */
  public Set<String> allowFields() {
    return this.allowFields;
  }

  /**
   * @return the denied field names
   */
  public Set<String> denyFields() {
    return this.denyFields;
  }

  /**
   * Returns whether a field should be included.
   *
   * @param name the field name
   * @param kind the field kind
   * @return {@code true} if this field should be decoded
   */
  public boolean includeField(String name, SearchFieldKind kind) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(kind, "kind");
    return (this.allowFields.isEmpty() || this.allowFields.contains(name)) && !this.denyFields.contains(name);
  }

  /**
   * Builder for search decode options.
   */
  public static final class Builder {

    private final Set<String> allowFields = new LinkedHashSet<>();
    private final Set<String> denyFields = new LinkedHashSet<>();

    private Builder() {
    }

    /**
     * Allows only the specified field names.
     *
     * @param fields the field names to allow
     * @return this builder
     */
    public Builder allowFields(String... fields) {
      return allowFields(Arrays.asList(fields));
    }

    /**
     * Allows only the specified field names.
     *
     * @param fields the field names to allow
     * @return this builder
     */
    public Builder allowFields(Iterable<String> fields) {
      addAll(this.allowFields, fields);
      return this;
    }

    /**
     * Excludes the specified field names.
     *
     * @param fields the field names to exclude
     * @return this builder
     */
    public Builder denyFields(String... fields) {
      return denyFields(Arrays.asList(fields));
    }

    /**
     * Excludes the specified field names.
     *
     * @param fields the field names to exclude
     * @return this builder
     */
    public Builder denyFields(Iterable<String> fields) {
      addAll(this.denyFields, fields);
      return this;
    }

    /**
     * Builds immutable search decode options.
     *
     * @return search decode options
     */
    public SearchDecodeOptions build() {
      return new SearchDecodeOptions(this.allowFields, this.denyFields);
    }

    private static void addAll(Set<String> target, Iterable<String> fields) {
      Objects.requireNonNull(fields, "fields");
      for (String field : fields) {
        target.add(Objects.requireNonNull(field, "field"));
      }
    }
  }
}
