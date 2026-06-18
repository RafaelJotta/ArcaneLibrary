# ArcaneLibrary — Biblioteca Arcana e Controle de Conhecimento

Projeto desenvolvido para a disciplina de Sistemas Operacionais, com foco em **concorrência, sincronização de processos, leitores e escritores, prioridade, starvation, deadlock e race condition**.

A aplicação simula uma biblioteca arcana onde vários magos tentam acessar grimórios compartilhados ao mesmo tempo. Cada tipo de mago representa um comportamento concorrente diferente dentro do sistema.

---

## Tema do Projeto

O projeto utiliza a metáfora de uma **biblioteca mágica** para representar o problema clássico dos **leitores e escritores**.

Na simulação:

* **Consultas simples** representam leitores comuns.
* **Pesquisas críticas** representam leitores com prioridade elevada.
* **Rituais mágicos** representam escritores que precisam de acesso exclusivo.
* **Rituais críticos** representam escritores de prioridade maior.

O objetivo é controlar o acesso aos grimórios de forma segura, evitando inconsistências, race condition e starvation.

---

## Objetivo

Implementar uma simulação concorrente capaz de demonstrar:

* Acesso simultâneo de leitores.
* Acesso exclusivo de escritores.
* Prioridade para pesquisas críticas.
* Controle de starvation.
* Cenários com múltiplos recursos compartilhados.
* Situações problemáticas como race condition e deadlock.
* Métricas em tempo real sobre a execução.

---

## Tecnologias Utilizadas

* Java 21
* JavaFX
* Maven
* Threads
* Programação concorrente
* Interface gráfica com elementos 3D
* Git e GitHub

---

## Estrutura Conceitual

### Grimórios

Os grimórios representam os recursos compartilhados da aplicação. Eles podem ser consultados por vários leitores simultaneamente, mas só podem ser modificados por um escritor por vez.

### Magos

Cada agente da simulação é representado por um mago.

| Tipo de agente   | Papel no problema    | Acesso                       |
| ---------------- | -------------------- | ---------------------------- |
| Consulta simples | Leitor comum         | Compartilhado                |
| Pesquisa crítica | Leitor prioritário   | Compartilhado com prioridade |
| Ritual mágico    | Escritor             | Exclusivo                    |
| Ritual crítico   | Escritor prioritário | Exclusivo                    |

---

## Cenários Implementados

A aplicação possui diferentes cenários para demonstrar comportamentos de sincronização.

### 1. Política Equilibrada

Simula o controle correto de acesso a um grimório, permitindo múltiplas leituras simultâneas e garantindo exclusividade para os rituais de escrita.

### 2. Biblioteca com Múltiplos Grimórios

Simula vários recursos compartilhados ao mesmo tempo. Na interface, cada grimório aparece como um livro arcano flutuante.

### 3. Race Condition

Demonstra o problema de acesso sem sincronização adequada, evidenciando riscos de inconsistência nos dados.

### 4. Deadlock

Simula uma situação em que agentes podem ficar bloqueados aguardando recursos entre si.

### 5. Starvation

Demonstra o problema de inanição, quando um tipo de agente pode ficar esperando indefinidamente se não houver uma política justa.

---

## Interface Gráfica

A interface foi desenvolvida em JavaFX com uma temática de biblioteca arcana.

Principais elementos visuais:

* Tela inicial com portal arcano.
* Fundo de biblioteca mágica.
* Livros flutuantes em 3D representando os grimórios.
* Magos 3D representando os agentes concorrentes.
* Anéis mágicos animados ao redor dos livros.
* Fila de acesso em tempo real.
* Métricas da simulação.
* Log de eventos.
* Botões para iniciar, parar e voltar ao portal.

---

## Métricas Exibidas

Durante a execução, a aplicação exibe informações como:

* Número de leitores ativos.
* Escritor ativo.
* Quantidade de leitores esperando.
* Quantidade de escritores esperando.
* Leituras concluídas.
* Escritas concluídas.
* Controle de burst crítico.
* Estado atual do acesso ao grimório.

---

## Como Executar

### Requisitos

Antes de executar, é necessário ter instalado:

* Java 21
* Maven

### Compilar o projeto

```bash
mvn clean compile
```

### Executar a aplicação

```bash
mvn javafx:run
```

---

## Organização do Projeto

Estrutura principal do projeto:

```text
src/main/java/br/edu/ifsuldeminas/rafael/arcanelibrary
├── domain
├── events
├── simulation
├── synchronization
└── ui/gui
```

### Pacotes principais

* `domain`: classes e enums do domínio da aplicação.
* `events`: eventos e observadores da simulação.
* `simulation`: motor da simulação e configuração dos agentes.
* `synchronization`: políticas de sincronização.
* `ui/gui`: interface gráfica em JavaFX.

---

## Principais Classes

### `SimulationEngine`

Responsável por iniciar, parar e controlar a execução da simulação.

### `SimulationConfig`

Define configurações como quantidade de leitores, escritores e tempos de execução.

### `AccessRequest`

Representa uma solicitação de acesso feita por um agente.

### `MageAccessType`

Define os tipos de acesso possíveis: consulta simples, pesquisa crítica, ritual mágico e ritual crítico.

### `ArcaneSynchronizationCoordinator`

Controla o acesso concorrente ao grimório, aplicando regras de sincronização.

### `MainController`

Controla a interface gráfica, animações, métricas, fila e visualização 3D.

---

## Funcionalidades

* Simulação concorrente com múltiplos agentes.
* Configuração da quantidade de agentes pela interface.
* Visualização gráfica em tempo real.
* Controle de acesso compartilhado e exclusivo.
* Cenários didáticos para problemas clássicos de Sistemas Operacionais.
* Interface temática com elementos 3D.
* Logs e métricas em tempo real.

---

## Conceitos de Sistemas Operacionais Aplicados

Este projeto aplica diretamente conceitos estudados em Sistemas Operacionais:

* Threads.
* Concorrência.
* Região crítica.
* Exclusão mútua.
* Problema dos leitores e escritores.
* Starvation.
* Deadlock.
* Race condition.
* Sincronização.
* Políticas de prioridade.
* Controle de acesso a recursos compartilhados.

---

## Possíveis Melhorias Futuras

Algumas melhorias que podem ser adicionadas futuramente:

* Substituir objetos 3D por sprites ou GIFs animados.
* Adicionar sons e efeitos visuais.
* Permitir salvar relatórios da simulação.
* Criar gráficos estatísticos ao final da execução.
* Exportar métricas para arquivo.
* Melhorar a representação visual dos magos.
* Adicionar novos cenários de concorrência.

---

## Autor

**Rafael Jotta Sobrinho**

Curso de Engenharia de Computação
IFSULDEMINAS — Campus Poços de Caldas

---

## Status

Projeto funcional para demonstração dos conceitos de concorrência e sincronização em Sistemas Operacionais.
