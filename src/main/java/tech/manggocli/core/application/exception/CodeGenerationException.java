package tech.manggocli.core.application.exception;

import java.io.IOException;

public class CodeGenerationException extends ApplicationException {

    public CodeGenerationException(final String clientName, final IOException cause) {
        super("Failed to generate client '" + clientName + "': " + cause.getMessage(), cause);
    }

    public CodeGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
