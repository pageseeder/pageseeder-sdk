package org.pageseeder.sdk.model.codec;

import org.pageseeder.sdk.model.ResultPage;
import org.pageseeder.sdk.exception.ServiceError;
import org.pageseeder.sdk.service.PayloadFormat;

import java.util.List;

/**
 * Parser contract for PageSeeder payloads.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PageSeederParser {

  /**
   * The format of the payload.
   *
   * @return the format of the payload.
   */
  PayloadFormat format();

  /**
   * Parse the specified body into an object of the specified type.
   *
   * @param body The body to parse.
   * @param type The type of the object to parse.
   *
   * @return The parsed object.
   *
   * @param <T> The type of the object to parse.
   */
  <T> T parse(byte[] body, Class<T> type);

  /**
   * Parse the specified body into a list of objects of the specified type.
   *
   * @param body The body to parse.
   * @param type The type of the object to parse.
   *
   * @return The parsed list of objects.
   *
   * @param <T> The type of the object to parse.
   */
  <T> List<T> parseList(byte[] body, Class<T> type);

  /**
   * Parse the specified body into a result page of objects of the specified type.
   *
   * @param body The body to parse.
   * @param type The type of the object to parse.
   *
   * @return The parsed result page.
   *
   * @param <T> The type of the object to parse.
   */
  <T> ResultPage<T> parseResultPage(byte[] body, Class<T> type);

  /**
   * Parse the specified body into an error.
   *
   * @param body The body to parse.
   *
   * @return The parsed error.
   */
  ServiceError parseError(byte[] body);

  byte[] serialize(Object value);
}
