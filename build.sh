#!/bin/bash
set -e

# Скачиваем JDK 21 через Adoptium API
JDK_DIR="$HOME/.local/jdk"
OS="linux"
ARCH="x64"
JDK_VERSION="21.0.7+7"

if [ ! -d "$JDK_DIR" ]; then
  echo "Downloading JDK 21..."
  # Получаем прямую ссылку через API
  DOWNLOAD_URL=$(curl -sL "https://api.adoptium.net/v3/assets/latest/21/hotspot?architecture=$ARCH&os=$OS&image_type=jdk&vendor=eclipse" | grep -o '"binaries.*download_link[^"]*"' | grep -o 'https://[^"]*')
  echo "Download URL: $DOWNLOAD_URL"
  
  JDK_FILE="/tmp/jdk.tar.gz"
  curl -sL "$DOWNLOAD_URL" -o "$JDK_FILE"
  
  mkdir -p "$JDK_DIR"
  tar -xzf "$JDK_FILE" -C "$JDK_DIR" --strip-components=1
  rm "$JDK_FILE"
fi

export JAVA_HOME="$JDK_DIR"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Java version: $(java -version 2>&1)"

# Скачиваем Maven
MAVEN_VERSION=3.9.6
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}-bin"

if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven..."
  mkdir -p "$MAVEN_HOME"
  curl -sL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip" -o /tmp/maven.zip
  unzip -q /tmp/maven.zip -d "$MAVEN_HOME"
  rm /tmp/maven.zip
fi

echo "Running Maven build..."
"$MAVEN_HOME/apache-maven-${MAVEN_VERSION}/bin/mvn" clean package -DskipTests

echo "Build successful!"
