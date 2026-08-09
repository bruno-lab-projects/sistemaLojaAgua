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
git clone https://github.com/projects-bruno/sistemaLojaAgua.git

# Entre na pasta
cd sistemaLojaAgua

# Execute via Maven (Linux/Mac/Windows)
mvn clean javafx:run
```

## 🔄 Atualização remota

O programa instalado se atualiza sozinho a partir dos GitHub Releases.

### Publicar uma versão nova

A tag é o que dispara a publicação e define o número da versão:

```bash
git tag v1.2.0
git push origin v1.2.0
```

O workflow compila, cria o release e publica quatro arquivos: o ZIP da
instalação completa, o `SistemaLoja.jar`, o `SistemaLoja.jar.sha256` e o
`version.txt`. Os três últimos são o que o programa consulta sozinho.

> Rodar o workflow pela aba Actions (`workflow_dispatch`) gera só um artifact
> para teste — **não** cria release e portanto não aparece como atualização
> para a loja.

### O que acontece na loja

1. Ao abrir o programa (e a cada 4 horas), ele lê o `version.txt` do release.
2. Se houver versão nova, aparece uma faixa no topo da tela. Nada é baixado
   sem clique, e a mensagem some com "Lembrar depois" até o dia seguinte.
3. O botão **Atualizar** baixa o JAR novo para `update\SistemaLoja.jar.new` e
   confere o SHA-256. O programa em uso **não** é substituído nesse momento —
   no Windows o arquivo fica travado enquanto a aplicação está aberta.
4. A troca acontece no próximo início, feita pelo `INICIAR.bat`.

Sem internet, nada aparece na tela: a falha vai só para
`~/.distribuidora_agua/logs/`.

O banco fica em `~/.distribuidora_agua/`, fora da pasta do programa, então
atualizar **nunca** apaga clientes, pedidos ou histórico.

### Reverter uma versão com problema

Na pasta do programa, com ele fechado:

1. Apague o `SistemaLoja.jar`.
2. Renomeie `update\SistemaLoja.jar.bak` para `SistemaLoja.jar`.
3. Abra o `INICIAR.bat` normalmente.

O `.bak` é a versão que estava rodando antes da última atualização.

## 🔒 Privacidade
-  Este repositório contém a estrutura funcional do sistema. Dados reais de clientes e transações foram removidos para garantir a privacidade do estabelecimento comercial.

[Desenvolvido por Bruno Barreto](https://github.com/projects-bruno)
