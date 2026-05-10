package org.pageseeder.sdk.cli;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of CLI commands.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
final class CliCommands {

  private final Map<String, CliCommand> commands;

  CliCommands() {
    this.commands = new LinkedHashMap<>();
    register(new HelpCommand(this));
    register(new VersionCommand());
  }

  /**
   * Find a command by name.
   *
   * @param name The name of the command.
   *
   * @return The command or <code>null</code> if not found.
   */
  @Nullable CliCommand find(String name) {
    return this.commands.get(name);
  }

  /**
   * Return all registered commands.
   *
   * @return The list of registered commands
   */
  List<CliCommand> all() {
    return List.copyOf(this.commands.values());
  }

  private void register(CliCommand command) {
    this.commands.put(command.name(), command);
  }
}
