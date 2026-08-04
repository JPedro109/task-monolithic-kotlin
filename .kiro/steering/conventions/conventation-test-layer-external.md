---
inclusion: always
---

# Testes de External

Os testes de integração de adaptadores têm como objetivo validar o comportamento dos componentes da camada **External** que integram a aplicação com bibliotecas, frameworks ou serviços externos.

Diferentemente dos testes de persistência, esses testes não validam acesso ao banco de dados. Seu objetivo é garantir que a implementação concreta do adaptador funcione corretamente utilizando suas dependências reais.

---

## Estrutura

Esses testes devem permanecer organizados conforme o componente testado.

Exemplo:

```text
src/test/kotlin/
└── core/
    └── external/
        └── security/
            ├── TokenAdapterTest.kt
            └── PasswordEncoderAdapterTest.kt
```

---

## Convenções

As seguintes regras devem ser respeitadas:

- Cada adaptador deve possuir sua própria classe de teste.
- Os adaptadores devem ser instanciados manualmente no `@BeforeEach`.
- Não deve ser utilizado contexto Spring.
- Não devem ser utilizados mocks (MockK) das bibliotecas externas.
- Devem ser utilizadas implementações reais das dependências externas.
- Configurações necessárias para os testes devem ser criadas manualmente (ex: `SecurityConfigProperties` construído programaticamente).
- Constantes utilizadas durante os testes devem ser declaradas como `private const val` no `companion object`.
- Usar backticks para nomes de testes.
- Os testes devem utilizar AssertJ.
- Os testes devem seguir o padrão AAA (Arrange → Act → Assert).

---

## Cenários obrigatórios

Todo adaptador deve possuir testes para:

- Fluxo de sucesso.
- Cenários de erro.
- Valores inválidos.
- Valores limites.
- Comportamentos específicos da biblioteca integrada.
- Exceções esperadas.

Os cenários específicos devem refletir a responsabilidade do adaptador.

Exemplos:

- Adaptadores de Token:
    - geração de access token;
    - geração de refresh token;
    - validação com subject correto;
    - tokens expirados;
    - tokens malformados;
    - tokens assinados com segredo diferente.

- Adaptadores de PasswordEncoder:
    - encode retorna hash diferente do raw;
    - hashes distintos para a mesma senha;
    - `matches` retorna `true` para senha correta;
    - `matches` retorna `false` para senha errada.

---

## ✔ Correto

```kotlin
class TokenAdapterTest {

    private lateinit var tokenAdapter: TokenAdapter

    @BeforeEach
    fun setUp() {
        tokenAdapter = TokenAdapter(buildProperties(SECRET, ACCESS_EXPIRATION_MS, REFRESH_EXPIRATION_MS))
    }

    @Test
    fun `should generate a valid access token`() {
        val user = UserFixture.aUser()
        val sub = user.id.asString()

        val token = tokenAdapter.generateAccessToken(sub)

        assertThat(token).isNotBlank()
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

    @Test
    fun `should throw InvalidTokenException when token is malformed`() {
        assertThatThrownBy { tokenAdapter.tokenValidation("not.a.valid.token") }
            .isInstanceOf(InvalidTokenException::class.java)
    }

    private fun buildProperties(secret: String, accessMs: Long, refreshMs: Long): SecurityConfigProperties =
        SecurityConfigProperties(
            jwt = SecurityConfigProperties.Jwt(
                secret = secret,
                accessTokenExpirationMs = accessMs,
                refreshTokenExpirationMs = refreshMs
            )
        )

    companion object {
        private const val SECRET = "test-secret-key-must-be-at-least-32-chars!!"
        private const val ACCESS_EXPIRATION_MS = 900_000L
        private const val REFRESH_EXPIRATION_MS = 604_800_000L
    }
}
```

---

## ❌ Incorreto

```kotlin
@SpringBootTest
class TokenAdapterTest { }
```

---

## ❌ Incorreto

```kotlin
@ExtendWith(MockKExtension::class)
class TokenAdapterTest { }
```

---

## ❌ Incorreto

```kotlin
@MockK
private lateinit var jwtLibrary: JwtParser
```

---

## Boas práticas

Sempre que possível:

- Instanciar manualmente o componente em teste.
- Utilizar implementações reais das bibliotecas externas.
- Validar os cenários de sucesso e de falha.
- Um cenário por teste.
- Evitar dependência entre testes.
- Nomear os testes com backticks seguindo o padrão `should [resultado esperado] when [condição]`.

---

## Resumo das Convenções

Todo teste de integração de adaptadores deve respeitar os seguintes princípios:

- Cada adaptador possui sua própria classe de teste.
- Não utilizar contexto Spring.
- Não utilizar MockK.
- Utilizar implementações reais das dependências externas.
- Instanciar manualmente o componente em teste.
- Utilizar AssertJ para todas as validações.
- Os testes devem ser rápidos, determinísticos e independentes.
