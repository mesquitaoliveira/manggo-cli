# Manggo CLI

CLI Java que lê uma especificação **OpenAPI 3** e gera um projeto Maven completo
com cliente HTTP pronto para uso.

Suporta três modos de geração:

| Modo        | Cliente HTTP                          | Caso de uso                        |
|-------------|---------------------------------------|------------------------------------|
| `feign`     | OpenFeign (default)                   | Integrações Spring/Spring Cloud    |
| `feign-hc5` | OpenFeign + Apache HttpClient 5       | Conexões persistentes, pool tuning |
| `native`    | `java.net.http.HttpClient` (JDK 11+)  | Zero dependências externas         |

## Objetivos

1. **Reduzir a zero o boilerplate** entre uma OpenAPI spec e um projeto Maven funcional.
2. **Isolar o usuário do framework HTTP** — trocar de Feign para HttpClient nativo é
   um único flag (`--mode`).
3. **Manter o código gerado idiomático** (DTOs imutáveis, enums, request objects
   consolidados, smoke tests).
4. **Ser extensível por terceiros** — adicionar um novo modo (ex: `retrofit`, `webclient`)
   é um plugin via SPI, sem alterar o core.

---

## Quick start

```bash
mvn -q -pl . package
java -jar target/manggo-cli.jar \
     --input pets ./pets.yaml \
     --base-package com.example.pets \
     --output ./generated \
     --mode feign
```

Argumentos:

| Opção                  | Obrigatório | Descrição                                    |
|------------------------|-------------|----------------------------------------------|
| `--input <name> <file>`| sim         | Par nome+spec; pode repetir para multi-cliente |
| `--output, -o`         | sim         | Diretório raiz do projeto Maven gerado       |
| `--base-package, -p`   | sim         | Pacote base Java (ex: `com.example.client`)  |
| `--mode, -m`           | não         | `feign` (default), `feign-hc5`, `native`     |

---

## Stack

- Java 21, Maven
- Swagger Parser 2.x (leitura OpenAPI)
- Mustache (templates)
- PicoCLI (parsing CLI)
- JUnit 5 (testes de integração)

---

## Arquitetura

Hexagonal / Clean Architecture. Domínio sem dependências externas; infraestrutura
implementa portas.

```
tech.manggocli/
├── ManggoGeneratorCLI.java                     ← entry point
│
├── core/                                       ← regras de negócio puras
│   ├── domain/
│   │   ├── api/                                ← modelo OpenAPI normalizado
│   │   │   ├── ParsedApi.java                  ← agregado raiz (collections imutáveis)
│   │   │   ├── NamedField.java                 ← contrato comum (param/property)
│   │   │   ├── operation/                      ← ApiOperation, ApiParameter
│   │   │   └── schema/                         ← ApiSchema, ApiSchemaProperty
│   │   │       └── SchemaNode (sealed)         ← EnumNode|ObjectNode|ArrayAliasNode|MissingNode
│   │   ├── client/ClientSpec.java              ← input do user (não OpenAPI)
│   │   ├── exception/GeneratorException.java   ← raiz da hierarquia
│   │   ├── enums/                              ← enums compartilhados do domínio
│   │   │   └── SchemaKind.java                 ← smart enum: classifica + factory of(name,schema)
│   │   └── service/                            ← serviços de domínio
│   │       ├── JavaNames.java                  ← case conversion (camel/pascal/capitalize)
│   │       ├── JavaIdentifiers.java            ← sanitização (class name, enum const, $ref)
│   │       ├── ImportResolver.java             ← resolução de imports Java
│   │       ├── OperationTypeResolver.java      ← request/response type por operation
│   │       └── SchemaGraphResolver.java        ← traversal transitivo (usa SchemaKind.of)
│   └── application/
│       ├── exception/                          ← falhas de use case
│       ├── port/UserNotifier.java              ← saída (output port)
│       ├── strategy/                           ← contrato + registry de modos
│       └── usecase/GenerateClientUseCase.java
│
└── infrastructure/                             ← adapters
    ├── cli/                                    ← entrada via PicoCLI
    ├── parser/                                 ← Swagger Parser → core/domain/api
    └── codegen/
        ├── dto/                                ← DTO orchestrator + strategies
        ├── layout/                             ← MavenLayout, PackageNames, Templates
        ├── shared/                             ← geradores comuns (DTO, Rest interface, ApiModel)
        └── mode/
            ├── AbstractClientGenerator.java               ← Template Method principal
            ├── AbstractClientConfigurationGenerator.java
            ├── AbstractSmokeTestGenerator.java
            ├── ProjectScaffoldingGenerator.java           ← POM + roots Maven (genérico)
            ├── MethodParamHandler.java                    ← Chain of Responsibility
            ├── nullarg/                                   ← chain de null-args para smoke tests
            ├── feign/                                     ← modo feign + feign-hc5 (compartilhado)
            ├── feignhc5/                                  ← FeignHc5ClientGenerator (entry SPI)
            └── nativeclient/                              ← modo native (JDK HttpClient)
```

### Padrões aplicados

| Padrão                 | Onde                                               | Por quê                                     |
|------------------------|----------------------------------------------------|---------------------------------------------|
| **Strategy**           | `ClientGeneratorStrategy`                          | Cada modo é plug-and-play                   |
| **Registry + SPI**     | `ClientGeneratorRegistry` + `ServiceLoader`        | CLI fechado p/ modificação (OCP)            |
| **Template Method**    | `AbstractClientGenerator`, `*ConfigurationGenerator`, `*SmokeTestGenerator` | Sequência fixa, hooks específicos por modo  |
| **Chain of Responsibility** | `MethodParamHandler`, `NullArgHandler`        | Resolução sequencial de parâmetros          |
| **Composition Root**   | `CliBootstrap`                                     | Único ponto que monta dependências          |
| **Hexagonal**          | `core/application/port/UserNotifier`               | Domínio independe de console/UI             |

### Hierarquia de exceções

```
RuntimeException
└── GeneratorException                      (core/domain/exception)   ← raiz do domínio
    └── ApplicationException                (core/application/exception)
        ├── InvalidModeException                ← --mode inválido
        ├── InputFileNotFoundException          ← spec não encontrada
        ├── OpenApiParseException               ← falha no parse
        └── CodeGenerationException             ← falha ao gerar/escrever
```

Propagação: `infrastructure` lança → `core/application` propaga → `infrastructure/cli`
captura e formata.

---

## Como adicionar um novo modo (ex: `retrofit`)

1. Criar `RetrofitClientGenerator extends AbstractClientGenerator` em
   `infrastructure/codegen/mode/retrofit/`. Implementar os 4 hooks
   (`generateProjectStructure`, `generateSharedClasses`, `createTagGenerators`,
   `generateClientConfiguration`) e `modeName()` retornando `"retrofit"`.
2. Adicionar templates Mustache em `src/main/resources/templates/retrofit/` e
   constantes em `infrastructure/codegen/layout/Templates`.
3. Reusar `ProjectScaffoldingGenerator` para POM + Maven roots.
4. Registrar no SPI:
   `src/main/resources/META-INF/services/tech.manggocli.core.application.strategy.ClientGeneratorStrategy`
   adicionando uma linha com o FQN da nova classe.
5. Pronto — `--mode retrofit` já funciona. **Nenhum arquivo do `core` foi tocado.**

---

## Convenções

| Item                              | Local                                                   |
|-----------------------------------|---------------------------------------------------------|
| Case conversion (camel/pascal)    | `core/domain/service/JavaNames`                         |
| Sanitização Java identifier       | `core/domain/service/JavaIdentifiers`                   |
| Resolução de imports              | `core/domain/service/ImportResolver`                    |
| Classificação schema OpenAPI      | `core/domain/enums/SchemaKind` (smart enum + factory)   |
| Traversal de dependências schema  | `core/domain/service/SchemaGraphResolver`               |
| Constantes Maven                  | `infrastructure/codegen/layout/MavenLayout`             |
| Constantes de templates           | `infrastructure/codegen/layout/Templates`               |
| Pacotes Java gerados              | `infrastructure/codegen/layout/PackageNames`            |
| Novo modo                         | `infrastructure/codegen/mode/{modo}/` + SPI             |
| Novo tipo de DTO                  | `infrastructure/codegen/dto/strategy/`                  |
| Geradores compartilhados          | `infrastructure/codegen/shared/`                        |
| Novo adapter de entrada           | `infrastructure/{adapter}/`                             |
| Exceção de domínio                | `core/domain/exception/`                                |
| Exceção de use case               | `core/application/exception/`                           |

---

## Testes

Todos de integração: parseiam uma spec real e validam os arquivos gerados.

| Suíte                          | Cobre                                          |
|--------------------------------|------------------------------------------------|
| `DtoGenerationTest`            | DTOs, enums, allOf, imports                    |
| `ArrayAliasDtoTest`            | Aliases de arrays                              |
| `OneOfSchemaDtoTest`           | Esquemas oneOf                                 |
| `RequestObjectTest`            | Request objects consolidados, headers          |
| `SignatureParityTest`          | Paridade de assinaturas Rest ↔ RestClient      |
| `MavenAndConfigTest`           | Estrutura Maven, pom.xml, config               |
| `PetstoreSpecTest`             | Smoke test multi-tag (Petstore)                |
| `NativeClientGenerationTest`   | Modo `native` (sem Feign)                      |
| `NativeDtoGenerationTest`      | DTOs no modo `native`                          |
| `JavaIdentifierTest`           | Sanitização de identificadores Java            |
| `CamelCaseTest`                | Conversão para camelCase                       |
| `ParserTest`                   | Parsing OpenAPI                                |

```bash
mvn test
```
