---
inclusion: fileMatch
fileMatchPattern: "**/src/main/**/application/**"
---

# Convenções da Camada Application

A camada **Application** é responsável por orquestrar os casos de uso da aplicação.

Ela coordena a execução das regras de negócio da camada Domain e realiza validações ou decisões que dependam de recursos externos.

Regras que possam ser implementadas exclusivamente pelo domínio não devem ser implementadas nesta camada.

---

# Estrutura

A organização da camada Application deve refletir os casos de uso da aplicação.

A estrutura recomendada é:

```text
application/
├── port/
│   ├── persistence/
│   │   └── repository/
│   └── security/
│       ├── dto/
│       └── exception/
└── usecase/
    └── sample/
        ├── interfaces/
        ├── implementation/
        ├── dto/
        │   ├── input/
        │   └── output/
        └── exception/
```

As seguintes regras devem ser respeitadas:

- Cada contexto deve possuir seu próprio pacote.
- Cada caso de uso deve possuir sua própria interface.
- Implementações devem permanecer em `implementation`.
- DTOs devem permanecer separados entre entrada e saída.
- Portas devem permanecer organizadas por responsabilidade.

---

# Casos de Uso

Casos de uso representam operações executadas pela aplicação.

Cada caso de uso deve representar apenas uma responsabilidade.

As seguintes regras devem ser respeitadas:

- Todo caso de uso deve possuir uma interface.
- Toda implementação deve implementar sua respectiva interface.
- Implementações devem utilizar o sufixo `Impl`.
- Cada caso de uso deve executar apenas um fluxo da aplicação.
- Casos de uso não devem conter regras de infraestrutura.
- Casos de uso não devem depender da camada Presentation.

## ✔ Correto

```kotlin
interface CreateSampleUseCase {
    fun execute(input: CreateSampleInputDTO): CreateSampleOutputDTO
}
```

```kotlin
@Service
class CreateSampleUseCaseImpl(
    private val sampleRepository: SampleRepository
) : CreateSampleUseCase {
    ...
}
```

## ❌ Incorreto

```kotlin
@Service
class SampleService {
    fun create(...) {}
    fun update(...) {}
    fun delete(...) {}
}
```

---

# DTOs

DTOs representam exclusivamente os dados trafegados entre camadas.

Não representam regras de negócio.

As seguintes regras devem ser respeitadas:

- Devem ser imutáveis.
- Devem utilizar `data class`.
- Não devem possuir validações.
- Não devem possuir regras de negócio.
- Não devem possuir dependência de frameworks.
- Devem receber apenas tipos primitivos ou strings — nunca value objects.

## ✔ Correto

```kotlin
data class CreateSampleInputDTO(
    val name: String,
    val value: String
)
```

```kotlin
data class CreateSampleOutputDTO(
    val id: String,
    val name: String
)
```

## ❌ Incorreto

```kotlin
data class CreateSampleInputDTO(
    @field:NotBlank
    val name: String
)
```

---

# Portas

Portas representam contratos entre a camada Application e componentes externos.

Toda comunicação com infraestrutura deve ocorrer através de portas.

As seguintes regras devem ser respeitadas:

- Portas devem ser interfaces.
- Portas devem representar comportamento.
- Não devem possuir implementação.
- Devem permanecer organizadas por responsabilidade.
- Assinaturas de repositórios trabalham exclusivamente com tipos do domínio: `Entity`, `ValueObject`. Nunca expõem tipos JPA.

## ✔ Correto

```kotlin
interface SampleRepository {
    fun save(sample: SampleEntity): SampleEntity
    fun findById(id: IdValueObject): SampleEntity?
}
```

```kotlin
interface PasswordEncoder {
    fun encode(value: String): String
    fun matches(raw: String, encoded: String): Boolean
}
```

## ❌ Incorreto

```kotlin
class SampleRepository { }
```

---

## ❌ Incorreto

```kotlin
@Service
class SampleService {
    fun create() {}
    fun update() {}
    fun delete() {}
}
```

---

# Fluxo de Dados

A camada Application é responsável por coordenar o fluxo entre as portas e o domínio.

O fluxo recomendado é:

```text
Input DTO
      ↓
Caso de Uso
      ↓
Value Objects
      ↓
Entidade
      ↓
Porta
      ↓
Output DTO
```

As seguintes regras devem ser respeitadas:

- O caso de uso recebe um DTO de entrada.
- O caso de uso instancia a entidade diretamente via construtor público.
- O caso de uso utiliza apenas portas para acessar infraestrutura.
- O retorno deve ocorrer através de um DTO de saída.
- A conversão da entidade para Output DTO deve ser feita por um método privado `toOutput(entity)`.

## ✔ Correto

```kotlin
override fun execute(input: CreateSampleInputDTO): CreateSampleOutputDTO {
    val sample = SampleEntity(
        id = UUID.randomUUID().toString(),
        name = input.name,
        value = input.value
    )

    val saved = sampleRepository.save(sample)

    return toOutput(saved)
}

private fun toOutput(sample: SampleEntity): CreateSampleOutputDTO =
    CreateSampleOutputDTO(
        id = sample.id.asString(),
        name = sample.name.asString()
    )
```

---

# Validação de Value Objects

Sempre que o input do use case contiver um campo que será usado **isoladamente** — como um ID para busca ou um campo para verificação de unicidade —, use `getValueResultOrThrow()` diretamente.

Quando todos os campos necessários estão disponíveis e a entidade será criada via construtor **sem verificação de negócio prévia**, não valide os value objects manualmente. O construtor já chama `validateOrThrow` internamente.

## ✔ Correto — busca por ID

```kotlin
val taskIdValue = IdValueObject.of(input.taskId).getValueResultOrThrow()
val task = taskRepository.findById(taskIdValue) ?: throw TaskNotFoundException()
```

## ✔ Correto — verificação de unicidade antes da criação

```kotlin
val usernameResult = UsernameValueObject.of(input.username).getValueResultOrThrow()
if (userRepository.existsByUsername(usernameResult)) {
    throw UsernameAlreadyExistsException()
}
val user = UserEntity(id = UUID.randomUUID().toString(), username = input.username, password = encodedPassword)
```

## ❌ Incorreto — validação duplicada desnecessária

```kotlin
val taskNameResult = TaskNameValueObject.of(input.taskName).getValueResultOrThrow()
val task = TaskEntity(id = UUID.randomUUID().toString(), userId = input.userId, taskName = input.taskName, finished = false)
```

---

# Conversões

A camada Application é responsável pela conversão entre DTOs e objetos do domínio.

As seguintes regras devem ser respeitadas:

- DTOs nunca devem atravessar para a camada Domain.
- Entidades nunca devem ser utilizadas diretamente pela camada Presentation.
- A conversão de entidade para Output DTO deve ser centralizada em um método privado `toOutput` dentro da implementação.
- Nunca repita o mapeamento inline nem exponha entidades de domínio fora da implementação.

## ✔ Correto

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

## ❌ Incorreto

```kotlin
return task
```

---

# Exceções de Caso de Uso

Exceções específicas de cada domínio de caso de uso ficam em `exception/` ao lado de `interfaces/` e `implementation/`.

As seguintes regras devem ser respeitadas:

- Estendem `RuntimeException` diretamente (não `DomainException`).
- Carregam uma mensagem fixa.
- Devem possuir nomes descritivos.

## ✔ Correto

```kotlin
class TaskNotFoundException : RuntimeException("Task not found")
class TaskAccessDeniedException : RuntimeException("Access denied to this task")
class UsernameAlreadyExistsException : RuntimeException("Username already exists")
```

---

# Dependências

A camada Application pode depender apenas da camada Domain e de contratos definidos pela própria Application.

Não é permitido depender de:

- Controllers;
- Requests;
- Responses;
- Models de persistência;
- Repositórios concretos;
- Frameworks de persistência;
- Componentes da camada External.

---

# Resumo das Convenções

Toda implementação da camada Application deve respeitar os seguintes princípios:

- Cada caso de uso representa uma única operação da aplicação.
- Todo caso de uso deve possuir interface e implementação.
- Implementações devem utilizar o sufixo `Impl`.
- DTOs devem utilizar `data class`.
- DTOs não possuem regras de negócio.
- Toda comunicação externa deve ocorrer através de portas.
- A camada Application coordena o fluxo da aplicação, executa validações que dependam de recursos externos e orquestra a execução das regras de negócio da camada Domain.
- Entidades pertencem ao Domain.
- Componentes da External nunca devem ser acessados diretamente.
- O retorno dos casos de uso deve ocorrer através de DTOs de saída.
- A conversão para Output DTO deve ser centralizada em um método privado `toOutput`.
