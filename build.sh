#!/bin/bash
set -e

# Скачиваем JDK 21
JDK_URL="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B7/OpenJDK21U-jdk_x64_linux_hotspot_21.0.7_7.tar.gz"
JDK_DIR="$HOME/.local/jdk"
JDK_FILE="/tmp/jdk.tar.gz"

if [ ! -d "$JDK_DIR" ]; then
  echo "Downloading JDK 21..."
  curl -sL "$JDK_URL" -o "$JDK_FILE"
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
