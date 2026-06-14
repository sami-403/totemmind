# TotemMind - Sistema de Feedback

## Visão Geral

TotemMind é uma aplicação Java desktop construída com JavaFX e Hibernate para gerenciamento de feedbacks em ambientes corporativos. O sistema permite registrar avaliações de funcionários por usuários, categorizando os feedbacks por sentimento e categoria, além de armazenar metadados como timestamp e possíveis análises futuras (como detecção de sarcasmo, confiança e reasoning).

## Tecnologias Utilizadas

- **Java 17+** (implícito pelo uso de `var` e APIs modernas)
- **JavaFX 21.0.6** para a interface gráfica
- **Hibernate** como ORM para persistência
- **ValidatorFX** para validação de formulários
- **Ikonli** para ícones na interface
- **JUnit 5** para testes
- **Maven** como gerenciador de build

## Estrutura do Projeto

```
src/main/java
├── com.br.devsami
│   ├── App.java                    # Ponto de entrada da aplicação
│   ├── Launcher.java               # Classe de inicialização JavaFX
│   ├── controller                  # Controladores da interface (ex: MainControllerScene.java)
│   ├── model
│   │   ├── entity                  # Entidades JPA (Feedback, Employee, User, Person)
│   │   ├── repository              # Repositórios JPA genéricos
│   │   └── service                 # Camada de serviço (FeedbackService, EmployeeService, UserService, AuthService)
│   ├── utils
│   │   ├── enums                   # Enumerados (EmployeeType, FeedbackCategory, Feelling)
│   │   ├── factory                 # Utilitários JPA (JPAUtil, HibernateUtil)
│   │   └── ...                     # Outros utilitários
└── resources
    ├── LoginSimples.fxml           # Tela de login
    ├── PrimeiraCena.fxml           # Tela principal
    └── teste.css                   # Estilos CSS
```

## Funcionalidades Principais

### 1. Gerenciamento de Usuários e Funcionários

O sistema distingue entre `User` (quem dá o feedback) e `Employee` (quem recebe o feedback). Ambos hercam de `Person` (classe base com dados comuns como nome, CPF e data de nascimento).

**Exemplo de criação de funcionário (do `App.java`):**

```java
EmployeeService employeeService = new EmployeeService();
Employee employee = employeeService.createEmployee(
    "João",
    "12345678900",
    EmployeeType.GERENTE
);
```

### 2. Registro de Feedbacks

O core do sistema é o registro de feedbacks, que inclui:
- **Usuário** (quem está dando o feedback)
- **Funcionário** (quem está recebendo)
- **Sentimento** (`Feelling`: SATISFIED, NEUTRAL, UNSATISFIED)
- **Categoria** (`FeedbackCategory`: SERVICE_QUALITY, COMMUNICATION, LEADERSHIP, etc.)
- **Texto livre** do feedback
- **Campos analíticos futuros**: confidence, sarcasmDetected, reasoning, score

**Exemplo de criação de feedback (do `App.java`):**

```java
FeedbackService feedbackService = new FeedbackService();
Feedback feedback = feedbackService.createFeedback(
    user,           // objeto User
    employee,       // objeto Employee
    Feelling.SATISFIED,
    FeedbackCategory.SERVICE_QUALITY,
    "Atendimento muito bom"
);
```

### 3. Validações e Regras de Negócio

Na camada de serviço (`FeedbackService.java`), há validações básicas:
- Usuário e funcionário são obrigatórios
- Se o texto do feedback estiver vazio, define valores padrão para campos analíticos (confidence=100, sarcasmDetected=false, reasoning="Feedback sem texto (voto direto)")

### 4. Persistência com Hibernate

Todas as entidades são mapeadas com annotations JPA/Hibernate. O `Feedback` usa:
- `@Id` com `@GeneratedValue(strategy = GenerationType.UUID)` para identificadores únicos
- `@CreationTimestamp` e `@UpdateTimestamp` para controle automático de timestamps
- Relacionamentos `@ManyToOne` para User e Employee
- Campos de texto longo com `@Column(columnDefinition = "TEXT")`

**Excerpt da entidade `Feedback`:**

```java
@Entity
@Table(name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    private Feelling feelling;

    @Enumerated(EnumType.STRING)
    private FeedbackCategory category;

    // ... outros campos e getters/setters
}
```

### 5. Interface Gráfica (JavaFX)

O projeto contém arquivos FXML para telas:
- `LoginSimples.fxml`: Tela de autenticação
- `PrimeiraCena.fxml`: Tela principal pós-login
- `teste.css`: Folha de estilos

Os controladores (como `MainControllerScene.java`) são simples por enquanto, mas estruturados para lidar com eventos da interface.

## Pontos de Extensão Futura

Com base nos comentários no código e nos campos reservados, o sistema foi projetado para evoluir para:
- Análise de sentimento avançada (usando o texto para calcular confidence, score)
- Detecção de sarcasmo em feedbacks
- Geração automática de reasoning (justificativa por trás da classificação)
- Integração com modelos de IA/NLP para análise de texto

## Como Executar

1. Certifique-se de ter o Java 17+ e o Maven instalados
2. Clone o repositório
3. Execute `mvn javafx:run` ou compile com `mvn clean package` e execute o JAR gerado
4. Certifique-se de que o banco de dados configurado no `hibernate.cfg.xml` (ou similar) esteja acessível

## Conclusão

TotemMind é um sistema modular e extensível para coleta e análise de feedbacks em organizações. Sua arquitetura separa claramente camadas de apresentação, serviço e persistência, facilitando manutenção e futura incorporação de recursos analíticos mais sofisticados.
