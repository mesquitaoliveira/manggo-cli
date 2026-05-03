package tech.manggocli.core.application.port;

public interface UserNotifier {

    void notifyProgress(String message);

    void notifySuccess(String message);

    void notifyError(String message);

    void notifyParseResult(String clientName, int tagsCount, int operationsCount);

    void notifyFileGenerated(String fileName);

    void notifyTagProcessed(String tagName, int operationsCount);

    UserNotifier NOOP = new UserNotifier() {
        @Override public void notifyProgress(String message) {}
        @Override public void notifySuccess(String message) {}
        @Override public void notifyError(String message) {}
        @Override public void notifyParseResult(String c, int t, int o) {}
        @Override public void notifyFileGenerated(String fileName) {}
        @Override public void notifyTagProcessed(String tagName, int operationsCount) {}
    };
}
