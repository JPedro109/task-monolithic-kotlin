---
name: update-readme
description: Analisa o código-fonte e atualiza o README.md para refletir o estado atual do projeto (arquitetura, tecnologias, endpoints, variáveis de ambiente, observabilidade, testes e qualidade de código)
inclusion: manual
---

# Skill: Atualizar README

Quando ativada, esta skill guia a atualização do `README.md` para refletir o estado atual do código-fonte. Antes de escrever qualquer coisa, **sempre leia o código** — nunca atualize com base em memória ou suposições.

## O que esta skill cobre

O README possui as seguintes seções que devem ser verificadas e atualizadas:

1. **Arquitetura** — estrutura de pacotes e decisões de design
2. **Tecnologias** — versões e ferramentas efetivamente usadas
3. **Como Executar** — pré-requisitos, Docker e execução local
4. **Variáveis de Ambiente** — todas as variáveis com valores padrão
5. **Referência da API** — domínios, endpoints, métodos e exemplos de payload
6. **Observabilidade** — serviços, URLs e dashboards
7. **Testes** — comandos, cobertura mínima e relatórios
8. **Qualidade de Código** — ferramentas de análise estática

---

## Protocolo de leitura obrigatória

Antes de alterar qualquer seção, leia os arquivos correspondentes:

| Seção                | Arquivos a ler                                                                                                                        |
|----------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| Arquitetura          | Toda a árvore `src/main/kotlin/com/jpmns/task/` (estrutura de pacotes)                                                               |
| Tecnologias          | `build.gradle.kts`                                                                                                                    |
| Como Executar        | `docker/docker-compose.yml`, `src/main/resources/application.yaml`, `README.md` atual                                                |
| Variáveis de Ambiente| `src/main/resources/application.yaml`, `src/test/resources/application-integration-test.yaml`                                        |
| Referência da API    | Todos os controllers em `src/main/kotlin/com/jpmns/task/core/presentation/controller/` e suas interfaces de documentação (`*Doc.kt`) |
| Observabilidade      | `docker/docker-compose.yml`, `docker/prometheus/`, `docker/grafana/`, `docker/otel-collector/`                                       |
| Testes               | `build.gradle.kts` (configuração do JaCoCo), `src/test/` (estrutura dos testes)                                                      |
| Qualidade de Código  | `build.gradle.kts` (configuração do ktlint e detekt), `detekt.yml`                                                                   |

---

## Regras obrigatórias

### Geral
- **Nunca** escreva algo que não esteja confirmado no código. Se uma informação não puder ser verificada, omita-a ou indique que deve ser conferida manualmente.
- Mantenha o idioma já utilizado no README (atualmente **português brasileiro**).
- Preserve a estrutura atual de seções e o índice (`## Índice`). Adicione novas seções ao índice se criá-las.
- Não remova seções existentes sem razão explícita verificada no código.
- O README é documentação para desenvolvedores: seja direto, preciso e sem floreios.

### Arquitetura
- Atualize a árvore de pacotes se houver novos pacotes ou camadas.
- Liste apenas decisões de design que estejam realmente implementadas no código.
- Nunca descreva uma camada que não existe ou um padrão que não é seguido.
- O projeto segue Clean Architecture (Ports & Adapters) com Kotlin — reflita isso na descrição.

### Tecnologias
- As versões devem corresponder exatamente às declaradas em `build.gradle.kts`.
- Se uma dependência for removida, retire-a da tabela.
- Se uma nova dependência relevante for adicionada, inclua-a na categoria correta.
- Inclua os plugins Kotlin relevantes: `kotlin-jvm`, `kotlin-spring`, `kotlin-jpa`, `allOpen`, `noArg`.

### Variáveis de Ambiente
- A fonte da verdade é `application.yaml`. Leia o arquivo antes de atualizar.
- Inclua todas as variáveis com `${VAR:default}` declaradas no YAML.
- A coluna "Padrão" deve refletir o valor default real do YAML, não o que estava no README antes.
- Mantenha o aviso sobre `JWT_SECRET` em produção.

### Referência da API
- Leia **todos** os controllers e suas respectivas interfaces de documentação Swagger (`*Doc.kt`) para extrair os endpoints.
- Para cada endpoint, confirme: método HTTP, path completo, se exige autenticação e o que faz.
- Os exemplos de payload (JSON) devem refletir os campos reais das data classes de request/response em `presentation/controller/payload/`.
- Não documente endpoints que não existam no código.

### Observabilidade
- As portas e URLs devem ser extraídas do `docker-compose.yml`, não inventadas.
- Liste apenas serviços que estejam declarados no Compose.
- Se o dashboard do Grafana mudar de nome ou configuração, atualize a descrição.

### Testes
- O percentual de cobertura mínima deve vir da configuração do JaCoCo em `build.gradle.kts`.
- O path do relatório HTML deve refletir o output real do Gradle.
- Mencione todos os tipos de teste presentes: unitários (MockK), integração (Testcontainers + PostgreSQL), Spring Security Test.
- H2 é usado apenas em testes unitários; Testcontainers (PostgreSQL) é usado nos testes de integração.

### Qualidade de Código
- Este projeto usa **ktlint** (formatação) e **detekt** (análise estática) — não mencione Checkstyle.
- Confirme as versões de ktlint e detekt declaradas em `build.gradle.kts`.
- Mencione o arquivo de configuração `detekt.yml` e o que ele customiza, se houver regras relevantes.
- Confirme o comando correto para executar a checagem completa (`./gradlew check`).

---

## Processo passo a passo

Siga esta sequência ao executar a skill:

1. **Leia o README atual** para entender o estado documentado.
2. **Leia `build.gradle.kts`** para coletar versões, dependências, cobertura mínima, plugins Kotlin e comandos de build.
3. **Leia `src/main/resources/application.yaml`** para mapear variáveis de ambiente.
4. **Explore a árvore `src/main/kotlin/`** para verificar a estrutura de pacotes.
5. **Leia todos os controllers e seus docs** para mapear endpoints.
6. **Leia `docker/docker-compose.yml`** para verificar serviços e portas.
7. **Compare** o que foi lido com o que está no README.
8. **Liste as diferenças** encontradas (o que está desatualizado, ausente ou incorreto).
9. **Aplique as correções** seção por seção, citando a fonte de cada mudança.
10. **Não altere** seções que estejam corretas e atualizadas.

---

## Checklist antes de finalizar

- [ ] `build.gradle.kts` lido — versões de dependências e plugins Kotlin conferidos
- [ ] `application.yaml` lido — todas as variáveis de ambiente verificadas
- [ ] Estrutura de pacotes explorada em `src/main/kotlin/` — árvore de arquitetura atualizada se necessário
- [ ] Todos os controllers e interfaces `*Doc.kt` lidos — tabelas de endpoints conferidas
- [ ] `docker-compose.yml` lido — portas e serviços de observabilidade confirmados
- [ ] JaCoCo coverage mínima verificada no `build.gradle.kts`
- [ ] ktlint e detekt verificados (versões e configuração em `detekt.yml`)
- [ ] Nenhuma informação inventada ou assumida sem leitura de código
- [ ] Idioma mantido em português brasileiro
- [ ] Índice atualizado se novas seções foram adicionadas
- [ ] README salvo e formatação Markdown verificada
