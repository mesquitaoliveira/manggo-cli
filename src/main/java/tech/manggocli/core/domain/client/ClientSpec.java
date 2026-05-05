package tech.manggocli.core.domain.client;

import tech.manggocli.core.domain.service.JavaNames;

import static java.util.Optional.ofNullable;

public class ClientSpec {
    private final String clientName;
    private final String filePath;

    public ClientSpec(final String clientName, final String filePath) {
        this.clientName = clientName;
        this.filePath = filePath;
    }

    public String getClientName() {
        return clientName;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getClientNamePascal() {
        return ofNullable(clientName)
                .map(JavaNames::toPascalCase)
                .orElse("Client");
    }

    @Override
    public String toString() {
        return clientName + " -> " + filePath;
    }
}
