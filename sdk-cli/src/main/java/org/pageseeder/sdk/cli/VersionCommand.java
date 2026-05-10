package org.pageseeder.sdk.cli;

import org.pageseeder.sdk.client.PageSeederClient;
import org.pageseeder.sdk.model.codec.Decoders;
import org.pageseeder.sdk.model.Version;
import org.pageseeder.sdk.service.ServiceCall;
import org.pageseeder.sdk.service.ServiceCatalog;

import java.io.PrintStream;
import java.net.URI;
import java.util.List;

/**
 * Fetches the remote PageSeeder version using the SDK client.
 *
 * @author Christophe Lauret
 *
 * @version 1.0.0
 * @since 1.0.0
 */
final class VersionCommand implements CliCommand {

  @Override
  public String name() {
    return "version";
  }

  @Override
  public String description() {
    return "Fetch the PageSeeder server version from an API origin.";
  }

  @Override
  public int run(List<String> arguments, PrintStream out, PrintStream err) {
    if (arguments.size() != 1) {
      err.println("Usage: pageseeder-sdk version <api-origin>");
      return 1;
    }

    try {
      URI apiOrigin = URI.create(arguments.get(0));
      PageSeederClient client = PageSeederClient.builder()
          .apiOrigin(apiOrigin)
          .build();
      Version version = client.execute(ServiceCall.of(ServiceCatalog.VERSION), Decoders.object(Version.class));
      out.println(version.getString());
      return 0;
    } catch (IllegalArgumentException ex) {
      err.println("Invalid API origin: " + ex.getMessage());
      return 1;
    } catch (RuntimeException ex) {
      err.println("Unable to fetch version: " + ex.getMessage());
      return 1;
    }
  }
}
