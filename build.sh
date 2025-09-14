#!/bin/bash

# SecureVault Build Script
# This script builds and optionally runs the SecureVault password manager

set -e

echo "🔐 SecureVault Build Script"
echo "=========================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    echo "   macOS: brew install maven"
    echo "   Ubuntu: sudo apt install maven"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt "17" ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java $JAVA_VERSION detected"
echo "✅ Maven detected"
echo ""

# Build the project
echo "🔨 Building SecureVault..."
mvn clean package -q

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    echo "📦 JAR file created: target/securevault.jar"
    echo ""
    
    # Ask if user wants to run the application
    read -p "🚀 Do you want to run SecureVault now? (y/n): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "🔐 Starting SecureVault..."
        echo ""
        java -jar target/securevault.jar
    else
        echo "💡 To run SecureVault later, use: java -jar target/securevault.jar"
    fi
else
    echo "❌ Build failed!"
    exit 1
fi
