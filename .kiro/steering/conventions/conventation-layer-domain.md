---
inclusion: always
---

# Convenções da Camada Domain

A camada **Domain** representa o núcleo da aplicação e concentra exclusivamente as regras de negócio.

Seu objetivo é modelar o domínio de forma independente de frameworks, bancos de dados, protocolos de comunicação ou qualquer outro detalhe técnico.

Toda implementação nesta camada deve priorizar:

- Encapsulamento das regras de negócio;
- Baixo acoplamento;
- Alta coesão;
- Imutabilidade sempre que possível;
- Independência de detalhes técnicos.

A camada Domain **não pode depender** das camadas de Application, External ou Presentation.

---

# Estrutura

A organização da camada Domain deve refletir os conceitos do domínio.

A estrutura recomendada é:

```text
domain/
├── common/
│   ├── abstracts/
│   │   └── Entity.kt
│   ├── exception/
│   │   └── DomainException.kt
│   └── valueobject/
│       └── IdValueObject.kt
└── sample/
    ├── SampleEntity.kt
    ├── exception/
    │   └── InvalidSampleNameException.kt
    └── valueobject/
        ├── SampleNameValueObject.kt
        └── SampleValueObject.kt
```

As seguintes regras devem ser respeitadas:

- Cada agregado deve possuir seu próprio pacote.
- Cada entidade deve possuir um único arquivo.
- Cada Value Object deve possuir um único arquivo.
- Exceções específicas devem permanecer dentro do agregado correspondente.
- Componentes compartilhados devem permanecer em `domain/common`.

---

# Entidades

Entidades representam objetos do domínio que possuem identidade própria durante todo o seu ciclo de vida.

Toda entidade deve herdar da classe abstrata `Entity`.

A classe base é responsável por centralizar comportamentos comuns entre todas as entidades.

Ela deve fornecer:

- Identificador (`IdValueObject`);
- Data de criação (`Instant`);
- Método `validateOrThrow()`;
- Igualdade baseada na identidade.

## Estrutura de entidade de domínio

Entidades seguem este esquema:

1. **Campos declarados no corpo** (`val` para imutáveis, `var private set` para mutáveis).
2. **Construtor primário**: recebe todos os campos como primitivos/strings. Campos opcionais (`createdAt`, `updatedAt`) têm valor padrão `null`.
3. **Bloco `init`**: cria os value objects a partir dos parâmetros primitivos, chama `validateOrThrow` e atribui os campos do corpo.
4. **Métodos de negócio** (`update*`, `markAs*`): recebem primitivos/strings, recriam o value object via `of(...).getValueResultOrThrow()` e atualizam o campo.

## ✔ Correto

```kotlin
class SampleEntity(
    id: String,
    sampleName: String,
    createdAt: Instant? = null,
    val updatedAt: Instant? = null
) : Entity(id, createdAt) {

    var sampleName: SampleNameValueObject
        private set
    val userId: IdValueObject

    init {
        val sampleNameResult = SampleNameValueObject.of(sampleName)

        val results = listOf(userIdResult, sampleNameResult)
        validateOrThrow(results)

        this.sampleName = sampleNameResult.getValueResult()
    }

    fun updateSampleName(sampleName: String) {
        this.sampleName = SampleNameValueObject.of(sampleName).getValueResultOrThrow()
    }
}
```

## ✔ Classe base

```kotlin
abstract class Entity(id: String, createdAt: Instant?) {

    val id: IdValueObject = IdValueObject.of(id).getValueResultOrThrow()
    val createdAt: Instant = createdAt ?: Instant.now()

    protected fun validateOrThrow(results: List<Result<*>>) {
        val errors = results
            .filter { it.isFailure }
            .map { it.getError() as DomainException }

        if (errors.isNotEmpty()) {
            throw DomainException.with(errors)
        }
    }
}
```

As seguintes regras devem ser respeitadas:

- Toda entidade deve herdar de `Entity`.
- Entidades representam comportamento, não apenas dados.
- Não devem possuir setters públicos.
- Alterações de estado devem ocorrer através de métodos de negócio.
- Toda entidade deve validar seu estado durante sua construção via `validateOrThrow`.
- Nenhuma entidade pode permanecer inválida após sua criação.

## ✔ Correto

```kotlin
sample.updateSampleName("New name")
```

## ❌ Incorreto

```kotlin
sample.sampleName = SampleNameValueObject.of("New name")
```

## ❌ Incorreto

```kotlin
@Entity
@Table(name = "sample")
class SampleEntity { }
```

---

# Objetos Base

Objetos reutilizáveis entre agregados devem permanecer em `domain/common`.

Estrutura recomendada:

```text
common/
├── abstracts/
│   └── Entity.kt
├── exception/
│   └── DomainException.kt
└── valueobject/
    └── IdValueObject.kt
```

As abstrações base recomendadas são:

- Entity
- DomainException
- IdValueObject

Novas abstrações somente devem ser adicionadas quando representarem conceitos reutilizáveis do domínio.

---

# Value Objects

Value Objects representam conceitos definidos exclusivamente pelo seu valor.

Todo Value Object deve encapsular apenas uma informação do domínio.

As seguintes regras devem ser respeitadas:

- Devem ser imutáveis.
- Devem validar seu próprio estado.
- Não devem possuir setters.
- Não devem possuir identidade.
- Devem implementar igualdade baseada no valor.
- Devem ser responsáveis pelas regras referentes ao valor encapsulado.
- Construtor `private` — instanciação exclusiva via `of(...)` no companion object.
- Factory `of(value: String)` retorna `Result<VO>`.
- O valor primitivo é exposto por um método de conversão correspondente ao seu tipo (`asString()`, `asInt()`, `asLong()`, etc.). Nunca use `getValue()`.
- Sobrescrevem `equals` e `hashCode` com base no valor retornado pelo método de conversão.

## ✔ Correto

```kotlin
class SampleNameValueObject private constructor(private val value: String) {

    fun asString(): String = value

    override fun equals(other: Any): Boolean {
        if (this === other) return true

        if (other !is SamplevalueValueObject) return false

        return asString() == other.asString()
    }

    override fun hashCode(): Int = value.hashCode()

    companion object {
        private const val MAX_LENGTH = 255

        fun of(value: String): Result<SamplevalueValueObject> {
            if (value.isBlank() || value.length > MAX_LENGTH) {
                return Result.fail(InvalidSampleNameException())
            }

            return Result.success(SampleNameValueObject(name))
        }
    }
}
```

## ❌ Incorreto

```kotlin
class SampleNameValueObject(var name: String) {
    fun setValue(name: String) {
        this.name = name
    }
}
```

---

# Result

A criação de objetos do domínio deve ocorrer, preferencialmente, através do objeto `Result`.

O `Result<T>` permite representar sucesso ou falha sem depender do lançamento imediato de exceções.

Ele deve ser utilizado principalmente na criação de Value Objects.

## ✔ Correto

```kotlin
val result = SampleNameValueObject.of(value)

if (result.isFailure) {
    throw result.getError()
}

val sample = result.getValueResult()
```

## ✔ Correto

```kotlin
val sample = SampleNameValueObject.of(value).getValueResultOrThrow()
```

## ❌ Incorreto

```kotlin
val sample = SampleNameValueObject(value)  // construtor direto
```

---

# Validações

Toda regra de validação pertencente ao domínio deve permanecer na camada Domain.

As seguintes regras devem ser respeitadas:

- Entidades devem proteger suas invariantes.
- Value Objects devem validar seu próprio estado.
- Nenhuma entidade pode existir em estado inválido.
- Regras de negócio não devem ser implementadas fora da camada Domain.

---

# Exceções

Exceções da camada Domain representam violações das regras de negócio.

Todas devem herdar da exceção base `DomainException`.

As seguintes regras devem ser respeitadas:

- Devem representar apenas regras de negócio.
- Devem possuir nomes descritivos.
- Devem ter uma mensagem customizada interna.
- Não devem representar detalhes técnicos.
- Não devem depender de infraestrutura.
- Nunca lance `RuntimeException` ou `IllegalArgumentException` no domínio.

## ✔ Correto

```kotlin
class SampleNameException : DomainException(
    "Sample name must not be blank and must have at most 255 characters"
)
```

## ❌ Incorreto

```kotlin
class SQLException : RuntimeException()
```

---

# Serviços de Domínio

Serviços de domínio devem existir apenas quando um comportamento não pertencer naturalmente a uma única entidade ou Value Object.

As seguintes regras devem ser respeitadas:

- Devem representar regras de negócio.
- Devem ser stateless.
- Não devem depender de infraestrutura.
- Devem operar exclusivamente sobre objetos do domínio.

## ✔ Correto

```kotlin
class SampleValidationService {

    fun validate(sample: SampleEntity) {
        ...
    }
}
```

## ❌ Incorreto

```kotlin
class SampleService(
    private val repository: SampleRepository
)
```

---

# Dependências

A camada Domain deve permanecer completamente independente de detalhes técnicos.

Não é permitido utilizar:

- Spring Framework;
- Jakarta EE;
- JPA;
- Hibernate;
- Jackson;
- OpenAPI;
- SLF4J;
- APIs externas;
- Clientes HTTP;
- Frameworks de persistência.

## ✔ Correto

```kotlin
class SampleEntity(id: String, name: String) : Entity(id, null)
```

## ❌ Incorreto

```kotlin
@Entity
class SampleEntity { }
```

```kotlin
@Slf4j
class SampleEntity { }
```

---

# Resumo das Convenções

Toda implementação da camada Domain deve respeitar os seguintes princípios:

- O domínio não depende de nenhuma outra camada.
- Entidades devem herdar de `Entity`.
- Identificadores devem utilizar `IdValueObject`.
- Value Objects devem ser imutáveis.
- Value Objects devem ser criados através de `Result<T>` via companion object `of(...)`.
- Toda entidade deve validar seu estado através de `validateOrThrow()`.
- Regras de negócio pertencem exclusivamente ao Domain.
- Exceções devem herdar de `DomainException`.
- Não é permitido utilizar anotações ou dependências de frameworks.
- O domínio deve permanecer independente de detalhes técnicos.
