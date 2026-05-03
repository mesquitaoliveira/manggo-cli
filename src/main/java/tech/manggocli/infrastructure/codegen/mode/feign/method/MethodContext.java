package tech.manggocli.infrastructure.codegen.mode.feign.method;

public record MethodContext(
    String requestLine,
    String headers,
    boolean hasReturn,
    String returnType,
    String methodName,
    String params
) {}
