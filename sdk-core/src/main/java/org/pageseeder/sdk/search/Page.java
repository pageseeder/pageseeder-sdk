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

/**
 * Specifies which page of search results to return.
 *
 * <p>Both the page number and page size must be greater than zero. Parameters
 * matching the defaults ({@value #DEFAULT_PAGE_NUMBER} and {@value #DEFAULT_PAGE_SIZE})
 * are omitted from the request to avoid sending redundant query parameters.</p>
 *
 * @param number The 1-based page number.
 * @param size   The maximum number of results per page.
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record Page(int number, int size) implements Serializable {

  /**
   * The default page number is 1.
   */
  public static final int DEFAULT_PAGE_NUMBER = 1;

  /**
   * The default page size is 100.
   */
  public static final int DEFAULT_PAGE_SIZE = 100;

  /**
   * The default page instance.
   */
  public static final Page DEFAULT_PAGE = new Page(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);

  /**
   * @param number The 1-based page number.
   * @param size   The maximum number of results per page.
   * @throws IllegalArgumentException if either value is not greater than zero.
   */
  public Page {
    if (number <= 0 || size <= 0)
      throw new IllegalArgumentException("Page and page size must be greater than 0");
  }

  /**
   * @param page The new page number
   * @return A new {@code Page} with the updated number.
   */
  public Page number(int page) {
    return new Page(page, size);
  }

  /**
   * @param size The new page size
   * @return A new {@code Page} with the updated size.
   */
  public Page size(int size) {
    return new Page(number, size);
  }

  @Override
  public String toString() {
    return number + "(" + size + ")";
  }

  /**
   * @param parameters The parameter map to update.
   * @return The page options as a set of parameters for the search services.
   */
  void toParameters(Map<String, String> parameters) {
    if (number != DEFAULT_PAGE_NUMBER) {
      parameters.put("page", Integer.toString(number));
    }
    if (size != DEFAULT_PAGE_SIZE) {
      parameters.put("pagesize", Integer.toString(size));
    }
  }
}
