package org.pageseeder.sdk.client;

import org.jspecify.annotations.Nullable;

/**
 * Decodes a raw response body into a typed value.
 *
 * <p>Implementations are provided by {@code sdk-model} via {@code Decoders}.
 *
 * @param <T> The type produced by this decoder.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
@FunctionalInterface
public interface BodyDecoder<T> {

  /**
   * Decodes the response body.
   *
   * @param body      The raw response bytes.
   * @param mediaType The response media type, may be {@code null}.
   *
   * @return The decoded value.
   */
  T decode(byte[] body, @Nullable String mediaType);
}
