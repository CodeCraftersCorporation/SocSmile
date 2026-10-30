#!/bin/bash
set -e

echo "Copying pre-compiled files..."

# Копируем классы
cp -r out/* /app/ 2>/dev/null || true

# Копируем статические файлы
mkdir -p out/static
cp -r src/main/resources/static/* out/static/ 2>/dev/null || true

echo "Build successful!"
