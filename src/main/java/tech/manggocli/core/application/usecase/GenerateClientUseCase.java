package tech.manggocli.core.application.usecase;

import tech.manggocli.core.application.exception.CodeGenerationException;
import tech.manggocli.core.application.exception.InputFileNotFoundException;
import tech.manggocli.core.application.exception.InvalidModeException;
import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.application.strategy.ClientGeneratorRegistry;
import tech.manggocli.core.application.strategy.ClientGeneratorStrategy;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.infrastructure.parser.OpenApiParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GenerateClientUseCase {

    private final ClientGeneratorRegistry registry;
    private final OpenApiParser           parser;
    private final UserNotifier            notifier;

    public GenerateClientUseCase(final ClientGeneratorRegistry registry,
                                 final OpenApiParser parser,
                                 final UserNotifier notifier) {
        this.registry = registry;
        this.parser   = parser;
        this.notifier = notifier;
    }

    public int execute(final List<ClientSpec> clients, final String mode, final String basePackage, final Path output) {
        final ClientGeneratorStrategy strategy = registry.find(mode)
                .orElseThrow(() -> new InvalidModeException(mode, registry.availableModes()));

        try {
            Files.createDirectories(output);
        } catch (final IOException e) {
            throw new CodeGenerationException("Failed to create output directory: " + output, e);
        }

        for (final ClientSpec spec : clients) {
            final Path inputPath = Path.of(spec.getFilePath());
            if (!Files.exists(inputPath)) {
                throw new InputFileNotFoundException(spec.getClientName(), inputPath);
            }

            notifier.notifyProgress("━━━ Cliente: [" + spec.getClientName() + "] (mode=" + mode + ") ━━━");
            notifier.notifyProgress("  Reading: " + inputPath.toAbsolutePath());

            final ParsedApi api = parser.parse(inputPath.toAbsolutePath().toString());
            notifier.notifyParseResult(spec.getClientName(), api.getTags().size(), api.getOperations().size());

            try {
                strategy.generate(api, spec, basePackage, output, notifier);
            } catch (final IOException e) {
                throw new CodeGenerationException(spec.getClientName(), e);
            }
        }

        notifier.notifySuccess("All clients generated successfully at: " + output.toAbsolutePath());
        return 0;
    }
}
