/**
 * Utilidades para cálculos de movimiento.
 * Clase extraída para permitir testing unitario.
 */
public class MovementUtils {

    /**
     * Resultado del cálculo de dirección basado en posición del mouse.
     */
    public static class DireccionResultado {
        public final Direccion direccionX;
        public final Direccion direccionY;
        public final double angulo;
        public final boolean debeMover;

        public DireccionResultado(Direccion x, Direccion y, double angulo, boolean debeMover) {
            this.direccionX = x;
            this.direccionY = y;
            this.angulo = angulo;
            this.debeMover = debeMover;
        }
    }

    /**
     * Calcula la dirección de movimiento basándose en la posición del mouse
     * relativa a la posición del personaje.
     *
     * @param personajeX Posición X del personaje (en coordenadas de pantalla)
     * @param personajeY Posición Y del personaje (en coordenadas de pantalla)
     * @param mouseX Posición X del mouse
     * @param mouseY Posición Y del mouse
     * @param umbralMovimiento Distancia mínima para activar el movimiento
     * @return DireccionResultado con las direcciones calculadas
     */
    public static DireccionResultado calcularDireccionPorMouse(
            double personajeX, double personajeY,
            int mouseX, int mouseY,
            int umbralMovimiento) {

        double opuesto = 0;
        double adyacente = 0;
        double sumar = 0;

        // Calcula el cuadrante en el cual está el mouse
        // Usar >= y <= para manejar casos donde están en la misma línea
        if (personajeX <= mouseX && personajeY <= mouseY) {
            // Cuadrante 3: mouse abajo-derecha (o en línea)
            opuesto = mouseY - personajeY;
            adyacente = mouseX - personajeX;
            sumar = 90;
        } else if (personajeX > mouseX && personajeY <= mouseY) {
            // Cuadrante 4: mouse abajo-izquierda
            opuesto = personajeX - mouseX;
            adyacente = mouseY - personajeY;
            sumar = 180;
        } else if (personajeX <= mouseX && personajeY > mouseY) {
            // Cuadrante 2: mouse arriba-derecha
            opuesto = mouseX - personajeX;
            adyacente = personajeY - mouseY;
            sumar = 0;
        } else if (personajeX > mouseX && personajeY > mouseY) {
            // Cuadrante 1: mouse arriba-izquierda
            opuesto = personajeY - mouseY;
            adyacente = personajeX - mouseX;
            sumar = 270;
        }

        // Calcular distancia total para verificar umbral
        double distanciaTotal = Math.sqrt(opuesto * opuesto + adyacente * adyacente);

        // Si el mouse está muy cerca del personaje, no lo mueve
        if (distanciaTotal < umbralMovimiento) {
            return new DireccionResultado(Direccion.Quieto, Direccion.Quieto, 0, false);
        }

        // Evitar división por cero
        if (adyacente == 0) {
            adyacente = 1;
        }

        double tangente = opuesto / adyacente;
        double angulo = Math.toDegrees(Math.atan(tangente)) + sumar;

        // Determinar direcciones basadas en el ángulo
        Direccion dirX;
        Direccion dirY;

        if (angulo > 22.5 && angulo < 67.5) {
            dirX = Direccion.Derecha;
            dirY = Direccion.Arriba;
        } else if (angulo > 67.5 && angulo < 112.5) {
            dirX = Direccion.Derecha;
            dirY = Direccion.Quieto;
        } else if (angulo > 112.5 && angulo < 157.5) {
            dirX = Direccion.Derecha;
            dirY = Direccion.Abajo;
        } else if (angulo > 157.5 && angulo < 202.5) {
            dirX = Direccion.Quieto;
            dirY = Direccion.Abajo;
        } else if (angulo > 202.5 && angulo < 247.5) {
            dirX = Direccion.Izquierda;
            dirY = Direccion.Abajo;
        } else if (angulo > 247.5 && angulo < 292.5) {
            dirX = Direccion.Izquierda;
            dirY = Direccion.Quieto;
        } else if (angulo > 292.5 && angulo < 337.5) {
            dirX = Direccion.Izquierda;
            dirY = Direccion.Arriba;
        } else {
            // angulo > 337.5 || angulo < 22.5
            dirX = Direccion.Quieto;
            dirY = Direccion.Arriba;
        }

        return new DireccionResultado(dirX, dirY, angulo, true);
    }

    /**
     * Calcula la nueva posición después de aplicar un movimiento de rebote.
     *
     * @param x Posición X actual
     * @param y Posición Y actual
     * @param velocidadX Velocidad en X
     * @param velocidadY Velocidad en Y
     * @param avanzandoX Dirección actual en X
     * @param avanzandoY Dirección actual en Y
     * @param anchoLimite Límite derecho (ancho de pantalla)
     * @param altoLimite Límite inferior (alto de pantalla)
     * @return Array con [nuevoX, nuevoY, nuevaDireccionX (0=Der, 1=Izq), nuevaDireccionY (0=Abajo, 1=Arriba)]
     */
    public static int[] calcularMovimientoRebote(
            int x, int y,
            int velocidadX, int velocidadY,
            Direccion avanzandoX, Direccion avanzandoY,
            int centroX, int centroY,
            int anchoLimite, int altoLimite) {

        int nuevoX = x;
        int nuevoY = y;
        int nuevaDirX = avanzandoX == Direccion.Derecha ? 0 : 1;
        int nuevaDirY = avanzandoY == Direccion.Abajo ? 0 : 1;

        // Aplicar movimiento
        if (avanzandoX == Direccion.Derecha) {
            nuevoX = x + velocidadX;
        } else if (avanzandoX == Direccion.Izquierda) {
            nuevoX = x - velocidadX;
        }

        if (avanzandoY == Direccion.Abajo) {
            nuevoY = y + velocidadY;
        } else if (avanzandoY == Direccion.Arriba) {
            nuevoY = y - velocidadY;
        }

        // Calcular nuevo centro aproximado
        int nuevoCentroX = centroX + (nuevoX - x);
        int nuevoCentroY = centroY + (nuevoY - y);

        // Verificar rebotes
        if (nuevoCentroX > anchoLimite) {
            nuevaDirX = 1; // Izquierda
        } else if (nuevoCentroX < 0) {
            nuevaDirX = 0; // Derecha
        }

        if (nuevoCentroY > altoLimite) {
            nuevaDirY = 1; // Arriba
        } else if (nuevoCentroY < 0) {
            nuevaDirY = 0; // Abajo
        }

        return new int[] { nuevoX, nuevoY, nuevaDirX, nuevaDirY };
    }

    /**
     * Calcula el movimiento de persecución hacia un objetivo.
     *
     * @param cazadorX Posición X del cazador
     * @param cazadorY Posición Y del cazador
     * @param presaX Posición X de la presa
     * @param presaY Posición Y de la presa
     * @param velocidadX Velocidad en X
     * @param velocidadY Velocidad en Y
     * @return Array con [nuevoX, nuevoY]
     */
    public static int[] calcularMovimientoCazar(
            int cazadorX, int cazadorY,
            int presaX, int presaY,
            int velocidadX, int velocidadY) {

        int nuevoX = cazadorX;
        int nuevoY = cazadorY;

        if (presaX < cazadorX) {
            nuevoX = cazadorX - velocidadX;
        } else {
            nuevoX = cazadorX + velocidadX;
        }

        if (presaY < cazadorY) {
            nuevoY = cazadorY - velocidadY;
        } else {
            nuevoY = cazadorY + velocidadY;
        }

        return new int[] { nuevoX, nuevoY };
    }

    /**
     * Calcula el desplazamiento para un movimiento diagonal.
     *
     * @param velocidad Velocidad base
     * @return Velocidad ajustada para movimiento diagonal (factor ~0.707)
     */
    public static int calcularVelocidadDiagonal(int velocidad) {
        // Para movimiento diagonal, aplicar factor de sqrt(2)/2 ≈ 0.707
        // Esto mantiene la velocidad total constante en todas las direcciones
        return (int) Math.round(velocidad * 0.707);
    }
}
