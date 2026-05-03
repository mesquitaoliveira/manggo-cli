package tech.manggocli.core.domain.exception;

public class GeneratorException extends RuntimeException {

    public GeneratorException(final String message) {
        super(message);
    }

    public GeneratorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
