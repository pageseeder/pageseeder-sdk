package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Immutable PageSeeder webhook.
 *
 * @param id          the webhook ID
 * @param created     the creation timestamp
 * @param modified    the last modification timestamp
 * @param url         the webhook callback URL
 * @param server      the PageSeeder server identifier
 * @param object      the webhook object type
 * @param format      the webhook payload format
 * @param insecureSsl whether insecure SSL is allowed
 * @param status      the webhook status
 * @param name        the webhook name
 * @param projects    the project filters
 * @param groups      the group filters
 * @param events      the event filters
 * @param client      the OAuth client associated with the webhook
 */
public record Webhook(@Nullable Long id, @Nullable OffsetDateTime created, @Nullable OffsetDateTime modified,
                      @Nullable URI url, @Nullable String server, @Nullable String object, @Nullable String format,
                      boolean insecureSsl, @Nullable String status, @Nullable String name, List<String> projects,
                      List<String> groups, List<String> events, @Nullable OAuthClient client) {

  /**
   * Creates a webhook with immutable filter lists.
   *
   * @param id          the webhook ID
   * @param created     the creation timestamp
   * @param modified    the last modification timestamp
   * @param url         the webhook callback URL
   * @param server      the PageSeeder server identifier
   * @param object      the webhook object type
   * @param format      the webhook payload format
   * @param insecureSsl whether insecure SSL is allowed
   * @param status      the webhook status
   * @param name        the webhook name
   * @param projects    the project filters
   * @param groups      the group filters
   * @param events      the event filters
   * @param client      the OAuth client associated with the webhook
   */
  public Webhook {
    projects = projects == null ? List.of() : List.copyOf(projects);
    groups = groups == null ? List.of() : List.copyOf(groups);
    events = events == null ? List.of() : List.copyOf(events);
  }
}
