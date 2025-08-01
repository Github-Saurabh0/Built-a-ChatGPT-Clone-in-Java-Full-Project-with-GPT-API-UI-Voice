#!/bin/bash

echo "Building the project..."
mvn clean package

echo ""
echo "Running the GPT example application..."
echo ""

mvn exec:java -Dexec.mainClass="com.chatgpt.clone.example.GPTExample"