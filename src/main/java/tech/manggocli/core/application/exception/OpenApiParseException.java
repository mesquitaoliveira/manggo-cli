package tech.manggocli.core.application.exception;

import java.util.List;

public class OpenApiParseException extends ApplicationException {

    public OpenApiParseException(final String location, final List<String> messages) {
        super("Unable to parse: " + location + ". Messages: " + messages);
    }

    public OpenApiParseException(final String location, final Throwable cause) {
        super("Unable to parse: " + location, cause);
    }
}
