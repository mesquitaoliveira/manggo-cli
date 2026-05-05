package tech.manggocli.infrastructure.codegen.mode;

import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.application.strategy.ClientGeneratorStrategy;
import tech.manggocli.core.domain.api.ParsedApi;
import tech.manggocli.core.domain.api.operation.ApiOperation;
import tech.manggocli.core.domain.client.ClientSpec;
import tech.manggocli.infrastructure.codegen.shared.TagCodeGenerator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;

public abstract class AbstractClientGenerator implements ClientGeneratorStrategy {

    @Override
    public final void generate(
            final ParsedApi api,
            final ClientSpec spec,
            final String basePackage,
            final Path projectRoot,
            final UserNotifier notifier) throws IOException {

        // 1. Maven project structure (mode-specific)
        generateProjectStructure(basePackage, projectRoot);

        // 2. Shared classes (mode-specific)
        generateSharedClasses(basePackage, projectRoot, spec, notifier);

        // 3. Tag generators (common to all modes)
        generateTagArtifacts(api, spec, basePackage, projectRoot, notifier);

        // 4. Client configuration (mode-specific)
        generateClientConfiguration(api, spec, basePackage, projectRoot, notifier);
    }

    /**
     * Generates the Maven project structure (pom.xml).
     * Differs per mode (native, feign, feign-hc5).
     */
    protected abstract void generateProjectStructure(String basePackage, Path projectRoot) throws IOException;

    /**
     * Generates shared base classes (ApiModel, Interceptors, etc.).
     * Varies per mode: feign has ApiKeyInterceptor, native has NativeBaseRestClient, etc.
     */
    protected abstract void generateSharedClasses(
            String basePackage,
            Path projectRoot,
            ClientSpec spec,
            UserNotifier notifier) throws IOException;

    /**
     * Factory for mode-specific tag generators.
     */
    protected abstract List<TagCodeGenerator> createTagGenerators(
            String basePackage,
            Path projectRoot,
            ClientSpec spec,
            ParsedApi api,
            UserNotifier notifier);

    /**
     * Generates the final client configuration.
     * Varies per mode: feign has ClientConfigurationGenerator, native has NativeClientConfigurationGenerator, etc.
     */
    protected abstract void generateClientConfiguration(
            ParsedApi api,
            ClientSpec spec,
            String basePackage,
            Path projectRoot,
            UserNotifier notifier) throws IOException;

    /**
     * Template Method: orchestrates parallel tag artifact generation.
     * Creates tag generators, iterates tags, filters operations, delegates generation.
     * Identical across all modes.
     */
    private void generateTagArtifacts(
            final ParsedApi api,
            final ClientSpec spec,
            final String basePackage,
            final Path projectRoot,
            final UserNotifier notifier) throws IOException {

        final List<TagCodeGenerator> tagGenerators = createTagGenerators(basePackage, projectRoot, spec, api, notifier);

        final ExecutorService executor = newFixedThreadPool(getRuntime().availableProcessors());
        final List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (final String tag : api.getTags()) {
            final List<ApiOperation> operationsForTag = filterOperationsByTag(api.getOperations(), tag);

            if (operationsForTag.isEmpty()) {
                continue;
            }

            notifier.notifyTagProcessed(tag, operationsForTag.size());

            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    for (final TagCodeGenerator generator : tagGenerators) {
                        generator.generate(tag, operationsForTag, api.getSchemas());
                    }
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            }, executor));
        }

        executor.shutdown();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (final CompletionException e) {
            if (e.getCause() instanceof UncheckedIOException uioe) throw uioe.getCause();
            throw new RuntimeException(e.getCause());
        }
    }

    private List<ApiOperation> filterOperationsByTag(final List<ApiOperation> operations, final String tag) {
        return operations.stream()
                .filter(operation -> tag.equals(operation.getTag()))
                .collect(toList());
    }
}
