/**
 * Search parameter builders for PageSeeder's search services.
 *
 * <p>This package provides a fluent API for constructing search requests
 * to PageSeeder's suite of search services. The main entry points are:</p>
 * <ul>
 *   <li>{@link org.pageseeder.sdk.search.QuestionSearch} — full-text question-based searches</li>
 *   <li>{@link org.pageseeder.sdk.search.FacetSearch} — facet value extraction</li>
 *   <li>{@link org.pageseeder.sdk.search.PredicateSearch} — Lucene predicate-based searches</li>
 * </ul>
 *
 * <p>To execute a search, build the query object and call {@link org.pageseeder.sdk.search.BasicSearch#toServiceCall()},
 * then pass the result to {@link org.pageseeder.sdk.client.PageSeederClient}.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * ServiceCall call = new QuestionSearch()
 *     .group("my-project-docs")
 *     .question("annual report")
 *     .filter("pstype", "document")
 *     .page(1)
 *     .toServiceCall();
 * PageSeederResponse response = client.execute(call);
 * }</pre>
 */
package org.pageseeder.sdk.search;
