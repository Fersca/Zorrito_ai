# Zorrito

<div align="center">

![Zorrito](assets/zorro.png)

**Un juego arcade 2D desarrollado 100% en Java puro**

[![Java](https://img.shields.io/badge/Java-14%2B-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Tests](https://img.shields.io/badge/Tests-112%20passing-brightgreen?style=for-the-badge)](./test/)
[![Coverage](https://img.shields.io/badge/Coverage-97%25-brightgreen?style=for-the-badge)](./coverage/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](./LICENSE)

[Jugar](#instalación) · [Controles](#controles) · [Documentación](./documentation/index.html)

</div>

---

## Acerca del Juego

**Zorrito** es un videojuego arcade donde controlas a un pequeño zorro que debe capturar pájaros mientras evita ser cazado por águilas. ¡Simple pero adictivo!

### Objetivo

- **Captura** todos los pájaros antes de que se acabe el tiempo
- **Evita** las águilas que te persiguen
- **Sobrevive** los 2 minutos para ganar

### Personajes

<table>
<tr>
<td align="center" width="200">

![Zorrito](assets/zorro.png)

**Zorrito**<br/>
<sub>El protagonista.<br/>Tú lo controlas.</sub>

</td>
<td align="center" width="200">

![Pájaro](assets/pajaro.png)

**Pájaros**<br/>
<sub>Rebotan por la pantalla.<br/>¡Captúralos!</sub>

</td>
<td align="center" width="200">

![Águila](assets/aguila.png)

**Águilas**<br/>
<sub>Te persiguen sin piedad.<br/>¡Evítalas!</sub>

</td>
<td align="center" width="200">

![Jaula](assets/jaula.png)

**Jaula**<br/>
<sub>Donde van los<br/>pájaros capturados.</sub>

</td>
</tr>
</table>

### Escenario

<div align="center">

![Bosque](assets/bosque.png)

*El bosque donde transcurre la acción*

</div>

---

## Instalación

### Requisitos

- Java 14 o superior
- Sistema operativo: Windows, macOS o Linux

### Opción 1: Ejecutar directamente

```bash
# Clonar el repositorio
git clone https://github.com/tu-usuario/zorrito.git
cd zorrito

# Ejecutar (compila automáticamente si es necesario)
./run.sh
```

### Opción 2: Compilar manualmente

```bash
# Compilar
./compilar.sh

# Ejecutar el JAR
java -jar Compilado/jar/Zorrito.jar
```

---

## Controles

| Tecla | Acción |
|:-----:|--------|
| `I` | Mover arriba |
| `K` | Mover abajo |
| `J` | Mover izquierda |
| `L` | Mover derecha |
| `Mouse` | Movimiento automático hacia el cursor |
| `Z` / `X` | Zoom + / - |
| `E` | Reiniciar juego |
| `Q` | Salir |

> **Tip:** Puedes combinar `I`+`J`, `I`+`L`, `K`+`J`, `K`+`L` para movimiento diagonal.

---

## Argumentos de Línea de Comando

```bash
./run.sh [opciones]
```

| Opción | Descripción | Ejemplo |
|--------|-------------|---------|
| `-pajaros:N` | Cantidad de pájaros | `-pajaros:30` |
| `-aguilas:N` | Cantidad de águilas | `-aguilas:5` |
| `-no-centrar` | Cámara fija (no sigue al jugador) | |
| `-sin-fondo` | Usa el escritorio como fondo | |
| `-help` | Muestra la ayuda | |

### Ejemplos

```bash
# Juego difícil: muchos pájaros y águilas
./run.sh -pajaros:50 -aguilas:10

# Modo tranquilo: pocos enemigos
./run.sh -pajaros:10 -aguilas:0

# Efecto transparente (usa tu escritorio de fondo)
./run.sh -sin-fondo
```

---

## Arquitectura

El proyecto sigue una arquitectura modular con separación de responsabilidades:

```
Zorrito_ai/
├── run.sh                # Ejecutar el juego
├── compilar.sh           # Compilar el proyecto
├── test.sh               # Ejecutar tests
├── src/                  # Código fuente Java
│   ├── Zorrito.java      # Punto de entrada
│   ├── Juego.java        # Lógica del game loop
│   ├── Display.java      # Renderizado y ventana
│   ├── Character.java    # Modelo de entidades
│   ├── MovimientoHandler.java
│   ├── CollisionUtils.java
│   ├── SpriteUtils.java
│   └── MovementUtils.java
├── assets/               # Imágenes del juego
│   ├── sprites.png       # Animación del zorro
│   ├── bosque.png        # Fondo del escenario
│   ├── pajaro.png        # Sprites de pájaros
│   ├── aguila.png        # Sprite de águilas
│   └── jaula.png         # Sprite de la jaula
├── test/                 # Tests unitarios (112 tests)
└── documentation/        # Documentación técnica
```

### Diagrama de Clases (simplificado)

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Zorrito   │────▶│    Juego    │────▶│   Display   │
│   (main)    │     │ (game loop) │     │ (rendering) │
└─────────────┘     └──────┬──────┘     └─────────────┘
                          │
                          ▼
                   ┌─────────────┐
                   │  Character  │
                   │  (modelo)   │
                   └──────┬──────┘
                          │
            ┌─────────────┼─────────────┐
            ▼             ▼             ▼
     ┌───────────┐ ┌───────────┐ ┌───────────┐
     │ Collision │ │  Sprite   │ │ Movement  │
     │   Utils   │ │   Utils   │ │  Handler  │
     └───────────┘ └───────────┘ └───────────┘
```

> Ver documentación completa con diagramas interactivos en [`documentation/index.html`](./documentation/index.html)

---

## Testing

El proyecto incluye 112 tests unitarios con 97% de cobertura.

```bash
# Ejecutar tests con reporte de cobertura
./test.sh
```

### Cobertura por clase

| Clase | Líneas | Branches | Métodos |
|-------|:------:|:--------:|:-------:|
| SpriteUtils | 97% | 90% | 90% |
| CollisionUtils | 93% | 100% | 83% |
| MovementUtils | 98% | 94% | 80% |
| **Total** | **97%** | **93%** | **86%** |

---

## Tecnologías

- **Java 14+** - Lenguaje principal
- **AWT/Swing** - Interfaz gráfica (sin librerías externas)
- **BufferStrategy** - Double/Triple buffering para renderizado fluido
- **AffineTransform** - Transformaciones 2D (rotación, escalado)
- **JUnit 5** - Framework de testing
- **JaCoCo** - Cobertura de código

---

## Screenshots

<div align="center">

### Animación del Zorrito

![Sprites](assets/sprites.png)

*Sprite sheet con 8 frames de animación*

</div>

---

## Contribuir

1. Fork el repositorio
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'Add: nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

### Directivas de código

- Sin librerías externas (Java puro)
- Tests para cada nueva funcionalidad
- Cobertura mínima del 90%
- Comentarios en español

---

## Licencia

Este proyecto está bajo la Licencia MIT. Ver [`LICENSE`](./LICENSE) para más detalles.

---

<div align="center">

**Hecho con Java puro**

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

</div>
