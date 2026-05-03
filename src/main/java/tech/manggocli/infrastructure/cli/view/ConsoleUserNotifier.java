package tech.manggocli.infrastructure.cli.view;

import tech.manggocli.core.application.port.UserNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleUserNotifier implements UserNotifier {

    private static final Logger log = LoggerFactory.getLogger(ConsoleUserNotifier.class);

    @Override
    public void notifyProgress(final String message) {
        log.info(message);
    }

    @Override
    public void notifySuccess(final String message) {
        log.info(message);
    }

    @Override
    public void notifyError(final String message) {
        log.error(message);
    }

    @Override
    public void notifyParseResult(final String clientName, final int tagsCount, final int operationsCount) {
        final var msg = String.format("  Tags: %d  |  Operations: %d", tagsCount, operationsCount);
        log.info(msg);
    }

    @Override
    public void notifyFileGenerated(final String fileName) {
        log.info("[OK] {}.java", fileName);
    }

    @Override
    public void notifyTagProcessed(final String tagName, final int operationsCount) {
        log.info("  Tag [{}] ({} op)", tagName, operationsCount);
    }
}
