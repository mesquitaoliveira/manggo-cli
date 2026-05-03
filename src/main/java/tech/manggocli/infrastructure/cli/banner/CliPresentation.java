package tech.manggocli.infrastructure.cli.banner;

import static java.util.Objects.requireNonNullElse;

public final class CliPresentation {

    private CliPresentation() {}

    public static void printBanner() {
        System.out.println();
        System.out.println("  ███╗   ███╗ █████╗ ███╗   ██╗ ██████╗  ██████╗  ██████╗ ");
        System.out.println("  ████╗ ████║██╔══██╗████╗  ██║██╔════╝ ██╔════╝ ██╔═══██╗");
        System.out.println("  ██╔████╔██║███████║██╔██╗ ██║██║  ███╗██║  ███╗██║   ██║");
        System.out.println("  ██║╚██╔╝██║██╔══██║██║╚██╗██║██║   ██║██║   ██║██║   ██║");
        System.out.println("  ██║ ╚═╝ ██║██║  ██║██║ ╚████║╚██████╔╝╚██████╔╝╚██████╔╝");
        System.out.println("  ╚═╝     ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝ ╚═════╝  ╚═════╝  ╚═════╝ ");
        String version = CliPresentation.class.getPackage().getImplementationVersion();
        System.out.println("                    manggo-cli v" + requireNonNullElse(version, "dev"));
        System.out.println();
    }
}
