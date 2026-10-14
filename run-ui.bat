@echo off
title RetailIQ Intelligence Hub
echo ===================================================
echo   Launching RetailIQ Desktop Intelligence Platform
echo ===================================================
java --module-path lib --add-modules javafx.controls,javafx.graphics -cp "bin;lib/*" retailiq.ui.RetailIQLauncher
pause
