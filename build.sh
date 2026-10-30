#!/bin/bash
set -e

# Скачиваем JDK 21
JDK_DIR="$HOME/.local/jdk"

if [ ! -d "$JDK_DIR" ]; then
  echo "Downloading JDK 21..."
  JDK_FILE="/tmp/jdk.tar.gz"
  curl -sL "https://download.java.net/java/GA/jdk21.0.7/f2f29f8c3826406397ebd1d4b9d0d690/35/GPL/openjdk-21.0.7_linux-x64_bin.tar.gz" -o "$JDK_FILE"
  
  mkdir -p "$JDK_DIR"
  tar -xzf "$JDK_FILE" -C "$JDK_DIR" --strip-components=1
  rm "$JDK_FILE"
fi

export JAVA_HOME="$JDK_DIR"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Java version: $(java -version 2>&1)"

# Компилируем без Maven
echo "Compiling..."
javac -d out $(find src -name "*.java")
echo "Compilation successful!"

# Копируем статические файлы
mkdir -p out/static
cp -r src/main/resources/static/* out/static/
echo "Static files copied."

echo "Build successful!"
