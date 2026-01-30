import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * Tests unitarios para la funcionalidad de disparo del Juego.
 * Verifica la creación de proyectiles y colisiones.
 */
@DisplayName("Tests de Disparo en Juego")
class JuegoDisparoTest {

    /**
     * Clase auxiliar para testear Juego sin display real.
     * Extiende Juego para acceder a métodos protegidos.
     */
    static class JuegoTesteable extends Juego {
        // Display simulado con dimensiones fijas
        private int anchoSimulado = 800;
        private int altoSimulado = 600;

        public JuegoTesteable() {
            super();
        }

        // Configura personajes de prueba
        public void configurarParaTest() {
            this.personajes = new ArrayList<Character>();
            this.proyectiles = new ArrayList<Character>();

            // Crea zorrito principal
            Character zorrito = new Character("Zorrito", "assets/bosque.png", 10, TipoMovimiento.NULO);
            zorrito.x = 400;
            zorrito.y = 300;
            zorrito.centroX = 400;
            zorrito.centroY = 300;
            zorrito.radio = 20;
            this.principal = zorrito;
            this.personajes.add(zorrito);
        }

        // Agrega un águila de prueba
        public Character agregarAguilaTest(int x, int y) {
            Character aguila = new Character("Aguila", "assets/bosque.png", 7, TipoMovimiento.CAZAR);
            aguila.x = x;
            aguila.y = y;
            aguila.centroX = x;
            aguila.centroY = y;
            aguila.radio = 15;
            aguila.follow = this.principal;
            this.personajes.add(aguila);
            return aguila;
        }

        // Agrega un proyectil de prueba
        public Character agregarProyectilTest(int x, int y, double dirX, double dirY) {
            Character piedra = new Character("Piedra", "assets/bosque.png", 15, TipoMovimiento.PROYECTIL);
            piedra.x = x;
            piedra.y = y;
            piedra.centroX = x;
            piedra.centroY = y;
            piedra.radio = 5;
            piedra.direccionX = dirX;
            piedra.direccionY = dirY;
            piedra.esProyectil = true;
            piedra.proyectilActivo = true;
            this.proyectiles.add(piedra);
            this.personajes.add(piedra);
            return piedra;
        }
    }

    @Nested
    @DisplayName("Tests de búsqueda de águila cercana")
    class BusquedaAguilaTests {

        private JuegoTesteable juego;

        @BeforeEach
        void setUp() {
            juego = new JuegoTesteable();
            juego.configurarParaTest();
        }

        @Test
        @DisplayName("Encuentra águila más cercana cuando hay una")
        void encuentraAguilaCercana() {
            juego.agregarAguilaTest(500, 300);

            // Usa reflexión o método público para probar
            // Como buscarAguilaMasCercana es privado, lo probamos indirectamente con disparar
            juego.disparar();

            // Si dispara, crea un proyectil
            assertEquals(1, juego.proyectiles.size());
        }

        @Test
        @DisplayName("No dispara si no hay águilas")
        void noDisparaSinAguilas() {
            // No hay águilas agregadas
            juego.disparar();

            assertEquals(0, juego.proyectiles.size());
        }

        @Test
        @DisplayName("Elige la más cercana de varias águilas")
        void eligeLaMasCercana() {
            // Águila lejos
            juego.agregarAguilaTest(700, 300);
            // Águila cercana
            Character aguilaCercana = juego.agregarAguilaTest(450, 300);

            juego.disparar();

            // Debe haber creado un proyectil
            assertEquals(1, juego.proyectiles.size());

            // El proyectil debe ir hacia la derecha (hacia el águila cercana)
            Character proyectil = juego.proyectiles.get(0);
            assertTrue(proyectil.direccionX > 0);
        }
    }

    @Nested
    @DisplayName("Tests de creación de proyectil")
    class CreacionProyectilTests {

        private JuegoTesteable juego;

        @BeforeEach
        void setUp() {
            juego = new JuegoTesteable();
            juego.configurarParaTest();
        }

        @Test
        @DisplayName("Proyectil se crea en posición del zorrito")
        void proyectilEnPosicionZorrito() {
            juego.agregarAguilaTest(600, 300);

            juego.disparar();

            Character proyectil = juego.proyectiles.get(0);
            assertEquals(juego.principal.centroX, proyectil.x);
            assertEquals(juego.principal.centroY, proyectil.y);
        }

        @Test
        @DisplayName("Proyectil tiene dirección hacia el águila")
        void proyectilDireccionHaciaAguila() {
            // Águila a la derecha del zorrito
            juego.agregarAguilaTest(600, 300);

            juego.disparar();

            Character proyectil = juego.proyectiles.get(0);
            // Dirección X debe ser positiva (hacia la derecha)
            assertTrue(proyectil.direccionX > 0);
            // Dirección Y debe ser aproximadamente 0
            assertTrue(Math.abs(proyectil.direccionY) < 0.1);
        }

        @Test
        @DisplayName("Proyectil tiene tipo PROYECTIL")
        void proyectilTieneTipoCorrecto() {
            juego.agregarAguilaTest(600, 300);

            juego.disparar();

            Character proyectil = juego.proyectiles.get(0);
            assertEquals(TipoMovimiento.PROYECTIL, proyectil.tipoMovimientoEnum);
        }

        @Test
        @DisplayName("Proyectil está activo al crearse")
        void proyectilActivoAlCrearse() {
            juego.agregarAguilaTest(600, 300);

            juego.disparar();

            Character proyectil = juego.proyectiles.get(0);
            assertTrue(proyectil.proyectilActivo);
            assertTrue(proyectil.esProyectil);
        }

        @Test
        @DisplayName("Respeta cooldown entre disparos")
        void respetaCooldown() {
            juego.agregarAguilaTest(600, 300);

            // Primer disparo
            juego.disparar();
            assertEquals(1, juego.proyectiles.size());

            // Segundo disparo inmediato (debería ignorarse por cooldown)
            juego.disparar();
            assertEquals(1, juego.proyectiles.size());
        }
    }

    @Nested
    @DisplayName("Tests de colisión proyectil-águila")
    class ColisionProyectilAguilaTests {

        private JuegoTesteable juego;

        @BeforeEach
        void setUp() {
            juego = new JuegoTesteable();
            juego.configurarParaTest();
        }

        @Test
        @DisplayName("Detecta colisión y activa estado empujado")
        void detectaColisionYActivaEmpuje() {
            // Crea águila en posición conocida
            Character aguila = juego.agregarAguilaTest(420, 300);

            // Crea proyectil que colisiona (misma posición)
            juego.agregarProyectilTest(420, 300, 1.0, 0.0);

            juego.verificarColisionesProyectiles();

            // Águila debe estar en estado empujado
            assertTrue(aguila.empujado);
            assertEquals(1.0, aguila.empujeDirX, 0.001);
            assertEquals(0.0, aguila.empujeDirY, 0.001);
        }

        @Test
        @DisplayName("Desactiva proyectil al impactar")
        void desactivaProyectilAlImpactar() {
            juego.agregarAguilaTest(420, 300);
            Character proyectil = juego.agregarProyectilTest(420, 300, 1.0, 0.0);

            juego.verificarColisionesProyectiles();

            assertFalse(proyectil.proyectilActivo);
        }

        @Test
        @DisplayName("Remueve proyectil de las listas al impactar")
        void remueveProyectilAlImpactar() {
            juego.agregarAguilaTest(420, 300);
            juego.agregarProyectilTest(420, 300, 1.0, 0.0);

            assertEquals(1, juego.proyectiles.size());

            juego.verificarColisionesProyectiles();

            assertEquals(0, juego.proyectiles.size());
        }

        @Test
        @DisplayName("No afecta águilas lejanas")
        void noAfectaAguilasLejanas() {
            Character aguila = juego.agregarAguilaTest(700, 300);
            int posXInicial = aguila.x;

            // Proyectil lejos del águila
            juego.agregarProyectilTest(100, 100, 1.0, 0.0);

            juego.verificarColisionesProyectiles();

            // Águila no debe moverse
            assertEquals(posXInicial, aguila.x);
        }

        @Test
        @DisplayName("Remueve proyectiles inactivos")
        void remueveProyectilesInactivos() {
            // Crea proyectil ya inactivo
            Character proyectil = juego.agregarProyectilTest(100, 100, 1.0, 0.0);
            proyectil.proyectilActivo = false;

            juego.verificarColisionesProyectiles();

            assertEquals(0, juego.proyectiles.size());
        }
    }

    @Nested
    @DisplayName("Tests de dirección del proyectil")
    class DireccionProyectilTests {

        private JuegoTesteable juego;

        @BeforeEach
        void setUp() {
            juego = new JuegoTesteable();
            juego.configurarParaTest();
        }

        @Test
        @DisplayName("Dirección normalizada hacia la derecha")
        void direccionHaciaDerecha() {
            juego.agregarAguilaTest(600, 300);

            juego.disparar();

            Character proyectil = juego.proyectiles.get(0);
            // La dirección debe estar normalizada (magnitud cercana a 1)
            double magnitud = Math.sqrt(proyectil.direccionX * proyectil.direccionX +
                                       proyectil.direccionY * proyectil.direccionY);
            assertEquals(1.0, magnitud, 0.01);
        }

        @Test
        @DisplayName("Dirección hacia arriba")
        void direccionHaciaArriba() {
            // Águila arriba del zorrito
            juego.agregarAguilaTest(400, 100);

            juego.disparar();

            Character proyectil = juego.proyectiles.get(0);
            assertTrue(proyectil.direccionY < 0);
            assertTrue(Math.abs(proyectil.direccionX) < 0.1);
        }

        @Test
        @DisplayName("Dirección diagonal")
        void direccionDiagonal() {
            // Águila en diagonal
            juego.agregarAguilaTest(600, 500);

            juego.disparar();

            Character proyectil = juego.proyectiles.get(0);
            assertTrue(proyectil.direccionX > 0);
            assertTrue(proyectil.direccionY > 0);
        }
    }
}
