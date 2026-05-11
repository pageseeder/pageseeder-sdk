package org.pageseeder.sdk.model;

import org.jspecify.annotations.Nullable;

/**
 * PageSeeder version information.
 *
 * @param major  The major version (e.g. '5').
 * @param build  The build number for that version (e.g. '5900').
 * @param string The full string version of the build (e.g. '5.5900').
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public record Version(int major, int build, @Nullable String string) {

  public Version {
    string = string == null ? major + "." + String.format("%04d", build) : string;
  }

  public int getMajor() {
    return this.major;
  }

  public int getBuild() {
    return this.build;
  }

  public String getString() {
    return this.string;
  }

  @Override
  public String toString() {
    return this.string;
  }
}
