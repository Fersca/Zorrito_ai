#!/bin/bash

# Script para ejecutar tests con JUnit 5 y JaCoCo (code coverage)
# Descarga las dependencias si no existen y ejecuta los tests

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m' # No Color

# Directorio del proyecto
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$PROJECT_DIR/lib"
TEST_DIR="$PROJECT_DIR/test"
BUILD_DIR="$PROJECT_DIR/build"
COVERAGE_DIR="$PROJECT_DIR/coverage"

# Versión de JUnit
JUNIT_VERSION="1.10.2"
JUNIT_JAR="junit-platform-console-standalone-${JUNIT_VERSION}.jar"
JUNIT_URL="https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/${JUNIT_JAR}"

# Versión de JaCoCo
JACOCO_VERSION="0.8.12"
JACOCO_AGENT_JAR="org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
JACOCO_CLI_JAR="org.jacoco.cli-${JACOCO_VERSION}-nodeps.jar"
JACOCO_AGENT_URL="https://repo1.maven.org/maven2/org/jacoco/org.jacoco.agent/${JACOCO_VERSION}/org.jacoco.agent-${JACOCO_VERSION}-runtime.jar"
JACOCO_CLI_URL="https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/${JACOCO_VERSION}/org.jacoco.cli-${JACOCO_VERSION}-nodeps.jar"

echo -e "${YELLOW}=== Zorrito Test Runner ===${NC}"
echo ""

# Crear directorios si no existen
mkdir -p "$LIB_DIR"
mkdir -p "$BUILD_DIR"
mkdir -p "$COVERAGE_DIR"

# Función para descargar si no existe
download_if_missing() {
    local file="$1"
    local url="$2"
    local name="$3"

    if [ ! -f "$LIB_DIR/$file" ]; then
        echo -e "${YELLOW}Descargando $name...${NC}"
        curl -sL -o "$LIB_DIR/$file" "$url"
        echo -e "${GREEN}$name descargado correctamente${NC}"
    else
        echo -e "${GREEN}$name ya existe en lib/${NC}"
    fi
}

# Descargar dependencias
download_if_missing "$JUNIT_JAR" "$JUNIT_URL" "JUnit 5"
download_if_missing "$JACOCO_AGENT_JAR" "$JACOCO_AGENT_URL" "JaCoCo Agent"
download_if_missing "$JACOCO_CLI_JAR" "$JACOCO_CLI_URL" "JaCoCo CLI"

echo ""
echo -e "${YELLOW}Compilando código fuente...${NC}"

# Compilar el código fuente principal con información de debug para coverage
# Usamos --release 21 para compatibilidad con JaCoCo (que aún no soporta Java 25)
if ! javac -g --release 21 -d "$BUILD_DIR" "$PROJECT_DIR"/src/*.java 2>&1; then
    echo -e "${RED}Error compilando código fuente${NC}"
    exit 1
fi

echo -e "${GREEN}Código fuente compilado${NC}"

echo ""
echo -e "${YELLOW}Compilando tests...${NC}"

# Compilar los tests
if ! javac -g --release 21 -cp "$BUILD_DIR:$LIB_DIR/$JUNIT_JAR" \
    -sourcepath "$PROJECT_DIR" \
    -d "$BUILD_DIR" \
    "$TEST_DIR"/*.java 2>&1; then
    echo -e "${RED}Error compilando tests${NC}"
    exit 1
fi

echo -e "${GREEN}Tests compilados${NC}"

echo ""
echo -e "${YELLOW}Ejecutando tests con coverage...${NC}"
echo ""

# Limpiar datos de coverage anteriores
rm -f "$COVERAGE_DIR/jacoco.exec"

# Ejecutar los tests con JaCoCo agent
# Usamos output-mode=none para suprimir el output de errores de JaCoCo
java -javaagent:"$LIB_DIR/$JACOCO_AGENT_JAR"=destfile="$COVERAGE_DIR/jacoco.exec",output=file \
    -jar "$LIB_DIR/$JUNIT_JAR" \
    --class-path "$BUILD_DIR" \
    --scan-class-path \
    --details=tree 2>/dev/null

# Capturar código de salida de los tests
TEST_EXIT_CODE=$?

echo ""
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}=== Todos los tests pasaron ===${NC}"
else
    echo -e "${RED}=== Algunos tests fallaron ===${NC}"
fi

# Generar reporte de coverage si existe el archivo exec
if [ -f "$COVERAGE_DIR/jacoco.exec" ]; then
    echo ""
    echo -e "${YELLOW}Generando reporte de coverage...${NC}"

    # Generar reporte CSV para parsear
    java -jar "$LIB_DIR/$JACOCO_CLI_JAR" report "$COVERAGE_DIR/jacoco.exec" \
        --classfiles "$BUILD_DIR/CollisionUtils.class" \
        --classfiles "$BUILD_DIR/MovementUtils.class" \
        --classfiles "$BUILD_DIR/MovementUtils\$DireccionResultado.class" \
        --classfiles "$BUILD_DIR/SpriteUtils.class" \
        --classfiles "$BUILD_DIR/SpriteUtils\$SpriteRegion.class" \
        --classfiles "$BUILD_DIR/GeneradorImagenes.class" \
        --classfiles "$BUILD_DIR/MovimientoHandler.class" \
        --sourcefiles "$PROJECT_DIR/src" \
        --csv "$COVERAGE_DIR/coverage.csv" \
        2>/dev/null

    # Generar reporte HTML
    java -jar "$LIB_DIR/$JACOCO_CLI_JAR" report "$COVERAGE_DIR/jacoco.exec" \
        --classfiles "$BUILD_DIR/CollisionUtils.class" \
        --classfiles "$BUILD_DIR/MovementUtils.class" \
        --classfiles "$BUILD_DIR/MovementUtils\$DireccionResultado.class" \
        --classfiles "$BUILD_DIR/SpriteUtils.class" \
        --classfiles "$BUILD_DIR/SpriteUtils\$SpriteRegion.class" \
        --classfiles "$BUILD_DIR/GeneradorImagenes.class" \
        --classfiles "$BUILD_DIR/MovimientoHandler.class" \
        --sourcefiles "$PROJECT_DIR/src" \
        --html "$COVERAGE_DIR/html" \
        2>/dev/null

    # Mostrar tabla de coverage
    echo ""
    echo -e "${CYAN}${BOLD}╔════════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}${BOLD}║                              CODE COVERAGE REPORT                              ║${NC}"
    echo -e "${CYAN}${BOLD}╠════════════════════════════════════════════════════════════════════════════════╣${NC}"
    echo -e "${CYAN}${BOLD}║  Class                    │ Lines     │ Branches  │ Methods   │ Complexity   ║${NC}"
    echo -e "${CYAN}${BOLD}╠═══════════════════════════╪═══════════╪═══════════╪═══════════╪══════════════╣${NC}"

    # Variables para totales
    TOTAL_LINE_MISSED=0
    TOTAL_LINE_COVERED=0
    TOTAL_BRANCH_MISSED=0
    TOTAL_BRANCH_COVERED=0
    TOTAL_METHOD_MISSED=0
    TOTAL_METHOD_COVERED=0

    # Parsear el CSV y mostrar la tabla
    if [ -f "$COVERAGE_DIR/coverage.csv" ]; then
        # Saltar la cabecera y procesar cada línea
        while IFS=',' read -r group package class instr_missed instr_covered branch_missed branch_covered line_missed line_covered complexity_missed complexity_covered method_missed method_covered; do
            # Solo mostrar las clases Utils principales (no inner classes)
            if [[ "$class" == "CollisionUtils" || "$class" == "MovementUtils" || "$class" == "SpriteUtils" || "$class" == "GeneradorImagenes" || "$class" == "MovimientoHandler" ]]; then
                # Acumular totales
                TOTAL_LINE_MISSED=$((TOTAL_LINE_MISSED + line_missed))
                TOTAL_LINE_COVERED=$((TOTAL_LINE_COVERED + line_covered))
                TOTAL_BRANCH_MISSED=$((TOTAL_BRANCH_MISSED + branch_missed))
                TOTAL_BRANCH_COVERED=$((TOTAL_BRANCH_COVERED + branch_covered))
                TOTAL_METHOD_MISSED=$((TOTAL_METHOD_MISSED + method_missed))
                TOTAL_METHOD_COVERED=$((TOTAL_METHOD_COVERED + method_covered))

                # Calcular porcentajes
                total_lines=$((line_missed + line_covered))
                total_branches=$((branch_missed + branch_covered))
                total_methods=$((method_missed + method_covered))
                total_complexity=$((complexity_missed + complexity_covered))

                if [ $total_lines -gt 0 ]; then
                    line_pct=$((line_covered * 100 / total_lines))
                else
                    line_pct=0
                fi

                if [ $total_branches -gt 0 ]; then
                    branch_pct=$((branch_covered * 100 / total_branches))
                else
                    branch_pct=100
                fi

                if [ $total_methods -gt 0 ]; then
                    method_pct=$((method_covered * 100 / total_methods))
                else
                    method_pct=0
                fi

                if [ $total_complexity -gt 0 ]; then
                    complexity_pct=$((complexity_covered * 100 / total_complexity))
                else
                    complexity_pct=0
                fi

                # Colorear según el porcentaje de líneas
                if [ $line_pct -ge 80 ]; then
                    COLOR=$GREEN
                elif [ $line_pct -ge 50 ]; then
                    COLOR=$YELLOW
                else
                    COLOR=$RED
                fi

                # Mostrar la fila
                printf "${CYAN}${BOLD}║${NC}  ${COLOR}%-25s${NC}${CYAN}${BOLD}│${NC} ${COLOR}%3d%% %2d/%-2d${NC} ${CYAN}${BOLD}│${NC} ${COLOR}%3d%% %2d/%-2d${NC} ${CYAN}${BOLD}│${NC} ${COLOR}%3d%% %2d/%-2d${NC} ${CYAN}${BOLD}│${NC} ${COLOR}%3d%% %2d/%-3d${NC}  ${CYAN}${BOLD}║${NC}\n" \
                    "$class" \
                    "$line_pct" "$line_covered" "$total_lines" \
                    "$branch_pct" "$branch_covered" "$total_branches" \
                    "$method_pct" "$method_covered" "$total_methods" \
                    "$complexity_pct" "$complexity_covered" "$total_complexity"
            fi
        done < <(tail -n +2 "$COVERAGE_DIR/coverage.csv")

        echo -e "${CYAN}${BOLD}╠═══════════════════════════╧═══════════╧═══════════╧═══════════╧══════════════╣${NC}"

        # Calcular y mostrar totales
        total_lines=$((TOTAL_LINE_MISSED + TOTAL_LINE_COVERED))
        total_branches=$((TOTAL_BRANCH_MISSED + TOTAL_BRANCH_COVERED))
        total_methods=$((TOTAL_METHOD_MISSED + TOTAL_METHOD_COVERED))

        if [ $total_lines -gt 0 ]; then
            line_pct=$((TOTAL_LINE_COVERED * 100 / total_lines))
        else
            line_pct=0
        fi

        if [ $total_branches -gt 0 ]; then
            branch_pct=$((TOTAL_BRANCH_COVERED * 100 / total_branches))
        else
            branch_pct=100
        fi

        if [ $total_methods -gt 0 ]; then
            method_pct=$((TOTAL_METHOD_COVERED * 100 / total_methods))
        else
            method_pct=0
        fi

        # Color del total basado en coverage de líneas
        if [ $line_pct -ge 80 ]; then
            TOTAL_COLOR=$GREEN
        elif [ $line_pct -ge 50 ]; then
            TOTAL_COLOR=$YELLOW
        else
            TOTAL_COLOR=$RED
        fi

        printf "${CYAN}${BOLD}║${NC}  ${TOTAL_COLOR}${BOLD}%-25s${NC}${CYAN}${BOLD}│${NC} ${TOTAL_COLOR}${BOLD}%3d%% %2d/%-2d${NC} ${CYAN}${BOLD}│${NC} ${TOTAL_COLOR}${BOLD}%3d%% %2d/%-2d${NC} ${CYAN}${BOLD}│${NC} ${TOTAL_COLOR}${BOLD}%3d%% %2d/%-2d${NC} ${CYAN}${BOLD}│${NC}              ${CYAN}${BOLD}║${NC}\n" \
            "TOTAL" \
            "$line_pct" "$TOTAL_LINE_COVERED" "$total_lines" \
            "$branch_pct" "$TOTAL_BRANCH_COVERED" "$total_branches" \
            "$method_pct" "$TOTAL_METHOD_COVERED" "$total_methods"
    fi

    echo -e "${CYAN}${BOLD}╚════════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${GREEN}Reporte HTML disponible en: ${NC}$COVERAGE_DIR/html/index.html"
else
    echo ""
    echo -e "${YELLOW}No se pudo generar el reporte de coverage${NC}"
fi

echo ""
exit $TEST_EXIT_CODE
