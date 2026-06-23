@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-compat.ps1" -Target Folia
exit /b %ERRORLEVEL%
