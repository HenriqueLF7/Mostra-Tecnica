@echo off

title Jogo Zumbi

:: Garante que o .bat sempre rode a partir da pasta do projeto
cd /d "%~dp0"

echo ================================
echo          JOGO ZUMBI
echo ================================
echo.

echo Compilando o jogo...
echo.

if not exist "backend\bin" mkdir "backend\bin"

:: Limpa compilacoes antigas
if exist "backend\bin\*.class" del /q "backend\bin\*.class"

:: Compila os arquivos Java do projeto
javac -encoding UTF-8 -cp "backend\lib\sqlite-jdbc-3.53.4.0.jar" -d "backend\bin" ^
    "backend\src\Main.java" ^
    "backend\src\Database.java" ^
    "backend\src\Zombie.java" ^
    "backend\src\Player.java"

if errorlevel 1 (
    echo.
    echo ERRO: Nao foi possivel compilar o Java.
    echo.
    pause
    exit /b
)

echo.
echo Java compilado com sucesso!
echo.
echo Iniciando servidor...
echo.

start "Servidor Jogo Zumbi" cmd /k java -cp "backend\bin;backend\lib\sqlite-jdbc-3.53.4.0.jar" Main

timeout /t 2 /nobreak >nul

echo Abrindo o jogo...
start "" "http://localhost:8080"

exit
