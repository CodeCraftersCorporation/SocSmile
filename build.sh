#!/bin/bash
set -e

MAVEN_VERSION=3.9.6
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}-bin"

if [ ! -d "$MAVEN_HOME" ]; then
  mkdir -p "$MAVEN_HOME"
  curl -sL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip" -o /tmp/maven.zip
  unzip -q /tmp/maven.zip -d "$MAVEN_HOME"
  rm /tmp/maven.zip
fi

"$MAVEN_HOME/apache-maven-${MAVEN_VERSION}/bin/mvn" clean package -DskipTests
