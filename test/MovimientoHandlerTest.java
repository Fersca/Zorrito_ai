import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para MovimientoHandler.
 * Verifica que cada estrategia de movimiento funcione correctamente.
 */
@DisplayName("Tests de MovimientoHandler")
class MovimientoHandlerTest {

    // Clase auxiliar para simular un Character en tests
    static class MockCharacter {
        public int x = 100;
        public int y = 100;
        public int centroX = 100;
        public int centroY = 100;
        public int radio = 20;
        public int velocidadX = 5;
        public int velocidadY = 5;
        public boolean colisionado = false;
        public int angulo = 0;
        public int rotaAngulo = 0;
        public Direccion avanzando_x = Direccion.Derecha;
        public Direccion avanzando_y = Direccion.Abajo;
        public double anguloMovimiento = 0;
        public double velocidadAngular = 0.1;
        public int contadorCambio = 0;
        public int frecuenciaCambio = 50;
        public MockCharacter follow = null;
        public boolean cazado = false;
    }

    @Nested
    @DisplayName("Tests de aplicarMovimientoNulo")
    class MovimientoNuloTests {

        @Test
        @DisplayName("No modifica la posición del personaje")
        void noModificaPosicion() {
            // Dado un personaje con posición definida
            Character c = new Character("Test", "bosque.png", 1, TipoMovimiento.NULO);
            c.x = 100;
            c.y = 200;

            // Cuando se aplica movimiento nulo
            MovimientoHandler.aplicarMovimientoNulo(c);

            // Entonces la posición no cambia
            assertEquals(100, c.x);
            assertEquals(200, c.y);
        }
    }

    @Nested
    @DisplayName("Tests de aplicarMovimientoRebote")
    class MovimientoReboteTests {

        @Test
        @DisplayName("Mueve hacia la derecha cuando avanza en esa dirección")
        void mueveHaciaDerecha() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.REBOTE);
            c.x = 100;
            c.velocidadX = 5;
            c.avanzando_x = Direccion.Derecha;
            c.avanzando_y = Direccion.Quieto;

            MovimientoHandler.aplicarMovimientoRebote(c, 0, 0, 800, 600);

            assertEquals(105, c.x);
        }

        @Test
        @DisplayName("Mueve hacia la izquierda cuando avanza en esa dirección")
        void mueveHaciaIzquierda() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.REBOTE);
            c.x = 100;
            c.velocidadX = 5;
            c.avanzando_x = Direccion.Izquierda;
            c.avanzando_y = Direccion.Quieto;

            MovimientoHandler.aplicarMovimientoRebote(c, 0, 0, 800, 600);

            assertEquals(95, c.x);
        }

        @Test
        @DisplayName("Rebota en el borde derecho")
        void rebotaBordeDerecho() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.REBOTE);
            c.centroX = 850;
            c.avanzando_x = Direccion.Derecha;

            MovimientoHandler.aplicarMovimientoRebote(c, 0, 0, 800, 600);

            assertEquals(Direccion.Izquierda, c.avanzando_x);
        }

        @Test
        @DisplayName("Rebota en el borde izquierdo")
        void rebotaBordeIzquierdo() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.REBOTE);
            c.centroX = -10;
            c.avanzando_x = Direccion.Izquierda;

            MovimientoHandler.aplicarMovimientoRebote(c, 0, 0, 800, 600);

            assertEquals(Direccion.Derecha, c.avanzando_x);
        }

        @Test
        @DisplayName("Si colisionado, va a la jaula")
        void colisionadoVaAJaula() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.REBOTE);
            c.x = 500;
            c.y = 500;
            c.colisionado = true;

            MovimientoHandler.aplicarMovimientoRebote(c, 100, 200, 800, 600);

            assertEquals(125, c.x);
            assertEquals(240, c.y);
        }
    }

    @Nested
    @DisplayName("Tests de aplicarMovimientoArco")
    class MovimientoArcoTests {

        @Test
        @DisplayName("Actualiza el ángulo de movimiento")
        void actualizaAngulo() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.ARCO);
            c.anguloMovimiento = 0;
            c.velocidadAngular = 0.1;

            MovimientoHandler.aplicarMovimientoArco(c, 0, 0, 800, 600);

            assertEquals(0.1, c.anguloMovimiento, 0.001);
        }

        @Test
        @DisplayName("Si colisionado, va a la jaula")
        void colisionadoVaAJaula() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.ARCO);
            c.x = 500;
            c.y = 500;
            c.colisionado = true;

            MovimientoHandler.aplicarMovimientoArco(c, 100, 200, 800, 600);

            assertEquals(125, c.x);
            assertEquals(240, c.y);
        }
    }

    @Nested
    @DisplayName("Tests de aplicarMovimientoAleatorio")
    class MovimientoAleatorioTests {

        @Test
        @DisplayName("Incrementa el contador de cambio")
        void incrementaContador() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.ALEATORIO);
            c.contadorCambio = 5;
            c.frecuenciaCambio = 100;

            MovimientoHandler.aplicarMovimientoAleatorio(c, 0, 0, 800, 600);

            assertEquals(6, c.contadorCambio);
        }

        @Test
        @DisplayName("Reinicia contador cuando alcanza frecuencia")
        void reiniciaContadorEnFrecuencia() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.ALEATORIO);
            c.contadorCambio = 49;
            c.frecuenciaCambio = 50;

            MovimientoHandler.aplicarMovimientoAleatorio(c, 0, 0, 800, 600);

            assertEquals(0, c.contadorCambio);
        }

        @Test
        @DisplayName("Si colisionado, va a la jaula")
        void colisionadoVaAJaula() {
            Character c = new Character("Pajaro", "pajaro.png", 20, TipoMovimiento.ALEATORIO);
            c.x = 500;
            c.y = 500;
            c.colisionado = true;

            MovimientoHandler.aplicarMovimientoAleatorio(c, 100, 200, 800, 600);

            assertEquals(125, c.x);
            assertEquals(240, c.y);
        }
    }

    @Nested
    @DisplayName("Tests de aplicarMovimientoCazar")
    class MovimientoCazarTests {

        @Test
        @DisplayName("Se mueve hacia la presa a la derecha")
        void seMueveHaciaDerecha() {
            Character cazador = new Character("Aguila", "aguila.png", 7, TipoMovimiento.CAZAR);
            Character presa = new Character("Zorrito", "sprites.png", 10, TipoMovimiento.NULO);

            cazador.x = 100;
            cazador.y = 100;
            cazador.velocidadX = 2;
            cazador.velocidadY = 2;
            cazador.follow = presa;

            presa.x = 200;
            presa.y = 100;

            MovimientoHandler.aplicarMovimientoCazar(cazador);

            assertEquals(102, cazador.x);
        }

        @Test
        @DisplayName("Se mueve hacia la presa a la izquierda")
        void seMueveHaciaIzquierda() {
            Character cazador = new Character("Aguila", "aguila.png", 7, TipoMovimiento.CAZAR);
            Character presa = new Character("Zorrito", "sprites.png", 10, TipoMovimiento.NULO);

            cazador.x = 200;
            cazador.y = 100;
            cazador.velocidadX = 2;
            cazador.velocidadY = 2;
            cazador.follow = presa;

            presa.x = 100;
            presa.y = 100;

            MovimientoHandler.aplicarMovimientoCazar(cazador);

            assertEquals(198, cazador.x);
        }

        @Test
        @DisplayName("Marca presa como cazada si colisiona")
        void marcaPresaCazada() {
            Character cazador = new Character("Aguila", "aguila.png", 7, TipoMovimiento.CAZAR);
            Character presa = new Character("Zorrito", "sprites.png", 10, TipoMovimiento.NULO);

            cazador.colisionado = true;
            cazador.follow = presa;
            presa.cazado = false;

            MovimientoHandler.aplicarMovimientoCazar(cazador);

            assertTrue(presa.cazado);
        }
    }
}
