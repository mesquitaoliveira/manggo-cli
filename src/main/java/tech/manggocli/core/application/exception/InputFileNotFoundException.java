package tech.manggocli.core.application.exception;

import java.nio.file.Path;

public class InputFileNotFoundException extends ApplicationException {

    public InputFileNotFoundException(final String clientName, final Path path) {
        super("File not found [" + clientName + "]: " + path.toAbsolutePath());
    }
}
