package org.pageseeder.sdk.model.codec;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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

  private XmlContentPreprocessor() {}

  static byte[] preserveContentMarkup(byte[] xml) {
    try {
      XMLInputFactory inputFactory = PageSeederParsers.newXmlInputFactory();
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

      XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(xml));
      ByteArrayOutputStream out = new ByteArrayOutputStream(xml.length);
      XMLStreamWriter writer = outputFactory.createXMLStreamWriter(out, "UTF-8");

      int commentDepth = 0;
      boolean capturing = false;
      int captureDepth = 0;
      String captureType = null;
      StringBuilder captured = new StringBuilder();

      while (reader.hasNext()) {
        int event = reader.next();

        if (capturing) {
          switch (event) {
            case XMLStreamConstants.START_ELEMENT -> {
              captureDepth++;
              serializeStartElement(reader, captured);
            }
            case XMLStreamConstants.END_ELEMENT -> {
              if (captureDepth == 0) {
                capturing = false;
                writer.writeStartElement("content");
                writer.writeAttribute("type", captureType);
                String value = captured.toString().strip();
                if (!value.isEmpty()) {
                  writer.writeStartElement("value");
                  writer.writeCharacters(value);
                  writer.writeEndElement();
                }
                writer.writeEndElement();
                captured.setLength(0);
                captureType = null;
              } else {
                captureDepth--;
                captured.append("</").append(reader.getLocalName()).append('>');
              }
            }
            case XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {
              captured.append(escapeXml(reader.getText()));
            }
            default -> {}
          }
          continue;
        }

        switch (event) {
          case XMLStreamConstants.START_ELEMENT -> {
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
                continue;
              }
            }
            writeStartElement(reader, writer);
          }
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
          default -> {}
        }
      }

      writer.flush();
      writer.close();
      reader.close();

      return out.toByteArray();
    } catch (XMLStreamException ex) {
      return xml;
    }
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

  private static void serializeStartElement(XMLStreamReader reader, StringBuilder sb) {
    sb.append('<').append(reader.getLocalName());
    for (int i = 0; i < reader.getAttributeCount(); i++) {
      sb.append(' ').append(reader.getAttributeLocalName(i))
          .append("=\"").append(escapeXmlAttr(reader.getAttributeValue(i))).append('"');
    }
    sb.append('>');
  }

  private static String escapeXml(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private static String escapeXmlAttr(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }
}
