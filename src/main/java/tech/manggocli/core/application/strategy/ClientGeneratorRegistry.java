package tech.manggocli.core.application.strategy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of available code generation strategies.
 * Decouples the CLI from concrete generator classes — adding a new mode
 * requires no changes to ManggoGeneratorCLI.
 */
public final class ClientGeneratorRegistry {

    private final Map<String, ClientGeneratorStrategy> strategies = new LinkedHashMap<>();

    public ClientGeneratorRegistry register(final ClientGeneratorStrategy strategy) {
        strategies.put(strategy.modeName(), strategy);
        return this;
    }

    public Optional<ClientGeneratorStrategy> find(final String mode) {
        return Optional.ofNullable(strategies.get(mode));
    }

    public List<String> availableModes() {
        return new ArrayList<>(strategies.keySet());
    }
}
