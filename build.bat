@echo off
setlocal
where gradle >nul 2>nul
if errorlevel 1 (
  echo Nie znaleziono Gradle. Uruchom projekt w IntelliJ IDEA lub zainstaluj Gradle.
  pause
  exit /b 1
)
gradle build
if errorlevel 1 exit /b 1
echo Gotowy plik JAR znajduje sie w build\libs\
pause
