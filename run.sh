#!/bin/bash

# Script para ejecutar el juego Zorrito
# Compila si es necesario y ejecuta el JAR

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=== Zorrito Game Runner ===${NC}"
echo ""

# Verifica si existe el JAR compilado
JAR_FILE="./Compilado/jar/Zorrito.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}JAR no encontrado. Compilando...${NC}"
    ./compilar.sh
    echo ""
fi

# Verifica que el JAR existe después de compilar
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: No se pudo crear el JAR"
    exit 1
fi

echo -e "${GREEN}Ejecutando Zorrito...${NC}"
echo ""

# Ejecuta el juego pasando los argumentos recibidos
java -jar "$JAR_FILE" "$@"
