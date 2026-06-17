# TotemMind: Análise Avançada dos Services e Repositories

Este documento detalha a implementação da camada de serviço e repositório do sistema TotemMind, focando em padrões de projeto, responsabilidades, tratamento de transações, consultas e pontos de extensão.

## 1. Visão Geral da Arquitetura em Camadas

O TotemMind organiza seu código em quatro camadas principais:

1. **Presentation** (JavaFX Controllers e FXML)
2. **Application/Services** (Lógica de negócio)
3. **Infrastructure/Repositories** (Acesso a dados)
4. **Domain** (Entidades JPA)

A camada de **Service** contém regras de negócio e orquestra operações, enquanto a camada de **Repository** é responsável exclusivamente por operações de persistência e consulta, seguindo o padrão Repository.

## 2. Camada de Repositories

Todos os repositórios compartilham características comuns:
- Uso de `EntityManager` obtido via `HibernateUtil.getEntityManager()`
- Gerenciamento explícito de transações (`begin()`, `commit()`, `rollback()`)
- Garantia de fechamento do `EntityManager` em blocos `finally`
- Tratamento de exceções com rollback automático em caso de falha
- Não contêm regras de negócio — apenas operações CRUD e consultas específicas

### 2.1 EmployeeRepository

**Responsabilidade:** Gerenciamento persistente de funcionários (Employee).

**Métodos-chave:**
- `findByCpf(String cpf): Optional<Employee>`  
  Busca por CPF usando JPQL e `Stream.findFirst()` para retornar `Optional`. Ideal para validações de login e unicidade.
  
- `existsByCpf(String cpf): boolean`  
  Verifica existência eficientemente usando `COUNT()` antes de inserir novos registros.

- `save(Employee employee): void`  
  Persiste novo funcionário com controle explícito de transação e rollback em caso de exceção.

- `findById(UUID id): Optional<Employee>`  
  Busca por identificador único, retornando `Optional` para evitar NPEs.

- `findByName(String name): List<Employee>`  
  Busca flexível por nome usando `LIKE` case-insensitive com curingas (`%`).

**Trecho ilustrativo (findByCpf):**
```java
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
```

**Pontos de extensão:**
- Métodos `update(Employee employee)` e `delete(UUID id)`
- Paginação para `findByName` (usando `setFirstResult`/`setMaxResults`)
- Índices no banco para colunas `cpf` e `name`

### 2.2 FeedbackRepository

**Responsabilidade:** Gerenciamento persistente de feedbacks e consultas analíticas.

**Métodos-chave:**
- `save(Feedback feedback): void`  
  Persistência básica com controle de transação.

- `findById(UUID id): Optional<Feedback>`  
  Busca por identificador.

- `findAll(): List<Feedback>`  
  Listagem completa (cuidado com desempenho em grandes volumes).

- `findByEmployeeId(UUID employeeId): List<Feedback>`  
  Consulta todos os feedbacks de um funcionário específico.

- `findByPeriod(LocalDateTime start, LocalDateTime end): List<Feedback>`  
  Filtra feedbacks por intervalo de criação usando cláusula `BETWEEN`.

- `findByEmployeeAndPeriod(UUID employeeId, LocalDateTime start, LocalDateTime end): List<Feedback>`  
  Consulta composta: funcionário específico + período.

**Trecho ilustrativo (findByPeriod com text block):**
```java
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
```

**Pontos de extensão:**
- Consultas por `feelling` (sentimento) e `category` (categoria)
- Agregações: contagem por tipo, média de `score` (quando implementado)
- Filtros combinados (ex: funcionário + categoria + período)
- Suporte a ordenação (`ORDER BY createdAt DESC`)
- Métodos de atualização e remoção lógico (soft delete)

### 2.3 UserRepository

**Responsabilidade:** Gerenciamento persistente de usuários (quem dá feedback).

**Diferencial:** Utiliza métodos auxiliares privados para reduzir boilerplate de transação:
- `executeInsideTransaction(Consumer<EntityManager> action)`
- `executeInsideTransactionReturning(Function<EntityManager, T> action)`

**Métodos-chave:**
- Semelhantes ao EmployeeRepository: `findByCpf`, `existsByCpf`, `findById`, `findAll`
- Métodos completos de CRUD: `save(User)`, `update(User)`, `delete(UUID)`

**Trecho ilustrativo (helpers):**
```java
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

// Uso no save
public void save(User user) {
    executeInsideTransaction(em -> em.persist(user));
}

// Uso no update (merge)
public User update(User user) {
    return executeInsideTransactionReturning(em -> em.merge(user));
}
```

**Pontos de extensão:**
- Busca por e-mail ou nome (se houver esses campos)
- Validação de unicidade de CPF antes de atualização
- Paginação em `findAll`

## 3. Camada de Services

Os services contêm a lógica de negócio, validam entradas, aplicam regras de domínio e delegam persistência aos repositórios. Eles seguiram um padrão de injeção de dependência simples (instanciação direta via `new` no construtor).

### 3.1 EmployeeService

**Responsabilidade:** Validação e orquestração de operações relacionadas a funcionários.

**Regras de negócio implementadas:**
- Nome e CPF são obrigatórios (não vazios)
- CPF deve ser único no sistema (verificação via `existsByCpf`)
- Orquestra a criação: valida → instancia Entity → persiste via repository

**Métodos:**
- `createEmployee(String name, String cpf, EmployeeType type): Employee`  
  Fluxo completo de validação + persistência.
- `findById(UUID id): Optional<Employee>`  
  Delegação direta ao repository (mera passagem de camada).

**Trecho ilustrativo (createEmployee):**
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
- Métodos `updateEmployee(...)` com validação de que CPF não está sendo alterado para outro já existente
- `deleteEmployee(UUID id)` (físico ou lógico)
- `findByName(String name, int page, int size)` com paginação
- Validação de formato de CPF (usando regex ou biblioteca especializada)

### 3.2 FeedbackService

**Responsabilidade:** Validação e criação de feedbacks com lógica analítica inicial.

**Regras de negócio implementadas:**
- Usuário e funcionário são obrigatórios (não nulos)
- Quando o texto do feedback está vazio, preenche campos analíticos com valores padrão:
  - `confidence = 100` (feedback considerado certo por ausência de texto)
  - `sarcasmDetected = false`
  - `reasoning = "Feedback sem texto (voto direto)"`

**Observação importante:** Este serviço atualmente **apenas cria** feedbacks. Não há métodos de atualização, exclusão ou consultas avançadas (essas ficariam no repository ou em novos services especializados).

**Trecho ilustrativo (createFeedback):**
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

**Pontos de extensão (críticos para evolução do sistema):**
1. **Integração com análise de texto (IA/NLP):**
   - Substituir o bloco `if (text == null || text.isBlank())` por chamada a serviço de análise
   - Preencher `confidence`, `score`, `reasoning` e `sarcasmDetected` baseado em processamento de linguagem natural
   - Exemplo: usar APIs como Google NLP, AWS Comprehend ou modelos Hugging Face locais

2. **Métodos de atualização:**
   - `updateFeedback(UUID id, String novoTexto)` que reexecuta análise de texto
   - Validação de que apenas o criador ou administrador pode alterar

3. **Services analíticos especializados:**
   - `FeedbackAnalyticsService` com métodos como:
     - `calcularMediaSatisfacaoPorPeriodo(LocalDateTime inicio, LocalDateTime fim)`
     - `contarFeedbackPorCategoria(UUID funcionarioId)`
     - `detectarTendenciasDeSarcasmo(List<Feedback>)`

4. **Tratamento de exceções de negócio:**
   - Criar exceções customizadas (ex: `FeedbackInvalidoException`) para diferenciar de falhas de sistema

### 3.3 UserService & AuthService (inferidos)

Com base na nomenclatura e padrão do projeto:

**UserService:**
- Espelharia EmployeeService: validações de entrada (nome, CPF, talvez e-mail) + delegação ao UserRepository
- Provavelmente incluiria métodos de atualização e exclusão

**AuthService (hipotético):**
- Responsável por autenticação (verificação de CPF/senha)
- Possivelmente geração e validação de tokens de sessão
- Integração com controllers de login JavaFX
- Poderia incluir métodos como:
  - `boolean autenticar(String cpf, String senha)`
  - `String gerarToken(User user)`
  - `boolean validarToken(String token)`

## 4. Padrões e Boas Práticas Identificadas

| Padrão | Onde está observado | Benefício |
|--------|---------------------|-----------|
| **Repository Pattern** | Todas as classes `*Repository` | Separa preocupações de acesso a dados; facilita teste e troca de ORM |
| **Service Layer** | Todas as classes `*Service` | Centraliza regras de negócio; evita vazamento para controllers ou repositories |
| **Optional** | Métodos de busca (`findById`, `findByCpf`) | Evita `NullPointerException` explícito; força tratamento do caso vazio |
| **Transação Explícita** | Métodos de `save` em repositories | Controle preciso de limites de transação; evita problemas de sessão lunga |
| **Try/Finally com Close** | Todo uso de `EntityManager` | Garante liberação de recursos mesmo em exceções (previne vazamentos) |
| **Validação de Entrada Precoce** | Services (ex: `createEmployee`) | "Fail fast": rejeita dados inválidos antes de consumir recursos |
| **Injeção de Dependência Simples** | Construtores dos services (`new EmployeeRepository()`) | Baixo acoplamento; substituição fácil por mocks em testes unitários |
| **Text Blocks (JPQL)** | FeedbackRepository (consultas complexas) | Melhora legibilidade de queries multi‑linha, especialmente com quebras |
| **Helper Methods de Transação** | UserRepository | Reduz boilerplate; padroniza begin/commit/rollback/close |

## 5. Dependências e Interações entre Camadas

```
JavaFX Controller 
        ↓ (chama)
Service (ex: FeedbackService) 
        ↓ (delega)
Repository (ex: FeedbackRepository) 
        ↓ (opera)
EntityManager ↔ Banco de Dados
```

**Fluxo típico de criação de feedback (do App.java):**
1. `UserService.findOrCreateUser(...)` → valida/cria user via UserRepository
2. `EmployeeService.createEmployee(...)` → valida/cria employee via EmployeeRepository
3. `FeedbackService.createFeedback(...)` → valida user/employee, preenche campos analíticos, salva via FeedbackRepository

**Observação crítica:** Os services **instanciam seus próprios repositórios** via `new` no construtor. Isso:
- ✅ Simplifica a configuração inicial (não precisa de framework de DI)
- ❌ Dificulta testes unitários (não é fácil injetar mocks sem reflexão ou construtores alternativos)
- ❌ Cria múltiplas instâncias de repositórios desnecessariamente (se um serviço usar vários repositórios)

## 6. Sugestões de Melhoria

### 6.1 Injeção de Dependência Adequada
Adicionar um contêiner léger ou usar construtores com parâmetros:
```java
// Em vez disso:
public FeedbackService() {
    this.feedbackRepository = new FeedbackRepository();
}

// Fazer isso:
public FeedbackService(FeedbackRepository feedbackRepository) {
    this.feedbackRepository = feedbackRepository;
}
```
Isso permitiria:
- Testes com mocks fáceis (Mockito, EasyMock)
- Configuração centralizada de dependências
- Vida útil controlada dos repositórios (singleton por request, por exemplo)

### 6.2 Camada de DTO (Data Transfer Object)
Separar objetos de transferência das entidades JPA:
- Evitar exposição acidental de campos internos ou mapeamentos complexos
- Permitir versionamento independente da API de serviço
- Exemplo: `FeedbackRequestDTO` para entrada, `FeedbackResponseDTO` para saída

### 6.3 Paginação e Ordenação
Adicionar suporte a `Pageable` nas consultas de listagem:
```java
// No repository
public Page<Feedback> findAll(Pageable pageable) {
    // implementação usando setFirstResult/setMaxResults
}

// No service
public Page<FeedbackResponseDTO> getFeedbacksPaginated(int page, int size) {
    // chama repository + converte para DTOs
}
```

### 6.4 Tratamento Centralizado de Exceções
Criar um mecanismo para converter exceções de serviço em respostas amigáveis:
- Em JavaFX: mostrar `Alert` com mensagens de erro
- Em API REST (se futura): retornar códigos HTTP apropriados (400, 404, 500)

### 6.5 Anotaciones e Metadados
Aproveitar melhor as anotações JPA/Hibernate:
- `@NaturalId` para campos como CPF (melhora performance de buscas)
- `@Where(clause = "deleted_at IS NULL")` para soft delete global
- `@SQLDelete(sql = "update feedbacks set deleted_at = now() where id = ?")` para override de delete

### 6.6 Cache de Leitura
Implementar cache para dados pouco mutáveis:
- Cache de segundo nível do Hibernate para entidades como `EmployeeType`, `FeedbackCategory`
- Cache local (Caffeine) para consultas frequentes como `findByCpf` em cenários de alta concorrência

### 6.7 Testes Automatizados
Criar suíte de testes:
- Testes unitários de services com mocks de repositories (usando Mockito)
- Testes de integração com banco em memória (H2) para validar queries JPQL
- Testes de comportamento (BDD) com Cucumber ou similares para fluxos de negócio críticos

### 6.8 Evolução para Arquitetura Hexagonal/Clean
A longo prazo, considerar:
- Definir interfaces para repositórios (ex: `FeedbackRepositoryInterface`)
- Ter services dependendo apenas de interfaces, não de implementações concretas
- Isso permitiria trocar facilmente entre Hibernate, JDBC puro ou até chamadas de API externa

## 7. Conclusão

A camada de serviço e repositório do TotemMind demonstra uma implementação sólida do padrão camadas com separação clara de responsabilidades:
- **Repositories** são verdadeiramente focados em acesso a dados, sem vazamento de regras de negócio
- **Services** contêm validações de entrada e orquestração de operações de domínio
- O código é legível, usa recursos modernos do Java (var, text blocks) e trata adequadamente recursos e transações

Os principais pontos de evolução identificados são:
1. **Aprimoramento da injeção de dependência** para facilitar testes e manutenção
2. **Adição de camada de DTO** para melhor controle de contratos de API
3. **Expansão dos services** com lógica analítica de texto (IA/NLP) para realizar a promessa inicial do sistema (detecção de sarcasmo, scoring, reasoning)
4. **Implementação de paginação, ordenação e tratamento centralizado de exceções**
5. **Cobertura de testes automatizados** para garantir regressão zero em mudanças futuras

Com essas melhorias, o TotemMind estaría bem posicionado para evoluir de um simples registrador de feedbacks para uma plataforma analítica robusta de experiência do colaborador, cumprindo plenamente sua visão original de usar dados de feedback para gerar insights acionáveis através de tecnologias de processamento de linguagem natural e aprendizado de máquina.