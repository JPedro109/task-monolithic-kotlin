# Estrutura do Projeto

## Layout do código-fonte

```
src/main/kotlin/com/jpmns/task/
├── TaskApplication.kt                    # Ponto de entrada do Spring Boot
├── configuration/                        # Configurações de framework (não cobertas pelo JaCoCo)
│   ├── security/SecurityConfig.kt        # Cadeia de filtros do Spring Security
│   ├── swagger/SwaggerConfig.kt          # OpenAPI / Swagger UI
│   └── tracing/OtelBaggageConfig.kt      # Contexto de rastreamento OTEL
├── core/
│   ├── domain/                           # Lógica de negócio pura — sem dependências de framework
│   │   ├── common/
│   │   │   ├── abstracts/Entity.kt       # Entidade base (id + createdAt + validateOrThrow)
│   │   │   ├── exception/DomainException.kt
│   │   │   └── valueobject/IdValueObject.kt
│   │   ├── task/
│   │   │   ├── TaskEntity.kt
│   │   │   └── valueobject/TaskNameValueObject.kt
│   │   └── user/
│   │       ├── UserEntity.kt
│   │       └── valueobject/  (UsernameValueObject, UserPasswordValueObject)
│   ├── application/                      # Casos de uso e interfaces de porta
│   │   ├── port/
│   │   │   ├── persistence/repository/   # TaskRepository, UserRepository (interfaces)
│   │   │   └── security/                 # Token, PasswordEncoder (interfaces + DTOs de segurança)
│   │   └── usecase/
│   │       ├── task/
│   │       │   ├── interfaces/           # Uma interface por caso de uso
│   │       │   ├── implementation/       # Implementações com @Service
│   │       │   ├── dto/input/            # DTOs de entrada (data classes sem anotações de framework)
│   │       │   ├── dto/output/           # DTOs de saída (data classes sem anotações de framework)
│   │       │   └── exception/            # Exceções de aplicação do domínio task
│   │       └── user/                     # Mesma estrutura que task
│   ├── external/                         # Adaptadores de infraestrutura
│   │   ├── persistence/
│   │   │   ├── dao/                      # Interfaces Spring Data JPA (TaskJpaDao, UserJpaDao)
│   │   │   ├── model/                    # Modelos @Entity do JPA (TaskJpaModel, UserJpaModel)
│   │   │   ├── mapper/                   # Objetos de mapeamento estático (domínio ↔ modelo JPA)
│   │   │   └── repository/               # Adaptadores @Repository implementando interfaces de porta
│   │   └── security/
│   │       ├── filter/JwtAuthenticationFilter.kt
│   │       ├── PasswordEncoderAdapter.kt
│   │       └── TokenAdapter.kt
│   └── presentation/                     # Camada HTTP
│       ├── controller/
│       │   ├── AuthController.kt
│       │   ├── TaskController.kt
│       │   ├── UserController.kt
│       │   ├── documentation/            # Interfaces *ControllerDoc com anotações @Operation do Swagger
│       │   │   └── payload/              # Interfaces *Doc para payloads (anotações @Schema)
│       │   ├── payload/                  # Classes de Request/Response por domínio
│       │   │   ├── task/
│       │   │   │   ├── request/          # CreateTaskRequest, UpdateTaskRequest
│       │   │   │   └── response/         # TaskResponse
│       │   │   └── user/
│       │   │       ├── request/          # UserLoginRequest, CreateUserRequest, etc.
│       │   │       └── response/         # UserLoginResponse, RefreshTokenResponse, etc.
│       │   └── common/
│       │       ├── handler/GlobalExceptionHandler.kt
│       │       ├── filter/               # Filtros Servlet (ex: TracingContextFilter)
│       │       └── resolver/AuthenticatedUserResolver.kt
│       └── scheduled/                    # Tarefas agendadas (se houver)
└── shared/                               # Utilitários transversais
    ├── extension/                        # Funções de extensão Kotlin
    └── type/Result.kt                    # Result<T, E> genérico para validação de value objects
```

## Regras de arquitetura (Clean Architecture)

- O **Domínio** não possui nenhuma dependência de Spring/JPA. Entidades e value objects são Kotlin puro.
- **Value objects** são criados via companion object com factory `of(...)` que retorna `Result<VO, DomainException>`. O construtor é sempre `private`; nunca instancie diretamente fora da própria classe.
- **Value objects** expõem o valor primitivo via método `asString()`. Não há getter genérico `getValue()` no value object em si.
- **Casos de uso** são definidos como interfaces em `usecase/.../interfaces/` e implementados em `usecase/.../implementation/`. Controllers dependem apenas da interface.
- **Port interfaces** (`TaskRepository`, `Token`, `PasswordEncoder`) ficam em `application/port/` e são implementadas por adaptadores em `external/`. As camadas de domínio e aplicação nunca importam de `external/`.
- **Mappers** são `object` (singleton sem estado). Possuem métodos `toModel()` (domínio → JPA) e `toDomain()` (JPA → domínio). Nunca adicionam lógica de negócio.
- **Implementações de use case** nunca retornam entidades de domínio diretamente.
- **Input DTOs** da camada de aplicação (`usecase/.../dto/input/`) são `data class` simples, sem anotações de framework. Recebem apenas tipos primitivos ou strings — nunca value objects.
- **Output DTOs** da camada de aplicação (`usecase/.../dto/output/`) são `data class` simples, sem anotações de framework. Contêm apenas tipos primitivos, strings e `Instant`.
- **Request payloads** (`presentation/controller/payload/.../request/`) são `data class` com anotações Bean Validation (`@NotBlank`, `@Size`, etc.) e implementam a interface `*RequestDoc` correspondente. Sempre sobrescrevem `toString()` quando contêm dados sensíveis.
- **Response payloads** (`presentation/controller/payload/.../response/`) são `data class` que implementam a interface `*ResponseDoc` correspondente e expõem uma factory estática `of(outputDTO)` no companion object para conversão a partir do output do use case.
- **Controllers** implementam a interface `*ControllerDoc` que concentra todas as anotações Swagger, mantendo a classe do controller limpa. Dependem exclusivamente das interfaces de casos de uso.
- **`AuthenticatedUserResolver`** é o único ponto de extração do ID do usuário autenticado a partir do `SecurityContext`.
- **Toda exceção originada em infraestrutura externa** (bibliotecas de terceiros, JPA, JJWT, etc.) deve ser capturada no adaptador correspondente e relançada como uma exceção do domínio/aplicação (ex: qualquer exceção da biblioteca JJWT é convertida para `InvalidTokenException`). As camadas de domínio e aplicação nunca devem depender de exceções de frameworks externos.
- **`GlobalExceptionHandler`** é o único ponto de mapeamento de exceções de domínio/aplicação para respostas HTTP. Nenhum controller trata exceções diretamente.

## Convenções principais

- **Nomenclatura**: `PascalCase` para tipos, `camelCase` para métodos/propriedades, `UPPER_SNAKE_CASE` para constantes, pacotes em letras minúsculas.
- **Sufixo `ConfigProperties`**: toda classe anotada com `@ConfigurationProperties` deve ter o sufixo `ConfigProperties`.
- **Formatação**: indentação de 4 espaços, sem tabs, máximo de 120 caracteres por linha — aplicado pelo ktlint.
- **Logging**: use `LoggerFactory.getLogger(XxxClass::class.java)` declarado no `companion object` (nunca `System.out`/`System.err`/`printStackTrace()`). Logue em nível `INFO` na entrada e saída dos métodos de controller.
- **IDs**: sempre strings UUID na fronteira do domínio; objetos `UUID` nos modelos JPA.
- **Schema do banco**: gerenciado exclusivamente pelo Flyway. Nunca use `ddl-auto: create/update`. Novas migrações seguem o padrão `V{n}__{descricao}.sql`.
- **ktlint + detekt**: aplicados em todo build. Política de zero warnings.

## Convenções de código

### Imutabilidade e uso de `val`

Sempre prefira `val` para declarar variáveis. Use `var` apenas em casos extremos onde a reatribuição seja estritamente necessária e não haja como modelar o problema com imutabilidade.

```kotlin
// CORRETO
val task = taskRepository.findById(taskIdValue) ?: throw TaskNotFoundException()
val output = toOutput(task)

// INCORRETO — var desnecessário
var task = taskRepository.findById(taskIdValue) ?: throw TaskNotFoundException()
var output = toOutput(task)
```

### Named parameters

Sempre use named parameters ao instanciar classes, chamar construtores ou invocar funções com mais de um argumento. Isso melhora a legibilidade e evita erros de posicionamento.

```kotlin
// CORRETO
val input = CreateTaskInputDTO(userId = userId.asString(), taskName = taskName.asString())
val task = TaskEntity(id = UUID.randomUUID().toString(), userId = input.userId, taskName = input.taskName, finished = false)

// INCORRETO — argumentos posicionais sem nome
val input = CreateTaskInputDTO(userId.asString(), taskName.asString())
val task = TaskEntity(UUID.randomUUID().toString(), input.userId, input.taskName, false)
```

Não há exceções a esta regra.

### Injeção de dependência via construtor

Toda dependência deve ser declarada como propriedade `private val` e injetada exclusivamente via construtor primário. Nunca use `@Autowired` em campo ou setter.

```kotlin
@Service
class CreateTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : CreateTaskUseCase {

}
```

### Separação de fases lógicas

Separe cada fase lógica de um método com **uma linha em branco**. Não insira linhas em branco dentro de uma mesma fase. Preserve o estilo do código ao redor.

Fases típicas de um use case:

```kotlin
override fun execute(input: UpdateTaskInputDTO): TaskOutputDTO {
    // 1. Preparação de entrada / validação
    val taskIdValueOrError = IdValueObject.of(input.taskId)
    if (taskIdValueOrError.isFail()) {
        throw taskIdValueOrError.getError()
    }

    // 2. Lógica de negócio / busca e autorização
    val taskIdValue = taskIdValueOrError.getValue()
    val task = taskRepository.findById(taskIdValue)
        ?: throw TaskNotFoundException()
    val userIsOwner = task.userId.asString() == input.userId
    if (!userIsOwner) {
        throw TaskAccessDeniedException()
    }

    // 3. Mutação no domínio
    task.updateTaskName(input.taskName)

    // 4. Persistência
    val saved = taskRepository.save(task)

    // 5. Mapeamento de resposta
    return toOutput(saved)
}
```

Fases típicas de um controller:

```kotlin
@PostMapping
override fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
    logger.info("Create task request received: ${request.taskName}")

    val userId = AuthenticatedUserResolver.getUserId()

    val input = CreateTaskInputDTO(userId = userId, taskName = request.taskName)

    val output = createTaskUseCase.execute(input)

    val response = TaskResponse.of(output)

    logger.info("Task created with id: ${response.id}")
    return ResponseEntity.status(HttpStatus.CREATED).body(response)
}
```

### Validação de value objects nas implementações de use case

Sempre que o input do use case contiver um campo que será usado **isoladamente** (sem instanciar uma entidade completa) — como um ID para busca ou um campo que será atualizado individualmente —, valide com `isFail()` antes de prosseguir e lance a exceção de domínio retornada pelo próprio `Result`. Nunca use o value object sem antes verificar o resultado.

```kotlin
val taskIdValueOrError = IdValueObject.of(input.taskId)
if (taskIdValueOrError.isFail()) {
    throw taskIdValueOrError.getError()
}
val taskIdValue = taskIdValueOrError.getValue()
```

**Exceção — instanciação de entidade completa**: quando todos os campos necessários estão disponíveis e a entidade será criada via construtor, **não** valide os value objects manualmente. O construtor já chama `validateOrThrow` internamente e lança `DomainException` automaticamente.

```kotlin
// CORRETO — entidade valida internamente, não repita a validação
val task = TaskEntity(
    id = UUID.randomUUID().toString(),
    userId = input.userId,
    taskName = input.taskName,
    finished = false
)

// INCORRETO — validação duplicada, desnecessária antes da instanciação completa
val taskNameValueOrError = TaskNameValueObject.of(input.taskName)
if (taskNameValueOrError.isFail()) {
    throw taskNameValueOrError.getError()
}
val task = TaskEntity(id = UUID.randomUUID().toString(), userId = input.userId, taskName = input.taskName, finished = false)
```

A conversão da entidade de domínio para `OutputDTO` deve sempre ser feita por um método privado `toOutput(entity: XxxEntity)` dentro da implementação. Nunca repita o mapeamento inline nem exponha entidades de domínio fora da implementação.

```kotlin
private fun toOutput(task: TaskEntity): TaskOutputDTO =
    TaskOutputDTO(
        id = task.id.asString(),
        userId = task.userId.asString(),
        taskName = task.taskName.asString(),
        finished = task.finished,
        createdAt = task.createdAt
    )
```

### DTOs e conversão de dados

O fluxo de dados entre camadas segue uma direção única, com tipos distintos em cada fronteira:

```
Request (payload) → InputDTO (usecase) → Entity (domain) → OutputDTO (usecase) → Response (payload)
```

- **Controller → Use Case**: o controller monta o `InputDTO` manualmente a partir dos campos do `Request`, nunca passa o `Request` diretamente ao use case.
- **Use Case → Controller**: o use case retorna um `OutputDTO`; o controller converte para `Response` via factory estática `Response.of(outputDto)` no companion object.
- **Use Case → Domain**: o use case instancia a entidade diretamente via construtor público. O `InputDTO` carrega apenas strings/primitivos.
- **Use Case → Repository → Domain**: mappers `object` fazem a conversão entre entidade de domínio e modelo JPA dentro dos adapters.

### `toString()` customizado para dados sensíveis

Todo `data class` de request ou response que contenha campos sensíveis (senha, token) **deve** sobrescrever `toString()` substituindo o valor por `'[PROTECTED]'`. Essa proteção garante que logs de entrada e saída dos controllers nunca exponham dados confidenciais.

```kotlin
// Exemplo: UserLoginRequest.kt
override fun toString(): String =
    "UserLoginRequest{username='$username', password='[PROTECTED]'}"

// Exemplo: UserLoginResponse.kt
override fun toString(): String =
    "UserLoginResponse{accessToken='[PROTECTED]', refreshToken='[PROTECTED]'}"
```

Campos considerados sensíveis: senhas (`password`, `currentPassword`, `newPassword`), tokens (`accessToken`, `refreshToken`) e qualquer credencial ou segredo.

### Agrupamento de ConfigurationProperties

Cada prefixo do `application.yaml` mapeado para uma classe Kotlin deve usar `@ConfigurationProperties` e seguir as regras abaixo:

- O nome da classe deve ter o sufixo `ConfigProperties` (ex: `SecurityConfigProperties`, `JwtConfigProperties`).
- Todas as classes `@ConfigurationProperties` devem ser registradas centralmente com `@EnableConfigurationProperties` na classe principal da aplicação (`TaskApplication.kt`), nunca espalhadas por classes de configuração individuais.
- Quando um prefixo contém subgrupos aninhados no YAML, use uma `data class` interna para o subgrupo (nunca achate tudo em uma única classe com nomes longos).

```kotlin
@ConfigurationProperties(prefix = "security")
data class SecurityConfigProperties(
    val jwt: Jwt
) {
    // Subgrupo aninhado: security.jwt.*
    data class Jwt(
        val secret: String,
        val accessTokenExpirationMs: Long,
        val refreshTokenExpirationMs: Long
    )
}

// Registro centralizado na classe principal:
@SpringBootApplication
@EnableConfigurationProperties(SecurityConfigProperties::class)
class TaskApplication
```

```yaml
# application.yaml
security:
  jwt:
    secret: ${JWT_SECRET:change-me-32-chars-minimum-value}
    access-token-expiration-ms: ${JWT_ACCESS_EXPIRATION_MS:900000}
    refresh-token-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
```

## Convenções de documentação

A documentação Swagger é isolada em interfaces dedicadas, mantendo os controllers e payloads livres de anotações OpenAPI.

### Interfaces `*ControllerDoc`

Cada controller possui uma interface `*ControllerDoc` em `presentation/controller/documentation/`. Ela concentra **todas** as anotações Swagger do controller: `@Tag`, `@RequestMapping`, `@Operation`, `@ApiResponses` e `@SecurityRequirement`. O controller implementa essa interface e não contém nenhuma anotação OpenAPI.

- `@Tag` e `@RequestMapping` ficam na interface, nunca no controller.
- `@SecurityRequirement(name = "bearerAuth")` é declarado na interface quando todos os endpoints do controller exigem autenticação. Para endpoints individuais que divergem da regra do controller, adicione `security` diretamente no `@Operation` do método correspondente.
- Cada método documenta `summary`, `description` (HTML inline com `<p>`, `<ul>`, `<li>`, `<code>`, `<strong>`), `requestBody` (quando há corpo) e `@ApiResponses` com todos os códigos de status possíveis.
- Todo `@ApiResponse` com corpo inclui `content` com `mediaType = "application/json"` e ao menos um `ExampleObject` com JSON representativo.
- Respostas sem corpo (ex: 204) declaram apenas `responseCode` e `description`, sem `content`.

```kotlin
@Tag(name = "Tasks", description = "Gerenciamento de tarefas — criação, listagem, atualização e exclusão")
@RequestMapping("/api/v1/tasks")
@SecurityRequirement(name = "bearerAuth")
interface TaskControllerDoc {

    @Operation(
        summary = "Criar nova tarefa",
        description = "<p>Cria uma nova tarefa vinculada ao usuário autenticado.</p>",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CreateTaskRequest::class),
                examples = [
                    ExampleObject(name = "Dados válidos", value = """{"taskName": "Estudar Kotlin"}"""),
                    ExampleObject(name = "Nome vazio (inválido)", value = """{"taskName": ""}""")
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "201",
            description = "Tarefa criada com sucesso",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = TaskResponse::class),
                examples = [ExampleObject(name = "Tarefa criada", value = """{"id": "...", "taskName": "Estudar Kotlin", "finished": false}""")]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(name = "Não autenticado", value = """{"status": 401, "detail": "Invalid or expired token"}""")]
            )]
        )
    )
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse>
}
```

### Interfaces `*RequestDoc` e `*ResponseDoc`

Cada classe de request e response possui uma interface `*Doc` em `presentation/controller/documentation/payload/`. Ela concentra as anotações `@Schema` dos campos. As classes de payload implementam a interface e não repetem anotações OpenAPI nos campos.

- A interface de payload é anotada com `@Schema(description = "...")` descrevendo o payload como um todo.
- Cada campo é documentado com `@get:Schema(description, example, minLength?, maxLength?)` na interface.
- As classes de request e response implementam a interface `*Doc` correspondente. Nenhuma anotação `@Schema` fica nas classes de payload — apenas na interface.

```kotlin
// Interface doc — documentation/payload/task/request/CreateTaskRequestDoc.kt
@Schema(description = "Payload para criação de uma nova tarefa")
interface CreateTaskRequestDoc {

    @get:Schema(
        description = "Nome da tarefa",
        example = "Estudar Kotlin",
        maxLength = 255
    )
    val taskName: String
}

// Payload — payload/task/request/CreateTaskRequest.kt
data class CreateTaskRequest(
    @field:NotBlank
    @field:Size(max = 255)
    override val taskName: String
) : CreateTaskRequestDoc
```

```kotlin
// Interface doc — documentation/payload/task/response/TaskResponseDoc.kt
@Schema(description = "Representação de uma tarefa")
interface TaskResponseDoc {

    @get:Schema(description = "Identificador único da tarefa", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    val id: String

    @get:Schema(description = "Nome da tarefa", example = "Estudar Kotlin")
    val taskName: String

    @get:Schema(description = "Indica se a tarefa foi concluída", example = "false")
    val finished: Boolean
}

// Payload — payload/task/response/TaskResponse.kt
data class TaskResponse(
    override val id: String,
    override val taskName: String,
    override val finished: Boolean
) : TaskResponseDoc {
    companion object {
        fun of(output: TaskOutputDTO): TaskResponse = TaskResponse(
            id = output.id,
            taskName = output.taskName,
            finished = output.finished
        )
    }
}
```

## Layout de testes

```
src/test/kotlin/com/jpmns/task/
├── core/
│   ├── application/usecase/       # Testes unitários de casos de uso (MockK, sem contexto Spring)
│   │   ├── task/                  # CreateTaskUseCaseTest, UpdateTaskUseCaseTest, etc.
│   │   └── user/                  # CreateUserUseCaseTest, UserLoginUseCaseTest, etc.
│   ├── domain/                    # Testes unitários de entidades e value objects
│   │   ├── task/                  # TaskEntityTest, TaskNameValueObjectTest
│   │   └── user/                  # UserEntityTest, UsernameValueObjectTest, etc.
│   ├── external/                  # Testes unitários de adaptadores e mappers
│   │   ├── persistence/
│   │   │   ├── dao/               # TaskJpaDaoTest, UserJpaDaoTest (@DataJpaTest)
│   │   │   ├── mapper/            # TaskMapperTest, UserMapperTest
│   │   │   └── repository/        # TaskRepositoryAdapterTest, UserRepositoryAdapterTest
│   │   └── security/              # PasswordEncoderAdapterTest, TokenAdapterTest
│   └── presentation/controller/   # Testes unitários de controllers (slice @WebMvcTest)
│       ├── AuthControllerTest.kt
│       ├── TaskControllerTest.kt
│       └── UserControllerTest.kt
├── integration/                   # Testes de integração completos (Testcontainers PostgreSQL)
│   ├── common/
│   │   ├── abstracts/IntegrationTestBase.kt    # Classe base: @SpringBootTest + MockMvc
│   │   ├── container/PostgresContainerConfig.kt
│   │   └── sql/SqlCreateSeed.kt               # Anotação: popula e limpa o BD por teste
│   ├── AuthIntegrationTest.kt
│   ├── TaskIntegrationTest.kt
│   └── UserIntegrationTest.kt
└── shared/
    ├── fixture/                   # TaskFixture, UserFixture — construtores de dados de teste compartilhados
    └── security/
        ├── WithJwtTokenMock.kt    # Anotação para injetar um principal JWT mockado nos testes
        └── factory/WithMockJwtTokenSecurityContextFactory.kt
```

## Convenções de testes

### Geral

- Use os fixtures existentes (`TaskFixture`, `UserFixture`) para construir dados de teste. Nunca instancie entidades de domínio inline dentro dos testes. Nunca use valores aleatórios (`UUID.randomUUID()`, etc.) — sempre fixture.
- Todo método de teste **deve** usar a sintaxe de backticks do Kotlin com uma frase descritiva em inglês no formato `` `should [resultado esperado] when [condição]` ``. Nunca use camelCase para nomear métodos de teste.

```kotlin
// CORRETO
@Test
fun `should return 201 with task data when input is valid`() { }

// INCORRETO
@Test
fun shouldReturn201WithTaskDataWhenInputIsValid() { }
```

- Exemplos: `` `should return 200 with tokens when credentials are valid` ``, `` `should throw when task is not found` ``, `` `should return 403 when user does not own the task` ``.
- Use `assertThat` do AssertJ para asserções.
- Verifique interações com `verify { mock.method(...) }` e `verify { mock wasNot Called }`.
- Testes devem ser ordenados: sucesso (happy path) primeiro, corner cases depois, exceções/erros por último.
- Siga o padrão **AAA (Arrange → Act → Assert)**, separando cada etapa com uma linha em branco, nunca utilize comentários para separação das etapas.
- Dentro do `Arrange`, separe a criação de variáveis dos stubs `every { ... }` com uma linha em branco:

```kotlin
val task = TaskFixture.aTask()
val user = UserFixture.aUser()
val userId = user.id
val taskName = task.taskName
val input = CreateTaskInputDTO(userId = userId.asString(), taskName = taskName.asString())

every { taskRepository.save(any()) } returns task

val output = useCase.execute(input)

assertThat(output.taskName).isEqualTo(taskName.asString())
assertThat(output.id).isNotNull()
verify { taskRepository.save(any()) }
```

- Sempre declare o fixture primeiro e depois extraia **cada campo que for utilizado** em variáveis separadas — nunca acesse propriedades do fixture diretamente no meio do código do teste:

```kotlin
// CORRETO
val task = TaskFixture.aTask()
val taskName = task.taskName
val taskId = task.id

// INCORRETO — acesso inline sem extração
every { taskRepository.findById(task.id) } returns task
assertThat(output.taskName).isEqualTo(task.taskName.asString())
```

- Cubra o máximo de cenários possível. Sempre siga a estrutura de layout definida para cada tipo de teste.

### Testes unitários de use cases

- Use `@ExtendWith(MockKExtension::class)` — sem contexto Spring.
- Dependências são declaradas com `@MockK`; a implementação sob teste com `@InjectMockKs`.
- Cenários obrigatórios para cada use case:
  - **Happy path**: fluxo principal de sucesso.
  - **Not found**: entidade não encontrada (quando o use case busca por ID).
  - **Access denied**: usuário não é dono do recurso (quando há verificação de ownership).
  - **Invalid input**: ID ou campo inválido que falha na criação do value object (quando aplicável).

```kotlin
@ExtendWith(MockKExtension::class)
class CreateTaskUseCaseTest {
    @MockK
    lateinit var taskRepository: TaskRepository

    @InjectMockKs
    lateinit var useCase: CreateTaskUseCaseImpl

    @Test
    fun `should create a task successfully`() {
        val task = TaskFixture.aTask()
        val user = UserFixture.aUser()
        val userId = user.id
        val taskName = task.taskName
        val input = CreateTaskInputDTO(userId = userId.asString(), taskName = taskName.asString())

        every { taskRepository.save(any()) } returns task

        val output = useCase.execute(input)

        assertThat(output.taskName).isEqualTo(taskName.asString())
        assertThat(output.id).isNotNull()
        verify { taskRepository.save(any()) }
    }
}
```

### Testes unitários de controllers

- Uma classe de teste por controller, com `@WebMvcTest(XxxController::class)`.
- Use `@Import` para incluir beans quando necessário (ex: `SecurityConfig`, `GlobalExceptionHandler`).
- Dependências são declaradas com `@MockkBean` (SpringMockK).
- Uma `inner class` por endpoint, anotada com `@Nested`, com `@DisplayName` indicando o método HTTP e o path (ex: `"POST /api/v1/tasks"`).
- Cada classe nested tem seu próprio método privado `perform(...)` que encapsula a chamada MockMvc para aquele endpoint.

```kotlin
@WebMvcTest(TaskController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
class TaskControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var createTaskUseCase: CreateTaskUseCase

    @MockkBean
    private lateinit var token: Token

    @Nested
    inner class CreateTask {
        @Test
        @WithJwtTokenMock
        fun `should return 201 with task data when creation succeeds`() {
            val task = TaskFixture.aTask()
            val taskName = task.taskName
            val output = buildTaskOutput()

            every { createTaskUseCase.execute(any()) } returns output

            perform(taskName.asString())
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.taskName").value(taskName.asString()))
        }

        @Test
        fun `should return 401 when request has no token`() {
            perform("My task")
                .andExpect(status().isUnauthorized)
        }

        private fun perform(taskName: String): ResultActions {
            val requestBody = """{"taskName": "$taskName"}"""
            return mockMvc.perform(
                post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }
}
```

### Testes de DAO (`@DataJpaTest`)

- Testes das interfaces Spring Data JPA (`*JpaDao`) usam `@DataJpaTest`, que sobe apenas o slice JPA sem o contexto completo do Spring.
- **Exceção permitida**: testes de DAO podem usar H2 em memória (dependência `testImplementation("com.h2database:h2")`), pois seu objetivo é verificar queries e mapeamentos JPA de forma rápida e isolada, sem necessidade de Testcontainers.
- Não use `@DataJpaTest` para testar lógica de negócio ou adaptadores de repositório — esses pertencem aos testes unitários com MockK e aos testes de integração com Testcontainers, respectivamente.

### Testes de integração

- Uma classe por controller com o sufixo `IntegrationTest` (ex: `TaskIntegrationTest`), estendendo `IntegrationTestBase`.
- `IntegrationTestBase` fornece `MockMvc`, `@SpringBootTest`, `@AutoConfigureMockMvc`, perfil `integration-test` e Testcontainers PostgreSQL via `PostgresContainerConfig`. Sempre estenda-a — nunca configure essas infraestruturas manualmente.
- Sempre usar Testcontainers — nunca banco em memória ou mocks de persistência (exceto testes de DAO com `@DataJpaTest`, conforme seção acima).
- Uma `inner class` por endpoint, anotada com `@Nested`, com `@DisplayName` indicando o método HTTP e o path.
- Cada classe nested tem seu próprio método privado `perform(...)` que encapsula a chamada MockMvc.
- Cada método de teste declara explicitamente se precisa de `@SqlCreateSeed` (para popular o banco) e/ou `@WithJwtTokenMock` (para autenticação). Não assuma nenhum estado prévio.

```kotlin
class TaskIntegrationTest : IntegrationTestBase() {
    @Nested
    inner class CreateTask {
        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 201 with task data when input is valid`() {
            val taskName = "My first task"

            perform(taskName)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").isNotEmpty)
                .andExpect(jsonPath("$.taskName").value(taskName))
                .andExpect(jsonPath("$.finished").value(false))
        }

        @Test
        fun `should return 401 when no token is provided`() {
            perform("My first task")
                .andExpect(status().isUnauthorized)
        }

        private fun perform(taskName: String): ResultActions {
            val requestBody = """{"taskName": "$taskName"}"""
            return mockMvc.perform(
                post("/api/v1/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }

    companion object {
        private const val EXISTING_TASK_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
    }
}
```
