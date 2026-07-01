@echo off
call mvn clean package
exit /b %ERRORLEVEL%
