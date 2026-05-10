package org.pageseeder.sdk.model.codec;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.pageseeder.sdk.model.ResultPage;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.service.PayloadFormat;

import java.util.List;

/**
 * XML parser for PageSeeder payloads.
 */
public final class XmlPageSeederParser implements PageSeederParser {

  private final XmlMapper mapper = PageSeederParsers.newXmlMapper();

  @Override
  public PayloadFormat format() {
    return PayloadFormat.XML;
  }

  @Override
  public <T> T parse(byte[] body, Class<T> type) {
    return PageSeederParsers.parse(this.mapper, body, type);
  }

  @Override
  public <T> List<T> parseList(byte[] body, Class<T> type) {
    return PageSeederParsers.parseList(this.mapper, body, type);
  }

  @Override
  public <T> ResultPage<T> parseResultPage(byte[] body, Class<T> type) {
    return PageSeederParsers.parseResultPage(this.mapper, body, type);
  }

  @Override
  public ServiceError parseError(byte[] body) {
    return PageSeederParsers.parseError(this.mapper, body);
  }

  @Override
  public byte[] serialize(Object value) {
    return PageSeederParsers.serialize(this.mapper, value);
  }
}
