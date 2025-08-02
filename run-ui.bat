@echo off
echo Starting ChatGPT Clone UI...
echo.
echo Note: If you haven't set up your OpenAI API key, the application will use a placeholder key
echo and provide simulated responses. To use real AI responses, edit config.properties
echo and replace "sk-your-api-key-here" with your actual OpenAI API key.
echo.

REM Compile the project
call mvn compile

REM Run the UI application
call mvn exec:java -Dexec.mainClass="com.chatgpt.clone.ui.ChatGPTApp"

echo Application closed.
pause