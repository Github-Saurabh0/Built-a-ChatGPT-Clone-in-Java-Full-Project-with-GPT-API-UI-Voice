@echo off
echo Building the project...
call mvn clean package

echo.
echo Running the GPT example application...
echo.

call mvn exec:java -Dexec.mainClass="com.chatgpt.clone.example.GPTExample"

pause