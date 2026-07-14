# TotemMind

**Inteligência de Negócios e IA de Borda para Terminais de Autoatendimento.**


---

O **TotemMind** redefine a coleta e a análise de métricas operacionais diretamente no ponto de interação. Desenvolvido especificamente para quiosques e totens de autoatendimento, o sistema substitui os processos manuais e passivos por um fluxo digital contínuo, seguro e autónomo.

Através de uma arquitetura robusta acoplada a Large Language Models (LLMs) locais, a plataforma captura a percepção do utilizador, extrai inteligência de dados não estruturados e atua como um analista de Business Intelligence em tempo real.

---

## 💡 Pilares Fundamentais

### 1. IA na Borda (Edge AI) & Privacidade Absoluta

A integração com a biblioteca **LangChain4j** permite que modelos de linguagem rodem 100% localmente e em modo *offline*. Toda análise de sentimento, deteção de ironia/sarcasmo e extração de métricas qualitativas ocorrem na própria máquina do totem, garantindo:

* **Independência:** Zero dependência de nuvem ou conexões externas à internet.
* **Performance:** Latência infinitesimal no processamento e resposta analítica.
* **Segurança:** Proteção total e conformidade com as diretivas mais rígidas de privacidade de dados.

### 2. Agente de BI Autónomo (*Tools / Function Calling*)

O TotemMind vai além do armazenamento de dados. O LLM local possui acesso direto a ferramentas de consulta e agregação de dados estruturados do banco de dados relacional. Isto permite que gestores interajam com o assistente inteligente para extrair relatórios consolidados, médias de satisfação e tendências de atendimento instantaneamente através de linguagem natural.

### 3. Engenharia de Software de Alta Disponibilidade

Construído com padrões de design rigorosos para garantir estabilidade ininterrupta em ambientes desktop de autoatendimento:

* **Interface Imersiva:** Telas responsivas e fluidas em JavaFX (utilizando ValidatorFX e Ikonli).
* **Persistência Confiável:** Mapeamento objeto-relacional robusto com Hibernate ORM.
* **Fail Fast:** Lógica de negócio isolada na camada de serviço para validação instantânea.

---

## 🛠️ Estrutura Tecnológica

* **Ambiente de Execução:** Java 17+
* **Framework Gráfico:** JavaFX 21.0.6
* **Mapeamento ORM:** Hibernate Framework
* **Orquestração de IA:** LangChain4j (LLMs locais via Ollama/similares)

---

## 🚀 Primeiros Passos

### Pré-requisitos

* Java Development Kit (JDK) 17 ou superior.
* Apache Maven instalado.
* Instância de LLM local (ex: Ollama com o modelo configurado ativo).

### Compilação e Execução

Para construir e inicializar o terminal TotemMind, execute as seguintes diretivas no seu terminal:

```bash
# Compilar o projeto e empacotar as dependências
mvn clean package

# Inicializar o terminal do totem
mvn javafx:run

```

---
