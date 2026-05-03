package tech.manggocli.infrastructure.cli.bootstrap;


import tech.manggocli.core.application.port.UserNotifier;
import tech.manggocli.core.application.strategy.ClientGeneratorRegistry;
import tech.manggocli.infrastructure.cli.view.ConsoleUserNotifier;
import tech.manggocli.infrastructure.codegen.mode.feign.FeignClientGenerator;
import tech.manggocli.infrastructure.codegen.mode.feignhc5.FeignHc5ClientGenerator;
import tech.manggocli.infrastructure.codegen.mode.httpclient.NativeClientGenerator;
import tech.manggocli.infrastructure.parser.OpenApiParser;

/**
 * Factory that creates all CLI dependencies.
 * Single place to swap implementations.
 */
public class CliBootstrap {

    public UserNotifier createNotifier() {
        return new ConsoleUserNotifier();
    }

    public ClientGeneratorRegistry createRegistry() {
        return new ClientGeneratorRegistry()
                .register(new FeignClientGenerator())
                .register(new FeignHc5ClientGenerator())
                .register(new NativeClientGenerator());
    }

    public OpenApiParser createParser() {
        return new OpenApiParser();
    }
}
