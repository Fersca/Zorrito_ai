# Reglas para Claude

## Restricciones

- **No instalar librerías externas.** Este proyecto debe ser puramente Java estándar (plain Java).
- Si algo es crucial y requiere una librería externa, **preguntar primero al usuario** antes de proceder.

---

# Análisis del Proyecto Zorrito

## Descripción General

**Zorrito** es un videojuego arcade 2D desarrollado 100% en Java puro (sin librerías externas). El jugador controla un pequeño zorro que debe evitar ser cazado por águilas y esquivar pájaros enemigos.

**Narrativa del juego:**
- El jugador controla a un "Zorrito" (pequeño zorro)
- Debe evitar ser cazado por águilas (predadores inteligentes)
- Debe esquivar pájaros (obstáculos que rebotan aleatoriamente)
- Victoria: eliminar todos los pájaros sin ser cazado
- Derrota: ser alcanzado por un águila

---

## Estructura de Archivos

```
Zorrito_ai/
├── Zorrito.java       # Código principal (1284 líneas) - TODO EL JUEGO
├── MANIFEST.MF        # Manifest para crear JAR ejecutable
├── compilar.sh        # Script de compilación
├── CLAUDE.md          # Este archivo - reglas y documentación
├── readme.txt         # Documentación sobre compilación nativa
└── Assets gráficos:
    ├── sprites.png    # Sprite sheet del zorro (8 frames, 1098x1932px)
    ├── bosque.png     # Fondo del escenario
    ├── pajaro.png     # Sprites de pájaros enemigos
    ├── aguila.png     # Sprite de águilas predadoras
    ├── jaula.png      # Sprite de la jaula/objetivo
    ├── zorro.png      # Icono del zorro
    └── zorro_muerto.png # Imagen del zorro cazado
```

---

## Arquitectura del Código

### Diagrama de Clases

```
┌─────────────────────────────────────────────────────────────┐
│                    ZORRITO (Main)                           │
│  - Punto de entrada del programa                            │
│  - Procesamiento de argumentos CLI                          │
│  - Captura de pantalla (si se requiere)                     │
│  Líneas: 43-185                                             │
└───────────────────┬─────────────────────────────────────────┘
                    │ instancia
         ┌──────────▼──────────┐
         │   JUEGO (Model)     │ ◄─── Lógica de negocio
         │  - Estado del juego │
         │  - Game loop (Timer)│
         │  - Física/colisiones│
         │  - Movimientos IA   │
         │  Líneas: 192-766    │
         └──────────┬──────────┘
                    │ referencia
         ┌──────────▼──────────┐
         │  DISPLAY (View)     │ ◄─── Presentación
         │  - Frame principal  │
         │  - Canvas de dibujo │
         │  - Rendering 2D     │
         │  - Double-buffering │
         │  Líneas: 769-1013   │
         └──────────┬──────────┘
                    │ contiene
         ┌──────────▼─────────────┐
         │  CHARACTER (Model)     │ ◄─── Entidades del juego
         │  - Posición, imagen    │
         │  - Colisión, sprite    │
         │  - Movimiento IA       │
         │  Líneas: 1017-1284     │
         └────────────────────────┘
```

### Clases y Responsabilidades

#### 1. Clase ZORRITO (Líneas 43-185)
**Responsabilidad:** Punto de entrada y configuración inicial.

| Método | Descripción |
|--------|-------------|
| `main(String[] args)` | Parsea argumentos CLI, crea instancia del juego |
| `Zorrito(buffer, cantMalos, centrar, sinFondo, aguilas)` | Constructor principal |
| `capturaPantalla()` | Captura el escritorio para fondo transparente |

**Argumentos CLI soportados:**
- `-pajaros:N` - Cantidad de pájaros (default: 20)
- `-aguilas:N` - Cantidad de águilas (default: 0)
- `-no-centrar` - No centra la cámara en el jugador
- `-sin-fondo` - Usa pantalla actual como fondo
- `-help` - Muestra ayuda

#### 2. Enum DIRECCION (Líneas 187-189)
```java
enum Direccion { Derecha, Izquierda, Arriba, Abajo, Quieto }
```

#### 3. Clase JUEGO (Líneas 192-766)
**Responsabilidad:** Toda la lógica del juego.

**Atributos importantes:**
- `terminado` - Estado: 0=jugando, 1=ganó, 2=cazado
- `pressedKeys` - Set de teclas presionadas
- `zoom` - Nivel de zoom actual
- `personajes` - ArrayList de todos los Character

**Métodos principales:**

| Método | Líneas | Descripción |
|--------|--------|-------------|
| `comenzar()` | ~250 | Inicia el Timer del game loop |
| `comienzaJuego()` | ~260-400 | TimerTask que ejecuta cada 50ms |
| `crearPersonajes()` | ~420 | Llama a creaListaDePersonajes() |
| `creaListaDePersonajes()` | ~430-550 | Factory de todos los personajes |
| `crearEnemigos()` | ~560 | Factory de pájaros enemigos |
| `mueveSegunMouse()` | ~600-700 | Calcula dirección basada en posición del mouse |
| `acciónDeTeclaPresionada()` | ~710-760 | Procesa input de teclado |
| `resetJuego()` | ~765 | Reinicia el juego |

**Estrategias de Movimiento (Lambdas):**
```java
Function<Character, Void> movimientoNulo    // Estático (fondo, jaula)
Function<Character, Void> movimientoRebote  // Rebota en bordes (pájaros)
Function<Character, Void> movimientoCazar   // Persigue al jugador (águilas)
```

#### 4. Clase DISPLAY (Líneas 769-1013)
**Responsabilidad:** Renderizado y ventana.

Extiende `java.awt.Frame`.

**Clase interna MyCanvas (Líneas 870-1013):**
- Implementa double-buffering con `BufferStrategy`
- `draw()` - Renderiza cada frame
- `drawImageCanvas()` - Dibuja personajes con transformaciones 2D
- `drawElementosComunes()` - Dibuja HUD (status, cronómetro, mensajes)

#### 5. Clase CHARACTER (Líneas 1017-1284)
**Responsabilidad:** Modelo de cada entidad del juego.

**Atributos principales:**
```java
// Posición
int x, y, centroX, centroY

// Física
int velocidadX, velocidadY
double angulo, rotaAngulo
int radio  // Para colisiones

// Renderizado
Image img, img_colision
int width, height
double scale

// Movimiento
boolean avanzando_x, avanzando_y
Function<Character, Void> movimiento  // Estrategia inyectada

// Estado
boolean colisionado, cazado, colisiona

// IA
Character follow  // Personaje a perseguir (para águilas)

// Sprites
boolean hasSprites
Sprite[] spritesArray
int spritesIndex
```

**Métodos importantes:**
| Método | Descripción |
|--------|-------------|
| `seMueve()` | Aplica la función de movimiento, calcula centro y radio |
| `verificaColision(Character c)` | Detecta colisión por distancia de centros |
| `getImagen()` | Retorna imagen actual (con soporte sprites) |
| `cropImage()` | Recorta región del sprite sheet |
| `espejarImagen()` | Invierte imagen horizontalmente |

**Record Sprite:**
```java
record Sprite(int x, int y, int w, int h){}
```

---

## Flujo de Ejecución

### Inicialización
```
main()
  ├─ Parsea argumentos CLI
  ├─ new Zorrito(...)
  │   ├─ new Juego()
  │   │   └─ Timer scheduledAtFixedRate (cada 50ms)
  │   ├─ new Display(juego)
  │   │   ├─ new MyCanvas(display)
  │   │   ├─ addKeyListener()
  │   │   └─ addWindowListener()
  │   └─ juego.crearPersonajes()
  │       ├─ new Character("Zorrito", "sprites.png", ...)
  │       ├─ new Character("Bosque", "bosque.png", ...)
  │       ├─ for: new Character("Aguila"+i, ...) con movimientoCazar
  │       ├─ for: new Character("Pajaro"+i, ...) con movimientoRebote
  │       └─ new Character("Jaula", ...)
  └─ juego.comenzar()
```

### Game Loop (cada 50ms)
```
TimerTask.run()
  ├─ Obtiene posición del mouse
  ├─ Calcula movimiento del jugador
  ├─ Para cada personaje:
  │   ├─ Aplica función de movimiento
  │   └─ Calcula centro y radio
  ├─ Detección de colisiones (todos vs todos)
  ├─ Verifica condiciones de fin:
  │   ├─ Si cazado: terminado=2
  │   └─ Si vivos==2: terminado=1 (victoria)
  └─ display.bufferedDraw()
```

### Renderizado
```
MyCanvas.draw()
  ├─ BufferStrategy (double-buffer)
  ├─ Para cada personaje:
  │   ├─ AffineTransform (traslación + rotación + escalado)
  │   └─ Dibuja imagen transformada
  ├─ Dibuja HUD
  └─ bs.show()
```

---

## Sistema de Colisiones

**Algoritmo:** Detección por distancia de centros (círculos)

```java
boolean verificaColision(Character c) {
    int lado1 = Math.abs(this.centroX - c.centroX);
    int lado2 = Math.abs(this.centroY - c.centroY);
    double distancia = Math.sqrt(lado1*lado1 + lado2*lado2);
    return distancia < (this.radio + c.radio);
}
```

---

## Sistema de Sprites

**Sprite sheet del zorro:** `sprites.png` (1098 x 1932 px)
- 2 columnas × 4 filas = 8 fotogramas
- Cada frame: 549 × 483 px

```
[Frame 0] [Frame 4]
[Frame 1] [Frame 5]
[Frame 2] [Frame 6]
[Frame 3] [Frame 7]
```

**Animación:**
- Index cíclico `spritesIndex` (0-7)
- Si quieto: frame 0
- Si se mueve: avanza frame cada tick
- Espeja imagen si va a la izquierda

---

## Controles

| Tecla | Acción |
|-------|--------|
| I/J/K/L | Movimiento 8-direccional (estilo Vim) |
| Mouse | Movimiento automático hacia el cursor |
| Z | Zoom +10% |
| X | Zoom -10% |
| V/C | Desplazar cámara horizontal |
| R/F | Desplazar cámara vertical |
| E | Reiniciar juego |
| Q | Salir |

---

## Tecnologías Java Utilizadas

| Librería | Uso |
|----------|-----|
| `java.awt.*` | Interfaz gráfica, canvas, eventos |
| `java.awt.event.*` | KeyListener, WindowListener |
| `java.awt.image.*` | BufferStrategy, BufferedImage |
| `java.awt.geom.AffineTransform` | Transformaciones 2D |
| `java.util.*` | ArrayList, HashMap, Timer, TimerTask |
| `java.util.function.Function` | Lambdas para estrategias de movimiento |
| `javax.imageio.ImageIO` | Carga de imágenes PNG |

**Características de Java usadas:**
- Java 11+ (text blocks con """)
- Records (Java 14+) para Sprite
- Lambda expressions
- Generics

---

## Compilación

```bash
./compilar.sh
# Genera: Compilado/jar/Zorrito.jar

# Ejecutar:
java -jar Compilado/jar/Zorrito.jar
```

---

## Notas para Modificaciones

### Para agregar un nuevo tipo de enemigo:
1. Crear imagen PNG en la raíz del proyecto
2. En `creaListaDePersonajes()`, instanciar nuevo Character
3. Asignar estrategia de movimiento (nueva lambda o existente)
4. Agregar a la lista de personajes

### Para modificar la IA de persecución:
- Editar `movimientoCazar` en clase Juego (~línea 500)

### Para cambiar velocidad del juego:
- Modificar el período del Timer (actualmente 50ms) en `comenzar()`

### Para agregar sonido:
- Usar `javax.sound.sampled.*` (está en Java estándar)

### Para agregar nuevos controles:
- Modificar `acciónDeTeclaPresionada()` en clase Juego

---

# Directivas Generales de Desarrollo

## Testing

- Siempre hacer tests. Modularizar el código para que sea testeable.
- Tests unitarios de cada función.
- Calcular test coverage, debe ser **arriba del 90%**.
- Crear archivo `test.sh` que corra los tests desde línea de comando y muestre el coverage al final.
- Usar herramientas del framework, no instalar librerías sin permiso.
- Un archivo de test por cada layer (front, back, jobs) si aplica.
- Archivo `test.sh` en directorio raíz que corra todos los tests con resumen total.
- Cada cambio debe incluir tests nuevos y correr los anteriores.
- Correr linter definido por el framework (o el más usado).
- Ejecutar formateador de código si el framework lo tiene.

## Arquitectura

- Modularizar el dominio en clases.
- Complejidad ciclomática buena: ni micro funciones ni macro funciones.
- Código de front, back, testing y jobs organizado en carpetas separadas.
- No instalar librerías sin consentimiento explícito.
- Acoplamiento bajo: evitar llamadas con más de 4 parámetros.
- Reutilizar funciones y métodos. Preferir una función con parámetro extra a dos funciones casi iguales.

## Código

- Evitar closures y lambdas. Preferir funciones con async/await.
- Código lo más procedural posible.
- Clases como transporte de datos y métodos, no orientado a objetos puro.
- El debugging debe ser legible de manera secuencial.
- Agregar comentarios por bloques de ejecución (no línea por línea, pero tampoco funciones enteras sin comentarios).
- Comentar cada procesamiento/decisión en bloque y cada llamada a función indicando por qué se llama.
- **Comentarios en español.**

## Branches

- Para modificaciones suficientemente grandes, preguntar si hacerlo en otro branch de git para poder hacer rollback.

## Ejecución

- Script `run.sh` para back, otro para front, y uno por cada layer.
- Script `run.sh` en directorio raíz que corra todas las dependencias necesarias.

## Compilación

- Archivo `compile.sh` con script para armar ejecutable del sistema (backend).

## Documentación

- Crear mini-site en carpeta `documentation/` en directorio raíz con:
  - Diagrama de clases
  - Diagrama de secuencia
  - Diagrama de arquitectura
  - DFD (Diagrama de Flujo de Datos)
  - Diagrama de entidad-relación
  - Otras cosas necesarias para que un desarrollador entienda el proyecto
