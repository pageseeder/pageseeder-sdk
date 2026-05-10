package org.pageseeder.sdk.cli;

import java.io.PrintStream;
import java.util.List;

/**
 * A single command exposed by the SDK CLI.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
interface CliCommand {

  /**
   * @return The command name used on the CLI.
   */
  String name();

  /**
   * @return A short human-readable description for help output.
   */
  String description();

  /**
   * Executes this command.
   *
   * @param arguments Remaining command arguments.
   * @param out       Standard output.
   * @param err       Standard error.
   *
   * @return Process-style exit code.
   */
  int run(List<String> arguments, PrintStream out, PrintStream err);

  /**
   * Executes this command with no arguments.
   *
   * @param out       Standard output.
   * @param err       Standard error.
   *
   * @return Process-style exit code.
   */
  default int run(PrintStream out, PrintStream err) {
    return run(List.of(), out, err);
  }
}
