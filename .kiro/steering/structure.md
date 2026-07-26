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
│   │       └── valueobject/  (UserEmailValueObject, UsernameValueObject, UserPasswordValueObject)
│   ├── application/                      # Casos de uso e interfaces de porta
│   │   ├── port/
│   │   │   ├── persistence/repository/   # TaskRepository, UserRepository (interfaces)
│   │   │   └── security/                 # Token, PasswordEncoder (interfaces)
│   │   │       ├── dto/                  # DecodeTokenDto e outros DTOs de porta de segurança
│   │   │       └── exception/            # InvalidTokenException e outras exceções de porta
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
│   │   │   ├── mapper/                   # Classes de mapeamento estático (domínio ↔ modelo JPA)
│   │   │   └── repository/               # Adaptadores @Repository implementando interfaces de porta
│   │   └── security/
│   │       ├── filter/JwtAuthenticationFilter.kt
│   │       ├── service/UserDetailsServiceImpl.kt
│   │       ├── PasswordEncoderAdapter.kt
│   │       └── TokenAdapter.kt
│   └── presentation/                     # Camada HTTP
│       └── controller/
│           ├── AuthController.kt
│           ├── TaskController.kt
│           ├── UserController.kt
│           ├── documentation/            # Interfaces *ControllerDoc com anotações @Operation do Swagger
│           │   └── payload/              # Interfaces *Doc para payloads (anotações @Schema)
│           ├── payload/                  # Classes de Request/Response por domínio
│           │   ├── task/
│           │   │   ├── request/          # CreateTaskRequest, UpdateTaskRequest
│           │   │   └── response/         # TaskResponse
│           │   └── user/
│           │       ├── request/          # UserLoginRequest, CreateUserRequest, etc.
│           │       └── response/         # UserLoginResponse, RefreshTokenResponse, etc.
│           └── common/
│               ├── handler/GlobalExceptionHandler.kt
│               ├── filter/               # Filtros Servlet (ex: TracingContextFilter)
│               └── resolver/AuthenticatedUserResolver.kt
└── shared/                               # Utilitários transversais
    └── type/Result.kt                    # Result<T, E> genérico para validação de value objects
```

## Regras de arquitetura (Clean Architecture)

- O **Domínio** não possui nenhuma dependência de Spring/JPA. Entidades e value objects são Kotlin puro.
- **Value objects** são criados via companion object com factory `of(...)` que retorna `Result<VO, DomainException>`. O construtor é sempre `private`; nunca instancie diretamente fora da própria classe.
- **Value objects** expõem o valor primitivo via método `asString()`. Não há getter genérico `getValue()` no value object em si.
- **Casos de uso** são definidos como interfaces em `usecase/.../interfaces/` e implementados em `usecase/.../implementation/`. Controllers dependem apenas da interface.
- **Port interfaces** (`TaskRepository`, `Token`, `PasswordEncoder`) ficam em `application/port/` e são implementadas por adaptadores em `external/`. As camadas de domínio e aplicação nunca importam de `external/`.
- **Mappers** são `object` Kotlin (equivalente a classes estáticas sem estado). Possuem métodos `toModel()` (domínio → JPA) e `toDomain()` (JPA → domínio). Nunca adicionam lógica de negócio.
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

Funções de extensão e factory methods no `companion object` ficam junto aos métodos públicos. Helpers internos no `companion object` ficam no final.

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

### Separação de fases lógicas

Separe cada fase lógica de um método com **uma linha em branco**. Não insira linhas em branco dentro de uma mesma fase. Não agrupe instruções não relacionadas. Preserve o estilo do código ao redor.

Fases típicas de um use case:

```kotlin
override fun execute(input: UpdateTaskInputDTO): TaskOutputDTO {
    val taskIdValue = IdValueObject.of(input.taskId).getValueResultOrThrow()

    val task = taskRepository.findById(taskIdValue) ?: throw TaskNotFoundException()
    val userIsOwner = task.userId.asString() == input.userId
    if (!userIsOwner) {
        throw TaskAccessDeniedException()
    }

    task.updateTaskName(input.taskName)

    val saved = taskRepository.save(task)

    return toOutput(saved)
}
```

### Injeção de dependência via construtor

Toda dependência deve ser declarada como propriedade `private val` e injetada exclusivamente via construtor primário. Nunca use `@Autowired` em campo ou setter.

```kotlin
@Service
class CreateTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : CreateTaskUseCase {

}
```

### Ordenação de membros de uma classe

Siga sempre esta ordem dentro de qualquer classe:

1. Constantes (`companion object` com `const val` / `val`)
2. Campos de instância / propriedades
3. Construtores
4. Métodos públicos
5. Métodos protegidos
6. Métodos privados

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

### DTOs e conversão de dados

O fluxo de dados entre camadas segue uma direção única, com tipos distintos em cada fronteira:

```
Request (payload) → InputDTO (usecase) → Entity (domain) → OutputDTO (usecase) → Response (payload)
```

- **Controller → Use Case**: o controller monta o `InputDTO` manualmente a partir dos campos do `Request`, nunca passa o `Request` diretamente ao use case.
- **Use Case → Controller**: o use case retorna um `OutputDTO`; o controller converte para `Response` via factory estática `Response.of(outputDto)` no companion object.
- **Use Case → Domain**: o use case instancia a entidade diretamente via construtor público. O `InputDTO` carrega apenas strings/primitivos.
- **Use Case → Repository → Domain**: mappers fazem a conversão entre entidade de domínio e modelo JPA dentro dos adapters.

## Convenções da camada domain

Campos considerados sensíveis: senhas (`password`, `currentPassword`, `newPassword`), tokens (`accessToken`, `refreshToken`) e qualquer credencial ou segredo.

### Entidade base (`Entity`)

Toda entidade de domínio estende `Entity`. O construtor da classe base recebe `id` (String) e `createdAt` (Instant), valida o ID via `IdValueObject.of(id)` e atribui `Instant.now()` quando `createdAt` for `null`. O campo `id` é `val IdValueObject` (somente leitura — em Kotlin, `val` implica apenas getter, sem setter); `createdAt` é `val Instant`.

As subclasses acessam `id` via a propriedade `id` exposta pela classe base (ex: `task.id.asString()`). Não existe nem é necessário um método `getId()` — Kotlin expõe propriedades diretamente.

O método `validateOrThrow(results: List<Result<*>>)` é `protected` e coleta todos os `Result` com falha, extrai as `DomainException`s e lança uma `DomainException` agregada. Subclasses o chamam no construtor após criar todos os value objects.

### Estrutura de entidade de domínio

Entidades seguem este esquema:

1. **Campos declarados no corpo** (`val` para imutáveis, `var private set` para mutáveis): campos que precisam de inicialização tardia no `init` são declarados no corpo da classe; campos com valor padrão ou recebidos diretamente como parâmetro do construtor podem ser declarados no próprio construtor primário.
2. **Construtor primário**: recebe todos os campos como primitivos/strings. Campos opcionais (`createdAt`, `updatedAt`) têm valor padrão `null`.
3. **Bloco `init`**: cria os value objects a partir dos parâmetros primitivos, chama `validateOrThrow` e atribui os campos do corpo.
4. **Métodos de negócio** (`update*`, `markAs*`): recebem primitivos/strings, recriam o value object via `of(...).getValueResultOrThrow()` e atualizam o campo.

```kotlin
class TaskEntity(
    id: String,
    userId: String,
    taskName: String,
    finished: Boolean,
    createdAt: Instant? = null,
    val updatedAt: Instant? = null
) : Entity(id, createdAt) {

    var taskName: TaskNameValueObject
        private set
    var finished: Boolean = finished
        private set
    val userId: IdValueObject

    init {
        val userIdResult = IdValueObject.of(userId)
        val taskNameResult = TaskNameValueObject.of(taskName)

        val results = listOf(userIdResult, taskNameResult)
        validateOrThrow(results)

        this.userId = userIdResult.getValueResult()
        this.taskName = taskNameResult.getValueResult()
    }

    fun updateTaskName(taskName: String) {
        this.taskName = TaskNameValueObject.of(taskName).getValueResultOrThrow()
    }
}
```

### Value objects

Cada value object segue este contrato:

- Construtor `private` — instanciação exclusiva via `of(...)`.
- Factory no companion object `of(value: String)` retorna `Result<VO>`: `Result.fail(InvalidXxxException())` quando inválido, `Result.success(XxxValueObject(value))` quando válido.
- Método `asString()` expõe o valor primitivo. Nunca use `getValue()`.
- Sobrescrevem `equals` e `hashCode` com base em `asString()`.
- Regras de validação ficam dentro do `of(...)` — `null`, formato, tamanho, padrão de regex, etc.

```kotlin
class UsernameValueObject private constructor(private val username: String) {

    fun asString(): String = username

    companion object {
        private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_]{3,50}$")

        fun of(username: String): Result<UsernameValueObject> {
            if (!USERNAME_PATTERN.matches(username)) {
                return Result.fail(InvalidUsernameException())
            }

            return Result.success(UsernameValueObject(username))
        }
    }
}
```

Value objects de domínio existentes:

- `IdValueObject` — UUID no formato padrão (`common/valueobject/`)
- `TaskNameValueObject` — não nulo, não vazio, máximo 255 caracteres
- `UsernameValueObject` — alfanumérico + underscore, 3–50 caracteres
- `UserEmailValueObject` — formato de e-mail válido
- `UserPasswordValueObject` — não nulo (a senha já chega codificada do adapter)

### `DomainException`

Classe base de todas as exceções de domínio. Subclasses de value object e entidade estendem diretamente `DomainException` com mensagem fixa no construtor:

```kotlin
class InvalidTaskNameException : DomainException(
    "Task name must not be blank and must have at most 255 characters"
)
```

Nunca lance `RuntimeException` ou `IllegalArgumentException` no domínio — sempre uma subclasse de `DomainException`.

## Convenções da camada application

### Interfaces de porta (`port/`)

As interfaces de porta definem o contrato entre a camada de aplicação e a infraestrutura. Ficam em `application/port/` e são organizadas por categoria:

- `port/persistence/repository/` — interfaces de repositório (`TaskRepository`, `UserRepository`). Assinaturas trabalham exclusivamente com tipos do domínio: `IdValueObject`, `UsernameValueObject`, `TaskEntity`, `UserEntity`. Nunca expõem tipos JPA.
- `port/security/` — interfaces de serviços de segurança (`Token`, `PasswordEncoder`). Trabalham com `String`s e com o DTO de porta `DecodeTokenDto`.
- `port/security/dto/` — DTOs usados nas assinaturas das interfaces de porta de segurança (ex: `DecodeTokenDto`). São `data class` simples sem anotações de framework.
- `port/security/exception/` — exceções lançadas pelas interfaces de porta de segurança (ex: `InvalidTokenException`). Subclasses de `RuntimeException`; não estendem `DomainException`.

```kotlin
interface TaskRepository {
    fun save(task: TaskEntity): TaskEntity
    fun findById(id: IdValueObject): TaskEntity?
    fun findAllByUserId(userId: IdValueObject): List<TaskEntity>
    fun deleteById(id: IdValueObject)
}

interface Token {
    fun generateAccessToken(sub: String): String
    fun generateRefreshToken(sub: String): String
    fun tokenValidation(token: String): DecodeTokenDto
}
```

### Implementações de caso de uso (`usecase/.../implementation/`)

- Anotadas com `@Service`, implementam a interface correspondente.
- Uma classe por caso de uso, sufixo `Impl` (ex: `CreateTaskUseCaseImpl`).
- Dependências injetadas via construtor como propriedades `private val`.
- O método `execute` é sempre anotado com `override` e segue as fases descritas em [Separação de fases lógicas](#separação-de-fases-lógicas).
- Use cases sem retorno declaram `Unit` na interface.
- Use cases que retornam lista convertem cada entidade com `.map { toOutput(it) }`.

Estrutura completa de um use case de criação (sem busca por ID):

```kotlin
@Service
class CreateTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : CreateTaskUseCase {

    override fun execute(input: CreateTaskInputDTO): TaskOutputDTO {
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            userId = input.userId,
            taskName = input.taskName,
            finished = false
        )

        val saved = taskRepository.save(task)

        return toOutput(saved)
    }

    private fun toOutput(task: TaskEntity): TaskOutputDTO =
        TaskOutputDTO(
            id = task.id.asString(),
            userId = task.userId.asString(),
            taskName = task.taskName.asString(),
            finished = task.finished,
            createdAt = task.createdAt
        )
}
```

Estrutura completa de um use case de criação **com verificação de unicidade antes de instanciar a entidade**:

Quando o use case precisa verificar uma regra de negócio (ex: unicidade de username) antes de criar a entidade, valide o value object manualmente para poder usá-lo como parâmetro da query, depois instancie a entidade normalmente — sem duplicar a validação:

```kotlin
@Service
class CreateUserUseCaseImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CreateUserUseCase {

    override fun execute(input: CreateUserInputDTO): CreateUserOutputDTO {
        val usernameResult = UsernameValueObject.of(input.username).getValueResultOrThrow()

        if (userRepository.existsByUsername(usernameResult)) {
            throw UsernameAlreadyExistsException()
        }

        val encodedPassword = passwordEncoder.encode(input.password)
        val user = UserEntity(
            id = UUID.randomUUID().toString(),
            username = input.username,
            password = encodedPassword
        )

        val saved = userRepository.save(user)

        return toOutput(saved)
    }
}
```

Estrutura completa de um use case de atualização (com busca por ID, verificação de ownership e mutação):

```kotlin
@Service
class UpdateTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : UpdateTaskUseCase {

    override fun execute(input: UpdateTaskInputDTO): TaskOutputDTO {
        val taskIdValue = IdValueObject.of(input.taskId).getValueResultOrThrow()

        val task = taskRepository.findById(taskIdValue) ?: throw TaskNotFoundException()

        val userIsOwner = task.userId.asString() == input.userId
        if (!userIsOwner) {
            throw TaskAccessDeniedException()
        }

        task.updateTaskName(input.taskName)

        val saved = taskRepository.save(task)

        return toOutput(saved)
    }

    private fun toOutput(task: TaskEntity): TaskOutputDTO = ...
}
```

Estrutura completa de um use case de exclusão (sem retorno, com ownership):

```kotlin
@Service
class DeleteTaskUseCaseImpl(
    private val taskRepository: TaskRepository
) : DeleteTaskUseCase {

    override fun execute(input: DeleteTaskInputDTO) {
        val taskIdValue = IdValueObject.of(input.taskId).getValueResultOrThrow()

        val task = taskRepository.findById(taskIdValue) ?: throw TaskNotFoundException()

        val userIsOwner = task.userId.asString() == input.userId
        if (!userIsOwner) {
            throw TaskAccessDeniedException()
        }

        taskRepository.deleteById(taskIdValue)
    }
}
```

### Exceções de caso de uso (`usecase/.../exception/`)

Exceções específicas de cada domínio de caso de uso ficam em `exception/` ao lado de `interfaces/` e `implementation/`. Estendem `RuntimeException` diretamente (não `DomainException`) e carregam uma mensagem fixa:

```kotlin
class TaskNotFoundException : RuntimeException("Task not found")
```

Exceções de porta (`port/security/exception/`) seguem o mesmo padrão.

### Validação de value objects nas implementações de use case

Sempre que o input do use case contiver um campo que será usado **isoladamente** — como um ID para busca ou um campo que será atualizado individualmente —, use `getValueResultOrThrow()` diretamente. A validação manual com `isFailure` é necessária apenas quando o value object precisa ser inspecionado antes de ser usado.

```kotlin
val taskIdValue = IdValueObject.of(input.taskId).getValueResultOrThrow()
```

**Exceção — instanciação de entidade completa sem verificação prévia**: quando todos os campos necessários estão disponíveis, a entidade será criada via construtor e **não há verificação de negócio que precise ocorrer antes**, não valide os value objects manualmente. O construtor já chama `validateOrThrow` internamente e lança `DomainException` automaticamente.

```kotlin
// CORRETO — entidade valida internamente, não repita a validação
val task = TaskEntity(
    id = UUID.randomUUID().toString(),
    userId = input.userId,
    taskName = input.taskName,
    finished = false
)

// INCORRETO — validação duplicada, desnecessária antes da instanciação completa
val taskNameResult = TaskNameValueObject.of(input.taskName).getValueResultOrThrow()
val task = TaskEntity(id = UUID.randomUUID().toString(), userId = input.userId, taskName = input.taskName, finished = false)
```

A validação manual é necessária em dois casos:
- Para usar um value object **antes** de instanciar a entidade (ex: verificar unicidade de username sem criar o objeto ainda — nesse caso valide o VO, use-o na query e só depois instancie a entidade com os dados primitivos).
- Para converter campos de IDs recebidos no `InputDTO` que serão usados como parâmetros de busca no repositório.

A conversão da entidade de domínio para `OutputDTO` deve sempre ser feita por um método privado `toOutput(entity: XxxEntity)` dentro da implementação. Nunca repita o mapeamento inline nem exponha entidades de domínio fora da implementação.

## Convenções da camada external

A camada `external` é o único lugar onde infraestrutura pode existir. Toda integração com tecnologia externa — banco de dados, biblioteca de JWT, encoder de senha, cliente HTTP, fila de mensagens, etc. — deve ser implementada aqui como um adaptador.

**Exceção — camada de apresentação (HTTP, scheduler)**: mecanismos de entrada da aplicação não são infraestrutura de suporte e por isso **não** pertencem a `external`. Tudo que representa uma forma de acesso à aplicação pertence à camada `presentation`.

Cada adaptador **obrigatoriamente** implementa uma interface de porta definida em `application/port/`. Nunca crie uma classe de infraestrutura sem uma interface correspondente em `port/`. As camadas de domínio e aplicação nunca importam nada de `external/` — a dependência flui sempre de fora para dentro.

```
application/port/security/Token.kt                        ← interface
external/security/TokenAdapter.kt                         ← implementação (só external conhece JJWT)
application/port/persistence/repository/TaskRepository.kt ← interface
external/persistence/repository/TaskRepositoryAdapter.kt  ← implementação (só external conhece JPA)
```

### Adaptadores de persistência

#### Modelos JPA (`*JpaModel`)

- Anotados com `@Entity` e `@Table(name = "...")`.
- Campos `UUID` para IDs (não `String`) — a conversão é feita nos mappers com `UUID.fromString(...)` / `.toString()`.
- `@Id` sem geração automática (`@GeneratedValue`) — o ID vem do domínio.
- `@CreationTimestamp` e `@UpdateTimestamp` do Hibernate para `createdAt` e `updatedAt`.
- Colunas imutáveis após criação recebem `updatable = false`.
- O plugin `kotlin-jpa` do Gradle gera automaticamente o construtor sem argumentos exigido pelo JPA para classes anotadas com `@Entity` — não é necessário declará-lo manualmente.
- Use um único construtor primário com todos os campos. Campos com `updatable = false` no banco (como `id` e `userId`) ainda são declarados como `var` para compatibilidade com o JPA; o `updatable = false` na coluna garante que não sejam atualizados no banco.

```kotlin
@Entity
@Table(name = "tasks")
class TaskJpaModel(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID,
    @Column(name = "user_id", nullable = false, updatable = false)
    var userId: UUID,
    @Column(name = "task_name", nullable = false)
    var taskName: String,
    @Column(nullable = false)
    var finished: Boolean,
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)
```

#### DAOs Spring Data JPA (`*JpaDao`)

- Estendem `JpaRepository<Model, UUID>`.
- Declaram apenas os métodos de query adicionais necessários (ex: `findAllByUserId`). Métodos padrão do `JpaRepository` são usados diretamente.
- Sem anotações `@Query` a não ser que a query derivada não seja possível.

```kotlin
interface TaskJpaDao : JpaRepository<TaskJpaModel, UUID> {
    fun findAllByUserId(userId: UUID): List<TaskJpaModel>
}
```

#### Adaptadores do Repository (`*RepositoryAdapter`)

- Anotados com `@Repository`, implementam a interface de porta correspondente.
- Recebem o DAO Spring Data JPA via construtor.
- Toda operação converte domínio → modelo com `Mapper.toModel(entity)` antes de persistir, e modelo → domínio com `Mapper.toDomain(model)` ao retornar.
- **Nunca** propagam exceções do JPA para fora — se necessário, capturam e relançam como exceção de aplicação.

```kotlin
@Repository
class TaskRepositoryAdapter(
    private val jpaRepository: TaskJpaDao
) : TaskRepository {

    override fun save(task: TaskEntity): TaskEntity {
        val model = TaskMapper.toModel(task)
        val saved = jpaRepository.save(model)
        return TaskMapper.toDomain(saved)
    }

    override fun findById(id: IdValueObject): TaskEntity? {
        val parsedId = UUID.fromString(id.asString())
        return jpaRepository.findById(parsedId).map { TaskMapper.toDomain(it) }.orElse(null)
    }
}
```

#### Mappers (`*Mapper`)

- Implementados como `object` Kotlin — equivalente a uma classe estática sem estado.
- `toModel(entity: XxxEntity)` — converte domínio para JPA.
- `toDomain(model: XxxJpaModel)` — converte JPA para domínio.
- Nunca adicionam lógica de negócio ou validação.

```kotlin
object TaskMapper {
    fun toModel(entity: TaskEntity): TaskJpaModel =
        TaskJpaModel(
            id = UUID.fromString(entity.id.asString()),
            userId = UUID.fromString(entity.userId.asString()),
            taskName = entity.taskName.asString(),
            finished = entity.finished,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

    fun toDomain(model: TaskJpaModel): TaskEntity =
        TaskEntity(
            id = model.id.toString(),
            userId = model.userId.toString(),
            taskName = model.taskName,
            finished = model.finished,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
}
```

### Adaptadores de segurança

**`TokenAdapter`** (`@Component`, implementa `Token`):
- Recebe `SecurityConfigProperties` via construtor e extrai `jwt.secret`, `jwt.accessTokenExpirationMs`, `jwt.refreshTokenExpirationMs`.
- Toda exceção da biblioteca JJWT é capturada e relançada como `InvalidTokenException`. Nunca deixa exceções de JJWT escapar para fora do adaptador.
- `generateAccessToken` e `generateRefreshToken` delegam para um método privado `buildToken(sub, expirationMs, tokenType)`.
- Loga em nível `ERROR` quando o token é inválido.

**`PasswordEncoderAdapter`** (`@Component`, implementa `PasswordEncoder`):
- Recebe `BCryptPasswordEncoder` (bean registrado em `SecurityConfig`) via construtor.
- Delega `encode` e `matches` diretamente ao `BCryptPasswordEncoder` sem lógica adicional.

**`JwtAuthenticationFilter`** (`@Component`, estende `OncePerRequestFilter`):
- Extrai o token do header `Authorization: Bearer <token>`.
- Se o header estiver ausente ou sem prefixo `Bearer `, segue a cadeia sem autenticar.
- Chama `token.tokenValidation(jwt)` — se lançar exceção, segue a cadeia sem autenticar (o endpoint protegido devolverá 401).
- Se o `sub` for válido e o `SecurityContext` estiver vazio, chama `getUserByIdUseCase.execute(input)` e popula o `SecurityContext` com `UsernamePasswordAuthenticationToken` tendo o `userId` como principal e lista de authorities vazia.

## Convenções da camada presentation

A camada `presentation` concentra tudo que representa uma forma de acesso à aplicação. Independentemente do protocolo ou mecanismo, qualquer ponto de entrada deve estar aqui:

- **HTTP** — controllers REST (`@RestController`), filtros Servlet, handlers de exceção
- **Scheduler** — tarefas agendadas (`@Scheduled`)

Nunca coloque pontos de entrada em `external/` — essa camada é exclusiva para adaptadores de infraestrutura de suporte (banco, JWT, etc.).

### Estrutura dos controllers

- Anotados com `@RestController` e `@RequestMapping` com o path base do endpoint.
- Implementam a interface `*ControllerDoc` correspondente — nenhuma anotação do SpringDoc no corpo do controller.
- A interface `*ControllerDoc` **não** declara `@RequestMapping` — o path fica exclusivamente no controller.
- Logger declarado como `private val logger = LoggerFactory.getLogger(XxxController::class.java)` no `companion object`.
- Todas as dependências (interfaces de caso de uso) injetadas via construtor.
- `AuthenticatedUserResolver.getUserId()` é o único ponto de extração do ID do usuário autenticado. Nunca acesse o `SecurityContext` diretamente nos controllers.
- Use string interpolation do Kotlin (`"$variavel"`) nos logs — não use placeholders `{}` do SLF4J.

```kotlin
@RestController
@RequestMapping("/api/v1/tasks")
class TaskController(
    private val createTaskUseCase: CreateTaskUseCase
) : TaskControllerDoc {

    override fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
        logger.info("Creating task - request: $request")

        val userId = AuthenticatedUserResolver.getUserId()

        val input = CreateTaskInputDTO(userId = userId, taskName = request.taskName)

        val output = createTaskUseCase.execute(input)

        val response = TaskResponse.of(output)

        logger.info("Creating task - response: $response")
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskController::class.java)
    }
}
```

### `GlobalExceptionHandler`

- Anotado com `@RestControllerAdvice`.
- Único ponto de mapeamento de exceções para respostas HTTP. Nenhum controller captura exceções.
- Retorna `ProblemDetail` (Problem Details RFC 7807) via `ProblemDetail.forStatusAndDetail(status, message)`.
- Mapeamentos obrigatórios:
  - `MethodArgumentNotValidException` → `400` (agrega mensagens dos field errors)
  - `HttpMessageNotReadableException` → `400`
  - `DomainException` → `422`
  - `*NotFoundException` → `404`
  - `UsernameAlreadyExistsException` → `409`
  - `InvalidCredentialsException`, `InvalidTokenException` → `401`
  - `*AccessDeniedException` → `403`
  - `Exception` (genérico) → `500` com mensagem `"Internal server error"` (nunca exponha detalhes de exceções genéricas)
- Todo handler loga em nível `ERROR` com `logger.error("...: {}", ex.message, ex)`.

### `AuthenticatedUserResolver`

Classe utilitária sem instância que lê o `principal` do `SecurityContext`:

- `getUserId()` — lança `IllegalArgumentException` se não autenticado. Use em todos os endpoints protegidos.
- `getUserIdOrNull()` — retorna `null` se não autenticado. Use apenas em endpoints opcionalmente autenticados.

O principal no `SecurityContext` é sempre uma `String` com o UUID do usuário, populada pelo `JwtAuthenticationFilter`.

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

## Convenções de documentação (Swagger / OpenAPI)

Toda a documentação da API é declarada fora dos controllers, em interfaces e classes dedicadas dentro de `presentation/controller/documentation/`. Os controllers permanecem limpos — sem nenhuma anotação do SpringDoc.

### `*ControllerDoc` — documentação de endpoints

- Cada controller possui uma interface `*ControllerDoc` correspondente em `documentation/`.
- A interface é anotada com `@Tag(name = "...", description = "...")` e `@SecurityRequirement` quando aplicável. **Não** declara `@RequestMapping` — o path fica no controller.
- Endpoints que exigem autenticação recebem `@SecurityRequirement(name = "bearerAuth")` na interface (ou no método, quando apenas alguns endpoints do controller são protegidos).
- Cada método da interface declara exatamente uma anotação `@Operation` e uma `@ApiResponses`.

`@Operation` deve conter:
- `summary`: título curto do endpoint.
- `description`: descrição em HTML construída por concatenação de strings (`"<p>...</p>" + "<ul>..."`) com tags `<p>`, `<ul>`, `<li>`, `<code>` e `<blockquote>` quando necessário. Nunca use text blocks (triple-quote `"""`), pois não são suportados neste contexto.
- `requestBody` (quando aplicável): com `mediaType = "application/json"`, `schema` apontando para a classe de request e ao menos dois `ExampleObject` — um válido e um inválido.
- `parameters` (quando aplicável): para path variables, com `name`, `description`, `required = true` e `example`.

`@ApiResponses` deve cobrir todos os status HTTP possíveis para o endpoint:
- Sucesso (`2xx`): com `schema` e ao menos um `ExampleObject` com corpo representativo.
- Erros de validação (`400`): quando o endpoint aceita `@RequestBody`.
- Não autenticado (`401`): em todo endpoint protegido.
- Acesso negado (`403`): quando há verificação de ownership.
- Não encontrado (`404`): quando o use case pode lançar `*NotFoundException`.
- Conflito (`409`): quando há verificação de unicidade.
- Erro interno (`500`): sempre presente, com exemplo padrão.

Para respostas sem corpo (`204 No Content`), omita o `content` na `@ApiResponse`.

```kotlin
@Tag(name = "Tasks", description = "Gerenciamento de tarefas — criação, listagem, atualização, exclusão e conclusão")
@RequestMapping("/api/v1/tasks")
@SecurityRequirement(name = "bearerAuth")
interface TaskControllerDoc {

    @Operation(
        summary = "Criar nova tarefa",
        description = "<p>Cria uma nova tarefa associada ao usuário autenticado.</p>" +
            "<p>A tarefa é criada com o status <code>finished: false</code> por padrão.</p>" +
            "<p>Requer autenticação via <code>Authorization: Bearer &lt;accessToken&gt;</code>.</p>",
        requestBody = RequestBody(
            required = true,
            content = [Content(
                mediaType = "application/json",
                schema = Schema(implementation = CreateTaskRequest::class),
                examples = [
                    ExampleObject(name = "Tarefa válida", value = """{"taskName": "Estudar Spring Boot"}"""),
                    ExampleObject(name = "Nome em branco (inválido)", value = """{"taskName": ""}""")
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
                examples = [ExampleObject(name = "Tarefa criada", value = """{"id": "b2c3d4e5-f6a7-8901-bcde-f12345678901", "taskName": "Estudar Spring Boot", "finished": false}""")]
            )]
        ),
        ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(name = "Não autenticado", value = """{"status": 401, "detail": "Invalid or expired token"}""")]
            )]
        ),
        ApiResponse(
            responseCode = "500",
            description = "Erro interno inesperado",
            content = [Content(
                mediaType = "application/json",
                examples = [ExampleObject(name = "Erro interno", value = """{"status": 500, "detail": "Internal server error"}""")]
            )]
        )
    )
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse>
}
```

### `*RequestDoc` / `*ResponseDoc` — documentação de payloads

- Cada `data class` de request ou response implementa uma interface `*Doc` correspondente em `documentation/payload/{domínio}/request/` ou `.../response/`.
- A interface é anotada com `@Schema(name = "NomeDaClasse", description = "...")`.
- Cada propriedade da interface declara `@get:Schema` com `description`, `example` e, quando aplicável, `minLength` / `maxLength`.
- O `data class` de request/response implementa a interface — as anotações `@Schema` são herdadas automaticamente pelo SpringDoc.
- Campos sensíveis (senha, token) devem ter `example` com valor fictício (nunca omitir o exemplo).

```kotlin
// Interface de documentação
@Schema(name = "CreateTaskRequest", description = "Dados para criação de uma nova tarefa")
interface CreateTaskRequestDoc {

    @get:Schema(
        description = "Nome da tarefa. Não pode ser vazio e deve ter no máximo 255 caracteres.",
        example = "Estudar Spring Boot",
        maxLength = 255
    )
    val taskName: String
}

// Data class que implementa a interface
data class CreateTaskRequest(
    @field:NotBlank
    @field:Size(max = 255)
    override val taskName: String
) : CreateTaskRequestDoc
```

### Regras gerais de documentação

- Nunca adicione anotações do SpringDoc (`@Operation`, `@ApiResponse`, `@Schema`, etc.) diretamente nos controllers ou nas `data class` de request/response. Toda documentação pertence às interfaces `*Doc`.
- Exemplos de corpo de resposta de erro devem seguir o formato Problem Details (RFC 7807): campos `type`, `title`, `status`, `detail`.
- Os nomes dos `@ExampleObject` devem ser descritivos e em português, indicando o cenário representado (ex: `"Credenciais válidas"`, `"Username muito curto (inválido)"`).

## Layout de testes

```
src/test/kotlin/com/jpmns/task/
├── core/
│   ├── application/usecase/       # Testes unitários de casos de uso (MockK, sem contexto Spring)
│   │   ├── task/                  # CreateTaskUseCaseTest, UpdateTaskUseCaseTest, etc.
│   │   └── user/                  # CreateUserUseCaseTest, UserLoginUseCaseTest, etc.
│   ├── controller/                # Testes unitários de controllers (slice @WebMvcTest)
│   │   ├── AuthControllerTest.kt
│   │   ├── TaskControllerTest.kt
│   │   └── UserControllerTest.kt
│   ├── domain/                    # Testes unitários de entidades e value objects
│   │   ├── task/                  # TaskEntityTest, TaskNameValueObjectTest
│   │   └── user/                  # UserEntityTest, UsernameValueObjectTest, etc.
│   ├── external/                  # Testes unitários de adaptadores e mappers
│   │   ├── persistence/
│   │   │   ├── dao/               # TaskJpaDaoTest, UserJpaDaoTest (@DataJpaTest)
│   │   │   ├── mapper/            # TaskMapperTest, UserMapperTest
│   │   │   └── repository/        # TaskRepositoryAdapterTest, UserRepositoryAdapterTest
│   │   └── security/              # PasswordEncoderAdapterTest, TokenAdapterTest
│   └── fixture/                   # TaskFixture, UserFixture — construtores de dados de teste compartilhados
├── integration/                   # Testes E2E completos (Testcontainers PostgreSQL)
│   ├── common/
│   │   ├── abstracts/IntegrationTestBase.kt    # Classe base: @SpringBootTest + MockMvc
│   │   ├── container/PostgresContainerConfig.kt
│   │   └── sql/SqlCreateSeed.kt               # Anotação: popula e limpa o BD por teste
│   ├── AuthIntegrationTest.kt
│   ├── TaskIntegrationTest.kt
│   └── UserIntegrationTest.kt
└── shared/
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

- Use `assertThat` do AssertJ para asserções e `assertThatThrownBy` para verificar exceções.
- Verifique interações com `verify { mock.method(...) }` e `verify(exactly = 0) { mock.method(...) }`.
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

### Testes unitários de value objects

Uma classe de teste por value object — sem contexto Spring, sem MockK.

Cenários obrigatórios:
- Happy path: criação com valor válido, verificar `isFailure` é `false` e `getValueResult().asString()` retorna o valor esperado.
- Boundary cases: valores nos limites (mínimo, máximo, exato).
- Falhas: vazio, em branco, fora do limite, formato inválido.

```kotlin
@Test
fun `should create a valid TaskNameValueObject`() {
    val name = "Buy groceries"

    val result = TaskNameValueObject.of(name)

    assertThat(result.isFailure).isFalse()
    assertThat(result.getValueResult().asString()).isEqualTo(name)
}

@Test
fun `should fail when task name is blank`() {
    val result = TaskNameValueObject.of("")

    assertThat(result.isFailure).isTrue()
}
```

### Testes unitários de entidades

Uma classe de teste por entidade — sem contexto Spring, sem MockK.

Cenários obrigatórios:
- Happy path: construção com dados válidos, verificar todos os campos via getters.
- Métodos de negócio: cada `update*` e `markAs*` tem ao menos um cenário de sucesso e um de falha.
- Falhas de construção: IDs inválidos, campos obrigatórios nulos/vazios/fora do limite — assert em `DomainException`.

```kotlin
@Test
fun `should throw when task name is blank`() {
    val id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    val userId = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
    val finished = false
    val emptyTaskName = ""

    assertThatThrownBy { TaskEntity(id = id, userId = userId, taskName = emptyTaskName, finished = finished) }
        .isInstanceOf(DomainException::class.java)
}
```

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
class CreateUserUseCaseTest {

    @MockK
    lateinit var userRepository: UserRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @InjectMockKs
    lateinit var useCase: CreateUserUseCaseImpl

    @Test
    fun `should create a user successfully`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val input = CreateUserInputDTO(username = username.asString(), password = password.asString())
        val savedUser = UserFixture.aUser()

        every { userRepository.existsByUsername(username) } returns false
        every { passwordEncoder.encode(password.asString()) } returns password.asString()
        every { userRepository.save(any()) } returns savedUser

        val output = useCase.execute(input)

        assertThat(output.username).isEqualTo(username.asString())
        assertThat(output.id).isNotNull()
        verify { userRepository.save(any()) }
    }

    @Test
    fun `should throw when username already exists`() {
        val user = UserFixture.aUser()
        val username = user.username
        val password = user.password
        val input = CreateUserInputDTO(username = username.asString(), password = password.asString())

        every { userRepository.existsByUsername(username) } returns true
        every { passwordEncoder.encode(password.asString()) } returns password.asString()

        assertThatThrownBy { useCase.execute(input) }
            .isInstanceOf(UsernameAlreadyExistsException::class.java)
        verify { userRepository wasNot Called }
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
@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var userLoginUseCase: UserLoginUseCase

    @MockkBean
    private lateinit var token: Token

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    inner class Login {

        @Test
        fun `should return 200 with tokens when credentials are valid`() {
            val user = UserFixture.aUser()
            val username = user.username
            val password = user.password
            val accessToken = "access-token"
            val refreshToken = "refresh-token"
            val output = UserLoginOutputDTO(accessToken = accessToken, refreshToken = refreshToken)

            every { userLoginUseCase.execute(any()) } returns output

            perform(username = username.asString(), password = password.asString())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.accessToken").value(accessToken))
                .andExpect(jsonPath("$.refreshToken").value(refreshToken))
        }

        @Test
        fun `should return 401 when credentials are invalid`() {
            val user = UserFixture.aUser()
            val username = user.username

            every { userLoginUseCase.execute(any()) } throws InvalidCredentialsException()

            perform(username = username.asString(), password = "wrong-password")
                .andExpect(status().isUnauthorized)
        }

        private fun perform(username: String, password: String): ResultActions {
            val requestBody = """{"username": "$username", "password": "$password"}"""
            return mockMvc.perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
        }
    }
}
```

### Testes de persistência

Cobrem os três componentes da camada `external/persistence/`: DAOs, mappers e repository adapters. Cada um tem sua própria estratégia de teste.

#### Testes de DAO (`@DataJpaTest`)

- Usam `@DataJpaTest` — sobe apenas o slice JPA, sem contexto Spring completo.
- **Exceção permitida**: podem usar H2 em memória (`testImplementation("com.h2database:h2")`), pois o objetivo é verificar queries e mapeamentos JPA de forma rápida e isolada.
- Dependências (`*JpaDao`) são injetadas com `@Autowired`.
- Constantes para IDs desconhecidos ficam como `private val` no `companion object` da classe de teste.
- Use `@BeforeEach` para popular dados de pré-requisito.
- Construa os modelos JPA via métodos auxiliares privados (`buildTask(...)`, `buildUser(...)`) para evitar repetição.
- Cenários obrigatórios: `save`, `findById` (encontrado e não encontrado), `findAll*` (com resultado e lista vazia), `deleteById`.

```kotlin
@DataJpaTest
class TaskJpaDaoTest {

    @Autowired
    private lateinit var taskJpaDao: TaskJpaDao

    @Autowired
    private lateinit var userJpaDao: UserJpaDao

    @BeforeEach
    fun setUp() {
        val user = UserFixture.aUser()
        userJpaDao.save(buildUser(user))
    }

    @Test
    fun `should return empty when task id does not exist`() {
        val found = taskJpaDao.findById(UNKNOWN_TASK_ID)

        assertThat(found).isEmpty()
    }

    companion object {
        private val UNKNOWN_TASK_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000002")
    }
}
```

#### Testes de mapper

- Sem anotações de extensão — são classes Kotlin puras.
- Um teste por direção de mapeamento: `toModel` e `toDomain`.
- Verificar que todos os campos são mapeados corretamente, incluindo conversão de `UUID` ↔ `String`.

```kotlin
@Test
fun `should map a TaskEntity to a TaskJpaModel correctly`() {
    val task = TaskFixture.aTask()
    val taskId = task.id

    val model = TaskMapper.toModel(task)

    assertThat(model.id.toString()).isEqualTo(taskId.asString())
    assertThat(model.taskName).isEqualTo(task.taskName.asString())
}
```

#### Testes de repository adapter

- Use `@ExtendWith(MockKExtension::class)` — o DAO é mockado com `@MockK`; o adapter com `@InjectMockKs`.
- O objetivo é verificar que o adapter converte corretamente domínio ↔ modelo e delega ao DAO.
- Cenários obrigatórios espelham as operações da interface de porta: `save`, `findById` (presente e vazio), `findAll*`, `deleteById`.

```kotlin
@ExtendWith(MockKExtension::class)
class TaskRepositoryAdapterTest {

    @MockK
    lateinit var jpaRepository: TaskJpaDao

    @InjectMockKs
    lateinit var adapter: TaskRepositoryAdapter

    @Test
    fun `should save a task and return the persisted domain entity`() {
        val task = TaskFixture.aTask()
        val taskId = task.id
        val model = buildTaskModel()

        every { jpaRepository.save(any()) } returns model

        val result = adapter.save(task)

        assertThat(result.id.asString()).isEqualTo(taskId.asString())
        verify { jpaRepository.save(any()) }
    }
}
```

### Testes de integração

Cobrem adaptadores de infraestrutura que **não** são persistência — como `TokenAdapter`, `PasswordEncoderAdapter` e qualquer outro adaptador de `external/` que integre com biblioteca de terceiros. Esses testes instanciam o adaptador diretamente (sem contexto Spring) e verificam o comportamento real da integração.

- Sem `@ExtendWith` — o adaptador é instanciado manualmente no `@BeforeEach`.
- Dependências externas reais são usadas (ex: `BCryptPasswordEncoder`, `SecurityConfigProperties` construído manualmente).
- Constantes de configuração (segredos, expirations) são declaradas como `private const val` no `companion object` da classe de teste.
- Cenários obrigatórios para `Token`: geração de access token, geração de refresh token, validação com subject correto, token expirado, token malformado, token assinado com segredo diferente.
- Cenários obrigatórios para `PasswordEncoder`: encode retorna hash diferente do raw, hashes distintos para a mesma senha, `matches` retorna `true` para senha correta e `false` para senha errada.

```kotlin
class TokenAdapterTest {

    private lateinit var tokenAdapter: TokenAdapter

    @BeforeEach
    fun setUp() {
        tokenAdapter = TokenAdapter(buildProperties(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS))
    }

    @Test
    fun `should throw InvalidTokenException when token is expired`() {
        val expiredAdapter = TokenAdapter(buildProperties(SECRET, -1L, -1L))
        val user = UserFixture.aUser()
        val sub = user.id.asString()
        val expiredToken = expiredAdapter.generateAccessToken(sub)

        assertThatThrownBy { tokenAdapter.tokenValidation(expiredToken) }
            .isInstanceOf(InvalidTokenException::class.java)
    }
    private fun buildProperties(secret: String, accessMs: Long, refreshMs: Long): SecurityConfigProperties =
        SecurityConfigProperties(jwt = SecurityConfigProperties.Jwt(
            secret = secret,
            accessTokenExpirationMs = accessMs,
            refreshTokenExpirationMs = refreshMs
        ))

    companion object {
        private const val SECRET = "test-secret-key-must-be-at-least-32-chars!!"
        private const val ACCESS_EXPIRATION_MS = 900_000L
        private const val REFRESH_EXPIRATION_MS = 604_800_000L
    }
}
```

### Testes E2E

- Uma classe por controller com o sufixo `IntegrationTest` (ex: `TaskIntegrationTest`), estendendo `IntegrationTestBase`.
- `IntegrationTestBase` fornece `MockMvc`, `@SpringBootTest`, `@AutoConfigureMockMvc`, perfil `integration-test` e Testcontainers PostgreSQL via `PostgresContainerConfig`. Sempre estenda-a — nunca configure essas infraestruturas manualmente.
- Sempre usar Testcontainers — nunca banco em memória ou mocks de persistência (exceto testes de DAO com `@DataJpaTest`).
- Uma `inner class` por endpoint, anotada com `@Nested`, com `@DisplayName` indicando o método HTTP e o path.
- Cada classe nested tem seu próprio método privado `perform(...)` que encapsula a chamada MockMvc.
- Cada método de teste declara explicitamente se precisa de `@SqlCreateSeed` (para popular o banco) e/ou `@WithJwtTokenMock` (para autenticação). Não assuma nenhum estado prévio.

```kotlin
@DisplayName("Task Integration Tests")
class TaskIntegrationTest : IntegrationTestBase() {

    @Nested
    @DisplayName("POST /api/v1/tasks")
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

        @Test
        @SqlCreateSeed
        @WithJwtTokenMock
        fun `should return 400 when task name is blank`() {
            perform("")
                .andExpect(status().isBadRequest)
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
