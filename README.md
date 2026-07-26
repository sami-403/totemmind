# TotemMind

**Inteligência de Negócios e IA de Borda para Terminais de Autoatendimento.**

---

O **TotemMind** redefine a coleta e a análise de métricas operacionais diretamente no ponto de interação. Desenvolvido especificamente para quiosques e totens de autoatendimento, o sistema substitui os processos manuais e passivos por um fluxo digital contínuo, seguro e autónomo.

Através de uma arquitetura robusta acoplada a Large Language Models (LLMs) locais, a plataforma captura a percepção do utilizador, extrai inteligência de dados não estruturados e atua como um analista de Business Intelligence (BI) em tempo real.

---

## 💡 Pilares Fundamentais

### 1. IA na Borda (Edge AI) & Otimização de Recursos (T4/VRAM Limitada)
A integração com o ecossistema **LangChain4j** permite que modelos de linguagem rodem localmente de forma eficiente. O sistema foi projetado com uma arquitetura de carregamento dinâmico (*lazy-load*) e liberação imediata de VRAM para otimizar o uso em GPUs de entrada ou ambientes em nuvem com recursos restritos (como instâncias T4 com 15-16GB):
* **Modelo Principal (BI & Chat):** Utiliza o `qwen2.5:7b` (ou similar) como orquestrador principal, mantendo um contexto de 4096 tokens e temperatura de 0.1 para respostas determinísticas com suporte a chamadas de ferramentas (*Tool Calling*).
* **Modelo de Classificação (Sentimento e Categoria):** Utiliza o Gemma-3-Gaia (`hf.co/cnmoro/Gemma-3-Gaia-PT-BR-4b-it-Q8_0-GGUF:Q8_0`) com contexto reduzido de 512 tokens. Ele é carregado na VRAM apenas no momento do processamento do feedback e descarregado imediatamente após a execução via chamada HTTP da API do Ollama (`keep_alive: 0`).
* **Resultados:** Redução do consumo de VRAM de ~16GB para **6-8GB** e redução de ~90% nas alucinações.

### 2. Agente de BI Autónomo (KICER - *Tools / Function Calling*)
O assistente inteligente integrado (**KICER**) interage em português com os gestores em linguagem natural. Ele possui acesso direto ao banco de dados relacional através de ferramentas customizadas (`TotemTools`), permitindo-lhe:
* Gerar e plotar gráficos dinâmicos de pizza e linha diretamente na tela do JavaFX.
* Exibir cards interativos e detalhados de produtos ou funcionários no painel lateral.
* Realizar buscas avançadas de métricas, médias de estrelas e rankings temporais.

### 3. Engenharia de Software de Alta Disponibilidade
Construído com padrões de design consolidados para garantir estabilidade e fluidez em ambientes desktop:
* **Interface Imersiva e Moderna:** Telas responsivas em JavaFX utilizando o tema moderno `CupertinoDark` da biblioteca AtlantaFX, suporte de ícones Ikonli e validações reativas com ValidatorFX.
* **Persistência Confiável:** Mapeamento objeto-relacional robusto com Hibernate ORM e banco de dados H2.
* **Fail Fast:** Camada de serviço responsável por isolar as regras de negócio e realizar validações instantâneas de integridade (CPF, código de barras, valores monetários).

---

## 🛠️ Estrutura Tecnológica

* **Ambiente de Execução:** Java 21 (JDK 21)
* **Framework Gráfico:** JavaFX 21.0.6 (com AtlantaFX 2.0.1, ValidatorFX 0.6.1 e Ikonli 12.3.1)
* **Mapeamento ORM:** Hibernate Framework 6.4.4.Final & Jakarta Persistence 3.1.0
* **Banco de Dados:** H2 Database 2.2.224 (Armazenamento em arquivo local)
* **Orquestração de IA:** LangChain4j 1.16.2 & LangChain4j-Ollama 1.15.1

---

## 📁 Configurações e Armazenamento Local

Para garantir a portabilidade e facilidade de deploy do totem em modo quiosque offline, a aplicação centraliza seus dados e arquivos de configuração no diretório local do usuário:

* **Diretório Raiz dos Dados:** `~/.totemassets`
* **Banco de Dados Relacional (H2):** Armazenado no arquivo local `~/.totemassets/dados/bd.mv.db`.
* **Arquivo de Configurações da IA:** `~/.totemassets/config.properties`
  O sistema gera este arquivo automaticamente na primeira execução com chaves vazias. Você pode editá-lo para apontar para a infraestrutura desejada:
  ```properties
  # URL do servidor do Ollama (padrão local se vazio: http://localhost:11434)
  OLLAMA_BASE_URL=http://localhost:11434
  
  # Nome do modelo principal para BI (padrão se vazio: qwen2.5:7b)
  AI_MODEL=qwen2.5:7b
  
  # Nome do modelo de classificação sob demanda (padrão se vazio: hf.co/cnmoro/Gemma-3-Gaia-PT-BR-4b-it-Q8_0-GGUF:Q8_0)
  AI_FEEDBACK=hf.co/cnmoro/Gemma-3-Gaia-PT-BR-4b-it-Q8_0-GGUF:Q8_0
  ```

---

## 🔑 Credenciais Administrativas de Desenvolvimento (Bypass)

Para facilitar a homologação, testes e a configuração inicial do banco de dados (cadastrar os primeiros produtos ou gerentes reais), o sistema possui um login de bypass para administradores na tela de login de gerenciamento:

* **CPF:** `admin`
* **Senha:** `admin`

*Nota: Esse bypass permite o acesso direto ao menu de gerenciamento sem a necessidade de registros persistidos na base de dados H2.*

---

## 🚀 Primeiros Passos

### Pré-requisitos

* **Java Development Kit (JDK) 21** ou superior instalado.
* **Apache Maven** instalado.
* **Ollama** ativo e executando na porta padrão (ou configurado no `.totemassets`).

### Configurando os Modelos locais no Ollama

Antes de rodar a aplicação, baixe os modelos recomendados no seu servidor Ollama executando os seguintes comandos no terminal:

```bash
# Baixar o modelo principal para o Chat de BI (Qwen-2.5 7B)
ollama pull qwen2.5:7b

# Baixar o modelo especialista de classificação (Gemma-3 Gaia 4B em português)
ollama pull hf.co/cnmoro/Gemma-3-Gaia-PT-BR-4b-it-Q8_0-GGUF:Q8_0
```

### Compilação e Execução

Para compilar o projeto e inicializar o terminal TotemMind, execute as seguintes diretivas:

```bash
# Limpar compilações anteriores e gerar o pacote
mvn clean package

# Inicializar o terminal do totem (JavaFX)
mvn javafx:run
```

---

## 💬 Capacidades do Assistente de BI (KICER)

O assistente **KICER** é especialista em extrair dados do estabelecimento. Você pode interagir com ele através das seguintes funcionalidades:

### Área de Produtos:
* **Métricas de Qualidade:** *"Média de estrelas por categoria"* ou *"quais categorias de produtos têm pior avaliação?"*
* **Pesquisa de Itens:** *"Buscar produto 'Hambúrguer'"* (ele exibirá o card com as avaliações no painel e o ID).
* **Análise Visual:** *"Gere um gráfico de estrelas para o hambúrguer"* (gera gráfico de pizza) ou *"Gráfico geral de categorias de produto"*.

### Área de Atendimento (Funcionários):
* **Identificação:** *"Buscar funcionário João"* (lista opções correspondentes ocultando o ID).
* **Satisfação Geral:** *"Gere o gráfico de satisfação do João"* ou *"Exiba a evolução temporal do João"* (gráfico de linhas).
* **Mapeamento de Queixas/Elogios:** *"Gráfico de categorias do funcionário Carlos"*.
* **Rankings de Equipe:** *"Qual funcionário tem a maior taxa de satisfação?"* ou *"Quem tem a maior insatisfação?"*.
