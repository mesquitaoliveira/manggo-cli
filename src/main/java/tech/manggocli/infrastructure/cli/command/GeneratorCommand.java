package tech.manggocli.infrastructure.cli.command;

import tech.manggocli.infrastructure.cli.GeneratorConfig;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Encapsulates all picocli configuration.
 * Responsibility: receive CLI arguments and delegate to the implementation.
 */
@Command(
        name = "manggo-generator",
        mixinStandardHelpOptions = true,
        version = "1.0.0",
        description = "Generates HTTP clients from OpenAPI specifications.",
        sortOptions = false
)
public class GeneratorCommand implements Callable<Integer> {

    @Option(
            names = "--input",
            required = true,
            arity = "2",
            paramLabel = "<name> <file>",
            description = "Name+file OpenAPI pair (repeatable). Ex: --input pets ./pets.yml"
    )
    private final List<String> rawInputs = new ArrayList<>();

    @Option(
            names = {"--output", "-o"},
            required = true,
            paramLabel = "<path>",
            description = "Output directory for generated code."
    )
    private Path output;

    @Option(
            names = {"--base-package", "-p"},
            required = true,
            paramLabel = "<package>",
            description = "Base Java package. Ex: com.example.client"
    )
    private String basePackage;

    @Option(
            names = {"--mode", "-m"},
            paramLabel = "<mode>",
            description = "Generation mode. Invalid modes list available options.",
            defaultValue = "feign"
    )
    private String mode;

    @Override
    public Integer call() throws Exception {
        final var config = new GeneratorConfig(rawInputs, output, basePackage, mode);
        return new GeneratorCommandImpl().execute(config);
    }
}
