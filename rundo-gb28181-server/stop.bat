@echo off
for /f "delims= " %%a in ('jps -lv ^| find /i "rundo-gb28181-server-1.0.0.jar"') do set PID=%%a
taskkill /f /t /PID %PID%
exit
echo "stop success"
pause