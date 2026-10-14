@echo off
title RetailIQ Web Intelligence Platform
echo ===================================================
echo   Starting RetailIQ Web Platform (White & Olive)
echo   Access at: http://localhost:8080
echo ===================================================

start "" http://localhost:8080
java --module-path lib --add-modules javafx.controls,javafx.graphics,jdk.httpserver -cp "bin;lib/*" retailiq.web.RetailIQWebServer
pause
