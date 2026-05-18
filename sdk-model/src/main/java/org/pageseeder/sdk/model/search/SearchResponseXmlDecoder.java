package org.pageseeder.sdk.model.search;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.client.BodyDecoder;
import org.pageseeder.sdk.exception.ParsingException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * XML decoder for PageSeeder search responses.
 *
 * <p>Most callers should use {@code Decoders.search()}.</p>
 */
public final class SearchResponseXmlDecoder implements BodyDecoder<SearchResponse> {

  private static final XMLInputFactory XML_FACTORY;

  static {
    XMLInputFactory factory = XMLInputFactory.newFactory();
    factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty(XMLInputFactory.IS_COALESCING, true);
    factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
    factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
    XML_FACTORY = factory;
  }

  private final SearchDecodeOptions options;

  /**
   * Creates a search response XML decoder.
   *
   * @param options the search decode options
   */
  public SearchResponseXmlDecoder(SearchDecodeOptions options) {
    this.options = Objects.requireNonNull(options, "options");
  }

  @Override
  public SearchResponse decode(byte[] body, @Nullable String mediaType) {
    try (ByteArrayInputStream in = new ByteArrayInputStream(body)) {
      XMLStreamReader xml = XML_FACTORY.createXMLStreamReader(in, resolveCharset(mediaType).name());
      try {
        while (xml.hasNext()) {
          if (xml.next() == XMLStreamConstants.START_ELEMENT && "search".equals(xml.getLocalName())) {
            return parseSearch(xml);
          }
        }
        throw new ParsingException("Unable to find search element in PageSeeder XML payload");
      } finally {
        xml.close();
      }
    } catch (XMLStreamException | IOException ex) {
      throw new ParsingException("Unable to parse PageSeeder search XML payload", ex);
    }
  }

  private SearchResponse parseSearch(XMLStreamReader xml) throws XMLStreamException {
    List<String> indexes = split(attr(xml, "indexes"));
    boolean reindexing = booleanAttr(xml, "reindexing");
    @Nullable String warning = attr(xml, "warning");
    List<SearchSuggestion> suggestions = List.of();
    List<SearchFacet> facets = List.of();
    SearchResults results = SearchResults.EMPTY;

    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        switch (xml.getLocalName()) {
          case "suggestions" -> suggestions = parseSuggestions(xml);
          case "facets" -> facets = parseFacets(xml);
          case "results" -> results = parseResults(xml);
          default -> skipElement(xml);
        }
      } else if (event == XMLStreamConstants.END_ELEMENT && "search".equals(xml.getLocalName())) {
        return SearchResponse.trusted(indexes, reindexing, warning, suggestions, facets, results);
      }
    }
    throw new XMLStreamException("Unexpected end of search response");
  }

  private List<SearchSuggestion> parseSuggestions(XMLStreamReader xml) throws XMLStreamException {
    List<SearchSuggestion> suggestions = new ArrayList<>();
    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        if ("suggestion".equals(xml.getLocalName())) {
          suggestions.add(new SearchSuggestion(defaultString(attr(xml, "question")), intAttr(xml, "cardinality")));
          skipElement(xml);
        } else {
          skipElement(xml);
        }
      } else if (event == XMLStreamConstants.END_ELEMENT && "suggestions".equals(xml.getLocalName())) {
        return suggestions;
      }
    }
    throw new XMLStreamException("Unexpected end of suggestions");
  }

  private List<SearchFacet> parseFacets(XMLStreamReader xml) throws XMLStreamException {
    List<SearchFacet> facets = new ArrayList<>();
    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        if ("facet".equals(xml.getLocalName())) {
          facets.add(parseFacet(xml));
        } else {
          skipElement(xml);
        }
      } else if (event == XMLStreamConstants.END_ELEMENT && "facets".equals(xml.getLocalName())) {
        return facets;
      }
    }
    throw new XMLStreamException("Unexpected end of facets");
  }

  private SearchFacet parseFacet(XMLStreamReader xml) throws XMLStreamException {
    String name = defaultString(attr(xml, "name"));
    String type = defaultString(attr(xml, "type"));
    boolean flexible = booleanAttr(xml, "flexible");
    boolean hasResults = booleanAttr(xml, "has-results");
    int totalTerms = intAttr(xml, "total-terms");
    @Nullable String dataType = attr(xml, "datatype");
    List<SearchFacetTerm> terms = new ArrayList<>();

    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        if ("term".equals(xml.getLocalName())) {
          terms.add(new SearchFacetTerm(defaultString(attr(xml, "text")), intAttr(xml, "cardinality")));
          skipElement(xml);
        } else {
          skipElement(xml);
        }
      } else if (event == XMLStreamConstants.END_ELEMENT && "facet".equals(xml.getLocalName())) {
        return SearchFacet.trusted(name, type, flexible, hasResults, totalTerms, dataType, terms);
      }
    }
    throw new XMLStreamException("Unexpected end of facet");
  }

  private SearchResults parseResults(XMLStreamReader xml) throws XMLStreamException {
    int page = intAttr(xml, "page");
    int pageSize = intAttr(xml, "page-size");
    int totalPages = intAttr(xml, "total-pages");
    int totalResults = intAttr(xml, "total-results");
    int firstResult = intAttr(xml, "first-result");
    int lastResult = intAttr(xml, "last-result");
    List<SearchHit> hits = new ArrayList<>();

    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        if ("result".equals(xml.getLocalName())) {
          hits.add(parseHit(xml));
        } else {
          skipElement(xml);
        }
      } else if (event == XMLStreamConstants.END_ELEMENT && "results".equals(xml.getLocalName())) {
        return SearchResults.trusted(page, pageSize, totalPages, totalResults, firstResult, lastResult, hits);
      }
    }
    throw new XMLStreamException("Unexpected end of results");
  }

  private SearchHit parseHit(XMLStreamReader xml) throws XMLStreamException {
    double score = doubleAttr(xml, "score");
    List<SearchField> fields = new ArrayList<>();
    while (xml.hasNext()) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        String element = xml.getLocalName();
        SearchFieldKind kind = switch (element) {
          case "field" -> SearchFieldKind.FIELD;
          case "extract" -> SearchFieldKind.EXTRACT;
          default -> null;
        };
        if (kind == null) {
          skipElement(xml);
          continue;
        }
        String name = defaultString(attr(xml, "name"));
        if (this.options.includeField(name, kind)) {
          fields.add(new SearchField(name, kind, readElementText(xml)));
        } else {
          skipElement(xml);
        }
      } else if (event == XMLStreamConstants.END_ELEMENT && "result".equals(xml.getLocalName())) {
        return SearchHit.trusted(score, fields);
      }
    }
    throw new XMLStreamException("Unexpected end of result");
  }

  private static String readElementText(XMLStreamReader xml) throws XMLStreamException {
    StringBuilder text = new StringBuilder();
    int depth = 1;
    while (xml.hasNext() && depth > 0) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        depth++;
      } else if (event == XMLStreamConstants.END_ELEMENT) {
        depth--;
      } else if (event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA
          || event == XMLStreamConstants.SPACE || event == XMLStreamConstants.ENTITY_REFERENCE) {
        text.append(xml.getText());
      }
    }
    return text.toString();
  }

  private static void skipElement(XMLStreamReader xml) throws XMLStreamException {
    int depth = 1;
    while (xml.hasNext() && depth > 0) {
      int event = xml.next();
      if (event == XMLStreamConstants.START_ELEMENT) {
        depth++;
      } else if (event == XMLStreamConstants.END_ELEMENT) {
        depth--;
      }
    }
  }

  private static @Nullable String attr(XMLStreamReader xml, String name) {
    return xml.getAttributeValue(null, name);
  }

  private static String defaultString(@Nullable String value) {
    return value == null ? "" : value;
  }

  private static boolean booleanAttr(XMLStreamReader xml, String name) {
    String value = attr(xml, name);
    return value != null && Boolean.parseBoolean(value);
  }

  private static int intAttr(XMLStreamReader xml, String name) {
    String value = attr(xml, name);
    return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
  }

  private static double doubleAttr(XMLStreamReader xml, String name) {
    String value = attr(xml, name);
    return value == null || value.isBlank() ? 0.0 : Double.parseDouble(value);
  }

  private static List<String> split(@Nullable String values) {
    if (values == null || values.isBlank()) {
      return List.of();
    }
    List<String> items = new ArrayList<>();
    for (String value : values.split(",")) {
      String trimmed = value.trim();
      if (!trimmed.isEmpty()) {
        items.add(trimmed);
      }
    }
    return items.isEmpty() ? List.of() : items;
  }

  private static Charset resolveCharset(@Nullable String mediaType) {
    if (mediaType == null) {
      return StandardCharsets.UTF_8;
    }
    for (String part : mediaType.split(";")) {
      String trimmed = part.trim();
      if (trimmed.startsWith("charset=")) {
        return Charset.forName(trimmed.substring("charset=".length()));
      }
    }
    return StandardCharsets.UTF_8;
  }
}
