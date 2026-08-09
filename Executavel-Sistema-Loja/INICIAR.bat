@echo off
cd /d "%~dp0"

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