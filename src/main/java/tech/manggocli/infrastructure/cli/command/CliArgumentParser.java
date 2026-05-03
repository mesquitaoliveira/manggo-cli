package tech.manggocli.infrastructure.cli.command;


import tech.manggocli.core.domain.api.ClientSpec;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates and transforms raw inputs into ClientSpec.
 */
public class CliArgumentParser {

    public List<ClientSpec> parseInputPairs(final List<String> rawInputs) {
        if (rawInputs.isEmpty()) {
            throw new CommandLine.ParameterException(
                    new CommandLine(new GeneratorCommand()),
                    "--input is required. Provide at least one <name> <file> pair."
            );
        }

        if (rawInputs.size() % 2 != 0) {
            throw new CommandLine.ParameterException(
                    new CommandLine(new GeneratorCommand()),
                    "--input requires exactly 2 values per occurrence: <name> <file>. " +
                            "Provided: " + rawInputs
            );
        }

        final List<ClientSpec> result = new ArrayList<>();
        for (int i = 0; i < rawInputs.size(); i += 2) {
            final String clientName = rawInputs.get(i);
            final String filePath = rawInputs.get(i + 1);
            result.add(new ClientSpec(clientName, filePath));
        }
        return result;
    }
}
