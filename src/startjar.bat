@echo off

start javaw -cp "autoUpdate_v2.0.jar"  update.AutoUpdateJob "http://localhost/autoUpdate_v{ver}.jar"

exit

@pause
