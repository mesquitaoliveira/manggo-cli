package tech.manggocli.core.application.strategy;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.domain.api.ClientSpec;
import tech.manggocli.core.domain.api.ParsedApi;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Strategy interface for client code generation.
 * Each mode (feign, feign-hc5, native) provides one implementation.
 * New modes require only a new class + one registry registration — CLI stays closed to modification.
 */
public interface ClientGeneratorStrategy {
    /** CLI mode name, e.g. "feign", "native", "feign-hc5". */
    String modeName();
    void generate(ParsedApi api, ClientSpec spec, String basePackage, Path outputDir, UserNotifier notifier) throws IOException;
}
