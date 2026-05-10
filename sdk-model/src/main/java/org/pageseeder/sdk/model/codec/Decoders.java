package org.pageseeder.sdk.model.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.client.BodyDecoder;
import org.pageseeder.sdk.model.ResultPage;

import java.util.List;

/**
 * Factory for standard {@link BodyDecoder} implementations backed by Jackson.
 *
 * <p>Usage with {@code PageSeederClient}:
 * <pre>{@code
 * Member member = client.execute(call, Decoders.object(Member.class));
 * List<Membership> list = client.execute(call, Decoders.list(Membership.class));
 * ResultPage<Membership> page = client.execute(call, Decoders.page(Membership.class));
 * }</pre>
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Decoders {

  private static final XmlPageSeederParser XML = new XmlPageSeederParser();
  private static final JsonPageSeederParser JSON = new JsonPageSeederParser();

  private Decoders() {
  }

  /**
   * Returns a decoder that maps the response body to a single object of the given type.
   *
   * @param type The target class.
   * @param <T>  The target type.
   * @return A decoder for a single object.
   */
  public static <T> BodyDecoder<T> object(Class<T> type) {
    return (body, mediaType) -> parserFor(mediaType).parse(body, type);
  }

  /**
   * Returns a decoder that maps the response body to a list of objects of the given type.
   *
   * @param type The target class.
   * @param <T>  The item type.
   * @return A decoder for a list.
   */
  public static <T> BodyDecoder<List<T>> list(Class<T> type) {
    return (body, mediaType) -> parserFor(mediaType).parseList(body, type);
  }

  /**
   * Returns a decoder that maps the response body to a paginated result of the given type.
   *
   * @param type The target class.
   * @param <T>  The item type.
   * @return A decoder for a result page.
   */
  public static <T> BodyDecoder<ResultPage<T>> page(Class<T> type) {
    return (body, mediaType) -> parserFor(mediaType).parseResultPage(body, type);
  }

  private static PageSeederParser parserFor(@Nullable String mediaType) {
    if (mediaType != null && mediaType.toLowerCase().contains("json")) {
      return JSON;
    }
    return XML;
  }
}
