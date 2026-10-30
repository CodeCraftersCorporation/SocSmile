#!/bin/bash
set -e

echo "Preparing application files..."

# Копируем статические файлы в out/static (если нет)
mkdir -p out/static
cp -r src/main/resources/static/* out/static/ 2>/dev/null || true

echo "Build successful!"
