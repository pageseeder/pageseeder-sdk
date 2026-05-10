package org.pageseeder.sdk.cli;

import java.io.PrintStream;
import java.util.List;

/**
 * Prints CLI usage information.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
final class HelpCommand implements CliCommand {

  private final CliCommands commands;

  HelpCommand(CliCommands commands) {
    this.commands = commands;
  }

  @Override
  public String name() {
    return "help";
  }

  @Override
  public String description() {
    return "Print available commands.";
  }

  @Override
  public int run(List<String> arguments, PrintStream out, PrintStream err) {
    if (!arguments.isEmpty()) {
      err.println("The help command does not accept arguments.");
      err.println();
      printUsage(err);
      return 1;
    }
    printUsage(out);
    return 0;
  }

  private void printUsage(PrintStream out) {
    out.println("Usage: pageseeder-sdk <command> [arguments]");
    out.println();
    out.println("Commands:");
    for (CliCommand command : this.commands.all()) {
      out.printf("  %-12s %s%n", command.name(), command.description());
    }
  }
}
