import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de MovementUtils")
class MovementUtilsTest {

    @Nested
    @DisplayName("Tests de calcularDireccionPorMouse")
    class DireccionPorMouseTests {

        @Test
        @DisplayName("No mueve cuando mouse está muy cerca del personaje")
        void testNoMueveCuandoMouseCerca() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 100, 120, 120, 50);

            assertFalse(resultado.debeMover, "No debería mover si el mouse está cerca");
            assertEquals(Direccion.Quieto, resultado.direccionX);
            assertEquals(Direccion.Quieto, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve hacia la derecha cuando mouse está a la derecha")
        void testMueveDerecha() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 100, 300, 100, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Derecha, resultado.direccionX);
            assertEquals(Direccion.Quieto, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve hacia la izquierda cuando mouse está a la izquierda")
        void testMueveIzquierda() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(300, 100, 100, 100, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Izquierda, resultado.direccionX);
            assertEquals(Direccion.Quieto, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve hacia arriba cuando mouse está arriba")
        void testMueveArriba() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 300, 100, 100, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Quieto, resultado.direccionX);
            assertEquals(Direccion.Arriba, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve hacia abajo cuando mouse está abajo")
        void testMueveAbajo() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 100, 100, 300, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Quieto, resultado.direccionX);
            assertEquals(Direccion.Abajo, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve en diagonal arriba-derecha")
        void testMueveArribaDerecha() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 200, 200, 100, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Derecha, resultado.direccionX);
            assertEquals(Direccion.Arriba, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve en diagonal abajo-izquierda")
        void testMueveAbajoIzquierda() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(200, 100, 100, 200, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Izquierda, resultado.direccionX);
            assertEquals(Direccion.Abajo, resultado.direccionY);
        }

        @Test
        @DisplayName("El ángulo calculado está en rango válido")
        void testAnguloEnRangoValido() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 100, 300, 200, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertTrue(resultado.angulo >= 0 && resultado.angulo < 360,
                "El ángulo debería estar entre 0 y 360");
        }

        @Test
        @DisplayName("Maneja adyacente cero correctamente (mouse directamente arriba)")
        void testAdyacenteCeroArriba() {
            // Mouse directamente arriba del personaje (mismo X, diferente Y)
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 300, 100, 50, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Arriba, resultado.direccionY);
        }

        @Test
        @DisplayName("Maneja adyacente cero correctamente (mouse directamente abajo)")
        void testAdyacenteCeroAbajo() {
            // Mouse directamente abajo del personaje (mismo X, diferente Y)
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 100, 100, 300, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Abajo, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve en diagonal abajo-derecha")
        void testMueveAbajoDerecha() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 100, 200, 200, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Derecha, resultado.direccionX);
            assertEquals(Direccion.Abajo, resultado.direccionY);
        }

        @Test
        @DisplayName("Mueve en diagonal arriba-izquierda")
        void testMueveArribaIzquierda() {
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(200, 200, 100, 100, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Izquierda, resultado.direccionX);
            assertEquals(Direccion.Arriba, resultado.direccionY);
        }

        @Test
        @DisplayName("Ángulo cerca de 0/360 grados (arriba puro)")
        void testAnguloCercaDe0Grados() {
            // Personaje abajo, mouse arriba-derecha (ángulo pequeño)
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(100, 200, 110, 50, 10);

            assertTrue(resultado.debeMover, "Debería mover");
            // Debería ser arriba o arriba-derecha
        }

        @Test
        @DisplayName("Cuadrante 4: mouse abajo-izquierda")
        void testCuadrante4() {
            // Personaje arriba-derecha, mouse abajo-izquierda
            MovementUtils.DireccionResultado resultado =
                MovementUtils.calcularDireccionPorMouse(300, 100, 100, 300, 50);

            assertTrue(resultado.debeMover, "Debería mover");
            assertEquals(Direccion.Izquierda, resultado.direccionX);
            assertEquals(Direccion.Abajo, resultado.direccionY);
        }
    }

    @Nested
    @DisplayName("Tests de calcularMovimientoRebote")
    class MovimientoReboteTests {

        @Test
        @DisplayName("Avanza hacia la derecha correctamente")
        void testAvanzaDerecha() {
            int[] resultado = MovementUtils.calcularMovimientoRebote(
                100, 100,      // posición
                10, 10,        // velocidad
                Direccion.Derecha, Direccion.Abajo,  // direcciones
                150, 150,      // centro
                1000, 800      // límites
            );

            assertEquals(110, resultado[0], "X debería incrementarse");
            assertEquals(110, resultado[1], "Y debería incrementarse");
        }

        @Test
        @DisplayName("Avanza hacia la izquierda correctamente")
        void testAvanzaIzquierda() {
            int[] resultado = MovementUtils.calcularMovimientoRebote(
                100, 100,
                10, 10,
                Direccion.Izquierda, Direccion.Arriba,
                150, 150,
                1000, 800
            );

            assertEquals(90, resultado[0], "X debería decrementarse");
            assertEquals(90, resultado[1], "Y debería decrementarse");
        }

        @Test
        @DisplayName("Rebota en el borde derecho")
        void testReboteBordeDerecho() {
            int[] resultado = MovementUtils.calcularMovimientoRebote(
                990, 100,
                10, 10,
                Direccion.Derecha, Direccion.Abajo,
                1010, 150,     // centro fuera del límite
                1000, 800
            );

            assertEquals(1, resultado[2], "Dirección X debería cambiar a Izquierda (1)");
        }

        @Test
        @DisplayName("Rebota en el borde izquierdo")
        void testReboteBordeIzquierdo() {
            int[] resultado = MovementUtils.calcularMovimientoRebote(
                10, 100,
                10, 10,
                Direccion.Izquierda, Direccion.Abajo,
                -5, 150,       // centro fuera del límite
                1000, 800
            );

            assertEquals(0, resultado[2], "Dirección X debería cambiar a Derecha (0)");
        }

        @Test
        @DisplayName("Rebota en el borde inferior")
        void testReboteBordeInferior() {
            int[] resultado = MovementUtils.calcularMovimientoRebote(
                100, 790,
                10, 10,
                Direccion.Derecha, Direccion.Abajo,
                150, 810,      // centro fuera del límite
                1000, 800
            );

            assertEquals(1, resultado[3], "Dirección Y debería cambiar a Arriba (1)");
        }

        @Test
        @DisplayName("Rebota en el borde superior")
        void testReboteBordeSuperior() {
            int[] resultado = MovementUtils.calcularMovimientoRebote(
                100, 10,
                10, 10,
                Direccion.Derecha, Direccion.Arriba,
                150, -5,       // centro fuera del límite
                1000, 800
            );

            assertEquals(0, resultado[3], "Dirección Y debería cambiar a Abajo (0)");
        }

        @Test
        @DisplayName("Movimiento quieto no cambia posición en ese eje")
        void testMovimientoQuieto() {
            int[] resultado = MovementUtils.calcularMovimientoRebote(
                100, 100,
                10, 10,
                Direccion.Quieto, Direccion.Quieto,
                150, 150,
                1000, 800
            );

            assertEquals(100, resultado[0], "X no debería cambiar si está quieto");
            assertEquals(100, resultado[1], "Y no debería cambiar si está quieto");
        }
    }

    @Nested
    @DisplayName("Tests de calcularMovimientoCazar")
    class MovimientoCazarTests {

        @Test
        @DisplayName("Cazador se mueve hacia la derecha para alcanzar presa")
        void testCazaMueveDerecha() {
            int[] resultado = MovementUtils.calcularMovimientoCazar(
                100, 100,   // cazador
                200, 100,   // presa
                5, 5        // velocidad
            );

            assertEquals(105, resultado[0], "Cazador debería moverse hacia la derecha");
            assertEquals(105, resultado[1], "Cazador debería moverse hacia abajo (presa en mismo Y pero suma)");
        }

        @Test
        @DisplayName("Cazador se mueve hacia la izquierda para alcanzar presa")
        void testCazaMueveIzquierda() {
            int[] resultado = MovementUtils.calcularMovimientoCazar(
                200, 100,   // cazador
                100, 100,   // presa
                5, 5        // velocidad
            );

            assertEquals(195, resultado[0], "Cazador debería moverse hacia la izquierda");
        }

        @Test
        @DisplayName("Cazador se mueve hacia arriba para alcanzar presa")
        void testCazaMueveArriba() {
            int[] resultado = MovementUtils.calcularMovimientoCazar(
                100, 200,   // cazador
                100, 100,   // presa
                5, 5        // velocidad
            );

            assertEquals(195, resultado[1], "Cazador debería moverse hacia arriba");
        }

        @Test
        @DisplayName("Cazador se mueve en diagonal hacia la presa")
        void testCazaDiagonal() {
            int[] resultado = MovementUtils.calcularMovimientoCazar(
                100, 100,   // cazador
                200, 200,   // presa
                5, 5        // velocidad
            );

            assertEquals(105, resultado[0], "Cazador debería moverse diagonalmente (X)");
            assertEquals(105, resultado[1], "Cazador debería moverse diagonalmente (Y)");
        }

        @Test
        @DisplayName("Cazador en misma posición que presa se mueve mínimamente")
        void testCazaMismaPosicion() {
            int[] resultado = MovementUtils.calcularMovimientoCazar(
                100, 100,   // cazador
                100, 100,   // presa (misma posición)
                5, 5        // velocidad
            );

            // Cuando presa no es < cazador, se suma velocidad
            assertEquals(105, resultado[0]);
            assertEquals(105, resultado[1]);
        }
    }

    @Nested
    @DisplayName("Tests de calcularVelocidadDiagonal")
    class VelocidadDiagonalTests {

        @Test
        @DisplayName("Velocidad diagonal es aproximadamente 70% de la original")
        void testVelocidadDiagonalPorcentaje() {
            int velocidadOriginal = 10;
            int velocidadDiagonal = MovementUtils.calcularVelocidadDiagonal(velocidadOriginal);

            // 10 * 0.707 ≈ 7
            assertTrue(velocidadDiagonal >= 6 && velocidadDiagonal <= 8,
                "Velocidad diagonal debería ser ~70% de la original");
        }

        @Test
        @DisplayName("Velocidad diagonal con valor pequeño")
        void testVelocidadDiagonalPequena() {
            int velocidadDiagonal = MovementUtils.calcularVelocidadDiagonal(1);

            // 1 * 0.707 ≈ 1 (redondeado)
            assertEquals(1, velocidadDiagonal);
        }

        @Test
        @DisplayName("Velocidad diagonal con valor cero")
        void testVelocidadDiagonalCero() {
            int velocidadDiagonal = MovementUtils.calcularVelocidadDiagonal(0);
            assertEquals(0, velocidadDiagonal);
        }

        @Test
        @DisplayName("Velocidad diagonal mantiene proporción para valores grandes")
        void testVelocidadDiagonalGrande() {
            int velocidadOriginal = 100;
            int velocidadDiagonal = MovementUtils.calcularVelocidadDiagonal(velocidadOriginal);

            // 100 * 0.707 ≈ 71
            assertTrue(velocidadDiagonal >= 70 && velocidadDiagonal <= 72,
                "Velocidad diagonal debería ser ~71 para entrada 100");
        }
    }
}
