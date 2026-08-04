---
inclusion: fileMatch
fileMatchPattern: "**/src/main/**"
---

# Arquitetura do Projeto

## Princípios arquiteturais

Toda implementação deve respeitar os seguintes princípios:

* O domínio representa o núcleo da aplicação e contém exclusivamente regras de negócio.
* Regras de negócio nunca dependem de frameworks, bibliotecas ou tecnologias externas.
* Casos de uso representam operações completas da aplicação e coordenam a execução das regras de negócio.
* Infraestrutura implementa apenas detalhes técnicos necessários para suportar a aplicação.
* A camada de apresentação apenas recebe requisições, delega sua execução e devolve respostas.
* Toda comunicação entre camadas ocorre através de contratos bem definidos.
* Implementações concretas nunca devem ser utilizadas quando um contrato puder representar a dependência.

---

## Estrutura de Pastas

A organização física do projeto deve refletir a arquitetura definida aqui. Cada camada deve possuir um espaço próprio, evitando a mistura de responsabilidades e mantendo uma separação clara entre os componentes da aplicação.

A estrutura do projeto é:

```text
src/
├── domain/
├── application/
├── external/
└── presentation/
```

---

## Fluxo de dependências

As dependências sempre apontam para o centro da arquitetura.

A direção permitida das dependências é:

```text
Presentation
      ↓
Application
      ↓
Domain

External
      ↓
Application
      ↓
Domain
```

Portanto:

* Domain não depende de nenhuma outra camada.
* Application depende apenas de Domain.
* External depende de Application e Domain.
* Presentation depende de Application.

Nenhuma camada interna conhece implementações localizadas em camadas externas.

---

## Fluxo de dados

O fluxo de dados entre as camadas deve ser unidirecional.

```text
Request
    ↓
Input DTO
    ↓
Domain
    ↓
Output DTO
    ↓
Response
```

Cada camada trabalha exclusivamente com seus próprios objetos.

Objetos pertencentes a uma camada nunca devem atravessar diretamente outra camada sem conversão explícita.

As conversões entre objetos devem ocorrer exatamente na fronteira entre duas camadas.

---

## Responsabilidades das camadas

Cada camada possui responsabilidades exclusivas.

### Domain

Responsável por:

* regras de negócio;
* entidades;
* value objects;
* validações de domínio;
* invariantes;
* comportamentos do domínio.

Não é responsável por:

* persistência;
* autenticação;
* protocolos de comunicação;
* frameworks;
* acesso ao banco de dados;
* detalhes de infraestrutura.

---

### Application

Responsável por:

* casos de uso;
* orquestração da aplicação;
* definição de contratos;
* coordenação entre domínio e infraestrutura.

Não é responsável por:

* regras de infraestrutura;
* detalhes de frameworks;
* protocolos externos.

---

### External

Responsável por:

* persistência;
* autenticação;
* integrações externas;
* mensageria;
* armazenamento;
* cache;
* implementação das portas definidas pela aplicação.

Não é responsável por:

* regras de negócio.

---

### Presentation

Responsável por:

* receber entradas externas;
* validar dados de entrada;
* converter dados para DTOs da aplicação;
* chamar casos de uso;
* converter respostas para o formato externo adequado.

Não é responsável por:

* implementar regras de negócio;
* acessar infraestrutura diretamente;
* realizar persistência.

---

## Regras gerais de desacoplamento

Todas as implementações devem seguir as seguintes regras:

* Cada classe deve possuir uma única responsabilidade.
* Cada componente deve depender de abstrações sempre que possível.
* Nenhuma camada pode acessar diretamente detalhes internos de outra camada.
* Toda integração com tecnologias externas deve ser encapsulada em adaptadores.
* Toda conversão entre modelos deve ser explícita.
* Nenhuma regra de negócio deve depender de detalhes técnicos.
* Nenhuma implementação concreta deve ser utilizada quando existir um contrato equivalente.
* Objetos pertencentes a uma camada não devem ser reutilizados por outra camada sem uma conversão explícita.
* Toda dependência deve possuir a menor abrangência possível.
* O acoplamento entre módulos deve ser minimizado e a coesão interna maximizada.
