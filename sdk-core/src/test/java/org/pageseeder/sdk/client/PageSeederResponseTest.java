package org.pageseeder.sdk.client;

import org.junit.jupiter.api.Test;
import org.pageseeder.sdk.xml.sax.BasicHandler;
import org.pageseeder.sdk.xml.sax.Handler;
import org.pageseeder.sdk.xml.stax.BasicXMLStreamHandler;
import org.pageseeder.sdk.xml.stax.XMLStreamHandler;
import org.xml.sax.Attributes;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class PageSeederResponseTest {

  @Test
  public void shouldDecodeXmlWithCustomSaxHandler() throws IOException {
    PageSeederResponse response = xmlResponse("fixtures/member.xml");

    Handler<String> handler = new BasicHandler<>() {
      private String username;
      private String fullname;

      @Override
      public void startElement(String element, Attributes atts) {
        if (isElement("member")) {
          this.username = atts.getValue("username");
          this.fullname = atts.getValue("firstname") + " " + atts.getValue("surname");
        }
      }

      @Override
      public void endElement(String element) {
        if (isElement("member")) {
          add(this.username + ":" + this.fullname);
        }
      }
    };

    String item = response.xml().saxItem(handler);

    assertEquals("jdoe:John Doe", item);
    assertEquals(List.of("jdoe:John Doe"), handler.list());
  }

  @Test
  public void shouldDecodeXmlWithCustomStaxHandler() throws IOException {
    PageSeederResponse response = xmlResponse("fixtures/memberships.xml");

    XMLStreamHandler<String> handler = new BasicXMLStreamHandler<String>() {
      @Override
      public boolean find(XMLStreamReader xml) throws XMLStreamException {
        while (xml.hasNext()) {
          if (xml.isStartElement() && "membership".equals(xml.getLocalName())) return true;
          xml.next();
        }
        return false;
      }

      @Override
      public String get(XMLStreamReader xml) throws XMLStreamException {
        long id = attribute(xml, "id", -1L);
        String role = attribute(xml, "role", "");
        skipToEndElement(xml, "membership");
        return id + ":" + role;
      }
    };

    List<String> items = response.xml().staxList(handler);
    assertEquals(List.of("10:manager", "11:writer"), items);
  }

  @Test
  public void shouldDecodeXmlWithDocumentFunction() throws IOException {
    PageSeederResponse response = xmlResponse("fixtures/member.xml");

    String summary = response.xml().document(document -> {
      org.w3c.dom.Element member = (org.w3c.dom.Element) document.getElementsByTagName("member").item(0);
      return member.getAttribute("username") + "|" + member.getAttribute("email");
    });

    assertEquals("jdoe|jdoe@example.com", summary);
  }

  @Test
  public void shouldDecodeXmlElementsWithFunction() throws IOException {
    PageSeederResponse response = xmlResponse("fixtures/memberships.xml");

    List<String> memberships = response.xml().elements("membership",
        element -> element.getAttribute("id") + ":" + element.getAttribute("role"));

    assertEquals(List.of("10:manager", "11:writer"), memberships);
  }

  private static PageSeederResponse xmlResponse(String path) throws IOException {
    return new PageSeederResponse(200,
        Map.of("Content-Type", List.of("application/xml")),
        read(path),
        "application/xml");
  }

  private static byte[] read(String path) throws IOException {
    try (InputStream in = PageSeederResponseTest.class.getClassLoader().getResourceAsStream(path)) {
      if (in == null) throw new IOException("Missing fixture " + path);
      return in.readAllBytes();
    }
  }
}
