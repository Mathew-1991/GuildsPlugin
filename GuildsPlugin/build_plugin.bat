@echo off
REM === BUILD SCRIPT FOR GUILDSPLUGIN ===

REM Check for Spigot API
if not exist "lib\spigot-api.jar" (
  echo [ERROR] Missing lib\spigot-api.jar
  echo Place your Spigot/Paper 1.20.1 API jar here and rename it to: spigot-api.jar
  pause
  exit /b 1
)

REM Clean output folder
if exist "out" rmdir /S /Q out
mkdir out

echo.
echo === Compiling Java sources ===

javac -cp "lib\spigot-api.jar" -d out ^
  src\com\ravengrade\guilds\GuildsPlugin.java ^
  src\com\ravengrade\guilds\data\*.java ^
  src\com\ravengrade\guilds\managers\*.java ^
  src\com\ravengrade\guilds\listeners\*.java ^
  src\com\ravengrade\guilds\util\*.java ^
  src\com\ravengrade\guilds\gui\*.java ^
  src\com\ravengrade\guilds\commands\*.java

if %errorlevel% neq 0 (
  echo.
  echo [ERROR] javac reported compilation errors. Scroll up to see them.
  pause
  exit /b 2
)

echo.
echo === Copying resources ===
xcopy "resources" "out" /E /I /Y >nul

echo.
echo === Creating GuildsPlugin.jar ===
pushd out
if exist GuildsPlugin.jar del /Q GuildsPlugin.jar
jar cf GuildsPlugin.jar .
popd

if exist "GuildsPlugin.jar" del /Q "GuildsPlugin.jar"
move /Y "out\GuildsPlugin.jar" "GuildsPlugin.jar" >nul

echo.
echo Build complete: GuildsPlugin.jar
echo Put GuildsPlugin.jar into your server's plugins folder.
echo.
pause
exit /b 0
