#!/bin/bash

# Script para ejecutar el juego Zorrito
# Compila y ejecuta directamente desde clases (más rápido para desarrollo)

# Colores para output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${YELLOW}=== Zorrito Game Runner ===${NC}"
echo ""

# Directorio de clases compiladas
BUILD_DIR="./Compilado/classes"
mkdir -p "$BUILD_DIR"

# Compila el código fuente
echo -e "${YELLOW}Compilando...${NC}"
if ! javac -d "$BUILD_DIR" src/*.java 2>&1; then
    echo -e "${RED}Error de compilación${NC}"
    exit 1
fi
echo -e "${GREEN}Compilado OK${NC}"
echo ""

echo -e "${GREEN}Ejecutando Zorrito...${NC}"
echo -e "${YELLOW}(Los mensajes de debug aparecerán aquí)${NC}"
echo ""

# Ejecuta el juego pasando los argumentos recibidos
java -cp "$BUILD_DIR" Zorrito "$@"
