package org.pageseeder.sdk.cli;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * Entry point for the SDK CLI.
 *
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Main {

  private final CliCommands commands = new CliCommands();

  @SuppressWarnings("java:S106")
  public static void main(String[] args) {
    int exitCode = new Main().run(Arrays.asList(args), System.out, System.err);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }



  int run(List<String> args, PrintStream out, PrintStream err) {
    if (args.isEmpty()) {
      return help(out, err);
    }

    String commandName = args.get(0);
    CliCommand command = this.commands.find(commandName);
    if (command == null) {
      err.println("Unknown command: " + commandName);
      err.println();
      help(err, err);
      return 1;
    }
    return command.run(args.subList(1, args.size()), out, err);
  }

  private int help(PrintStream out, PrintStream err) {
    CliCommand help = this.commands.find("help");
    assert help != null; // Always available
    return help.run(out, err);
  }

}
