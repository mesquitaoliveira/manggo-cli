package tech.manggocli;

import tech.manggocli.infrastructure.cli.command.GeneratorCommand;
import picocli.CommandLine;

import static tech.manggocli.infrastructure.cli.banner.CliPresentation.printBanner;
import static java.lang.System.exit;

public class ManggoGeneratorCLI {

    public static void main(String[] args) {
        printBanner();
        int exitCode = new CommandLine(new GeneratorCommand())
                .execute(args);
        exit(exitCode);
    }
}
