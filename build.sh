#!/bin/bash
set -e

echo "Preparing application files..."

# Копируем классы из out/
mkdir -p /app/out
cp -r out/* /app/out/

# Копируем статические файлы в out/static
mkdir -p out/static
cp -r src/main/resources/static/* out/static/

# Копируем обновлённые статические файлы
cp -r out/static/* /app/out/static/

echo "Build successful!"
