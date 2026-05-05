package tech.manggocli.infrastructure.cli.command;

import tech.manggocli.core.application.usecase.GenerateClientUseCase;
import tech.manggocli.core.domain.client.ClientSpec;
import tech.manggocli.core.domain.exception.GeneratorException;
import tech.manggocli.infrastructure.cli.GeneratorConfig;
import tech.manggocli.infrastructure.cli.bootstrap.CliBootstrap;

import java.util.List;

/**
 * Responsibilities:
 * - Parse arguments
 * - Build dependencies
 * - Invoke use case
 */
public class GeneratorCommandImpl {

    /**
     * Executes client generation.
     *
     * @param config Parsed configuration (picocli-independent)
     * @return 0 on success, 1 on error
     */
    public int execute(final GeneratorConfig config) {
        try {
            final var bootstrap = new CliBootstrap();
            final var notifier = bootstrap.createNotifier();
            final var registry = bootstrap.createRegistry();
            final var parser = bootstrap.createParser();

            final CliArgumentParser argParser = new CliArgumentParser();
            final List<ClientSpec> clients = argParser.parseInputPairs(config.rawInputs());

            final GenerateClientUseCase useCase = new GenerateClientUseCase(registry, parser, notifier);

            return useCase.execute(clients, config.mode(), config.basePackage(), config.output()
            );

        } catch (final GeneratorException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return 1;
        } catch (final Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}
