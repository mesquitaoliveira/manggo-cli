package tech.manggocli.core.application.exception;

import java.util.List;

public class InvalidModeException extends ApplicationException {

    public InvalidModeException(final String mode, final List<String> available) {
        super("Invalid --mode: '" + mode + "'. Available: " + available);
    }
}
