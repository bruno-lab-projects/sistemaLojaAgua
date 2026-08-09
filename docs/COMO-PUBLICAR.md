# Como publicar uma versão nova

Guia rápido para o dia a dia: o que fazer quando você só quer salvar o código, e
o que fazer quando quer que a mudança chegue na loja.

---

## As duas situações

| O que você quer | O que usar | A loja vê? |
|---|---|---|
| Salvar o código no GitHub | `git push` | Não |
| Publicar uma versão para a loja | `git push` + **tag** | Sim, na próxima abertura |

---

## O que é uma tag

Uma **tag** é um marcador permanente num commit específico. Diferente de uma
branch, que anda para frente a cada commit novo, a tag fica presa naquele ponto
para sempre. Serve para dizer "este commit aqui é a versão 1.2.0".

Um detalhe importante: **`git push` normal não envia tags.** Elas precisam ser
enviadas explicitamente. É justamente isso que torna a tag um bom gatilho de
publicação — você nunca publica sem querer.

No `.github/workflows/build-windows.yml`, o robô do GitHub só acorda quando
chega uma tag que começa com `v`:

```yaml
on:
  push:
    tags: ['v*']
```

E a versão sai do próprio nome da tag (`v1.2.0` → `1.2.0`), indo parar dentro do
JAR e no `version.txt`. A tag é a **fonte única da verdade** sobre a versão —
não é preciso editar o `pom.xml` a cada release.

---

## Situação A — só salvar o código no GitHub

Trabalho do dia a dia: corrigiu algo, mexeu num texto, ainda não quer publicar.

```bash
git add -A
git commit -m "fix: corrige o total do pedido"
git push
```

Não acontece nada para a loja: nenhum release é criado, nenhuma faixa aparece na
tela do cliente. O código fica guardado no GitHub, só isso.

---

## Situação B — publicar uma versão para a loja

Quando o que está na `main` já está pronto para ir para a rua.

**1.** Garanta que o código está no GitHub (senão você marca um commit que só
existe na sua máquina):

```bash
git push
```

**2.** Crie a tag e envie:

```bash
git tag v1.2.1
git push origin v1.2.1
```

**3.** Acompanhe pela aba **Actions** do repositório. Quando terminar, confira na
página do release que os 4 arquivos estão lá:

- `Sistema-Loja-Windows-v1.2.1.zip` — instalação completa, para uma máquina nova
- `SistemaLoja.jar` — usado pela atualização automática
- `SistemaLoja.jar.sha256` — verificação de integridade do download
- `version.txt` — o que o programa lê para saber que existe versão nova

**4.** Um teste rápido no navegador, que responde a pergunta "o mecanismo está
enxergando a versão nova?":

```
https://github.com/projects-bruno/sistemaLojaAgua/releases/latest/download/version.txt
```

Tem que abrir mostrando o número da versão que você acabou de publicar.

Daí em diante é automático: na próxima abertura do programa, a loja vê a faixa.

---

## Como escolher o número

O padrão do mercado é `MAIOR.MENOR.CORREÇÃO` (chamado *versionamento semântico*):

| Situação | Vai de | Para |
|---|---|---|
| Corrigiu um bug | v1.2.0 | **v1.2.1** |
| Adicionou uma funcionalidade | v1.2.1 | **v1.3.0** |
| Mudou tudo, quebrou compatibilidade | v1.3.0 | **v2.0.0** |

O número **sempre precisa subir**, porque o `AppVersion` compara numericamente
para decidir se avisa a loja.

---

## Duas regras que evitam dor de cabeça

**1. Nunca reaproveite uma tag já publicada.** Se saiu errado, publique uma nova
(`v1.2.2`). Se você apagar e recriar a `v1.2.1` com outro conteúdo, uma loja pode
já ter baixado a versão antiga com esse mesmo número — e aí o hash não bate mais.

**2. Crie a tag a partir da `main`.** A tag marca o commit onde você está no
momento. Se tagear de uma branch de trabalho, publica código que ainda não foi
para a `main`. O costume é: mergeia na `main` → volta para a `main` → cria a tag.

Errou e ainda **não** deu push na tag? Dá para desfazer sem sequelas:

```bash
git tag -d v1.2.1
```

---

## Testar o build sem publicar

Na aba **Actions** do GitHub, o workflow também pode ser disparado no botão
"Run workflow" (`workflow_dispatch`). Nesse modo ele compila e disponibiliza o
resultado para download, mas **não cria release** — a versão sai como
`0.0.0-dev.N`, que o programa trata como inválida de propósito.

Serve para conferir que o build passa sem que a faixa de "nova versão" pisque na
tela da loja.

---

## Se uma versão sair com problema

Na máquina da loja, com o programa fechado:

1. Apague o `SistemaLoja.jar`.
2. Renomeie `update\SistemaLoja.jar.bak` para `SistemaLoja.jar`.
3. Abra o `INICIAR.bat` normalmente.

O `.bak` é a versão que estava rodando antes da última atualização — o
`INICIAR.bat` guarda ela automaticamente a cada troca.

Depois, corrija o problema e publique uma versão **maior** (nunca reaproveite a
que deu errado).

---

## Comandos úteis

Ver as tags que existem:

```bash
git tag
```

Ver em qual commit uma tag está:

```bash
git show v1.2.0 --stat
```

Apagar uma tag que **já foi** enviada (evite; prefira publicar uma nova):

```bash
git push origin --delete v1.2.1
```
