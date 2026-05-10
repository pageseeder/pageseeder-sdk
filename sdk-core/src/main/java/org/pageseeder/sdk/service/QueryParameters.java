package org.pageseeder.sdk.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Multi-value query or form parameter collection.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class QueryParameters {

  private final Map<String, List<String>> values = new LinkedHashMap<>();

  /**
   * Add a new value to the collection.
   *
   * @param name  the name of the parameter
   * @param value the value of the parameter
   *
   * @return this collection for easy chaining.
   */
  public QueryParameters add(String name, String value) {
    this.values.computeIfAbsent(name, ignored -> new ArrayList<>()).add(value);
    return this;
  }

  /**
   * Returns <code>true</code> if this collection is empty.
   *
   * @return <code>true</code> if this collection is empty.
   */
  public boolean isEmpty() {
    return this.values.isEmpty();
  }

  /**
   * Returns a copy of the values as a map.
   *
   * @return a copy of the values as a map.
   */
  public Map<String, List<String>> asMap() {
    Map<String, List<String>> copy = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : this.values.entrySet()) {
      copy.put(entry.getKey(), List.copyOf(entry.getValue()));
    }
    return Collections.unmodifiableMap(copy);
  }

  /**
   * Returns the values as a form-urlencoded string.
   *
   * @return the values as a form-urlencoded string.
   */
  public String toFormUrlEncoded() {
    List<String> encoded = new ArrayList<>();
    for (Map.Entry<String, List<String>> entry : this.values.entrySet()) {
      for (String value : entry.getValue()) {
        encoded.add(encode(entry.getKey()) + "=" + encode(value));
      }
    }
    return String.join("&", encoded);
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
