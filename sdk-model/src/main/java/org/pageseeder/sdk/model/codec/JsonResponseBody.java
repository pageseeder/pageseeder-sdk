package org.pageseeder.sdk.model.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import org.pageseeder.sdk.exception.ParsingException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * JSON-specific decoding helpers for a response body.
 */
public final class JsonResponseBody {

  private static final ObjectMapper JSON = new ObjectMapper();

  private final byte[] body;

  public JsonResponseBody(byte[] body) {
    this.body = body;
  }

  /**
   * Parses the response body as JSON and returns the root tree node.
   *
   * @return The root JSON node.
   */
  public JsonNode tree() {
    try {
      return JSON.readTree(this.body);
    } catch (IOException ex) {
      throw new ParsingException("Unable to parse PageSeeder JSON payload", ex);
    }
  }

  /**
   * Parses the response body as JSON and lets the caller map the root tree node.
   *
   * @param decoder The JSON decoder.
   * @param <T> The type returned by the decoder.
   *
   * @return The decoded value.
   */
  public <T> T map(Function<JsonNode, T> decoder) {
    return decoder.apply(tree());
  }

  /**
   * Parses the response body as JSON, selects the node at the specified JSON Pointer and maps it.
   *
   * @param pointer The JSON Pointer to the node.
   * @param decoder The node decoder.
   * @param <T> The type returned by the decoder.
   *
   * @return The decoded value or {@code null} if the pointer does not resolve to a value.
   */
  public <T> @Nullable T at(String pointer, Function<JsonNode, T> decoder) {
    JsonNode node = tree().at(pointer);
    if (node.isMissingNode() || node.isNull()) {
      return null;
    }
    return decoder.apply(node);
  }

  /**
   * Parses the response body as JSON, selects the node at the specified JSON Pointer and maps it as a list.
   *
   * <p>If the selected node is an array, each item is decoded. Otherwise the selected node is decoded as a single item.
   *
   * @param pointer The JSON Pointer to the node.
   * @param decoder The node decoder.
   * @param <T> The item type.
   *
   * @return The decoded items.
   */
  public <T> List<T> listAt(String pointer, Function<JsonNode, T> decoder) {
    JsonNode node = tree().at(pointer);
    if (node.isMissingNode() || node.isNull()) {
      return List.of();
    }
    if (!node.isArray()) {
      return List.of(decoder.apply(node));
    }
    List<T> items = new ArrayList<>(node.size());
    for (JsonNode item : node) {
      items.add(decoder.apply(item));
    }
    return items;
  }
}
