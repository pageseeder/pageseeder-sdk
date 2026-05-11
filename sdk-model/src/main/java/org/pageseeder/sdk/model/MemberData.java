package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;

/**
 * Immutable PageSeeder member data metadata.
 *
 * @param id        the member data ID
 * @param name      the member data type name
 * @param title     the member data title
 * @param created   the creation timestamp
 * @param modified  the last modification timestamp
 * @param mediaType the content media type
 * @param length    the content length in bytes, or {@code -1} when unavailable
 * @param publiclyVisible whether the data type is public
 * @param memberId  the owner member ID
 */
public record MemberData(long id, String name, @Nullable String title, @Nullable OffsetDateTime created,
                         @Nullable OffsetDateTime modified, @Nullable String mediaType, int length,
                         boolean publiclyVisible, @Nullable Long memberId) {
}
