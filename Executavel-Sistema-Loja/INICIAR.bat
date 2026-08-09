@echo off
cd /d "%~dp0"

REM ---------------------------------------------------------------
REM  Aplica a atualizacao baixada, se houver.
REM
REM  Precisa acontecer AQUI, antes de abrir o programa: enquanto a JVM
REM  esta rodando o Windows trava o SistemaLoja.jar e ele nao pode ser
REM  substituido. Por isso o programa so deixa o arquivo pronto em
REM  update\SistemaLoja.jar.new e quem troca de verdade e este script.
REM
REM  Se a troca falhar, o JAR antigo continua no lugar e funcionando.
REM  O .bak e o caminho para reverter a mao uma versao com problema.
REM ---------------------------------------------------------------
if exist "update\SistemaLoja.jar.new" (
    echo Aplicando atualizacao, aguarde...
    if exist "SistemaLoja.jar" move /y "SistemaLoja.jar" "update\SistemaLoja.jar.bak" >nul
    move /y "update\SistemaLoja.jar.new" "SistemaLoja.jar" >nul

    REM Se o segundo move falhou, a loja ficaria sem JAR nenhum. Volta o antigo.
    if not exist "SistemaLoja.jar" (
        echo A atualizacao falhou. Restaurando a versao anterior...
        if exist "update\SistemaLoja.jar.bak" move /y "update\SistemaLoja.jar.bak" "SistemaLoja.jar" >nul
    )
)

where javaw >nul 2>nul
if errorlevel 1 goto sem_java

if not exist "SistemaLoja.jar" (
    echo.
    echo Arquivo SistemaLoja.jar nao encontrado nesta pasta.
    echo Verifique se o arquivo foi extraido junto com este INICIAR.bat.
    echo.
    pause
    exit /b 1
)

start "" javaw -jar SistemaLoja.jar
exit /b 0

:sem_java
echo.
echo ====================================================
echo   O Java nao esta instalado neste computador.
echo ====================================================
echo.
echo   O sistema precisa do Java 17 (ou superior) para funcionar.
echo.
echo   Baixe em: https://adoptium.net/temurin/releases/
echo   Escolha: Windows / x64 / JRE / versao 17
echo.
echo   Depois de instalar, abra este arquivo novamente.
echo.
pause
exit /b 1