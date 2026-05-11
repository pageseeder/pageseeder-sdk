package org.pageseeder.sdk.model;

/**
 * PageSeeder group together with its optional configuration settings.
 *
 * @param group    the group
 * @param settings the group configuration settings
 */
public record ConfiguredGroup(Group group, GroupSettings settings) {
}
