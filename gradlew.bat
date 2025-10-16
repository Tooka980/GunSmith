@rem Gradle wrapper bat script
@echo off
setlocal
set DIR=%~dp0
if "%DIR%"=="" set DIR=.
set APP_HOME=%DIR%
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
set WRAPPER_JAR=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
set WRAPPER_MAIN=org.gradle.wrapper.GradleWrapperMain
set JAVA_EXE=
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)
if not defined JAVA_EXE (
    for %%i in (java.exe) do (
        where %%i >nul 2>&1 && set JAVA_EXE=%%~$PATH:i
    )
)
if not defined JAVA_EXE (
    echo.
    echo ERROR: JAVA_HOME is not set and no "java" command could be found in your PATH.
    echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
    exit /b 1
)
if not exist "%WRAPPER_JAR%" (
    echo.
    echo WARNING: gradle-wrapper.jar is missing in gradle\\wrapper.
    echo Please generate it locally by running: gradle wrapper --gradle-version 8.10.2
    exit /b 1
)
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% -classpath "%WRAPPER_JAR%" %WRAPPER_MAIN% %*
endlocal
