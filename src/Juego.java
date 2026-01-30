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

    // Lista de proyectiles activos (piedras)
    public ArrayList<Character> proyectiles = new ArrayList<Character>();

    // Cooldown para disparo (evita disparar muy rápido)
    private long ultimoDisparo = 0;
    private final long COOLDOWN_DISPARO_MS = 500;

    // Velocidad de empuje del águila cuando es impactada (píxeles por frame)
    private final int VELOCIDAD_EMPUJE_AGUILA = 15;

    // Personaje principal (el zorro)
    public Character principal;

    // Posición general del mapa (para scroll)
    int general_x = 0;
    int general_y = 0;

    // Posición de la jaula
    int jaulaX = 375;
    int jaulaY = 360;

    public Function<Void, Integer> terminadoFunc(){
        return (Void) -> {return this.terminado;};
    }

    public void crearPersonajes(){
        personajes.addAll(creaListaDePersonajes());
        this.display.trackeaPersonajes(this.personajes, this.display);
    }

    /**
     * Aplica el movimiento a un personaje según su tipo.
     * Usa MovimientoHandler en lugar de lambdas para código procedural.
     */
    private void aplicarMovimiento(Character c) {
        int ancho = display.getWidth();
        int alto = display.getHeight();

        // Selecciona el método de movimiento según el tipo
        switch (c.tipoMovimientoEnum) {
            case NULO:
                MovimientoHandler.aplicarMovimientoNulo(c);
                break;
            case REBOTE:
                MovimientoHandler.aplicarMovimientoRebote(c, jaulaX, jaulaY, ancho, alto);
                break;
            case ARCO:
                MovimientoHandler.aplicarMovimientoArco(c, jaulaX, jaulaY, ancho, alto);
                break;
            case ALEATORIO:
                MovimientoHandler.aplicarMovimientoAleatorio(c, jaulaX, jaulaY, ancho, alto);
                break;
            case CAZAR:
                MovimientoHandler.aplicarMovimientoCazar(c);
                break;
            case PROYECTIL:
                MovimientoHandler.aplicarMovimientoProyectil(c, ancho, alto);
                break;
        }

        // Actualiza centro y radio después del movimiento
        c.actualizaCentroYRadio();
    }

    /**
     * Crea todos los personajes del juego: zorro, fondo, águilas, pájaros, jaula.
     */
    private Collection<Character> creaListaDePersonajes() {
        ArrayList<Character> personajesCreados = new ArrayList<Character>();

        // Crea al zorrito (personaje principal) con movimiento nulo
        Character zorrito = new Character("Zorrito", "assets/sprites.png", 10, TipoMovimiento.NULO);

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
        zorrito.setImagenColision("assets/zorro_muerto.png");

        // Crea la jaula (objetivo) con movimiento nulo
        Character jaula = new Character("Jaula", "assets/jaula.png", 5, TipoMovimiento.NULO);
        jaula.x = jaulaX;
        jaula.y = jaulaY;
        jaula.colisiona = false;

        // Crea el fondo del mapa con movimiento nulo
        Character bosque;
        if (sinFondo){
            bosque = new Character("Bosque", "assets/screenshot.png", 1, TipoMovimiento.NULO);
        } else {
            bosque = new Character("Bosque", "assets/bosque.png", 1, TipoMovimiento.NULO);
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

        // Crea el águila principal con movimiento de caza
        Character aguila = new Character("Aguila", "assets/aguila.png", 7, TipoMovimiento.CAZAR);
        aguila.x = display.getWidth();
        aguila.y = 0;
        aguila.velocidadX = 2;
        aguila.velocidadY = 2;
        aguila.follow = zorrito;

        // Crea águilas adicionales con movimiento de caza
        Random random = new Random();
        for (int i = 0; i < cantidadAguilas; i++){
            Character enemy = new Character("Aguila" + i, "assets/aguila.png", 7, TipoMovimiento.CAZAR);
            enemy.x = random.nextInt(display.getWidth());
            enemy.y = random.nextInt(display.getHeight());
            enemy.velocidadX = 2;
            enemy.velocidadY = 2;
            enemy.follow = zorrito;
            personajesCreados.add(enemy);
        }

        personajesCreados.add(zorrito);
        personajesCreados.add(aguila);

        // Crea los pájaros enemigos con diferentes tipos de movimiento
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

        // Array de tipos de movimiento disponibles para pájaros
        TipoMovimiento[] tiposMovimiento = {
            TipoMovimiento.REBOTE,
            TipoMovimiento.ARCO,
            TipoMovimiento.ALEATORIO
        };

        for (int i = 0; i < this.cantidadMalos; i++){
            // Selecciona un tipo de movimiento aleatorio
            int tipoMov = random.nextInt(3);
            TipoMovimiento tipo = tiposMovimiento[tipoMov];

            // Crea el pájaro con el tipo de movimiento seleccionado
            Character pajaro = new Character("Pajaro" + i, "assets/pajaro.png", 20, tipo);
            pajaro.tipoMovimiento = tipoMov;
            pajaro.velocidadX = random.nextInt(15) + 3;
            pajaro.velocidadY = random.nextInt(15) + 3;
            pajaro.avanzando_y = movimientosArriba_Abajo[random.nextInt(2)];
            pajaro.avanzando_x = movimientosIzquierda_Derecha[random.nextInt(2)];

            // Configuración específica según tipo de movimiento
            if (tipoMov == 1) {
                // Movimiento en arco: ángulo inicial aleatorio
                pajaro.anguloMovimiento = random.nextDouble() * Math.PI * 2;
                pajaro.velocidadAngular = (random.nextDouble() - 0.5) * 0.1;
            } else if (tipoMov == 2) {
                // Movimiento aleatorio: configuración inicial
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

                // Aplica movimiento a todos los personajes usando MovimientoHandler
                for (Character c : personajes) {
                    aplicarMovimiento(c);
                }

                // Verifica colisiones entre el principal y los demás
                boolean colisionPrincipal = false;
                int vivos = 0;

                for (Character c : personajes) {
                    // Salta personajes ya colisionados
                    if (c.colisionado) continue;
                    // No verifica colisión consigo mismo
                    if (c.name.equals(principal.name)) continue;

                    // Verifica colisión con el personaje principal
                    if (principal.verificaColision(c)){
                        c.setColision(true);
                        colisionPrincipal = true;
                    } else {
                        c.setColision(false);
                        // Cuenta pájaros vivos (no águilas)
                        if (c.follow == null)
                            vivos++;
                    }
                }

                principal.setColision(colisionPrincipal);

                // Verifica colisiones entre proyectiles y águilas
                verificarColisionesProyectiles();

                // Verifica condiciones de fin del juego
                long tiempoTranscurrido = System.currentTimeMillis() - initTimeMillis;
                if (tiempoTranscurrido >= TIEMPO_LIMITE_MS) {
                    // Tiempo agotado
                    terminado = 3;
                    display.bufferedDraw();
                    timer.cancel();
                } else if (principal.cazado) {
                    // El zorro fue cazado por un águila
                    terminado = 2;
                    display.bufferedDraw();
                    timer.cancel();
                } else if (vivos == 2) {
                    // Todos los pájaros capturados (quedan fondo y jaula)
                    terminado = 1;
                    display.bufferedDraw();
                    timer.cancel();
                }

                // Dibuja el frame actual
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

        // Calcula la posición del personaje en pantalla
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

        // Determina la dirección según el ángulo calculado
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
        proyectiles.clear();
        crearPersonajes();

        // Cancela el timer actual y crea uno nuevo
        timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(comienzaJuego(timer), 0, delay);
    }

    /**
     * Dispara un proyectil (piedra) hacia el águila más cercana.
     * El proyectil se mueve en línea recta hacia la posición actual del águila.
     */
    public void disparar() {
        // Verifica cooldown para evitar disparar muy rápido
        long tiempoActual = System.currentTimeMillis();
        if (tiempoActual - ultimoDisparo < COOLDOWN_DISPARO_MS) {
            return;
        }
        ultimoDisparo = tiempoActual;

        // Busca el águila más cercana al zorrito
        Character aguilaCercana = buscarAguilaMasCercana();
        if (aguilaCercana == null) {
            return;
        }

        // Crea el proyectil en la posición del zorrito (scale 2 = piedra grande y visible)
        Character piedra = new Character("Piedra", "assets/piedra.png", 2, TipoMovimiento.PROYECTIL);
        piedra.x = principal.centroX;
        piedra.y = principal.centroY;
        piedra.esProyectil = true;
        piedra.proyectilActivo = true;
        piedra.colisiona = false;

        // Calcula la dirección hacia el águila
        double deltaX = aguilaCercana.centroX - principal.centroX;
        double deltaY = aguilaCercana.centroY - principal.centroY;
        double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Normaliza la dirección
        if (distancia > 0) {
            piedra.direccionX = deltaX / distancia;
            piedra.direccionY = deltaY / distancia;
        }

        // Agrega el proyectil a las listas
        proyectiles.add(piedra);
        personajes.add(piedra);
    }

    /**
     * Busca el águila más cercana al zorrito principal.
     *
     * @return El personaje águila más cercana o null si no hay águilas
     */
    private Character buscarAguilaMasCercana() {
        Character aguilaCercana = null;
        double distanciaMinima = Double.MAX_VALUE;

        // Itera sobre los personajes buscando águilas
        for (Character c : personajes) {
            // Las águilas tienen follow != null (siguen al zorrito)
            if (c.follow != null) {
                double deltaX = c.centroX - principal.centroX;
                double deltaY = c.centroY - principal.centroY;
                double distancia = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                // Actualiza si es la más cercana
                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    aguilaCercana = c;
                }
            }
        }

        return aguilaCercana;
    }

    /**
     * Verifica colisiones entre proyectiles y águilas.
     * Si hay impacto, el águila retrocede y el proyectil se desactiva.
     */
    public void verificarColisionesProyectiles() {
        // Lista para proyectiles a remover
        ArrayList<Character> proyectilesARemover = new ArrayList<Character>();

        // Itera sobre cada proyectil activo
        for (Character proyectil : proyectiles) {
            if (!proyectil.proyectilActivo) {
                proyectilesARemover.add(proyectil);
                continue;
            }

            // Verifica colisión con cada águila
            for (Character c : personajes) {
                // Las águilas tienen follow != null
                if (c.follow != null) {
                    // Calcula la distancia entre proyectil y águila
                    boolean colision = CollisionUtils.verificaColisionCircular(
                        proyectil.centroX, proyectil.centroY, proyectil.radio,
                        c.centroX, c.centroY, c.radio
                    );

                    // Si hay colisión, activa empuje y desactiva proyectil
                    if (colision) {
                        MovimientoHandler.aplicarRetrocesoAguila(c, proyectil, VELOCIDAD_EMPUJE_AGUILA);
                        proyectil.proyectilActivo = false;
                        proyectilesARemover.add(proyectil);
                        break;
                    }
                }
            }
        }

        // Remueve los proyectiles inactivos de las listas
        for (Character p : proyectilesARemover) {
            proyectiles.remove(p);
            personajes.remove(p);
        }
    }
}
