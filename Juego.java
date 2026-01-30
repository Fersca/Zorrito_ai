import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

/**
 * Clase principal que contiene toda la lógica del juego.
 * Maneja el game loop, los personajes, las colisiones y el input.
 */
public class Juego {

    // Cantidad de enemigos
    public int cantidadMalos;
    public int cantidadAguilas;

    // Opciones de visualización
    public boolean centrar;
    public boolean sinFondo;

    // Estado del juego: 0=jugando, 1=ganó, 2=cazado, 3=tiempo agotado
    int terminado = 0;

    // Tiempo límite en milisegundos (2 minutos)
    final long TIEMPO_LIMITE_MS = 2 * 60 * 1000;

    // Timer del game loop
    Timer timer = new Timer();

    // Referencia al display
    Display display;

    // Tiempo entre iteraciones (50ms = 20 FPS)
    private int delay = 50;

    public void setDisplay(Display d){
        this.display = d;
    }

    public void comenzar(){
        timer.scheduleAtFixedRate(comienzaJuego(timer), 0, delay);
    }

    // Teclas actualmente presionadas
    public Set<Integer> pressedKeys = new HashSet<>();

    // Nivel de zoom
    double zoom = 1;

    // Tiempo inicial para el cronómetro
    long initTimeMillis;

    // Lista de personajes del juego
    public ArrayList<Character> personajes = new ArrayList<Character>();

    // Personaje principal (el zorro)
    public Character principal;

    // Posición general del mapa (para scroll)
    int general_x = 0;
    int general_y = 0;

    // Posición de la jaula
    int jaulaX = 375;
    int jaulaY = 360;

    // Estrategia de movimiento nulo (elementos estáticos)
    Function<Character, Void> movimientoNulo = (Character c) -> {
        return null;
    };

    // Estrategia de movimiento rebote (pájaros)
    Function<Character, Void> movimientoRebote = (Character c) -> {
        // Si colisionó, lo manda a la jaula
        if (c.colisionado){
            c.x = jaulaX + 25;
            c.y = jaulaY + 40;
            return null;
        }

        // Avanza según dirección actual
        if (Direccion.Derecha == c.avanzando_x)
            c.x = c.x + c.velocidadX;
        if (Direccion.Izquierda == c.avanzando_x)
            c.x = c.x - c.velocidadX;
        if (Direccion.Abajo == c.avanzando_y)
            c.y = c.y + c.velocidadY;
        if (Direccion.Arriba == c.avanzando_y)
            c.y = c.y - c.velocidadY;

        // Rebota en los bordes de la pantalla
        int ancho = display.getWidth();
        int alto = display.getHeight();

        if (c.centroX > ancho)
            c.avanzando_x = Direccion.Izquierda;
        if (c.centroX < 0)
            c.avanzando_x = Direccion.Derecha;
        if (c.centroY > alto)
            c.avanzando_y = Direccion.Arriba;
        if (c.centroY < 0)
            c.avanzando_y = Direccion.Abajo;

        // Rota el personaje
        c.angulo = c.angulo + c.rotaAngulo;
        return null;
    };

    // Estrategia de movimiento en arco (curvas suaves)
    Function<Character, Void> movimientoArco = (Character c) -> {
        if (c.colisionado){
            c.x = jaulaX + 25;
            c.y = jaulaY + 40;
            return null;
        }

        // Actualiza el ángulo de movimiento para crear curva
        c.anguloMovimiento += c.velocidadAngular;

        // Calcula el movimiento basado en el ángulo
        double velocidad = Math.sqrt(c.velocidadX * c.velocidadX + c.velocidadY * c.velocidadY);
        int deltaX = (int) Math.round(Math.cos(c.anguloMovimiento) * velocidad);
        int deltaY = (int) Math.round(Math.sin(c.anguloMovimiento) * velocidad);

        c.x += deltaX;
        c.y += deltaY;

        // Rebote en los bordes
        int ancho = display.getWidth();
        int alto = display.getHeight();

        if (c.centroX > ancho || c.centroX < 0) {
            c.anguloMovimiento = Math.PI - c.anguloMovimiento;
            c.velocidadAngular = -c.velocidadAngular;
        }
        if (c.centroY > alto || c.centroY < 0) {
            c.anguloMovimiento = -c.anguloMovimiento;
            c.velocidadAngular = -c.velocidadAngular;
        }

        c.angulo = c.angulo + c.rotaAngulo;
        return null;
    };

    // Estrategia de movimiento aleatorio con arcos cambiantes
    Function<Character, Void> movimientoAleatorio = (Character c) -> {
        if (c.colisionado){
            c.x = jaulaX + 25;
            c.y = jaulaY + 40;
            return null;
        }

        // Incrementa contador para cambio de dirección
        c.contadorCambio++;

        // Cambia la velocidad angular periódicamente
        if (c.contadorCambio >= c.frecuenciaCambio) {
            c.contadorCambio = 0;
            Random rand = new Random();
            c.velocidadAngular = (rand.nextDouble() - 0.5) * 0.15;
            c.frecuenciaCambio = rand.nextInt(60) + 20;
        }

        // Actualiza el ángulo de movimiento
        c.anguloMovimiento += c.velocidadAngular;

        // Calcula el movimiento
        double velocidad = Math.sqrt(c.velocidadX * c.velocidadX + c.velocidadY * c.velocidadY);
        int deltaX = (int) Math.round(Math.cos(c.anguloMovimiento) * velocidad);
        int deltaY = (int) Math.round(Math.sin(c.anguloMovimiento) * velocidad);

        c.x += deltaX;
        c.y += deltaY;

        // Rebote en los bordes con corrección de posición
        int ancho = display.getWidth();
        int alto = display.getHeight();

        if (c.centroX > ancho) {
            c.anguloMovimiento = Math.PI - c.anguloMovimiento;
            c.x = ancho - c.radio;
        }
        if (c.centroX < 0) {
            c.anguloMovimiento = Math.PI - c.anguloMovimiento;
            c.x = c.radio;
        }
        if (c.centroY > alto) {
            c.anguloMovimiento = -c.anguloMovimiento;
            c.y = alto - c.radio;
        }
        if (c.centroY < 0) {
            c.anguloMovimiento = -c.anguloMovimiento;
            c.y = c.radio;
        }

        c.angulo = c.angulo + c.rotaAngulo;
        return null;
    };

    // Estrategia de movimiento de caza (águilas persiguen al zorro)
    Function<Character, Void> movimientoCazar = (Character c) -> {
        if (c.colisionado){
            c.follow.cazado = true;
            return null;
        }

        // Obtiene posiciones de presa y cazador
        int presaX = c.follow.x;
        int presaY = c.follow.y;
        int cazadorX = c.x;
        int cazadorY = c.y;

        // Se mueve hacia la presa
        if (presaX < cazadorX)
            c.x = c.x - c.velocidadX;
        else
            c.x = c.x + c.velocidadX;

        if (presaY < cazadorY)
            c.y = c.y - c.velocidadY;
        else
            c.y = c.y + c.velocidadY;

        c.angulo = c.angulo + c.rotaAngulo;
        return null;
    };

    public Function<Void, Integer> terminadoFunc(){
        return (Void) -> {return this.terminado;};
    }

    public void crearPersonajes(){
        personajes.addAll(creaListaDePersonajes());
        this.display.trackeaPersonajes(this.personajes, this.display);
    }

    /**
     * Crea todos los personajes del juego: zorro, fondo, águilas, pájaros, jaula.
     */
    private Collection<Character> creaListaDePersonajes() {
        ArrayList<Character> personajesCreados = new ArrayList<Character>();

        // Crea al zorrito (personaje principal)
        Character zorrito = new Character("Zorrito", "sprites.png", 10, movimientoNulo);

        // Configura los sprites de animación
        zorrito.hasSprites = true;
        zorrito.spritesArray = new Character.Sprite[8];
        zorrito.spritesArray[0] = new Character.Sprite(0, 0, 1098/2, 1932/4);
        zorrito.spritesArray[1] = new Character.Sprite(0, 1932/4, 1098/2, 1932/4);
        zorrito.spritesArray[2] = new Character.Sprite(0, 1932/2, 1098/2, 1932/4);
        zorrito.spritesArray[3] = new Character.Sprite(0, (1932/4)*3, 1098/2, 1932/4);
        zorrito.spritesArray[4] = new Character.Sprite(1098/2, 0, 1098/2, 1932/4);
        zorrito.spritesArray[5] = new Character.Sprite(1098/2, 1932/4, 1098/2, 1932/4);
        zorrito.spritesArray[6] = new Character.Sprite(1098/2, 1932/2, 1098/2, 1932/4);
        zorrito.spritesArray[7] = new Character.Sprite(1098/2, (1932/4)*3, 1098/2, 1932/4);

        zorrito.cacheSprites();

        zorrito.x = display.getWidth() / 2;
        zorrito.y = display.getHeight() / 2;
        zorrito.setImagenColision("zorro_muerto.png");

        // Crea la jaula (objetivo)
        Character jaula = new Character("Jaula", "jaula.png", 5, movimientoNulo);
        jaula.x = jaulaX;
        jaula.y = jaulaY;
        jaula.colisiona = false;

        // Crea el fondo del mapa
        Character bosque;
        if (sinFondo){
            bosque = new Character("Bosque", "screenshot.png", 1, movimientoNulo);
        } else {
            bosque = new Character("Bosque", "bosque.png", 1, movimientoNulo);
        }

        bosque.x = 0;
        bosque.y = 0;
        bosque.drawFromCenter = false;
        bosque.fixedSize = true;
        bosque.fixed_witdh = display.getWidth();
        bosque.fixed_heigth = display.getHeight();
        bosque.colisiona = false;
        bosque.esFondoInfinito = !sinFondo;
        personajesCreados.add(bosque);

        // Crea el águila principal
        Character aguila = new Character("Aguila", "aguila.png", 7, movimientoCazar);
        aguila.x = display.getWidth();
        aguila.y = 0;
        aguila.velocidadX = 2;
        aguila.velocidadY = 2;
        aguila.follow = zorrito;

        // Crea águilas adicionales
        Random random = new Random();
        for (int i = 0; i < cantidadAguilas; i++){
            Character enemy = new Character("Aguila" + i, "aguila.png", 7, movimientoCazar);
            enemy.x = random.nextInt(display.getWidth());
            enemy.y = random.nextInt(display.getHeight());
            enemy.velocidadX = 2;
            enemy.velocidadY = 2;
            enemy.follow = zorrito;
            personajesCreados.add(enemy);
        }

        personajesCreados.add(zorrito);
        personajesCreados.add(aguila);

        // Crea los pájaros enemigos
        for (Character p : crearEnemigos()) {
            personajesCreados.add(p);
        }
        personajesCreados.add(jaula);

        this.principal = zorrito;

        return personajesCreados;
    }

    /**
     * Crea los pájaros enemigos con estrategias de movimiento aleatorias.
     */
    private ArrayList<Character> crearEnemigos(){
        Random random = new Random();
        ArrayList<Character> enemigos = new ArrayList<Character>();
        Direccion[] movimientosArriba_Abajo = {Direccion.Arriba, Direccion.Abajo};
        Direccion[] movimientosIzquierda_Derecha = {Direccion.Izquierda, Direccion.Derecha};

        // Array de estrategias de movimiento disponibles
        @SuppressWarnings("unchecked")
        Function<Character, Void>[] estrategias = new Function[] {
            movimientoRebote,
            movimientoArco,
            movimientoAleatorio
        };

        for (int i = 0; i < this.cantidadMalos; i++){
            // Selecciona una estrategia aleatoria
            int tipoMov = random.nextInt(3);
            Function<Character, Void> estrategia = estrategias[tipoMov];

            Character pajaro = new Character("Pajaro" + i, "pajaro.png", 20, estrategia);
            pajaro.tipoMovimiento = tipoMov;
            pajaro.velocidadX = random.nextInt(15) + 3;
            pajaro.velocidadY = random.nextInt(15) + 3;
            pajaro.avanzando_y = movimientosArriba_Abajo[random.nextInt(2)];
            pajaro.avanzando_x = movimientosIzquierda_Derecha[random.nextInt(2)];

            // Configuración específica según tipo de movimiento
            if (tipoMov == 1) {
                pajaro.anguloMovimiento = random.nextDouble() * Math.PI * 2;
                pajaro.velocidadAngular = (random.nextDouble() - 0.5) * 0.1;
            } else if (tipoMov == 2) {
                pajaro.anguloMovimiento = random.nextDouble() * Math.PI * 2;
                pajaro.velocidadAngular = (random.nextDouble() - 0.5) * 0.1;
                pajaro.frecuenciaCambio = random.nextInt(40) + 30;
                pajaro.contadorCambio = random.nextInt(pajaro.frecuenciaCambio);
            }

            pajaro.x = display.getWidth();
            pajaro.y = display.getHeight();
            enemigos.add(pajaro);
        }

        return enemigos;
    }

    /**
     * Crea el TimerTask que ejecuta el game loop cada 50ms.
     */
    private TimerTask comienzaJuego(Timer timer){
        initTimeMillis = System.currentTimeMillis();
        return new TimerTask() {
            @Override
            public void run() {
                // Detecta posición del mouse para mover al personaje
                PointerInfo pi = MouseInfo.getPointerInfo();
                Point p = pi.getLocation();
                mueveSegunMouse(p.x, p.y);

                // Actualiza posición de todos los personajes
                for (Character c : personajes) {
                    c.seMueve();
                }

                // Verifica colisiones
                boolean colisionPrincipal = false;
                int vivos = 0;

                for (Character c : personajes) {
                    if (c.colisionado) continue;
                    if (c.name.equals(principal.name)) continue;

                    if (principal.verificaColision(c)){
                        c.setColision(true);
                        colisionPrincipal = true;
                    } else {
                        c.setColision(false);
                        if (c.follow == null)
                            vivos++;
                    }
                }

                principal.setColision(colisionPrincipal);

                // Verifica condiciones de fin del juego
                long tiempoTranscurrido = System.currentTimeMillis() - initTimeMillis;
                if (tiempoTranscurrido >= TIEMPO_LIMITE_MS) {
                    terminado = 3;
                    display.bufferedDraw();
                    timer.cancel();
                } else if (principal.cazado) {
                    terminado = 2;
                    display.bufferedDraw();
                    timer.cancel();
                } else if (vivos == 2) {
                    terminado = 1;
                    display.bufferedDraw();
                    timer.cancel();
                }

                display.bufferedDraw();
            }
        };
    }

    /**
     * Calcula la dirección de movimiento basada en la posición del mouse.
     */
    private void mueveSegunMouse(int x, int y){
        // Si hay tecla presionada, ignora el mouse
        if (!pressedKeys.isEmpty())
            return;

        double px = (general_x + principal.centroX) * zoom;
        double py = (general_y + principal.centroY) * zoom;

        double opuesto = 0;
        double adyacente = 0;
        double sumar = 0;

        // Calcula el cuadrante del mouse respecto al personaje
        int cuadrante = 0;
        if (px < x & py < y){
            cuadrante = 3;
            opuesto = y - py;
            adyacente = x - px;
            sumar = 90;
        }
        if (px > x & py < y){
            cuadrante = 4;
            opuesto = px - x;
            adyacente = y - py;
            sumar = 180;
        }
        if (px < x & py > y){
            cuadrante = 2;
            opuesto = x - px;
            adyacente = py - y;
            sumar = 0;
        }
        if (px > x & py > y){
            cuadrante = 1;
            opuesto = py - y;
            adyacente = px - x;
            sumar = 270;
        }

        // Si el mouse está muy cerca, no mueve
        if (opuesto < 50 && adyacente < 50){
            noMueve();
            return;
        }

        // Evita división por cero
        if (adyacente == 0)
            adyacente = 1;

        // Calcula el ángulo hacia el mouse
        double tangente = opuesto / adyacente;
        double angulo = Math.toDegrees(Math.atan(tangente));
        angulo = angulo + sumar;

        // Determina la dirección según el ángulo
        if (angulo > 22.5 && angulo < 67.5){
            mueveArribaDerecha();
        } else if (angulo > 67.5 && angulo < 112.5){
            mueveDerecha();
        } else if (angulo > 112.5 && angulo < 157.5){
            mueveAbajoDerecha();
        } else if (angulo > 157.5 && angulo < 202.5){
            mueveAbajo();
        } else if (angulo > 202.5 && angulo < 247.5){
            mueveAbajoIzquierda();
        } else if (angulo > 247.5 && angulo < 292.5){
            mueveIzquierda();
        } else if (angulo > 292.5 && angulo < 337.5){
            mueveArribaIzquierda();
        } else if (angulo > 337.5 || angulo < 22.5){
            mueveArriba();
        }
    }

    // Métodos de movimiento del personaje principal
    private void noMueve(){
        principal.avanzando_x = Direccion.Quieto;
        principal.avanzando_y = Direccion.Quieto;
    }

    private void mueveArribaIzquierda(){
        if (centrar){
            general_x += 4;
            general_y += 4;
        }
        principal.x -= 4;
        principal.y -= 4;
        principal.avanzando_x = Direccion.Izquierda;
        principal.avanzando_y = Direccion.Arriba;
    }

    private void mueveAbajoDerecha(){
        if (centrar){
            general_x -= 4;
            general_y -= 4;
        }
        principal.x += 4;
        principal.y += 4;
        principal.avanzando_x = Direccion.Derecha;
        principal.avanzando_y = Direccion.Abajo;
    }

    private void mueveArribaDerecha(){
        if (centrar){
            general_x -= 4;
            general_y += 4;
        }
        principal.x += 4;
        principal.y -= 4;
        principal.avanzando_x = Direccion.Derecha;
        principal.avanzando_y = Direccion.Arriba;
    }

    private void mueveAbajoIzquierda(){
        if (centrar){
            general_x += 4;
            general_y -= 4;
        }
        principal.x -= 4;
        principal.y += 4;
        principal.avanzando_x = Direccion.Izquierda;
        principal.avanzando_y = Direccion.Abajo;
    }

    private void mueveIzquierda(){
        if (centrar){
            general_x += 6;
        }
        principal.x -= 6;
        principal.avanzando_x = Direccion.Izquierda;
        principal.avanzando_y = Direccion.Quieto;
    }

    private void mueveDerecha(){
        if (centrar){
            general_x -= 6;
        }
        principal.x += 6;
        principal.avanzando_x = Direccion.Derecha;
        principal.avanzando_y = Direccion.Quieto;
    }

    private void mueveArriba(){
        if (centrar){
            general_y += 6;
        }
        principal.y -= 6;
        principal.avanzando_x = Direccion.Quieto;
        principal.avanzando_y = Direccion.Arriba;
    }

    private void mueveAbajo(){
        if (centrar){
            general_y -= 6;
        }
        principal.y += 6;
        principal.avanzando_x = Direccion.Quieto;
        principal.avanzando_y = Direccion.Abajo;
    }

    /**
     * Procesa las teclas presionadas para movimiento y acciones.
     */
    public void accionDeTeclaPresionada() {
        // Combinaciones de teclas para movimiento diagonal
        if (pressedKeys.contains(KeyEvent.VK_J) && pressedKeys.contains(KeyEvent.VK_I)) {
            mueveArribaIzquierda();
        } else if (pressedKeys.contains(KeyEvent.VK_L) && pressedKeys.contains(KeyEvent.VK_K)) {
            mueveAbajoDerecha();
        } else if (pressedKeys.contains(KeyEvent.VK_L) && pressedKeys.contains(KeyEvent.VK_I)) {
            mueveArribaDerecha();
        } else if (pressedKeys.contains(KeyEvent.VK_J) && pressedKeys.contains(KeyEvent.VK_K)) {
            mueveAbajoIzquierda();
        }
        // Teclas individuales para movimiento cardinal
        else if (pressedKeys.contains(KeyEvent.VK_J) && pressedKeys.size() == 1) {
            mueveIzquierda();
        } else if (pressedKeys.contains(KeyEvent.VK_L) && pressedKeys.size() == 1) {
            mueveDerecha();
        } else if (pressedKeys.contains(KeyEvent.VK_I) && pressedKeys.size() == 1) {
            mueveArriba();
        } else if (pressedKeys.contains(KeyEvent.VK_K) && pressedKeys.size() == 1) {
            mueveAbajo();
        }
        // Controles de zoom
        else if (pressedKeys.contains(KeyEvent.VK_Z) && pressedKeys.size() == 1) {
            this.zoom = this.zoom + 0.1;
        } else if (pressedKeys.contains(KeyEvent.VK_X) && pressedKeys.size() == 1) {
            this.zoom = this.zoom - 0.1;
        }
        // Controles de cámara
        else if (pressedKeys.contains(KeyEvent.VK_V) && pressedKeys.size() == 1) {
            this.general_x = this.general_x + 10;
        } else if (pressedKeys.contains(KeyEvent.VK_C) && pressedKeys.size() == 1) {
            this.general_x = this.general_x - 10;
        } else if (pressedKeys.contains(KeyEvent.VK_F) && pressedKeys.size() == 1) {
            this.general_y = this.general_y + 10;
        } else if (pressedKeys.contains(KeyEvent.VK_R) && pressedKeys.size() == 1) {
            this.general_y = this.general_y - 10;
        }
        // Reiniciar y salir
        else if (pressedKeys.contains(KeyEvent.VK_E) && pressedKeys.size() == 1) {
            resetJuego();
        } else if (pressedKeys.contains(KeyEvent.VK_Q) && pressedKeys.size() == 1) {
            timer.cancel();
            System.out.println("End.");
            System.exit(0);
        }
    }

    /**
     * Reinicia el juego a su estado inicial.
     */
    private void resetJuego() {
        terminado = 0;
        personajes.clear();
        crearPersonajes();

        timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(comienzaJuego(timer), 0, delay);
    }
}
