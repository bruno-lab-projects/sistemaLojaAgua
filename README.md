# Sistema de Gestão - Distribuidora de Água

Aplicação Desktop desenvolvida sob medida para modernizar o gerenciamento de vendas e estoque de uma distribuidora de água mineral.

> 🎓 **Contexto Acadêmico:** Este software foi desenvolvido e aprovado como **Projeto de Extensão Universitária** (PUCPR), unindo requisitos acadêmicos rigorosos à solução de um problema real de negócio.

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/JavaFX-4285F4?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/sqlite-%2307405e.svg?style=for-the-badge&logo=sqlite&logoColor=white)

## 📋 Sobre o Projeto
O sistema foi projetado a partir do **levantamento de requisitos** realizado diretamente com o proprietário da distribuidora. O objetivo principal foi substituir controles manuais por uma solução digital que atendesse regras de negócio específicas do nicho de água mineral.

**Principais Problemas Resolvidos:**
* Controle de vasilhames/garrafões (quem levou o casco e não devolveu).
* Gestão de pagamentos pendentes.
* Visão clara do faturamento diário.

## ⚙️ Funcionalidades Detalhadas

### 📊 Dashboard Gerencial
Visualização rápida de métricas essenciais para a tomada de decisão:
- Total vendido no dia.
- Gráficos de desempenho mensal.

### 💰 Gestão Financeira e Vendas
- **Controle de Pagamentos:** Suporte a múltiplas formas de pagamento (Pix, Dinheiro, Cartão) e registro de vendas a prazo (pendentes).
- **Logística Reversa:** Checkbox específico para marcar se o cliente devolveu o garrafão vazio no ato da compra ou se ficou pendente.

### 👥 Gestão de Clientes
- Cadastro completo para entrega.
- Histórico de compras e débitos.

## 📸 Screenshots

<div align="center">
  <h3>Dashboard e Métricas</h3>
  <img src="https://github.com/user-attachments/assets/bdb3f46a-4b47-4d56-8044-41305ca3d6bf" width="85%" alt="Dashboard Visão Geral" />
  <br><br>
  <img src="https://github.com/user-attachments/assets/0e92deef-e123-45a9-91d4-6a4952d5d013" width="85%" alt="Gráficos de Vendas" />
  <br><br>
  <img src="https://github.com/user-attachments/assets/6127d30b-f3e5-496c-a56b-f25116e01232" width="85%" alt="Tabela de Dados" />

  <h3>Tela de Vendas (PDV)</h3>
  <img src="https://github.com/user-attachments/assets/1f441050-64eb-4f57-b2cc-849c3a238c2f" width="85%" alt="Ponto de Venda" />
</div>

## 🛠️ Tecnologias e Arquitetura

- **Linguagem:** Java 11+.
- **Interface:** JavaFX (Modular) com FXML.
- **Banco de Dados:** SQLite (JDBC) - Escolhido pela portabilidade e por não exigir instalação de servidor no cliente.
- **Build Tool:** Maven (com plugin Shade para geração de Fat JAR).

## 🚀 Como Rodar o Projeto

```bash
# Clone o repositório
git clone [https://github.com/brunombs/sistemaLojaAgua.git](https://github.com/brunombs/sistemaLojaAgua.git)

# Entre na pasta
cd sistemaLojaAgua

# Execute via Maven (Linux/Mac/Windows)
mvn clean javafx:run
