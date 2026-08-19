@echo off
setlocal

set "GRADLE_VERSION=9.2.1"
set "GRADLE_CACHE=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin"
set "GRADLE_HOME=%GRADLE_CACHE%\gradle-%GRADLE_VERSION%"
set "GRADLE_ZIP=%TEMP%\gradle-%GRADLE_VERSION%-bin.zip"
set "GRADLE_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip"

if not exist "%GRADLE_HOME%\bin\gradle.bat" (
    echo Gradle %GRADLE_VERSION% was not found. Downloading it to %GRADLE_CACHE%...
    if not exist "%GRADLE_CACHE%" mkdir "%GRADLE_CACHE%"
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; Invoke-WebRequest -UseBasicParsing -Uri '%GRADLE_URL%' -OutFile '%GRADLE_ZIP%'; Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%GRADLE_CACHE%' -Force"
    if errorlevel 1 (
        echo Failed to download Gradle. Check your internet connection and try again.
        exit /b 1
    )
    del /q "%GRADLE_ZIP%" >nul 2>nul
)

call "%GRADLE_HOME%\bin\gradle.bat" %*
exit /b %errorlevel%