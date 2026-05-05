package tech.manggocli.infrastructure.cli.bootstrap;


import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.application.strategy.ClientGeneratorRegistry;
import tech.manggocli.core.application.strategy.ClientGeneratorStrategy;
import tech.manggocli.infrastructure.cli.view.ConsoleUserNotifier;
import tech.manggocli.infrastructure.parser.OpenApiParser;

import java.util.ServiceLoader;

/**
 * Composition root. Discovers ClientGeneratorStrategy implementations via
 * {@link ServiceLoader} (META-INF/services). Adding a new mode requires
 * registering a new SPI entry — no changes to the CLI.
 */
public class CliBootstrap {

    public UserNotifier createNotifier() {
        return new ConsoleUserNotifier();
    }

    public ClientGeneratorRegistry createRegistry() {
        final ClientGeneratorRegistry registry = new ClientGeneratorRegistry();
        ServiceLoader.load(ClientGeneratorStrategy.class).forEach(registry::register);
        return registry;
    }

    public OpenApiParser createParser() {
        return new OpenApiParser();
    }
}
