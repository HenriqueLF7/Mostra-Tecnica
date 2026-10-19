@echo off
title Jogo Zumbi
cd /d "%~dp0"

echo ================================
echo          JOGO ZUMBI
echo ================================
echo.

:: Usa o Java embutido na pasta "runtime" (nao precisa ter Java instalado).
:: Se a pasta runtime nao existir, tenta o Java do computador.
set "JAVAC=javac"
set "JAVA=java"
if exist "runtime\bin\java.exe" (
    set "JAVAC=runtime\bin\javac.exe"
    set "JAVA=runtime\bin\java.exe"
)

echo Compilando o jogo...
if exist "backend\bin" rmdir /s /q "backend\bin"
mkdir "backend\bin"

"%JAVAC%" -encoding UTF-8 -sourcepath "backend\src" -d "backend\bin" "backend\src\Main.java"

if errorlevel 1 (
    echo.
    echo ERRO: Nao foi possivel compilar o Java.
    pause
    exit /b
)

echo Java compilado com sucesso!
echo.
echo Iniciando servidor...
start "Servidor Jogo Zumbi" cmd /k ""%JAVA%" -cp "backend\bin" Main"

timeout /t 2 /nobreak >nul
start "" "http://localhost:8080"
exit
