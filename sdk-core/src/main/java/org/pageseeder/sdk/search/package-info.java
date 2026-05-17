/**
 * Immutable query objects for PageSeeder's search services.
 *
 * <p>The main entry points are:</p>
 * <ul>
 *   <li>{@link org.pageseeder.sdk.search.QuestionSearch} — full-text question-based searches</li>
 *   <li>{@link org.pageseeder.sdk.search.FacetSearch} — facet value extraction</li>
 *   <li>{@link org.pageseeder.sdk.search.PredicateSearch} — Lucene predicate-based searches</li>
 * </ul>
 *
 * <p>Each query type is immutable; fluent methods return new instances. To execute a search,
 * build a query and bind it to a {@link org.pageseeder.sdk.search.SearchScope}:</p>
 * <pre>{@code
 * ServiceCall call = QuestionSearch.of("annual report")
 *     .withType("document")
 *     .withStatus("Approved")
 *     .facet("psstatus")
 *     .page(2)
 *     .toServiceCall(SearchScope.group("my-project-docs"));
 * PageSeederResponse response = client.execute(call);
 * }</pre>
 *
 * <p>The same query can be reused across different scopes:</p>
 * <pre>{@code
 * var query = QuestionSearch.of("annual report").withType("document");
 * ServiceCall groupCall   = query.toServiceCall(SearchScope.group("docs"));
 * ServiceCall projectCall = query.toServiceCall(SearchScope.project("my-project", "jdoe"));
 * ServiceCall globalCall  = query.toServiceCall(SearchScope.global("jdoe"));
 * }</pre>
 */
@org.jspecify.annotations.NullMarked
package org.pageseeder.sdk.search;
