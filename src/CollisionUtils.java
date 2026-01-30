/**
 * Utilidades para detección de colisiones.
 * Clase extraída para permitir testing unitario.
 */
public class CollisionUtils {

    /**
     * Verifica si dos círculos colisionan basándose en sus centros y radios.
     *
     * @param x1 Centro X del primer objeto
     * @param y1 Centro Y del primer objeto
     * @param radio1 Radio del primer objeto
     * @param x2 Centro X del segundo objeto
     * @param y2 Centro Y del segundo objeto
     * @param radio2 Radio del segundo objeto
     * @return true si hay colisión, false en caso contrario
     */
    public static boolean verificaColisionCircular(int x1, int y1, int radio1,
                                                    int x2, int y2, int radio2) {
        int deltaX = Math.abs(x1 - x2);
        int deltaY = Math.abs(y1 - y2);

        // Usar distancia al cuadrado para evitar sqrt (optimización)
        int distanciaCuadrada = deltaX * deltaX + deltaY * deltaY;
        int sumaRadios = radio1 + radio2;
        int sumaRadiosCuadrada = sumaRadios * sumaRadios;

        return distanciaCuadrada < sumaRadiosCuadrada;
    }

    /**
     * Calcula la distancia entre dos puntos.
     *
     * @param x1 Coordenada X del primer punto
     * @param y1 Coordenada Y del primer punto
     * @param x2 Coordenada X del segundo punto
     * @param y2 Coordenada Y del segundo punto
     * @return La distancia euclidiana entre los dos puntos
     */
    public static double calcularDistancia(int x1, int y1, int x2, int y2) {
        int deltaX = x1 - x2;
        int deltaY = y1 - y2;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    /**
     * Calcula la distancia al cuadrado entre dos puntos.
     * Más eficiente que calcularDistancia cuando solo se necesita comparar distancias.
     *
     * @param x1 Coordenada X del primer punto
     * @param y1 Coordenada Y del primer punto
     * @param x2 Coordenada X del segundo punto
     * @param y2 Coordenada Y del segundo punto
     * @return La distancia al cuadrado entre los dos puntos
     */
    public static int calcularDistanciaCuadrada(int x1, int y1, int x2, int y2) {
        int deltaX = x1 - x2;
        int deltaY = y1 - y2;
        return deltaX * deltaX + deltaY * deltaY;
    }

    /**
     * Calcula el centro de un rectángulo dado su origen y dimensiones.
     *
     * @param x Coordenada X del origen
     * @param y Coordenada Y del origen
     * @param width Ancho del rectángulo
     * @param height Alto del rectángulo
     * @return Array con [centroX, centroY]
     */
    public static int[] calcularCentro(int x, int y, int width, int height) {
        return new int[] { x + width / 2, y + height / 2 };
    }

    /**
     * Calcula el radio de colisión basado en las dimensiones.
     * Usa el mayor de los dos lados dividido por 2.
     *
     * @param width Ancho del objeto
     * @param height Alto del objeto
     * @return Radio de colisión
     */
    public static int calcularRadio(int width, int height) {
        return Math.max(width, height) / 2;
    }
}
