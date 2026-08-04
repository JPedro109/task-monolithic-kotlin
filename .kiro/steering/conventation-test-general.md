---
inclusion: fileMatch
fileMatchPattern: "**/src/test/**"
---

# Convenções de Testes

# Layout

```text
src/test/kotlin/com/jpmns/sample/
├── core/
│   ├── application/usecase/       # Testes unitários dos casos de uso (MockK, sem contexto Spring)
│   │   ├── sample/
│   ├── controller/                # Testes unitários dos controllers (@WebMvcTest)
│   │   ├── SampleControllerTest.kt
│   ├── domain/                    # Testes unitários de entidades e value objects
│   │   ├── sample/
│   ├── external/                  # Testes unitários de adaptadores e mappers
│   │   ├── persistence/
│   │   │   ├── dao/
│   │   │   ├── mapper/
│   │   │   └── repository/
│   │   └── security/
│   └── fixture/                   # Fixtures compartilhados
│       ├── SampleFixture.kt
├── integration/                   # Testes de integração dos controllers (Testcontainers PostgreSQL)
│   ├── common/
│   │   ├── abstracts/IntegrationTestBase.kt
│   │   ├── container/PostgresContainerConfig.kt
│   │   └── sql/SqlCreateSeed.kt
│   ├── SampleIntegrationTest.kt
└── shared/
    └── security/
        ├── WithJwtTokenMock.kt
        └── factory/WithMockJwtTokenSecurityContextFactory.kt
```

# Geral

- Use ou crie fixtures existentes para construir dados de teste. Nunca instancie entidades de domínio inline dentro dos testes. Nunca use valores aleatórios (`UUID.randomUUID()`, `Math.random()`, etc.) — sempre fixture.
- Todo método de teste **deve** usar a sintaxe de backticks do Kotlin com uma frase descritiva em inglês no formato `` `should [resultado esperado] when [condição]` ``. Nunca use camelCase para nomear métodos de teste.

```kotlin
// CORRETO
@Test
fun `should return 201 with sample data when input is valid`() { }

// INCORRETO
@Test
fun shouldReturn201WithSampleDataWhenInputIsValid() { }
```

- Use `assertThat` do AssertJ para asserções e `assertThatThrownBy` para verificar exceções.
- Verifique interações com `verify { mock.method(...) }` e `verify(exactly = 0) { mock.method(...) }`.
- Testes devem ser ordenados: sucesso (happy path) primeiro, corner cases depois, exceções/erros por último.
- Use `@BeforeEach` e `@AfterEach` quando necessário para setup e teardown compartilhados.
- Siga o padrão **AAA (Arrange → Act → Assert)**, separando cada etapa com uma linha em branco, nunca utilize comentários para separação das etapas.
- Dentro do `Arrange`, separe a criação de variáveis dos stubs `every { ... }` com uma linha em branco:

```kotlin
val sample = SampleFixture.aSample()
val userId = user.id
val sampleName = sample.sampleName
val input = CreateSampleInputDTO(sampleName = sampleName.asString())

every { sampleRepository.save(any()) } returns sample

val output = useCase.execute(input)

assertThat(output.id).isNotNull()
assertThat(output.sampleName).isEqualTo(sampleName.asString())
verify { sampleRepository.save(any()) }
```

- Sempre declare o fixture primeiro e depois extraia **cada campo que for utilizado** em variáveis separadas — nunca acesse propriedades do fixture diretamente no meio do código do teste:

```kotlin
// CORRETO
val sample = SampleFixture.aSample()
val sampleName = sample.sampleName
val sampleId = sample.id

// INCORRETO — acesso inline sem extração
every { sampleRepository.findById(sample.id) } returns sample
assertThat(output.sampleName).isEqualTo(sample.sampleName.asString())
```

- Cubra o máximo de cenários possível. Sempre siga a estrutura de layout definida para cada tipo de teste.

---

# Fixtures

Fixtures são responsáveis por centralizar a criação de objetos utilizados durante os testes.

Seu objetivo é evitar duplicação de código, facilitar a leitura dos testes e manter um único ponto de manutenção para dados de teste.

Todos os fixtures compartilhados devem permanecer no pacote `core/fixture`.

As seguintes regras devem ser respeitadas:

- Devem ser implementados como `object` Kotlin.
- Não devem possuir estado.
- Devem disponibilizar métodos para criação dos objetos.
- Devem fornecer valores padrão válidos para todos os atributos obrigatórios.
- Devem permitir a criação rápida de objetos válidos para diferentes cenários de teste.
- Devem ser reutilizados por todos os testes da aplicação sempre que possível.
- Constantes devem ser declaradas como `private const val` no `companion object` ou diretamente no `object`.

## ✔ Correto

```kotlin
object SampleFixture {
    private const val DEFAULT_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    private const val DEFAULT_USER_ID = "b2c3d4e5-f6a7-8901-bcde-f12345678901"
    private const val DEFAULT_TASK_NAME = "Sample Sample"
    private const val DEFAULT_FINISHED = false

    fun aSample(): SampleEntity =
        SampleEntity(
            id = DEFAULT_ID,
            userId = DEFAULT_USER_ID,
            sampleName = DEFAULT_TASK_NAME,
            finished = DEFAULT_FINISHED
        )
}
```

## ✔ Utilização

```kotlin
@Test
fun `should create sample successfully`() {
    val sample = SampleFixture.aSample()
    ...
}
```

## ❌ Incorreto

```kotlin
@Test
fun `should create sample successfully`() {
    val sample = SampleEntity(
        id = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        userId = "b2c3d4e5-f6a7-8901-bcde-f12345678901",
        sampleName = "Sample Sample",
        finished = false
    )
    ...
}
```

---

# WireMock

Toda integração HTTP com sistemas externos deve ser testada utilizando **WireMock**.

As seguintes regras devem ser respeitadas:

- Nunca realizar chamadas HTTP para ambientes reais durante os testes.
- Toda API externa deve possuir uma classe responsável por centralizar seus stubs.
- Os testes nunca devem utilizar `stubFor(...)` diretamente.
- Cada método da classe de stub deve representar um cenário de teste.
- Devem existir cenários de sucesso, erro, timeout e indisponibilidade do serviço.
- Nunca utilizar MockK para simular clientes HTTP concretos.

Estrutura recomendada:

```text
src/test/kotlin/
└── integration/
    └── common/
        ├── container/
        │   └── WireMockConfig.kt
        └── wiremock/
            └── SampleApiStub.kt
```

## ✔ Correto

```kotlin
object SampleApiStub {

    fun sampleExists(id: String) {
        stubFor(
            get(urlEqualTo("/samples/$id"))
                .willReturn(okJson("""{"exists": true}"""))
        )
    }

    fun sampleDoesNotExist(id: String) {
        stubFor(
            get(urlEqualTo("/samples/$id"))
                .willReturn(okJson("""{"exists": false}"""))
        )
    }

    fun serviceUnavailable() {
        stubFor(
            get(urlPathMatching("/samples/.*"))
                .willReturn(serverError())
        )
    }
}
```

## ❌ Incorreto

```kotlin
@Test
fun `should create sample`() {
    stubFor(get(urlEqualTo("/samples/1"))
        .willReturn(okJson("""{"exists": false}""")))
    ...
}
```

## ❌ Incorreto

```kotlin
every { sampleApiClient.exists(any()) } returns false
```
