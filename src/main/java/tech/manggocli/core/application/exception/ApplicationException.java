package tech.manggocli.core.application.exception;

import tech.manggocli.core.domain.exception.GeneratorException;

public class ApplicationException extends GeneratorException {

    public ApplicationException(final String message) {
        super(message);
    }

    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
