package tech.manggocli.infrastructure.cli;

import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;

public record GeneratorConfig(
        List<String> rawInputs,
        Path output,
        String basePackage,
        String mode
) {
    public GeneratorConfig(
            final List<String> rawInputs,
            final Path output,
            final String basePackage,
            final String mode
    ) {
        this.rawInputs = List.copyOf(requireNonNull(rawInputs, "rawInputs cannot be null"));
        this.output = requireNonNull(output, "output cannot be null");
        this.basePackage = requireNonNull(basePackage, "basePackage cannot be null");
        this.mode = requireNonNull(mode, "mode cannot be null");
    }
}
