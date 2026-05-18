package org.pageseeder.sdk.model.codec;

import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.model.search.SearchDecodeOptions;
import org.pageseeder.sdk.model.search.SearchFacet;
import org.pageseeder.sdk.model.search.SearchFieldKind;
import org.pageseeder.sdk.model.search.SearchHit;
import org.pageseeder.sdk.model.search.SearchResponse;
import org.pageseeder.sdk.search.Fields;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class SearchResponseDecoderTest {

  @Test
  void shouldDecodeSearchSuggestionsFacetsAndResults() {
    SearchResponse response = Decoders.search().decode(searchXml(), "application/xml; charset=UTF-8");

    assertEquals(List.of("42", "43"), response.indexes());
    assertEquals("partial index", response.warning());
    assertEquals(2, response.suggestions().size());
    assertEquals("minutes", response.suggestions().get(0).question());
    assertEquals(12, response.suggestions().get(0).cardinality());

    SearchFacet facet = response.facets().get(0);
    assertEquals(Fields.TYPE, facet.name());
    assertEquals("field", facet.type());
    assertEquals(2, facet.totalTerms());
    assertEquals("string", facet.dataType());
    assertEquals("document", facet.terms().get(0).text());
    assertEquals(4, facet.terms().get(0).cardinality());

    assertEquals(1, response.results().page());
    assertEquals(10, response.results().pageSize());
    assertEquals(2, response.results().totalResults());

    SearchHit hit = response.results().hits().get(0);
    assertEquals(2.34, hit.score());
    assertEquals("100", hit.firstValue(Fields.ID));
    assertEquals(List.of("alpha", "beta"), hit.values("pslabel"));
    assertEquals(SearchFieldKind.EXTRACT, hit.fields("pscontent").get(0).kind());
    assertEquals("The meeting minutes include budget notes.", hit.firstValue("pscontent"));
  }

  @Test
  void shouldAllowOnlyConfiguredFields() {
    SearchDecodeOptions options = SearchDecodeOptions.builder()
        .allowFields(Fields.ID, Fields.TITLE)
        .build();

    SearchResponse response = Decoders.search(options).decode(searchXml(), "application/xml");
    SearchHit hit = response.results().hits().get(0);

    assertEquals(List.of("100"), hit.values(Fields.ID));
    assertEquals(List.of("Budget minutes"), hit.values(Fields.TITLE));
    assertEquals(List.of(), hit.values("pslabel"));
    assertNull(hit.firstValue("pscontent"));
    assertEquals(2, hit.fields().size());
  }

  @Test
  void shouldDenyConfiguredFieldsAfterAllowingThem() {
    SearchDecodeOptions options = SearchDecodeOptions.builder()
        .allowFields(Fields.ID, Fields.TITLE, "pslabel")
        .denyFields("pslabel")
        .build();

    SearchResponse response = Decoders.search(options).decode(searchXml(), "application/xml");
    SearchHit hit = response.results().hits().get(0);

    assertEquals(List.of("100"), hit.values(Fields.ID));
    assertEquals(List.of("Budget minutes"), hit.values(Fields.TITLE));
    assertEquals(List.of(), hit.values("pslabel"));
    assertEquals(2, hit.fields().size());
  }

  private static byte[] searchXml() {
    return """
        <search indexes="42, 43" warning="partial index">
          <query empty="false" predicate="+pstitle:minutes">
            <question empty="false">
              <field boost="1.0" name="pstitle"/>
              <text>minutes</text>
            </question>
          </query>
          <suggestions>
            <suggestion question="minutes" cardinality="12"/>
            <suggestion question="minute" cardinality="7"/>
          </suggestions>
          <facets>
            <facet name="pstype" type="field" flexible="false" has-results="true" total-terms="2" datatype="string">
              <term text="document" cardinality="4"/>
              <term text="comment" cardinality="1"/>
            </facet>
          </facets>
          <results page="1" page-size="10" total-pages="1" total-results="2" first-result="1" last-result="2">
            <result score="2.34">
              <field name="psid">100</field>
              <field name="pstitle">Budget minutes</field>
              <field name="pslabel">alpha</field>
              <field name="pslabel">beta</field>
              <extract name="pscontent">The meeting <b>minutes</b> include budget notes.</extract>
            </result>
            <result score="1.11">
              <field name="psid">101</field>
              <field name="pstitle">Action items</field>
            </result>
          </results>
        </search>
        """.getBytes(StandardCharsets.UTF_8);
  }
}
