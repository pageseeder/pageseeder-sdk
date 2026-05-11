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

  /**
   * Creates version information, deriving the display string when omitted.
   *
   * @param major  the major version
   * @param build  the build number
   * @param string the full version string, or {@code null} to derive it
   */
  public Version {
    string = string == null ? major + "." + String.format("%04d", build) : string;
  }

  /**
   * Returns the major version.
   *
   * @return the major version
   */
  public int getMajor() {
    return this.major;
  }

  /**
   * Returns the build number.
   *
   * @return the build number
   */
  public int getBuild() {
    return this.build;
  }

  /**
   * Returns the full version string.
   *
   * @return the full version string
   */
  public String getString() {
    return this.string;
  }

  @Override
  public String toString() {
    return this.string;
  }
}
