package org.pageseeder.sdk.model.codec;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.pageseeder.sdk.model.Comment;
import org.pageseeder.sdk.model.ResultPage;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.model.Workflow;
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
    return PageSeederParsers.parse(this.mapper, preprocess(body, type), type);
  }

  @Override
  public <T> List<T> parseList(byte[] body, Class<T> type) {
    return PageSeederParsers.parseList(this.mapper, preprocess(body, type), type);
  }

  @Override
  public <T> ResultPage<T> parseResultPage(byte[] body, Class<T> type) {
    return PageSeederParsers.parseResultPage(this.mapper, preprocess(body, type), type);
  }

  @Override
  public ServiceError parseError(byte[] body) {
    return PageSeederParsers.parseError(this.mapper, body);
  }

  private static byte[] preprocess(byte[] body, Class<?> type) {
    return requiresContentPreprocessing(type) ? XmlContentPreprocessor.preserveContentMarkup(body) : body;
  }

  private static boolean requiresContentPreprocessing(Class<?> type) {
    return type == Comment.class || type == Workflow.class;
  }

  @Override
  public byte[] serialize(Object value) {
    return PageSeederParsers.serialize(this.mapper, value);
  }
}
