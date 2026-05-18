package org.pageseeder.sdk.exception;

import org.jspecify.annotations.Nullable;

/**
 * PageSeeder service error returned by the API.
 *
 * @param id      the service error ID
 * @param message the service error message
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record ServiceError(@Nullable String id, String message) {
}
