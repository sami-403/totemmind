# Análise Detalhada dos Services e Repositories do TotemMind

Este documento aprofunda a camada de serviço e repositório do sistema TotemMind, explicando responsabilidades, padrões de projeto utilizados e pontos de extensão.

## Visão Geral da Camada de Persistência e Negócio

O TotemMind segue uma arquitetura em camadas clássica:

1. **Controller** (JavaFX) → 2. **Service** (regra de negócio) → 3. **Repository** (acesso a dados) → 4. **Entity** (mapeamento JPA)

Os **Repositories** são responsáveis exclusivamente por operações de CRUD e consultas ao banco de dados, contendo **nenhuma regra de negócio**.  
Os **Services** orquestram uma ou mais operações de repositório, aplicam validações de negócio e lidam com exceções.

Cada serviço instancia seu próprio repositório via `new` (injeção de dependência simples). Para testes, seria ideal usar um framework de DI (Spring, Guice) ou passar o repositório via construtor.

---

## Repositories

Todos os repositórios compartilham um padrão comum de obtenção e fechamento do `EntityManager` via `HibernateUtil`. As transações são iniciadas e finalizadas dentro de cada método, com `try/finally` para garantir o fechamento e `catch` para rollback em caso de exceção.

### EmployeeRepository (`EmployeeRepository.java`)

**Responsabilidades:**
- Busca de funcionário por CPF (`findByCpf`, `existsByCpf`)
- Persistência de novos funcionários (`save`)
- Busca por ID (`findById`)
- Busca por nome (retorna lista, usando `LIKE` case-insensitive)

**Trechos relevantes:**

```java
// Busca por CPF usando JPQL e Stream para Optional
public Optional<Employee> findByCpf(String cpf) {
    EntityManager em = HibernateUtil.getEntityManager();
    try {
        return em.createQuery(
                "SELECT e FROM Employee e WHERE e.cpf = :cpf", Employee.class)
                .setParameter("cpf", cpf)
                .getResultStream()
                .findFirst();
    } finally {
        em.close();
    }
}

// Verifica existência antes de inserir (evita duplicidade)
public boolean existsByCpf(String cpf) {
    EntityManager em = HibernateUtil.getEntityManager();
    try {
        Long count = em.createQuery(
                "SELECT COUNT(e) FROM Employee e WHERE e.cpf = :cpf", Long.class)
                .setParameter("cpf", cpf)
                .getSingleResult();
        return count > 0;
    } finally {
        em.close();
    }
}

// Persiste com controle explícito de transação e rollback
public void save(Employee employee) {
    EntityManager em = HibernateUtil.getEntityManager();
    try {
        em.getTransaction().begin();
        em.persist(employee);
        em.getTransaction().commit();
    } catch (Exception e) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
        throw e;
    } finally {
        em.close();
    }
}
```

**Pontos de extensão:**
- Adicionar métodos de atualização e remoção (não implementados atualmente).
- Paginação para buscas por nome.
- Índices no banco para CPF e nome.

### FeedbackRepository (`FeedbackRepository.java`)

**Responsabilidades:**
- Persistência de feedback (`save`)
- Busca por ID (`findById`)
- Listagem geral (`findAll`)
- Consultas específicas:
  - Por funcionário (`findByEmployeeId`)
  - Por período (`findByPeriod`)
  - Por funcionário + período (`findByEmployeeAndPeriod`)

**Trechos relevantes:**

```java
// Consulta por intervalo de datas usando JPQL de múltiplas linhas (text block)
public List<Feedback> findByPeriod(LocalDateTime start, LocalDateTime end) {
    EntityManager em = HibernateUtil.getEntityManager();
    try {
        return em.createQuery(
                """
                SELECT f
                FROM Feedback f
                WHERE f.createdAt BETWEEN :start AND :end
                """,
                Feedback.class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    } finally {
        em.close();
    }
}

// Consulta composta: funcionário + período
public List<Feedback> findByEmployeeAndPeriod(UUID employeeId,
                                              LocalDateTime start,
                                              LocalDateTime end) {
    EntityManager em = HibernateUtil.getEntityManager();
    try {
        return em.createQuery(
                """
                SELECT f
                FROM Feedback f
                WHERE f.employee.id = :employeeId
                AND f.createdAt BETWEEN :start AND :end
                """,
                Feedback.class)
                .setParameter("employeeId", employeeId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
    } finally {
        em.close();
    }
}
```

**Pontos de extensão:**
- Busca por sentimento (`feelling`) ou categoria (`category`).
- Agregações (contagem por tipo, média de score futuro).
- Suporte a filtros combinados (ex: funcionário + categoria + período).
- Métodos de atualização e remoção lógico (soft delete).

### UserRepository (`UserRepository.java`)

**Responsabilidades:**
- Busca por CPF (`findByCpf`, `existsByCpf`)
- Busca por ID (`findById`)
- Listagem geral (`findAll`)
- Persistência (`save`), atualização (`update`) e remoção (`delete`)

**Diferencial:** Utiliza métodos auxiliares privados `executeInsideTransaction` e `executeInsideTransactionReturning` para reduzir boilerplate de transação.

```java
// Helper que encapsula begin/commit/rollback e fechamento
private void executeInsideTransaction(Consumer<EntityManager> action) {
    EntityManager em = HibernateUtil.getEntityManager();
    try {
        em.getTransaction().begin();
        action.accept(em);
        em.getTransaction().commit();
    } catch (Exception e) {
        em.getTransaction().rollback();
        throw e;
    } finally {
        em.close();
    }
}

// Usado no save
public void save(User user) {
    executeInsideTransaction(em -> em.persist(user));
}

// Usado no update (merge)
public User update(User user) {
    return executeInsideTransactionReturning(em -> em.merge(user));
}
```

**Pontos de extensão:**
- Busca por nome ou e-mail (se houver).
- Validação de unicidade de CPF antes de atualizar.
- Paginação em `findAll`.

---

## Services

Os serviços contêm a lógica de negócio, validam entradas, aplicam regras de domínio e delegam a persistência aos repositórios.

### EmployeeService (`EmployeeService.java`)

**Responsabilidades:**
- Validação de dados de funcionário (nome, CPF não vazios; CPF único)
- Orquestração da criação (`createEmployee`)
- Busca por ID (delega ao repositório)

**Trechos relevantes:**

```java
public Employee createEmployee(String name, String cpf, EmployeeType type) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Nome obrigatório");
    }
    if (cpf == null || cpf.isBlank()) {
        throw new IllegalArgumentException("CPF obrigatório");
    }
    if (employeeRepository.existsByCpf(cpf)) {
        throw new IllegalArgumentException("CPF já cadastrado");
    }

    var employee = new Employee();
    employee.setName(name);
    employee.setCpf(cpf);
    employee.setTipo(type);

    employeeRepository.save(employee);
    return employee;
}
```

**Pontos de extensão:**
- Métodos de atualização (com validação de CPF não alterado para outro já existente).
- Remoção lógica ou física.
- Busca por nome com paginação.

### FeedbackService (`FeedbackService.java`)

**Responsabilidades:**
- Validação de usuário e funcionário obrigatórios
- Criação de feedback com preenchimento de campos analíticos iniciais (confidence, sarcasmDetected, reasoning) quando o texto está vazio
- Persistência via repository

**Trechos relevantes:**

```java
public Feedback createFeedback(User user,
                               Employee employee,
                               Feelling feeling,
                               FeedbackCategory category,
                               String text) {
    if (user == null) {
        throw new IllegalArgumentException("User obrigatório");
    }
    if (employee == null) {
        throw new IllegalArgumentException("Employee obrigatório");
    }

    var feedback = new Feedback();
    feedback.setUser(user);
    feedback.setEmployee(employee);
    feedback.setFeelling(feeling);
    feedback.setCategory(category);
    feedback.setText(text);

    // regras simples iniciais
    if (text == null || text.isBlank()) {
        feedback.setConfidence(100);
        feedback.setSarcasmDetected(false);
        feedback.setReasoning("Feedback sem texto (voto direto)");
    }

    feedbackRepository.save(feedback);
    return feedback;
}
```

**Pontos de extensão:**
- Integração com serviço de análise de texto (IA/NLP) para preencher `confidence`, `score`, `reasoning` e `sarcasmDetected` automaticamente.
- Atualização de feedback (permite editar texto e reanalisar).
- Busca avançada combinando filtros de sentimento, categoria, período e funcionário.
- Cálculo de métricas (média de satisfaction por período, distribuição por categoria).

### UserService & AuthService (arquivos não detalhados anteriormente, mas existentes)

**UserService** provavelmente espelha o padrão de `EmployeeService`: validações de entrada e delegação ao `UserRepository`.

**AuthService** (não lido, mas sugerido pelo nome) deveria tratar:
- Autenticação (verificação de CPF/senha)
- Geração/validação de tokens (se houver)
- Controle de sessão

---

## Padrões e Boas Práticas Observadas

| Padrão | Onde está | Benefício |
|--------|-----------|-----------|
| **Repository Pattern** | Todas as classes `*Repository` | Isola acesso a dados, facilita troca de ORM ou banco |
| **Service Layer** | Todas as classes `*Service` | Centraliza regras de negócio, reutilizável por múltiplos controllers |
| **Optional** | Métodos de busca (`findById`, `findByCpf`) | Evita `NullPointerException` e força tratamento explícito |
| **Transação explícita** | Métodos de `save`/`update` em repositórios | Controle preciso de quando iniciar e terminar transações |
| **Try/finally com close** | Todo uso de `EntityManager` | Garante liberação de recursos mesmo em caso de exceção |
| **Validação de entrada** | Services (ex: `createEmployee`) | Falha rápido, retorna mensagens claras ao usuário |
| **Injeção de dependência simples** | Construtores dos services (`new EmployeeRepository()`) | Baixo acoplamento; fácil de substituir por mock em testes |
| **Text Blocks (JPQL)** | `FeedbackRepository` (consultas complexas) | Melhora legibilidade de queries multi‑linha |

## Pontos de Melhoria Sugeridos

1. **Injeção de Dependência verdadeira**  
   Usar um contêiner léger (Spring Boot, Guice) ou passar repositórios via construtor/setter para facilitar testes unitários com mocks.

2. **Camada de DTO (Data Transfer Object)**  
   Separar objetos de entrada/saída da camada de serviço das entidades JPA, evitando exposição acidental de campos internos e permitindo versionamento de API.

3. **Tratamento Centralizado de Exceções**  
   Criar um `@ControllerAdvice` (se usando Spring) ou um handler customizado para converter exceções de serviço em respostas amigáveis ao usuário (JavaFX Dialogs, por exemplo).

4. **Pagination e Sorting**  
   Adicionar suporte a `Pageable` nas consultas de listagem (ex: `findAll`, `findByName`) usando `javax.persistence.TypedQuery` com `setFirstResult`/`setMaxResults`.

5. **Auditoria e Soft Delete**  
   Incluir campos como `deletedAt` ou `active` nas entidades e atualizar repositórios para filtrar automaticamente registros excluídos logicamente.

6. **Cache de Leitura**  
   Para dados de referência pouco mutáveis (tipos de funcionário, categorias de feedback), usar cache de primeiro ou segundo nível do Hibernate ou um cache externo (Caffeine, Redis).

7. **Testes Automatizados**  
   Criar testes de integração com banco em memória (H2) para serviços e repositórios, cobrando cenários de sucesso, falha de validação e contraintes de unicidade.

8. **Migração de Schema**  
   Adicionar Flyway ou Liquibase para versionamento do schema de banco de dados em ambientes de teste e produção.

---

## Conclusão

A camada de serviço e repositório do TotemMind está bem estruturada, seguindo princípios de separação de preocupações e boas práticas de persistência com Hibernate. Cada repositório foca em operações CRUD e consultas específicas, enquanto os serviços adicionam validações de negócio e orquestram transações. O código é legível e contém pontos claros de extensão para futuras melhorias, especialmente na área de análise de feedback e escalabilidade.

O próximo passo natural seria aprimorar a injeção de dependência, adicionar camada de DTO e escrever testes automatizados para garantir que mudanças futuras não quebrem a funcionalidade existente.