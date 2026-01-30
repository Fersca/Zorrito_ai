import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la clase Character.
 * Verifica la lógica de colisiones, estado y propiedades.
 */
@DisplayName("Tests de Character")
class CharacterTest {

    private Character personaje;

    @BeforeEach
    void setUp() {
        // Crea un personaje con imagen existente para los tests
        personaje = new Character("TestChar", "assets/bosque.png", 1, TipoMovimiento.NULO);
    }

    @Nested
    @DisplayName("Tests de construcción")
    class ConstruccionTests {

        @Test
        @DisplayName("Constructor asigna nombre correctamente")
        void constructorAsignaNombre() {
            Character c = new Character("MiPersonaje", "assets/bosque.png", 1, TipoMovimiento.REBOTE);
            assertEquals("MiPersonaje", c.name);
        }

        @Test
        @DisplayName("Constructor asigna tipo de movimiento")
        void constructorAsignaTipoMovimiento() {
            Character c = new Character("Test", "assets/bosque.png", 1, TipoMovimiento.CAZAR);
            assertEquals(TipoMovimiento.CAZAR, c.tipoMovimientoEnum);
        }

        @Test
        @DisplayName("Valores por defecto son correctos")
        void valoresPorDefecto() {
            assertEquals(500, personaje.x);
            assertEquals(500, personaje.y);
            assertEquals(1, personaje.velocidadX);
            assertEquals(1, personaje.velocidadY);
            assertEquals(0, personaje.angulo);
            assertFalse(personaje.colisionado);
            assertFalse(personaje.cazado);
            assertTrue(personaje.colisiona);
        }

        @Test
        @DisplayName("Dirección inicial es Derecha/Abajo")
        void direccionInicial() {
            assertEquals(Direccion.Derecha, personaje.avanzando_x);
            assertEquals(Direccion.Abajo, personaje.avanzando_y);
        }
    }

    @Nested
    @DisplayName("Tests de setColision")
    class SetColisionTests {

        @Test
        @DisplayName("Marca personaje como colisionado")
        void marcaColisionado() {
            personaje.setColision(true);
            assertTrue(personaje.colisionado);
        }

        @Test
        @DisplayName("Desmarca personaje colisionado")
        void desmarcaColisionado() {
            personaje.colisionado = true;
            personaje.setColision(false);
            assertFalse(personaje.colisionado);
        }

        @Test
        @DisplayName("Colisión activa rotación para no-Zorrito")
        void colisionActivaRotacion() {
            personaje.name = "Pajaro1";
            personaje.setColision(true);
            assertEquals(5, personaje.rotaAngulo);
        }

        @Test
        @DisplayName("Colisión NO activa rotación para Zorrito")
        void colisionNoRotaZorrito() {
            personaje.name = "Zorrito";
            personaje.setColision(true);
            assertEquals(0, personaje.rotaAngulo);
        }

        @Test
        @DisplayName("Sin colisión resetea rotación")
        void sinColisionResetaRotacion() {
            personaje.rotaAngulo = 5;
            personaje.setColision(false);
            assertEquals(0, personaje.rotaAngulo);
        }
    }

    @Nested
    @DisplayName("Tests de verificaColision")
    class VerificaColisionTests {

        @Test
        @DisplayName("Detecta colisión cuando círculos se superponen")
        void detectaColision() {
            Character c1 = new Character("A", "assets/bosque.png", 1, TipoMovimiento.NULO);
            Character c2 = new Character("B", "assets/bosque.png", 1, TipoMovimiento.NULO);

            c1.centroX = 100;
            c1.centroY = 100;
            c1.radio = 50;

            c2.centroX = 120;
            c2.centroY = 100;
            c2.radio = 50;

            assertTrue(c1.verificaColision(c2));
        }

        @Test
        @DisplayName("No detecta colisión cuando círculos están separados")
        void noDetectaColisionSeparados() {
            Character c1 = new Character("A", "assets/bosque.png", 1, TipoMovimiento.NULO);
            Character c2 = new Character("B", "assets/bosque.png", 1, TipoMovimiento.NULO);

            c1.centroX = 0;
            c1.centroY = 0;
            c1.radio = 10;

            c2.centroX = 100;
            c2.centroY = 100;
            c2.radio = 10;

            assertFalse(c1.verificaColision(c2));
        }

        @Test
        @DisplayName("No colisiona si el otro tiene colisiona=false")
        void noColisionaSiDeshabilitado() {
            Character c1 = new Character("A", "assets/bosque.png", 1, TipoMovimiento.NULO);
            Character c2 = new Character("B", "assets/bosque.png", 1, TipoMovimiento.NULO);

            c1.centroX = 100;
            c1.centroY = 100;
            c1.radio = 50;

            c2.centroX = 100;
            c2.centroY = 100;
            c2.radio = 50;
            c2.colisiona = false;  // Deshabilitado como la jaula

            assertFalse(c1.verificaColision(c2));
        }
    }

    @Nested
    @DisplayName("Tests de actualizaCentroYRadio")
    class ActualizaCentroYRadioTests {

        @Test
        @DisplayName("Calcula centro correctamente")
        void calculaCentro() {
            personaje.x = 100;
            personaje.y = 200;
            personaje.width = 50;
            personaje.height = 40;

            personaje.actualizaCentroYRadio();

            assertEquals(125, personaje.centroX);
            assertEquals(220, personaje.centroY);
        }

        @Test
        @DisplayName("Calcula radio tomando el mayor lado")
        void calculaRadioMayorLado() {
            personaje.width = 100;
            personaje.height = 60;
            personaje.radio = 0;  // Fuerza recálculo

            personaje.actualizaCentroYRadio();

            assertEquals(50, personaje.radio);  // 100/2
        }

        @Test
        @DisplayName("No recalcula radio si ya existe")
        void noRecalculaRadio() {
            personaje.radio = 30;
            personaje.width = 100;

            personaje.actualizaCentroYRadio();

            assertEquals(30, personaje.radio);  // Mantiene el original
        }

        @Test
        @DisplayName("No procesa si es Bosque")
        void noProcesaBosque() {
            personaje.name = "Bosque";
            personaje.x = 999;
            personaje.centroX = 0;

            personaje.actualizaCentroYRadio();

            assertEquals(0, personaje.centroX);  // No cambió
        }

        @Test
        @DisplayName("Incrementa ángulo si colisionado")
        void incrementaAnguloSiColisionado() {
            personaje.colisionado = true;
            personaje.angulo = 10;
            personaje.rotaAngulo = 5;

            personaje.actualizaCentroYRadio();

            assertEquals(15, personaje.angulo);
        }
    }

    @Nested
    @DisplayName("Tests de propiedades de sprites")
    class SpritesTests {

        @Test
        @DisplayName("hasSprites es false por defecto")
        void hasSpritesFalsePorDefecto() {
            assertFalse(personaje.hasSprites);
        }

        @Test
        @DisplayName("spritesArray es null por defecto")
        void spritesArrayNullPorDefecto() {
            assertNull(personaje.spritesArray);
        }

        @Test
        @DisplayName("Puede asignar array de sprites")
        void puedeAsignarSprites() {
            personaje.hasSprites = true;
            personaje.spritesArray = new Character.Sprite[4];
            personaje.spritesArray[0] = new Character.Sprite(0, 0, 100, 100);

            assertEquals(4, personaje.spritesArray.length);
            assertEquals(0, personaje.spritesArray[0].x());
            assertEquals(100, personaje.spritesArray[0].w());
        }
    }

    @Nested
    @DisplayName("Tests de follow (para águilas)")
    class FollowTests {

        @Test
        @DisplayName("follow es null por defecto")
        void followNullPorDefecto() {
            assertNull(personaje.follow);
        }

        @Test
        @DisplayName("Puede asignar personaje a seguir")
        void puedeAsignarFollow() {
            Character presa = new Character("Zorrito", "assets/bosque.png", 1, TipoMovimiento.NULO);
            personaje.follow = presa;

            assertNotNull(personaje.follow);
            assertEquals("Zorrito", personaje.follow.name);
        }
    }

    @Nested
    @DisplayName("Tests de propiedades de movimiento")
    class MovimientoPropsTests {

        @Test
        @DisplayName("anguloMovimiento inicia en 0")
        void anguloMovimientoInicial() {
            assertEquals(0, personaje.anguloMovimiento, 0.001);
        }

        @Test
        @DisplayName("velocidadAngular inicia en 0")
        void velocidadAngularInicial() {
            assertEquals(0, personaje.velocidadAngular, 0.001);
        }

        @Test
        @DisplayName("contadorCambio inicia en 0")
        void contadorCambioInicial() {
            assertEquals(0, personaje.contadorCambio);
        }

        @Test
        @DisplayName("frecuenciaCambio inicia en 50")
        void frecuenciaCambioInicial() {
            assertEquals(50, personaje.frecuenciaCambio);
        }

        @Test
        @DisplayName("esFondoInfinito es false por defecto")
        void esFondoInfinitoFalse() {
            assertFalse(personaje.esFondoInfinito);
        }
    }
}
