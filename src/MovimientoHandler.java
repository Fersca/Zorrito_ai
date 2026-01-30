import java.util.Random;

/**
 * Clase que maneja las estrategias de movimiento de forma procedural.
 * Reemplaza las lambdas por métodos estáticos para mejorar debuggeo.
 */
public class MovimientoHandler {

    // Instancia de Random reutilizable para movimientos aleatorios
    private static final Random random = new Random();

    /**
     * Movimiento nulo: el personaje no se mueve.
     * Usado para elementos estáticos como fondo y jaula.
     */
    public static void aplicarMovimientoNulo(Character c) {
        // No hace nada, el personaje permanece estático
    }

    /**
     * Movimiento de rebote: el personaje rebota en los bordes.
     * Usado para los pájaros enemigos.
     *
     * @param c El personaje a mover
     * @param jaulaX Posición X de la jaula (destino si colisiona)
     * @param jaulaY Posición Y de la jaula (destino si colisiona)
     * @param anchoDisplay Ancho de la pantalla
     * @param altoDisplay Alto de la pantalla
     */
    public static void aplicarMovimientoRebote(Character c, int jaulaX, int jaulaY,
            int anchoDisplay, int altoDisplay) {

        // Si colisionó, lo manda a la jaula
        if (c.colisionado) {
            c.x = jaulaX + 25;
            c.y = jaulaY + 40;
            return;
        }

        // Avanza según la dirección actual en X
        if (Direccion.Derecha == c.avanzando_x) {
            c.x = c.x + c.velocidadX;
        }
        if (Direccion.Izquierda == c.avanzando_x) {
            c.x = c.x - c.velocidadX;
        }

        // Avanza según la dirección actual en Y
        if (Direccion.Abajo == c.avanzando_y) {
            c.y = c.y + c.velocidadY;
        }
        if (Direccion.Arriba == c.avanzando_y) {
            c.y = c.y - c.velocidadY;
        }

        // Verifica rebote en bordes horizontales
        if (c.centroX > anchoDisplay) {
            c.avanzando_x = Direccion.Izquierda;
        }
        if (c.centroX < 0) {
            c.avanzando_x = Direccion.Derecha;
        }

        // Verifica rebote en bordes verticales
        if (c.centroY > altoDisplay) {
            c.avanzando_y = Direccion.Arriba;
        }
        if (c.centroY < 0) {
            c.avanzando_y = Direccion.Abajo;
        }

        // Aplica rotación visual
        c.angulo = c.angulo + c.rotaAngulo;
    }

    /**
     * Movimiento en arco: el personaje se mueve en curvas suaves.
     *
     * @param c El personaje a mover
     * @param jaulaX Posición X de la jaula
     * @param jaulaY Posición Y de la jaula
     * @param anchoDisplay Ancho de la pantalla
     * @param altoDisplay Alto de la pantalla
     */
    public static void aplicarMovimientoArco(Character c, int jaulaX, int jaulaY,
            int anchoDisplay, int altoDisplay) {

        // Si colisionó, lo manda a la jaula
        if (c.colisionado) {
            c.x = jaulaX + 25;
            c.y = jaulaY + 40;
            return;
        }

        // Actualiza el ángulo de movimiento para crear la curva
        c.anguloMovimiento += c.velocidadAngular;

        // Calcula la velocidad total
        double velocidad = Math.sqrt(c.velocidadX * c.velocidadX + c.velocidadY * c.velocidadY);

        // Calcula el desplazamiento basado en el ángulo
        int deltaX = (int) Math.round(Math.cos(c.anguloMovimiento) * velocidad);
        int deltaY = (int) Math.round(Math.sin(c.anguloMovimiento) * velocidad);

        // Aplica el movimiento
        c.x += deltaX;
        c.y += deltaY;

        // Verifica rebote en bordes horizontales
        if (c.centroX > anchoDisplay || c.centroX < 0) {
            c.anguloMovimiento = Math.PI - c.anguloMovimiento;
            c.velocidadAngular = -c.velocidadAngular;
        }

        // Verifica rebote en bordes verticales
        if (c.centroY > altoDisplay || c.centroY < 0) {
            c.anguloMovimiento = -c.anguloMovimiento;
            c.velocidadAngular = -c.velocidadAngular;
        }

        // Aplica rotación visual
        c.angulo = c.angulo + c.rotaAngulo;
    }

    /**
     * Movimiento aleatorio: el personaje cambia de dirección periódicamente.
     * Combina arcos con cambios aleatorios de trayectoria.
     *
     * @param c El personaje a mover
     * @param jaulaX Posición X de la jaula
     * @param jaulaY Posición Y de la jaula
     * @param anchoDisplay Ancho de la pantalla
     * @param altoDisplay Alto de la pantalla
     */
    public static void aplicarMovimientoAleatorio(Character c, int jaulaX, int jaulaY,
            int anchoDisplay, int altoDisplay) {

        // Si colisionó, lo manda a la jaula
        if (c.colisionado) {
            c.x = jaulaX + 25;
            c.y = jaulaY + 40;
            return;
        }

        // Incrementa el contador para cambio de dirección
        c.contadorCambio++;

        // Cambia la velocidad angular periódicamente
        if (c.contadorCambio >= c.frecuenciaCambio) {
            c.contadorCambio = 0;
            c.velocidadAngular = (random.nextDouble() - 0.5) * 0.15;
            c.frecuenciaCambio = random.nextInt(60) + 20;
        }

        // Actualiza el ángulo de movimiento
        c.anguloMovimiento += c.velocidadAngular;

        // Calcula la velocidad total y el desplazamiento
        double velocidad = Math.sqrt(c.velocidadX * c.velocidadX + c.velocidadY * c.velocidadY);
        int deltaX = (int) Math.round(Math.cos(c.anguloMovimiento) * velocidad);
        int deltaY = (int) Math.round(Math.sin(c.anguloMovimiento) * velocidad);

        // Aplica el movimiento
        c.x += deltaX;
        c.y += deltaY;

        // Rebote en borde derecho
        if (c.centroX > anchoDisplay) {
            c.anguloMovimiento = Math.PI - c.anguloMovimiento;
            c.x = anchoDisplay - c.radio;
        }

        // Rebote en borde izquierdo
        if (c.centroX < 0) {
            c.anguloMovimiento = Math.PI - c.anguloMovimiento;
            c.x = c.radio;
        }

        // Rebote en borde inferior
        if (c.centroY > altoDisplay) {
            c.anguloMovimiento = -c.anguloMovimiento;
            c.y = altoDisplay - c.radio;
        }

        // Rebote en borde superior
        if (c.centroY < 0) {
            c.anguloMovimiento = -c.anguloMovimiento;
            c.y = c.radio;
        }

        // Aplica rotación visual
        c.angulo = c.angulo + c.rotaAngulo;
    }

    // Duración del empuje en milisegundos (1.5 segundos)
    private static final long DURACION_EMPUJE_MS = 1500;

    /**
     * Movimiento de caza: el personaje persigue a otro.
     * Si está empujado, se mueve en la dirección del empuje durante 1.5 segundos.
     * Usado por las águilas para perseguir al zorro.
     *
     * @param c El personaje cazador
     */
    public static void aplicarMovimientoCazar(Character c) {
        // Si colisionó con la presa, la marca como cazada
        if (c.colisionado) {
            c.follow.cazado = true;
            return;
        }

        // Verifica si está en estado "empujado" por una piedra
        if (c.empujado) {
            long tiempoActual = System.currentTimeMillis();
            long tiempoTranscurrido = tiempoActual - c.tiempoInicioEmpuje;

            // Si pasó el tiempo de empuje, vuelve al comportamiento normal
            if (tiempoTranscurrido >= DURACION_EMPUJE_MS) {
                c.empujado = false;
            } else {
                // Continúa moviéndose en dirección del empuje (misma dir que la piedra)
                c.x += (int) (c.empujeDirX * c.velocidadEmpuje);
                c.y += (int) (c.empujeDirY * c.velocidadEmpuje);
                c.angulo = c.angulo + 8; // Gira mientras es empujada
                return;
            }
        }

        // Obtiene las posiciones de presa y cazador
        int presaX = c.follow.x;
        int presaY = c.follow.y;
        int cazadorX = c.x;
        int cazadorY = c.y;

        // Se mueve hacia la presa en X
        if (presaX < cazadorX) {
            c.x = c.x - c.velocidadX;
        } else {
            c.x = c.x + c.velocidadX;
        }

        // Se mueve hacia la presa en Y
        if (presaY < cazadorY) {
            c.y = c.y - c.velocidadY;
        } else {
            c.y = c.y + c.velocidadY;
        }

        // Aplica rotación visual
        c.angulo = c.angulo + c.rotaAngulo;
    }

    /**
     * Movimiento de proyectil: se mueve en línea recta hacia un objetivo.
     * Usado por las piedras que dispara el zorro.
     *
     * @param c El proyectil a mover
     * @param anchoDisplay Ancho de la pantalla
     * @param altoDisplay Alto de la pantalla
     */
    public static void aplicarMovimientoProyectil(Character c, int anchoDisplay, int altoDisplay) {
        // Si el proyectil ya no está activo, no hace nada
        if (!c.proyectilActivo) {
            return;
        }

        // Avanza en la dirección calculada al momento del disparo
        c.x += (int) (c.direccionX * c.velocidadProyectil);
        c.y += (int) (c.direccionY * c.velocidadProyectil);

        // Verifica si salió de la pantalla para desactivarlo
        if (c.x < -50 || c.x > anchoDisplay + 50 ||
            c.y < -50 || c.y > altoDisplay + 50) {
            c.proyectilActivo = false;
        }

        // Aplica rotación visual para efecto de giro de piedra
        c.angulo = c.angulo + 10;
    }

    /**
     * Activa el estado "empujado" en un águila cuando es impactada por un proyectil.
     * El águila se moverá en la misma dirección del proyectil durante 1.5 segundos.
     *
     * @param aguila El águila a empujar
     * @param proyectil El proyectil que impactó
     * @param velocidadEmpuje Velocidad del empuje (no se usa, se usa la del águila)
     */
    public static void aplicarRetrocesoAguila(Character aguila, Character proyectil, int velocidadEmpuje) {
        // Activa el estado empujado
        aguila.empujado = true;
        aguila.tiempoInicioEmpuje = System.currentTimeMillis();

        // La dirección del empuje es la MISMA que llevaba el proyectil (lo empuja)
        aguila.empujeDirX = proyectil.direccionX;
        aguila.empujeDirY = proyectil.direccionY;

        // Configura velocidad del empuje (más rápido que la velocidad normal)
        aguila.velocidadEmpuje = velocidadEmpuje;
    }
}
