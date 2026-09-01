---
inclusion: fileMatch
fileMatchPattern: "**/*.kt"
---

# Convenções de Código

# Nomenclatura

A nomenclatura dos componentes deve ser consistente, descritiva e refletir claramente sua responsabilidade.

As seguintes convenções devem ser adotadas:

- Classes devem utilizar **PascalCase**.
- Métodos e propriedades devem utilizar **camelCase**.
- Constantes devem utilizar **UPPER_SNAKE_CASE**.
- Pacotes devem utilizar apenas letras minúsculas.
- Interfaces devem representar comportamentos ou contratos.
- Interfaces **não devem** utilizar o prefixo `I`.
- Casos de uso são classes concretas, sem interface e sem sufixo `Impl`.
- Adaptadores de infraestrutura (implementações de portas) utilizam o sufixo `Adapter` (ou um nome que descreva a tecnologia, ex: `JpaSampleRepository`).
- Classes concretas devem possuir nomes que representem claramente sua responsabilidade.

Evite abreviações desnecessárias e nomes genéricos que não expressem claramente a responsabilidade do componente.

### ✔ Correto

```kotlin
class CreateSampleUseCase

interface PasswordEncoder

class TokenAdapter : Token

class SampleRepositoryAdapter : SampleRepository
```

### ❌ Incorreto

```kotlin
class SampleManager

class Helper

class Util

class Processor
```

---

# Constantes

Valores reutilizados ou que representem regras estáticas devem ser declarados como constantes.

Não é permitido utilizar valores literais ("magic numbers" ou "magic strings") quando seu significado puder ser representado por uma constante nomeada.

Constantes devem ser declaradas no `companion object` com `const val` para primitivos ou `private val` para tipos complexos.

### ✔ Correto

```kotlin
companion object {
    private const val MAX_SAMPLE_NAME_LENGTH = 255
}
```

### ❌ Incorreto

```kotlin
if (sampleName.length > 255) {
    ...
}
```

---

# Imutabilidade e uso de `val`

Sempre prefira `val` para declarar variáveis. Use `var` apenas em casos extremos onde a reatribuição seja estritamente necessária e não haja como modelar o problema com imutabilidade.

### ✔ Correto

```kotlin
val sample = sampleRepository.findById(sampleIdValue) ?: throw SampleNotFoundException()
val output = toOutput(sample)
```

### ❌ Incorreto

```kotlin
var sample = sampleRepository.findById(sampleIdValue) ?: throw SampleNotFoundException()
var output = toOutput(sample)
```

---

# Named Parameters

Sempre use named parameters ao instanciar classes, chamar construtores ou invocar funções com mais de um argumento. Isso melhora a legibilidade e evita erros de posicionamento.

### ✔ Correto

```kotlin
val input = CreateSampleInputDTO(userId = userId.asString(), sampleName = sampleName.asString())
val sample = SampleEntity(id = UUID.randomUUID().toString(), userId = input.userId, sampleName = input.sampleName, finished = false)
```

### ❌ Incorreto

```kotlin
val input = CreateSampleInputDTO(userId.asString(), sampleName.asString())
val sample = SampleEntity(UUID.randomUUID().toString(), input.userId, input.sampleName, false)
```

Não há exceções a esta regra.

---

# Formatação

Todo o código deve seguir um único padrão de formatação.

A formatação deve garantir:

- Consistência visual;
- Facilidade de leitura;
- Padronização entre todos os projetos.

A formatação deve ser automatizada por **ktlint** (indentação de 4 espaços, sem tabs, máximo de 120 caracteres por linha).

Além da formatação automática, o código deve ser organizado em **blocos lógicos**, utilizando linhas em branco para separar etapas distintas de uma operação.

Cada bloco deve representar uma fase claramente identificável da execução, como:

- Validação;
- Conversão de dados;
- Execução da regra de negócio;
- Persistência;
- Publicação de eventos;
- Retorno do resultado.

### ✔ Correto

```kotlin
fun execute(input: CreateSampleInputDTO): CreateSampleOutputDTO {
    val sampleNameResult = SampleNameValueObject.of(input.sampleName).getValueResultOrThrow()

    if (sampleRepository.existsBySampleName(sampleNameResult)) {
        throw SampleNameAlreadyExistsException()
    }

    val sample = SampleEntity(
        id = UUID.randomUUID().toString(),
        sampleName = input.sampleName
    )

    val saved = sampleRepository.save(sample)

    return toOutput(saved)
}
```

### ❌ Incorreto

```kotlin
fun execute(input: CreateSampleInputDTO): CreateSampleOutputDTO {
    val sampleNameResult = SampleNameValueObject.of(input.sampleName).getValueResultOrThrow()
    if (sampleRepository.existsBySampleName(sampleNameResult)) {
        throw SampleNameAlreadyExistsException()
    }
    val sample = SampleEntity(id = UUID.randomUUID().toString(), sampleName = input.sampleName)
    val saved = sampleRepository.save(sample)
    return toOutput(saved)
}
```

---

# Ordenação dos Membros

Os membros de uma classe devem seguir uma ordem lógica e consistente.

A ordem recomendada é:

1. Campos de instância / propriedades;
2. Construtores / bloco `init`;
3. Métodos públicos;
4. Métodos protegidos;
5. Métodos privados;
6. `companion object` (constantes).

Métodos auxiliares devem permanecer próximos dos métodos que os utilizam, mas sempre métodos privados devem ficar abaixo dos públicos.

### ✔ Correto

```kotlin
@Service
class CreateSampleUseCase(
    private val sampleRepository: SampleRepository
) {

    fun execute(input: CreateSampleInputDTO): SampleOutputDTO {
        val sample = SampleEntity(
            id = UUID.randomUUID().toString(),
            userId = input.userId,
            sampleName = input.sampleName,
            finished = false
        )

        val saved = sampleRepository.save(sample)

        return toOutput(saved)
    }

    private fun toOutput(sample: SampleEntity): SampleOutputDTO =
        SampleOutputDTO(
            id = sample.id.asString(),
            userId = sample.userId.asString(),
            sampleName = sample.sampleName.asString(),
            finished = sample.finished,
            createdAt = sample.createdAt
        )
}
```

---

# Organização dos Métodos

Cada método deve possuir apenas uma responsabilidade.

Sempre que possível:

- Métodos públicos devem representar operações de alto nível.
- Detalhes de implementação devem ser extraídos para métodos privados.
- Métodos devem ser pequenos e objetivos.
- Um método não deve executar múltiplas responsabilidades.

---

# Injeção de Dependência

Toda dependência entre componentes deve ser realizada por meio de injeção de dependência via construtor primário.

As seguintes regras devem ser respeitadas:

- A injeção deve ocorrer exclusivamente pelo construtor primário.
- Dependências obrigatórias devem ser imutáveis (`private val`).
- Instanciações diretas devem ser evitadas para componentes gerenciados pela aplicação.
- Dependências devem ser representadas por contratos sempre que possível.
- Nunca utilizar `@Autowired` em campo ou setter.

### ✔ Correto

```kotlin
@Service
class CreateSampleUseCase(
    private val sampleRepository: SampleRepository,
    private val passwordEncoder: PasswordEncoder
) {
    ...
}
```

### ❌ Incorreto

```kotlin
@Service
class CreateSampleUseCase {
    @Autowired
    private lateinit var sampleRepository: SampleRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
}
```

---

# Logging

Logs devem registrar informações relevantes para auditoria, monitoramento e diagnóstico da aplicação.

As seguintes práticas devem ser adotadas:

- Registrar apenas eventos relevantes para o funcionamento da aplicação.
- Utilizar o nível de log apropriado (`TRACE`, `DEBUG`, `INFO`, `WARN` ou `ERROR`).
- Utilizar mensagens objetivas e descritivas.
- Nunca registrar dados sensíveis, como senhas, tokens, chaves ou informações pessoais.
- Nunca utilizar logs como mecanismo de controle de fluxo.
- Nunca registrar identificadores de rastreamento (Trace ID, Span ID, Request ID, etc.), pois essas informações já são propagadas automaticamente pelo Baggage Field.
- Nunca registrar a mesma exceção em múltiplas camadas da aplicação.
- Utilizar string interpolation do Kotlin (`"$variavel"`) nos logs — não usar placeholders `{}` do SLF4J.
- Registrar exceções apenas no ponto em que elas forem efetivamente tratadas ou onde houver contexto relevante a ser acrescentado.
- Logs de operações devem ser realizados na camada de Presentation, registrando as entradas (requests) e saídas (responses) quando apropriado.
- Logs de exceções devem ser realizados exclusivamente pelo Global Exception Handler.
- A única exceção ocorre quando uma exceção de infraestrutura precisa ser traduzida para uma exceção da aplicação ou do domínio. Nesse caso, a exceção original deve ser registrada antes da tradução.
- Utilizar exclusivamente o Logger do SLF4J para registro de logs.
- Não utilizar `println`, `System.out`, `System.err` ou `printStackTrace()`.

### ✔ Correto

```kotlin
log.info("Creating sample - request: $request")
```

```kotlin
log.info("Creating sample - response: $response")
```

```kotlin
log.error("Resource not found: ${ex.message}", ex)
```

### ❌ Incorreto

```kotlin
log.info("User $username authenticated using password $password")
```

```kotlin
log.info("Sample created: " + sampleName)
```

```kotlin
log.info("RequestId=$requestId TraceId=$traceId Sample created.")
```

- A instância do logger sempre deve ser criada no `companion object`.

### Exemplo

```kotlin
@RestController
@RequestMapping("/api/v1/samples")
class SampleController(
    private val createSampleUseCase: CreateSampleUseCase
) : SampleControllerDoc {

    companion object {
        private val log = LoggerFactory.getLogger(SampleController::class.java)
    }
}
```

---

# Configurações

As configurações da aplicação devem permanecer centralizadas e desacopladas da lógica de negócio.

As seguintes regras devem ser respeitadas:

- Configurações devem ser externalizadas.
- Valores configuráveis não devem ser codificados diretamente na aplicação.
- Configurações devem ser agrupadas conforme sua responsabilidade.
- Configurações tipadas devem utilizar `@ConfigurationProperties`.
- Classes de configuração devem representar apenas um grupo específico de propriedades.
- Regras de negócio não devem depender diretamente da origem das configurações.
- Componentes devem acessar apenas as configurações necessárias para sua responsabilidade.
- Classes de configuração sempre devem ter o sufixo `ConfigProperties`.
- Quando um prefixo contém subgrupos aninhados no YAML, use uma `data class` interna para o subgrupo.
- Todas as classes `@ConfigurationProperties` devem ser registradas centralmente com `@EnableConfigurationProperties` na classe principal da aplicação.

### ✔ Correto

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
```

```kotlin
@Service
class TokenAdapter(
    private val properties: SecurityConfigProperties
) : Token {
    ...
}
```

### ❌ Incorreto

```kotlin
class TokenAdapter {
    companion object {
        private const val ISSUER = "My Application"
        private const val EXPIRATION = 86400000L
    }
}
```

### ❌ Incorreto

```kotlin
@Service
class TokenAdapter {
    @Value("\${security.jwt.secret}")
    private lateinit var secret: String

    @Value("\${security.jwt.access-token-expiration-ms}")
    private var expiration: Long = 0
}
```

---

# Imports

As instruções de importação devem permanecer organizadas e conter apenas dependências utilizadas pela classe.

As seguintes regras devem ser respeitadas:

- Imports não utilizados devem ser removidos.
- Imports curinga (`*`) não são permitidos.
- A organização dos imports deve seguir o padrão definido pelo ktlint.

### ✔ Correto

```kotlin
import java.time.Instant
import java.util.UUID

import org.springframework.stereotype.Service
```

### ❌ Incorreto

```kotlin
import java.util.*
```

---

# Análise Estática

Todo o código deve ser validado por ferramentas de análise estática antes de sua integração ao projeto.

As seguintes regras devem ser respeitadas:

- O projeto deve possuir configurações padronizadas de **ktlint** (formatação) e **detekt** (análise estática).
- Todo o código deve estar em conformidade com as regras definidas por essas configurações.
- Violações não devem ser ignoradas ou desabilitadas sem justificativa documentada.
- Política de zero warnings em ambas as ferramentas.
- Alterações no conjunto de regras devem ser revisadas e aprovadas pela equipe.

A configuração das ferramentas de análise estática faz parte da arquitetura do projeto e deve ser compartilhada entre todos os desenvolvedores.
