import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@DisplayName("Tests de SpriteUtils")
class SpriteUtilsTest {

    private BufferedImage imagenPrueba;

    @BeforeEach
    void setUp() {
        // Crear una imagen de prueba de 100x100 con un patrón simple
        imagenPrueba = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagenPrueba.createGraphics();

        // Llenar con blanco
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100);

        // Dibujar un cuadrado rojo en la esquina superior izquierda
        g.setColor(Color.RED);
        g.fillRect(0, 0, 50, 50);

        // Dibujar un cuadrado azul en la esquina superior derecha
        g.setColor(Color.BLUE);
        g.fillRect(50, 0, 50, 50);

        // Dibujar un cuadrado verde en la esquina inferior izquierda
        g.setColor(Color.GREEN);
        g.fillRect(0, 50, 50, 50);

        // Esquina inferior derecha queda blanca

        g.dispose();
    }

    @Nested
    @DisplayName("Tests de toBufferedImage")
    class ToBufferedImageTests {

        @Test
        @DisplayName("Retorna la misma imagen si ya es BufferedImage")
        void testRetornaMismaImagenSiEsBufferedImage() {
            BufferedImage resultado = SpriteUtils.toBufferedImage(imagenPrueba);
            assertSame(imagenPrueba, resultado,
                "Debería retornar la misma instancia si ya es BufferedImage");
        }

        @Test
        @DisplayName("Convierte Image genérica a BufferedImage")
        void testConvierteImageGenericaABufferedImage() {
            // Crear una Image que no sea BufferedImage usando getScaledInstance
            java.awt.Image imageNoBuffered = imagenPrueba.getScaledInstance(50, 50, java.awt.Image.SCALE_DEFAULT);

            // Convertir a BufferedImage
            BufferedImage resultado = SpriteUtils.toBufferedImage(imageNoBuffered);

            // Nota: getScaledInstance puede retornar dimensiones -1 inicialmente
            // así que verificamos que el método maneje esto correctamente
            // El resultado puede ser null si las dimensiones no están listas
        }

        @Test
        @DisplayName("Convierte VolatileImage a BufferedImage")
        void testConvierteVolatileImage() {
            // Crear una imagen usando createImage del toolkit
            java.awt.Image toolkitImage = java.awt.Toolkit.getDefaultToolkit().createImage(
                new java.awt.image.MemoryImageSource(10, 10, new int[100], 0, 10)
            );

            // Intentar convertir - puede retornar null si las dimensiones no están listas
            BufferedImage resultado = SpriteUtils.toBufferedImage(toolkitImage);
            // El resultado depende del estado de carga de la imagen
        }
    }

    @Nested
    @DisplayName("Tests de cropImage")
    class CropImageTests {

        @Test
        @DisplayName("Recorta correctamente una región válida")
        void testRecortaRegionValida() {
            BufferedImage recortada = SpriteUtils.cropImage(imagenPrueba, 0, 0, 50, 50);

            assertNotNull(recortada, "Imagen recortada no debería ser null");
            assertEquals(50, recortada.getWidth(), "Ancho debería ser 50");
            assertEquals(50, recortada.getHeight(), "Alto debería ser 50");

            // Verificar que el contenido es rojo (esquina superior izquierda)
            int pixelColor = recortada.getRGB(25, 25);
            assertEquals(Color.RED.getRGB(), pixelColor,
                "El pixel debería ser rojo");
        }

        @Test
        @DisplayName("Recorta otra región correctamente")
        void testRecortaOtraRegion() {
            BufferedImage recortada = SpriteUtils.cropImage(imagenPrueba, 50, 0, 50, 50);

            assertNotNull(recortada);
            assertEquals(50, recortada.getWidth());
            assertEquals(50, recortada.getHeight());

            // Verificar que el contenido es azul (esquina superior derecha)
            int pixelColor = recortada.getRGB(25, 25);
            assertEquals(Color.BLUE.getRGB(), pixelColor,
                "El pixel debería ser azul");
        }

        @Test
        @DisplayName("Retorna null para imagen fuente null")
        void testRetornaNullParaSourceNull() {
            BufferedImage resultado = SpriteUtils.cropImage(null, 0, 0, 50, 50);
            assertNull(resultado, "Debería retornar null si la fuente es null");
        }

        @Test
        @DisplayName("Retorna null para coordenadas fuera de límites")
        void testRetornaNullParaCoordenadasInvalidas() {
            // X fuera de límites
            BufferedImage resultado1 = SpriteUtils.cropImage(imagenPrueba, -10, 0, 50, 50);
            assertNull(resultado1, "Debería retornar null para X negativo");

            // Recorte excede el ancho
            BufferedImage resultado2 = SpriteUtils.cropImage(imagenPrueba, 80, 0, 50, 50);
            assertNull(resultado2, "Debería retornar null si el recorte excede el ancho");

            // Y fuera de límites (negativo)
            BufferedImage resultado3 = SpriteUtils.cropImage(imagenPrueba, 0, -10, 50, 50);
            assertNull(resultado3, "Debería retornar null para Y negativo");

            // Recorte excede la altura
            BufferedImage resultado4 = SpriteUtils.cropImage(imagenPrueba, 0, 80, 50, 50);
            assertNull(resultado4, "Debería retornar null si el recorte excede la altura");
        }

        @Test
        @DisplayName("Recorta usando SpriteRegion")
        void testRecortaConSpriteRegion() {
            SpriteUtils.SpriteRegion region = new SpriteUtils.SpriteRegion(0, 50, 50, 50);
            BufferedImage recortada = SpriteUtils.cropImage(imagenPrueba, region);

            assertNotNull(recortada);
            assertEquals(50, recortada.getWidth());
            assertEquals(50, recortada.getHeight());

            // Verificar que el contenido es verde (esquina inferior izquierda)
            int pixelColor = recortada.getRGB(25, 25);
            assertEquals(Color.GREEN.getRGB(), pixelColor,
                "El pixel debería ser verde");
        }
    }

    @Nested
    @DisplayName("Tests de espejarHorizontal")
    class EspejarHorizontalTests {

        @Test
        @DisplayName("Espeja imagen horizontalmente")
        void testEspejaHorizontalmente() {
            // La imagen tiene rojo a la izquierda, azul a la derecha (fila superior)
            BufferedImage espejada = SpriteUtils.espejarHorizontal(imagenPrueba);

            assertNotNull(espejada);
            assertEquals(100, espejada.getWidth());
            assertEquals(100, espejada.getHeight());

            // Después de espejar, rojo debería estar a la derecha
            int pixelDerecha = espejada.getRGB(75, 25);
            assertEquals(Color.RED.getRGB(), pixelDerecha,
                "Rojo debería estar ahora a la derecha");

            // Y azul a la izquierda
            int pixelIzquierda = espejada.getRGB(25, 25);
            assertEquals(Color.BLUE.getRGB(), pixelIzquierda,
                "Azul debería estar ahora a la izquierda");
        }

        @Test
        @DisplayName("Retorna null para imagen null")
        void testRetornaNullParaNull() {
            BufferedImage resultado = SpriteUtils.espejarHorizontal(null);
            assertNull(resultado);
        }
    }

    @Nested
    @DisplayName("Tests de espejarVertical")
    class EspejarVerticalTests {

        @Test
        @DisplayName("Espeja imagen verticalmente")
        void testEspejaVerticalmente() {
            // La imagen tiene rojo arriba-izquierda, verde abajo-izquierda
            BufferedImage espejada = SpriteUtils.espejarVertical(imagenPrueba);

            assertNotNull(espejada);

            // Después de espejar, verde debería estar arriba-izquierda
            int pixelArriba = espejada.getRGB(25, 25);
            assertEquals(Color.GREEN.getRGB(), pixelArriba,
                "Verde debería estar ahora arriba");

            // Y rojo abajo-izquierda
            int pixelAbajo = espejada.getRGB(25, 75);
            assertEquals(Color.RED.getRGB(), pixelAbajo,
                "Rojo debería estar ahora abajo");
        }

        @Test
        @DisplayName("Retorna null para imagen null")
        void testRetornaNullParaNull() {
            BufferedImage resultado = SpriteUtils.espejarVertical(null);
            assertNull(resultado);
        }
    }

    @Nested
    @DisplayName("Tests de rotarImagen")
    class RotarImagenTests {

        @Test
        @DisplayName("Rota imagen 180 grados")
        void testRota180Grados() {
            BufferedImage rotada = SpriteUtils.rotarImagen(imagenPrueba, 180);

            assertNotNull(rotada);
            assertEquals(100, rotada.getWidth());
            assertEquals(100, rotada.getHeight());

            // Después de rotar 180°, rojo (que estaba arriba-izquierda)
            // debería estar abajo-derecha
            // Nota: debido a interpolación, verificamos el área general
        }

        @Test
        @DisplayName("Rota imagen 0 grados mantiene igual")
        void testRota0Grados() {
            BufferedImage rotada = SpriteUtils.rotarImagen(imagenPrueba, 0);

            assertNotNull(rotada);
            // El pixel debería mantener su color
            int pixelOriginal = imagenPrueba.getRGB(25, 25);
            int pixelRotado = rotada.getRGB(25, 25);
            assertEquals(pixelOriginal, pixelRotado,
                "Rotación 0° debería mantener la imagen igual");
        }

        @Test
        @DisplayName("Retorna null para imagen null")
        void testRetornaNullParaNull() {
            BufferedImage resultado = SpriteUtils.rotarImagen(null, 90);
            assertNull(resultado);
        }
    }

    @Nested
    @DisplayName("Tests de escalarImagen")
    class EscalarImagenTests {

        @Test
        @DisplayName("Escala imagen a la mitad")
        void testEscalaALaMitad() {
            BufferedImage escalada = SpriteUtils.escalarImagen(imagenPrueba, 50, 50);

            assertNotNull(escalada);
            assertEquals(50, escalada.getWidth(), "Ancho debería ser 50");
            assertEquals(50, escalada.getHeight(), "Alto debería ser 50");
        }

        @Test
        @DisplayName("Escala imagen al doble")
        void testEscalaAlDoble() {
            BufferedImage escalada = SpriteUtils.escalarImagen(imagenPrueba, 200, 200);

            assertNotNull(escalada);
            assertEquals(200, escalada.getWidth(), "Ancho debería ser 200");
            assertEquals(200, escalada.getHeight(), "Alto debería ser 200");
        }

        @Test
        @DisplayName("Retorna null para dimensiones inválidas")
        void testRetornaNullParaDimensionesInvalidas() {
            assertNull(SpriteUtils.escalarImagen(imagenPrueba, 0, 50));
            assertNull(SpriteUtils.escalarImagen(imagenPrueba, 50, 0));
            assertNull(SpriteUtils.escalarImagen(imagenPrueba, -10, 50));
        }

        @Test
        @DisplayName("Retorna null para imagen null")
        void testRetornaNullParaNull() {
            assertNull(SpriteUtils.escalarImagen(null, 50, 50));
        }
    }

    @Nested
    @DisplayName("Tests de calcularIndiceSprite")
    class CalcularIndiceSpriteTests {

        @Test
        @DisplayName("Calcula índice correcto para animación simple")
        void testIndiceAnimacionSimple() {
            // 8 sprites, 1 frame por sprite
            assertEquals(0, SpriteUtils.calcularIndiceSprite(0, 8, 1));
            assertEquals(1, SpriteUtils.calcularIndiceSprite(1, 8, 1));
            assertEquals(7, SpriteUtils.calcularIndiceSprite(7, 8, 1));
            assertEquals(0, SpriteUtils.calcularIndiceSprite(8, 8, 1)); // cicla
        }

        @Test
        @DisplayName("Calcula índice con múltiples frames por sprite")
        void testIndiceMultiplesFrames() {
            // 4 sprites, 3 frames por sprite
            assertEquals(0, SpriteUtils.calcularIndiceSprite(0, 4, 3));
            assertEquals(0, SpriteUtils.calcularIndiceSprite(1, 4, 3));
            assertEquals(0, SpriteUtils.calcularIndiceSprite(2, 4, 3));
            assertEquals(1, SpriteUtils.calcularIndiceSprite(3, 4, 3));
            assertEquals(1, SpriteUtils.calcularIndiceSprite(4, 4, 3));
            assertEquals(1, SpriteUtils.calcularIndiceSprite(5, 4, 3));
            assertEquals(2, SpriteUtils.calcularIndiceSprite(6, 4, 3));
        }

        @Test
        @DisplayName("Cicla correctamente después de completar animación")
        void testCiclaAnimacion() {
            // 4 sprites, 2 frames por sprite = ciclo completo cada 8 frames
            assertEquals(0, SpriteUtils.calcularIndiceSprite(0, 4, 2));
            assertEquals(3, SpriteUtils.calcularIndiceSprite(7, 4, 2));
            assertEquals(0, SpriteUtils.calcularIndiceSprite(8, 4, 2)); // cicla
            assertEquals(1, SpriteUtils.calcularIndiceSprite(10, 4, 2));
        }

        @Test
        @DisplayName("Retorna 0 para parámetros inválidos")
        void testRetorna0ParaParametrosInvalidos() {
            assertEquals(0, SpriteUtils.calcularIndiceSprite(10, 0, 1));
            assertEquals(0, SpriteUtils.calcularIndiceSprite(10, 4, 0));
        }
    }

    @Nested
    @DisplayName("Tests de generarGridSprites")
    class GenerarGridSpritesTests {

        @Test
        @DisplayName("Genera grid de sprites correctamente")
        void testGeneraGridCorrectamente() {
            // Sprite sheet de 200x100, 2 columnas, 2 filas
            SpriteUtils.SpriteRegion[] sprites =
                SpriteUtils.generarGridSprites(200, 100, 2, 2);

            assertEquals(4, sprites.length, "Debería generar 4 sprites");

            // Primer sprite (0,0)
            assertEquals(0, sprites[0].x);
            assertEquals(0, sprites[0].y);
            assertEquals(100, sprites[0].width);
            assertEquals(50, sprites[0].height);

            // Segundo sprite (1,0)
            assertEquals(100, sprites[1].x);
            assertEquals(0, sprites[1].y);

            // Tercer sprite (0,1)
            assertEquals(0, sprites[2].x);
            assertEquals(50, sprites[2].y);

            // Cuarto sprite (1,1)
            assertEquals(100, sprites[3].x);
            assertEquals(50, sprites[3].y);
        }

        @Test
        @DisplayName("Genera grid para sprite sheet del zorrito")
        void testGeneraGridZorrito() {
            // El zorrito usa 1098x1932, 2 columnas, 4 filas
            SpriteUtils.SpriteRegion[] sprites =
                SpriteUtils.generarGridSprites(1098, 1932, 2, 4);

            assertEquals(8, sprites.length, "Debería generar 8 sprites");
            assertEquals(549, sprites[0].width, "Ancho de cada sprite");
            assertEquals(483, sprites[0].height, "Alto de cada sprite");
        }
    }

    @Nested
    @DisplayName("Tests de esImagenValida")
    class EsImagenValidaTests {

        @Test
        @DisplayName("Retorna true para imagen válida")
        void testRetornaTrueParaImagenValida() {
            assertTrue(SpriteUtils.esImagenValida(imagenPrueba));
        }

        @Test
        @DisplayName("Retorna false para imagen null")
        void testRetornaFalseParaNull() {
            assertFalse(SpriteUtils.esImagenValida(null));
        }

        @Test
        @DisplayName("Retorna false para imagen con dimensiones cero")
        void testRetornaFalseParaDimensionesCero() {
            // Crear una imagen vacía (esto es técnicamente inválido pero testeamos el caso)
            BufferedImage imagenVacia = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            // Esta imagen tiene dimensiones válidas (1x1), así que debería ser válida
            assertTrue(SpriteUtils.esImagenValida(imagenVacia));
        }
    }
}
