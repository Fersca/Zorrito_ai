import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de CollisionUtils")
class CollisionUtilsTest {

    @Test
    @DisplayName("Detecta colisión cuando círculos se superponen")
    void testColisionCuandoCirculosSeSuperponen() {
        // Dos círculos en posiciones cercanas que deberían colisionar
        // Círculo 1: centro (100, 100), radio 50
        // Círculo 2: centro (140, 100), radio 50
        // Distancia entre centros: 40, suma de radios: 100 -> colisión
        assertTrue(
            CollisionUtils.verificaColisionCircular(100, 100, 50, 140, 100, 50),
            "Debería detectar colisión cuando los círculos se superponen"
        );
    }

    @Test
    @DisplayName("No detecta colisión cuando círculos están separados")
    void testNoColisionCuandoCirculosEstanSeparados() {
        // Círculo 1: centro (100, 100), radio 50
        // Círculo 2: centro (300, 100), radio 50
        // Distancia entre centros: 200, suma de radios: 100 -> no colisión
        assertFalse(
            CollisionUtils.verificaColisionCircular(100, 100, 50, 300, 100, 50),
            "No debería detectar colisión cuando los círculos están separados"
        );
    }

    @Test
    @DisplayName("Detecta colisión exacta en el límite")
    void testColisionEnElLimite() {
        // Círculo 1: centro (0, 0), radio 50
        // Círculo 2: centro (99, 0), radio 50
        // Distancia: 99, suma de radios: 100 -> colisión (justo en el límite)
        assertTrue(
            CollisionUtils.verificaColisionCircular(0, 0, 50, 99, 0, 50),
            "Debería detectar colisión en el límite"
        );
    }

    @Test
    @DisplayName("No detecta colisión justo fuera del límite")
    void testNoColisionJustoFueraDelLimite() {
        // Círculo 1: centro (0, 0), radio 50
        // Círculo 2: centro (101, 0), radio 50
        // Distancia: 101, suma de radios: 100 -> no colisión
        assertFalse(
            CollisionUtils.verificaColisionCircular(0, 0, 50, 101, 0, 50),
            "No debería detectar colisión justo fuera del límite"
        );
    }

    @Test
    @DisplayName("Detecta colisión con círculos en diagonal")
    void testColisionEnDiagonal() {
        // Círculo 1: centro (0, 0), radio 50
        // Círculo 2: centro (50, 50), radio 50
        // Distancia: sqrt(50^2 + 50^2) = ~70.7, suma de radios: 100 -> colisión
        assertTrue(
            CollisionUtils.verificaColisionCircular(0, 0, 50, 50, 50, 50),
            "Debería detectar colisión en diagonal"
        );
    }

    @Test
    @DisplayName("Maneja coordenadas negativas correctamente")
    void testCoordenadasNegativas() {
        // Círculo 1: centro (-100, -100), radio 50
        // Círculo 2: centro (-60, -100), radio 50
        // Distancia: 40, suma de radios: 100 -> colisión
        assertTrue(
            CollisionUtils.verificaColisionCircular(-100, -100, 50, -60, -100, 50),
            "Debería manejar coordenadas negativas"
        );
    }

    @Test
    @DisplayName("Detecta colisión con círculos de diferente tamaño")
    void testColisionCirculosDiferenteTamano() {
        // Círculo 1: centro (100, 100), radio 100
        // Círculo 2: centro (180, 100), radio 30
        // Distancia: 80, suma de radios: 130 -> colisión
        assertTrue(
            CollisionUtils.verificaColisionCircular(100, 100, 100, 180, 100, 30),
            "Debería detectar colisión con círculos de diferente tamaño"
        );
    }

    @Test
    @DisplayName("Círculos en el mismo punto siempre colisionan")
    void testCirculosEnMismoPunto() {
        assertTrue(
            CollisionUtils.verificaColisionCircular(100, 100, 50, 100, 100, 50),
            "Círculos en el mismo punto deberían colisionar"
        );
    }

    @Test
    @DisplayName("Calcula distancia correctamente")
    void testCalcularDistancia() {
        // Triángulo 3-4-5
        double distancia = CollisionUtils.calcularDistancia(0, 0, 3, 4);
        assertEquals(5.0, distancia, 0.001, "La distancia debería ser 5");
    }

    @Test
    @DisplayName("Calcula distancia al cuadrado correctamente")
    void testCalcularDistanciaCuadrada() {
        // Triángulo 3-4-5, distancia al cuadrado = 25
        int distanciaCuadrada = CollisionUtils.calcularDistanciaCuadrada(0, 0, 3, 4);
        assertEquals(25, distanciaCuadrada, "La distancia al cuadrado debería ser 25");
    }

    @Test
    @DisplayName("Calcula centro correctamente")
    void testCalcularCentro() {
        // Rectángulo en (100, 100) con dimensiones 50x30
        // Centro debería ser (125, 115)
        int[] centro = CollisionUtils.calcularCentro(100, 100, 50, 30);
        assertEquals(125, centro[0], "Centro X debería ser 125");
        assertEquals(115, centro[1], "Centro Y debería ser 115");
    }

    @Test
    @DisplayName("Calcula radio correctamente tomando el mayor lado")
    void testCalcularRadio() {
        // Con dimensiones 100x60, el radio debería ser 50 (100/2)
        assertEquals(50, CollisionUtils.calcularRadio(100, 60),
            "El radio debería ser la mitad del lado mayor");

        // Con dimensiones 60x100, el radio debería ser 50 (100/2)
        assertEquals(50, CollisionUtils.calcularRadio(60, 100),
            "El radio debería ser la mitad del lado mayor");
    }

    @Test
    @DisplayName("Calcula radio con dimensiones iguales")
    void testCalcularRadioDimensionesIguales() {
        assertEquals(50, CollisionUtils.calcularRadio(100, 100),
            "Con dimensiones iguales, el radio es la mitad");
    }
}
