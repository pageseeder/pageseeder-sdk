package org.pageseeder.sdk.search;

import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.service.ServiceCatalog;
import org.pageseeder.sdk.service.ServiceCall;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    Facet exclusiveRangeFacet = Facet.rangeFacet(Fields.SIZE, false, false, "0", "10", "20");
    Facet exclusiveOpenIntervalFacet = Facet.intervalFacet(Fields.TITLE, false, false, "a", "1");
    Facet exclusiveBoundedIntervalFacet = Facet.intervalFacet(Fields.SIZE, false, false, "0", "100", "10");

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
    assertEquals("pssize:{0;10;20}", exclusiveRangeFacet.definition());
    assertEquals("pstitle:{a|1}", exclusiveOpenIntervalFacet.definition());
    assertEquals("pssize:{0;100|10}", exclusiveBoundedIntervalFacet.definition());
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
    LocalDateTime from = LocalDateTime.of(2026, Month.JANUARY, 1, 9, 30, 15, 123_000_000);
    LocalDateTime to = LocalDateTime.of(2026, Month.FEBRUARY, 1, 17, 45, 30);

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

  @Test
  void questionSearchCoversFluentAccessorsAndNamedShorthands() {
    LocalDateTime from = LocalDateTime.of(2026, Month.MARCH, 1, 10, 0);
    LocalDateTime to = LocalDateTime.of(2026, Month.MARCH, 31, 17, 0);
    Question question = Question.of("policy").question("policy update").fields(Fields.TITLE).suggestSize(3);

    QuestionSearch search = QuestionSearch.create()
        .question(question)
        .filters(List.of(new Filter(Fields.STATUS, "Draft")))
        .filter(Fields.PRIORITY, "High", Filter.Occur.MUST)
        .ranges(List.of(new RangeFilter(Fields.SIZE, Range.from("100", true))))
        .range(Fields.SIZE, Range.between("100", "200", true, true))
        .withType("document")
        .withStatus("Approved")
        .withPriority("Low")
        .withMediaType("application/pdf")
        .withAssignedTo("jsmith")
        .withFolder("/reports")
        .withDocumentType("minutes")
        .withProperty("year", "2026")
        .withMetadata("department", "finance")
        .withBetween(from, to)
        .page(new Page(3, 15))
        .sortFields(Fields.TITLE, Fields.DATE)
        .sortFields(List.of("-" + Fields.DATE));

    assertEquals(question, search.question());
    assertEquals(new Page(3, 15), search.page());
    assertEquals(List.of("-" + Fields.DATE), search.sortFields());
    assertEquals("policy updatepsstatus:Draft,+pspriority:High,pstype:document,psstatus:Approved,pspriority:Low,"
        + "psmediatype:application/pdf,psassignedto:jsmith,psfolder:/reports,psdocumenttype:minutes,"
        + "psproperty-year:2026,psmetadata-department:finance"
        + "pssize:[100;200],psmodifieddate:[" + Search.format(from) + ";" + Search.format(to) + "]",
        search.toString());
  }

  @Test
  void facetSearchCoversFluentAccessorsAndNamedShorthands() {
    LocalDateTime from = LocalDateTime.of(2026, Month.APRIL, 1, 10, 0);
    LocalDateTime to = LocalDateTime.of(2026, Month.APRIL, 30, 17, 0);
    Question question = Question.of("agenda", Fields.TITLE);

    FacetSearch search = FacetSearch.create()
        .question(question)
        .facets(List.of(Facet.of(Fields.STATUS)))
        .facet(Fields.STATUS, true)
        .facetSize(0)
        .filters(List.of(new Filter(Fields.STATUS, "Draft")))
        .filter(Fields.PRIORITY, "High", Filter.Occur.MUST_NOT)
        .ranges(List.of(new RangeFilter(Fields.SIZE, Range.to("500", true))))
        .range(Fields.SIZE, Range.between("100", "500", true, true))
        .withType("document")
        .withStatus("Approved")
        .withPriority("Low")
        .withMediaType("application/pdf")
        .withAssignedTo("jsmith")
        .withFolder("/reports")
        .withDocumentType("minutes")
        .withProperty("year", "2026")
        .withMetadata("department", "finance")
        .withFrom(from)
        .withTo(to);

    assertEquals(question, search.question());
    assertEquals(List.of(Facet.of(Fields.STATUS, true)), search.facets());
    assertEquals(0, search.facetSize());
    assertEquals("agendapsstatus:Draft,-pspriority:High,pstype:document,psstatus:Approved,pspriority:Low,"
        + "psmediatype:application/pdf,psassignedto:jsmith,psfolder:/reports,psdocumenttype:minutes,"
        + "psproperty-year:2026,psmetadata-department:finance"
        + "pssize:[100;500],psmodifieddate:[" + Search.format(from) + ";" + Search.format(to) + "]",
        search.toString());
  }

  @Test
  void predicateSearchCoversFluentAccessors() {
    Predicate predicate = new Predicate("pstitle:agenda", Fields.TITLE);

    PredicateSearch search = PredicateSearch.create()
        .predicate(predicate)
        .predicate("pstitle:minutes")
        .defaultField(Fields.CONTENT)
        .facets(List.of(Facet.of(Fields.STATUS)))
        .facet(Fields.PRIORITY, true)
        .facetSize(10)
        .page(new Page(4, 40))
        .sortField(Fields.TITLE)
        .sortFields(List.of("-" + Fields.DATE));

    assertEquals(new Predicate("pstitle:minutes", Fields.CONTENT), search.predicate());
    assertEquals(List.of(Facet.of(Fields.STATUS), Facet.of(Fields.PRIORITY, true)), search.facets());
    assertEquals(10, search.facetSize());
    assertEquals(new Page(4, 40), search.page());
    assertEquals(List.of("-" + Fields.DATE), search.sortFields());
    assertEquals("Predicate[predicate=pstitle:minutes, defaultField=pscontent]", search.toString());
  }

  @Test
  void searchScopeFactoriesCoverAllProjectOverloads() {
    ServiceCall allProjectGroups = QuestionSearch.create()
        .toServiceCall(SearchScope.project("project-a", "jsmith"));
    ServiceCall listedProjectGroups = FacetSearch.create()
        .toServiceCall(SearchScope.project("project-a", "jsmith", List.of("group-a")));

    assertEquals(ServiceCatalog.MEMBER_PROJECT_SEARCH, allProjectGroups.endpoint());
    assertEquals(Map.of("project", "project-a", "member", "jsmith"), allProjectGroups.pathVariables());
    assertNull(allProjectGroups.queryParameters().asMap().get("groups"));
    assertEquals(ServiceCatalog.MEMBER_PROJECT_SEARCH_FACETS, listedProjectGroups.endpoint());
    assertEquals(List.of("group-a"), listedProjectGroups.queryParameters().asMap().get("groups"));
  }

  @Test
  void valueObjectsExposeReadableRepresentationsAndFactories() {
    Filter mustFilter = new Filter(Fields.STATUS, "Approved", Filter.Occur.MUST);
    Facet flexibleFacet = Facet.of(Fields.STATUS).flexible(true);
    Facet rawRangeFacet = new Facet.Range("custom-field", false);
    Range range = Range.from("A", true).max("Z", false).min("B", false);
    Range dateFrom = Range.from(LocalDateTime.of(2026, Month.MAY, 1, 9, 0), true);
    Range dateTo = Range.to(LocalDateTime.of(2026, Month.MAY, 2, 9, 0), false);

    assertEquals("+psstatus:Approved", mustFilter.toString());
    assertEquals(Fields.STATUS, flexibleFacet.field());
    assertEquals(Fields.STATUS, flexibleFacet.definition());
    assertEquals("custom-field", rawRangeFacet.field());
    assertEquals("{B;Z}", range.toString());
    assertEquals("[" + Search.format(LocalDateTime.of(2026, Month.MAY, 1, 9, 0)) + ";}", dateFrom.toString());
    assertEquals("{;" + Search.format(LocalDateTime.of(2026, Month.MAY, 2, 9, 0)) + "}", dateTo.toString());
    assertEquals("psstatus:[Approved;Approved]", new RangeFilter(Fields.STATUS, Range.between("Approved", "Approved", true, true)).toString());
  }

  @Test
  void facetListCoversFactoriesReplacementAndDefaultFacetSize() {
    FacetList list = FacetList.of(Fields.STATUS, Fields.PRIORITY)
        .facet(Fields.STATUS, true)
        .facetSize(5)
        .facetSize(5);
    FacetList flexible = FacetList.flexible(Fields.AUTHOR);

    Map<String, String> parameters = new java.util.LinkedHashMap<>();
    list.toParameters(parameters);
    flexible.toParameters(parameters);

    assertEquals(List.of(Facet.of(Fields.PRIORITY), Facet.of(Fields.STATUS, true)), list.facets());
    assertEquals("pspriority", parameters.get("facets"));
    assertEquals("psauthor", parameters.get("flexiblefacets"));
    assertEquals("5", parameters.get("facetsize"));
  }
}
