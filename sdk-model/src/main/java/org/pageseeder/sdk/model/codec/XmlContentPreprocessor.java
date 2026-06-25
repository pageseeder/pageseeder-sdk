package org.pageseeder.sdk.model.codec;

import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.exception.ParsingException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pre-processes XML responses to preserve comment content as verbatim strings.
 *
 * <p>When Jackson XmlMapper parses XML into a JsonNode tree, it flattens attributes and child
 * elements into the same key–value structure, losing the distinction. This is harmless for most
 * elements, but comment content blocks ({@code <content type="...">}) contain arbitrary XML
 * that must be round-tripped faithfully.
 *
 * <p>This preprocessor rewrites those content blocks so the inner markup appears as escaped text
 * inside a {@code <value>} element, matching the JSON representation and allowing the downstream
 * parser to preserve it verbatim.
 */
final class XmlContentPreprocessor {

  private int commentDepth = 0;
  private boolean capturing = false;
  private int captureDepth = 0;
  private @Nullable String captureType = null;
  private final StringBuilder captured = new StringBuilder();
  private final Map<String, String> captureNamespaces = new LinkedHashMap<>();
  private final Deque<Map<String, String>> capturedNamespaceScopes = new ArrayDeque<>();

  private XmlContentPreprocessor() {}

  static byte[] preserveContentMarkup(byte[] xml) {
    return new XmlContentPreprocessor().process(xml);
  }

  private byte[] process(byte[] xml) {
    try {
      XMLInputFactory inputFactory = PageSeederParsers.newXmlInputFactory();
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

      XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(xml));
      ByteArrayOutputStream out = new ByteArrayOutputStream(xml.length);
      XMLStreamWriter writer = outputFactory.createXMLStreamWriter(out, "UTF-8");

      while (reader.hasNext()) {
        int event = reader.next();
        if (capturing) {
          processCapturedEvent(event, reader, writer);
        } else {
          processOutputEvent(event, reader, writer);
        }
      }

      writer.flush();
      writer.close();
      reader.close();

      return out.toByteArray();
    } catch (XMLStreamException ex) {
      throw new ParsingException("Unable to preserve XML comment content markup", ex);
    }
  }

  private void processCapturedEvent(int event, XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
    switch (event) {
      case XMLStreamConstants.START_ELEMENT -> {
        boolean topLevel = captureDepth == 0;
        captureDepth++;
        serializeStartElement(reader, captured, topLevel ? captureNamespaces : Map.of(), capturedNamespaceScopes);
      }
      case XMLStreamConstants.END_ELEMENT -> {
        if (captureDepth == 0) {
          flushCapturedContent(writer);
        } else {
          captureDepth--;
          serializeEndElement(reader, captured);
          capturedNamespaceScopes.removeLast();
        }
      }
      case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA ->
          captured.append(XmlEscapers.escapeText(reader.getText()));
      default -> { /* ignore irrelevant events */ }
    }
  }

  private void flushCapturedContent(XMLStreamWriter writer) throws XMLStreamException {
    capturing = false;
    String type = captureType;
    if (type == null) {
      throw new XMLStreamException("Captured comment content is missing its type attribute");
    }
    writer.writeStartElement("content");
    writer.writeAttribute("type", type);
    String value = captured.toString().strip();
    if (!value.isEmpty()) {
      writer.writeStartElement("value");
      writer.writeCharacters(value);
      writer.writeEndElement();
    }
    writer.writeEndElement();
    captured.setLength(0);
    captureNamespaces.clear();
    capturedNamespaceScopes.clear();
    captureType = null;
  }

  private void processOutputEvent(int event, XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
    switch (event) {
      case XMLStreamConstants.START_ELEMENT -> processStartElement(reader, writer);
      case XMLStreamConstants.END_ELEMENT -> {
        if ("comment".equals(reader.getLocalName())) {
          commentDepth--;
        }
        writer.writeEndElement();
      }
      case XMLStreamConstants.CHARACTERS -> writer.writeCharacters(reader.getText());
      case XMLStreamConstants.CDATA -> writer.writeCData(reader.getText());
      case XMLStreamConstants.START_DOCUMENT -> {
        String encoding = reader.getCharacterEncodingScheme();
        String version = reader.getVersion();
        if (encoding != null) {
          writer.writeStartDocument(encoding, version != null ? version : "1.0");
        } else if (version != null) {
          writer.writeStartDocument(version);
        }
      }
      case XMLStreamConstants.END_DOCUMENT -> writer.writeEndDocument();
      case XMLStreamConstants.COMMENT -> writer.writeComment(reader.getText());
      case XMLStreamConstants.PROCESSING_INSTRUCTION ->
          writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
      default -> { /* ignore irrelevant events */ }
    }
  }

  private void processStartElement(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
    String localName = reader.getLocalName();
    if ("comment".equals(localName)) {
      commentDepth++;
    }
    if (commentDepth > 0 && "content".equals(localName)) {
      String type = reader.getAttributeValue(null, "type");
      if (type != null) {
        capturing = true;
        captureDepth = 0;
        captureType = type;
        captureNamespaces(reader, captureNamespaces);
        return;
      }
    }
    writeStartElement(reader, writer);
  }

  private static void writeStartElement(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
    String prefix = reader.getPrefix();
    String namespaceURI = reader.getNamespaceURI();
    String localName = reader.getLocalName();

    if (namespaceURI != null && !namespaceURI.isEmpty()) {
      if (prefix != null && !prefix.isEmpty()) {
        writer.writeStartElement(prefix, localName, namespaceURI);
      } else {
        writer.writeStartElement(namespaceURI, localName);
      }
    } else {
      writer.writeStartElement(localName);
    }

    for (int i = 0; i < reader.getNamespaceCount(); i++) {
      writer.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
    }

    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String attrNs = reader.getAttributeNamespace(i);
      if (attrNs != null && !attrNs.isEmpty()) {
        writer.writeAttribute(attrNs, reader.getAttributeLocalName(i), reader.getAttributeValue(i));
      } else {
        writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
      }
    }
  }

  private static void serializeStartElement(XMLStreamReader reader, StringBuilder sb,
                                            Map<String, String> additionalNamespaces,
                                            Deque<Map<String, String>> namespaceScopes) {
    Map<String, String> activeNamespaces = namespaceScopes.peekLast();
    Map<String, String> declarations = new LinkedHashMap<>();
    captureNamespaces(reader, declarations);
    additionalNamespaces.forEach(declarations::putIfAbsent);
    ensureElementNamespace(reader, activeNamespaces, declarations);
    ensureAttributeNamespaces(reader, activeNamespaces, declarations);

    sb.append('<');
    appendQualifiedName(sb, reader.getPrefix(), reader.getLocalName());
    declarations.forEach((prefix, uri) -> appendNamespace(sb, prefix, uri));
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      sb.append(' ');
      appendQualifiedName(sb, reader.getAttributePrefix(i), reader.getAttributeLocalName(i));
      sb.append("=\"").append(XmlEscapers.escapeAttribute(reader.getAttributeValue(i))).append('"');
    }
    sb.append('>');

    Map<String, String> scope = new LinkedHashMap<>();
    if (activeNamespaces != null) {
      scope.putAll(activeNamespaces);
    }
    scope.putAll(declarations);
    namespaceScopes.addLast(scope);
  }

  private static void serializeEndElement(XMLStreamReader reader, StringBuilder sb) {
    sb.append("</");
    appendQualifiedName(sb, reader.getPrefix(), reader.getLocalName());
    sb.append('>');
  }

  private static void captureNamespaces(XMLStreamReader reader, Map<String, String> namespaces) {
    for (int i = 0; i < reader.getNamespaceCount(); i++) {
      String prefix = normalizePrefix(reader.getNamespacePrefix(i));
      String uri = reader.getNamespaceURI(i);
      if (uri != null) {
        namespaces.put(prefix, uri);
      }
    }
  }

  private static void ensureElementNamespace(XMLStreamReader reader, Map<String, String> activeNamespaces,
                                             Map<String, String> declarations) {
    String namespaceURI = reader.getNamespaceURI();
    if (namespaceURI != null && !namespaceURI.isEmpty()) {
      ensureNamespace(reader.getPrefix(), namespaceURI, activeNamespaces, declarations);
    }
  }

  private static void ensureAttributeNamespaces(XMLStreamReader reader, Map<String, String> activeNamespaces,
                                                Map<String, String> declarations) {
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      String namespaceURI = reader.getAttributeNamespace(i);
      String prefix = reader.getAttributePrefix(i);
      if (namespaceURI != null && !namespaceURI.isEmpty() && prefix != null && !prefix.isEmpty()) {
        ensureNamespace(prefix, namespaceURI, activeNamespaces, declarations);
      }
    }
  }

  private static void ensureNamespace(String prefix, String uri, Map<String, String> activeNamespaces,
                                      Map<String, String> declarations) {
    String normalizedPrefix = normalizePrefix(prefix);
    if (uri.equals(declarations.get(normalizedPrefix))) {
      return;
    }
    if (activeNamespaces != null && uri.equals(activeNamespaces.get(normalizedPrefix))) {
      return;
    }
    declarations.put(normalizedPrefix, uri);
  }

  private static void appendQualifiedName(StringBuilder sb, String prefix, String localName) {
    if (prefix != null && !prefix.isEmpty()) {
      sb.append(prefix).append(':');
    }
    sb.append(localName);
  }

  private static void appendNamespace(StringBuilder sb, String prefix, String uri) {
    if (prefix == null || prefix.isEmpty()) {
      sb.append(" xmlns=\"").append(XmlEscapers.escapeAttribute(uri)).append('"');
    } else {
      sb.append(" xmlns:").append(prefix).append("=\"").append(XmlEscapers.escapeAttribute(uri)).append('"');
    }
  }

  private static String normalizePrefix(String prefix) {
    return prefix == null ? "" : prefix;
  }

}
