package org.pageseeder.sdk.model.codec;

final class XmlEscapers {

  private XmlEscapers() {
  }

  static String escapeText(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  static String escapeAttribute(String text) {
    return escapeText(text).replace("\"", "&quot;");
  }
}
