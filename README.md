# Manggo - CLI

CLI que lê specs OpenAPI e gera projetos Maven completos com clientes HTTP em três modos:
`feign`, `feign-hc5` (Feign + Apache HttpClient 5), `native` (Java HttpClient puro).

---
## Stack

- Java 21, Maven
- Swagger Parser 2.x (leitura OpenAPI)
- Mustache (templates de código gerado)
- PicoCLI (parsing de argumentos CLI)
- JUnit 5 (testes de integração)
---
## Estado Atual — Estrutura Real de Pacotes

```
tech.manggocli/
│
├── ManggoGeneratorCLI.java             
│
├── core/
│   ├── domain/                         
│   │   ├── ApiOperation.java
│   │   ├── ApiSchema.java
│   │   ├── ApiSchemaProperty.java
│   │   ├── ApiParameter.java
│   │   ├── ParsedApi.java
│   │   ├── ClientSpec.java
│   │   ├── exception/
│   │   │   └── FeignGenException.java
│   │   └── service/
│   │       ├── SchemaDependencyWalker.java
│   │       ├── OperationTypeResolver.java
│   │       ├── NamingService.java
│   │       └── ImportResolver.java
│   └── application/                    
│       ├── exception/
│       │   ├── ApplicationException.java
│       │   ├── CodeGenerationException.java
│       │   ├── InputFileNotFoundException.java
│       │   ├── InvalidModeException.java
│       │   └── OpenApiParseException.java
│       ├── port/
│       │   └── UserNotifier.java
│       ├── strategy/
│       │   ├── ClientGeneratorStrategy.java
│       │   └── ClientGeneratorRegistry.java
│       └── usecase/
│           └── GenerateClientUseCase.java
│
└── infrastructure/                     
    ├── cli/                            
    │   ├── banner/
    │   │   └── CliPresentation.java
    │   ├── bootstrap/
    │   │   └── CliBootstrap.java
    │   ├── command/
    │   │   ├── FeignGeneratorCommand.java
    │   │   ├── FeignGeneratorCommandImpl.java
    │   │   └── CliArgumentParser.java
    │   ├── view/
    │   │   └── ConsoleUserNotifier.java
    │   └── vo/
    │       └── FeignGeneratorConfig.java
    ├── codegen/                       
    │   ├── dto/
    │   │   ├── DtoGenerationContext.java
    │   │   ├── DtoGenerationStrategy.java
    │   │   ├── DtoOrchestrator.java
    │   │   ├── builder/
    │   │   │   ├── DtoImportsCollector.java
    │   │   │   └── FieldMapper.java
    │   │   └── strategy/
    │   │       ├── ConsolidatedRequestStrategy.java
    │   │       ├── EnumDtoStrategy.java
    │   │       ├── QueryMapDtoStrategy.java
    │   │       └── RegularDtoStrategy.java
    │   ├── layout/
    │   │   ├── MavenLayout.java
    │   │   └── PackageNames.java
    │   ├── mode/
    │   │   ├── AbstractClientGenerator.java
    │   │   ├── AbstractClientConfigurationGenerator.java
    │   │   ├── AbstractSmokeTestGenerator.java
    │   │   ├── feign/
    │   │   │   ├── AbstractFeignSmokeTestGenerator.java
    │   │   │   ├── FeignClientGenerator.java
    │   │   │   ├── FeignRestClientGenerator.java
    │   │   │   ├── FeignApiKeyInterceptorGenerator.java
    │   │   │   ├── FeignProjectPomGenerator.java
    │   │   │   ├── FeignClientConfigGenerator.java
    │   │   │   ├── FeignLoggerGenerator.java
    │   │   │   ├── FeignClientConfigurationGenerator.java
    │   │   │   ├── FeignSmokeTestGenerator.java
    │   │   │   └── method/
    │   │   │       ├── MethodContext.java
    │   │   │       ├── MethodParamsBuilder.java
    │   │   │       ├── RequestLineBuilder.java
    │   │   │       ├── HeadersBuilder.java
    │   │   │       ├── FrameworkImportsResolver.java
    │   │   │       └── ImportCollector.java
    │   │   ├── feignhc5/
    │   │   │   ├── FeignHc5ClientGenerator.java
    │   │   │   ├── FeignHc5ProjectPomGenerator.java
    │   │   │   ├── FeignHc5ClientConfigurationGenerator.java
    │   │   │   └── FeignHc5SmokeTestGenerator.java
    │   │   └── httpclient/
    │   │       ├── NativeClientGenerator.java
    │   │       ├── NativeProjectPomGenerator.java
    │   │       ├── NativeRestClientGenerator.java
    │   │       ├── NativeBaseRestClientGenerator.java
    │   │       ├── NativeClientConfigurationGenerator.java
    │   │       └── NativeSmokeTestGenerator.java
    │   └── shared/
    │       ├── TagCodeGenerator.java
    │       ├── TemplateRenderer.java
    │       ├── ApiModelGenerator.java
    │       ├── DtoGenerator.java
    │       └── RestInterfaceGenerator.java
    └── parser/                        
        └── OpenApiParser.java
```

## Hierarquia de Exceções

```
RuntimeException
└── FeignGenException              (core/domain/exception)   ← raiz do domínio
    └── ApplicationException       (core/application/exception)
        ├── InvalidModeException       ← --mode inválido
        ├── InputFileNotFoundException ← arquivo .yaml não encontrado
        ├── OpenApiParseException      ← falha no parse do OpenAPI
        └── CodeGenerationException    ← falha ao gerar/escrever arquivo
```

Propagação: `infrastructure` lança → `core/application` propaga → `infrastructure/cli` captura e formata.

## Padrões

- **Template Method** — `AbstractClientGenerator`, `DtoOrchestrator` (sequência fixa + hooks)
- **Strategy** — `ClientGeneratorStrategy`, `DtoGenerationStrategy` (plug-and-play por modo)
- **Registry** — `ClientGeneratorRegistry` (seleção por mode name)
- **Composition Root** — `CliBootstrap` (monta dependências)

## Convenções

### Onde colocar coisas novas

| Item | Local |
|------|-------|
| Regra naming Java | `core/domain/service/NamingService` |
| Resolução imports | `core/domain/service/ImportResolver` |
| Constantes Maven | `infrastructure/codegen/layout/MavenLayout` |
| Novo modo (ex: retrofit) | `infrastructure/codegen/mode/{modo}/*Generator` |
| Novo DTO type | `infrastructure/codegen/dto/strategy/*Strategy` |
| Código compartilhado | `infrastructure/codegen/shared/` |
| Novo adapter entrada | `infrastructure/{adapter}/` |
| Exceção domínio | `core/domain/exception/` |
| Exceção use case | `core/application/exception/` |

## Testes (todos integração)

| Teste | Cobre |
|-------|-------|
| `DtoGenerationTest` | DTOs, enums, allOf, imports |
| `RequestObjectTest` | ConsolidatedRequestObject, headers |
| `JavaIdentifierTest` | Sanitização nomes Java |
| `SignatureParityTest` | Paridade de assinaturas |
| `MavenAndConfigTest` | Estrutura Maven, pom.xml |
| `ParserTest` | OpenAPI parsing |
| `NativeClientGenerationTest` | Modo native (sem Feign) |
| `PetstoreSpecTest` | Smoke test multi-tag |
