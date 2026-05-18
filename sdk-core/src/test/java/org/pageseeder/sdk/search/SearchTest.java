package org.pageseeder.sdk.search;

import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.service.ServiceCatalog;
import org.pageseeder.sdk.service.ServiceCall;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class SearchTest {

  @Test
  void questionSearchSerializesAllSupportedParameters() {
    QuestionSearch search = QuestionSearch.of("annual report")
        .questionFields(Fields.TITLE, Fields.CONTENT)
        .suggestSize(5)
        .filter(Fields.STATUS, "Approved")
        .filter(Fields.TITLE, "one,two\\three|four;five", Filter.Occur.MUST)
        .range(Fields.SIZE, Range.between("10", "20", true, false))
        .facet(Fields.STATUS)
        .facet(Fields.PRIORITY, true)
        .facetSize(20)
        .page(2)
        .pageSize(25)
        .sortField(Fields.MODIFIEDDATE)
        .sortField("-" + Fields.TITLE);

    Map<String, String> parameters = search.toParameters();

    assertEquals("annual report", parameters.get("question"));
    assertEquals("pstitle,pscontent", parameters.get("questionfields"));
    assertEquals("5", parameters.get("suggestsize"));
    assertEquals("psstatus:Approved,+pstitle:one\\,two\\\\three\\|four\\;five", parameters.get("filters"));
    assertEquals("pssize:[10;20}", parameters.get("ranges"));
    assertEquals("psstatus", parameters.get("facets"));
    assertEquals("pspriority", parameters.get("flexiblefacets"));
    assertEquals("20", parameters.get("facetsize"));
    assertEquals("2", parameters.get("page"));
    assertEquals("25", parameters.get("pagesize"));
    assertEquals("psmodifieddate,-pstitle", parameters.get("sortfields"));
  }

  @Test
  void facetSearchSupportsQuestionFieldsSuggestionsAndRangeFacets() {
    Facet rangeFacet = Facet.rangeFacet(Fields.DATE, "2020", "2021", "2022");
    Facet intervalFacet = Facet.intervalFacet(Fields.MODIFIEDDATE, "2020-01-01T00:00:00Z", "1M");

    FacetSearch search = FacetSearch.of("minutes")
        .questionField(Fields.CONTENT)
        .suggestSize(0)
        .facet(rangeFacet)
        .facet(intervalFacet.flexible(true));

    Map<String, String> parameters = search.toParameters();

    assertEquals("pscontent", parameters.get("questionfields"));
    assertEquals("0", parameters.get("suggestsize"));
    assertEquals("psdate:[2020;2021;2022]", parameters.get("facets"));
    assertEquals("psmodifieddate:[2020-01-01T00:00:00Z|1M]", parameters.get("flexiblefacets"));
  }

  @Test
  void predicateSearchSerializesPredicateFacetsPagingAndSorting() {
    PredicateSearch search = PredicateSearch.of("title:report AND pstype:document")
        .defaultField(Fields.CONTENT)
        .facet(Facet.intervalFacet(Fields.MODIFIEDDATE, true, false, "2020", "2022", "1Y"))
        .pageSize(50)
        .sortFields(Fields.TITLE, "-" + Fields.MODIFIEDDATE);

    Map<String, String> parameters = search.toParameters();

    assertEquals("title:report AND pstype:document", parameters.get("predicate"));
    assertEquals("pscontent", parameters.get("defaultfield"));
    assertEquals("psmodifieddate:[2020;2022|1Y}", parameters.get("facets"));
    assertEquals("50", parameters.get("pagesize"));
    assertEquals("pstitle,-psmodifieddate", parameters.get("sortfields"));
  }

  @Test
  void dateRangeHelpersUpdateModifiedDateBoundsWithoutDuplicatingRanges() {
    LocalDateTime from = LocalDateTime.of(2026, 1, 1, 9, 30, 15, 123_000_000);
    LocalDateTime to = LocalDateTime.of(2026, 2, 1, 17, 45, 30);

    QuestionSearch search = QuestionSearch.create()
        .withFrom(from)
        .withTo(to);

    assertEquals(List.of(new RangeFilter(
        Fields.MODIFIEDDATE,
        Range.between(Search.format(from), Search.format(to), true, true)
    )), search.ranges());
    assertEquals("psmodifieddate:[" + Search.format(from) + ";" + Search.format(to) + "]",
        search.toParameters().get("ranges"));
  }

  @Test
  void rangeForSameFieldReplacesExistingRange() {
    QuestionSearch search = QuestionSearch.create()
        .range(Fields.DATE, Range.from("2020", true))
        .range(Fields.DATE, Range.to("2021", false));

    assertEquals(List.of(new RangeFilter(Fields.DATE, Range.to("2021", false))), search.ranges());
    assertEquals("psdate:{;2021}", search.toParameters().get("ranges"));
  }

  @Test
  void toServiceCallBindsProjectScopeAndGroups() {
    ServiceCall call = QuestionSearch.of("budget")
        .toServiceCall(SearchScope.project("project-a", "jsmith", "group-a", "group-b"));

    assertEquals(ServiceCatalog.MEMBER_PROJECT_SEARCH, call.endpoint());
    assertEquals(Map.of("project", "project-a", "member", "jsmith"), call.pathVariables());
    assertEquals(List.of("budget"), call.queryParameters().asMap().get("question"));
    assertEquals(List.of("group-a,group-b"), call.queryParameters().asMap().get("groups"));
  }

  @Test
  void toServiceCallBindsGroupAndGlobalScopes() {
    ServiceCall groupCall = FacetSearch.create().toServiceCall(SearchScope.group("docs"));
    ServiceCall globalCall = PredicateSearch.create().toServiceCall(SearchScope.global("jsmith"));

    assertEquals(ServiceCatalog.GROUP_SEARCH_FACETS, groupCall.endpoint());
    assertEquals(Map.of("group", "docs"), groupCall.pathVariables());
    assertEquals(ServiceCatalog.MEMBER_SEARCH_PREDICATE, globalCall.endpoint());
    assertEquals(Map.of("member", "jsmith"), globalCall.pathVariables());
  }

  @Test
  void valueObjectsValidateAndCopyInputs() {
    assertThrows(IllegalArgumentException.class, () -> new Page(0, 100));
    assertThrows(NullPointerException.class, () -> SearchScope.group(null));
    assertThrows(NullPointerException.class, () -> SearchScope.project("project", "member", "group-a", null));
  }
}
