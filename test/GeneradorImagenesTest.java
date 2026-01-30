import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

/**
 * Tests unitarios para GeneradorImagenes.
 * Verifica que las imágenes se generen correctamente.
 */
@DisplayName("Tests de GeneradorImagenes")
class GeneradorImagenesTest {

    private File archivoPiedra;

    @BeforeEach
    void setUp() {
        archivoPiedra = new File("assets/piedra.png");
        // Elimina el archivo si existe para probar la generación
        if (archivoPiedra.exists()) {
            archivoPiedra.delete();
        }
    }

    @AfterEach
    void tearDown() {
        // No elimina el archivo después porque puede ser necesario para el juego
    }

    @Test
    @DisplayName("Genera imagen de piedra cuando no existe")
    void generaImagenPiedraCuandoNoExiste() {
        // Asegura que no existe
        assertFalse(archivoPiedra.exists());

        // Genera la imagen
        GeneradorImagenes.generarImagenPiedra();

        // Verifica que se creó
        assertTrue(archivoPiedra.exists());
    }

    @Test
    @DisplayName("No regenera imagen si ya existe")
    void noRegeneraSiExiste() {
        // Genera la imagen primero
        GeneradorImagenes.generarImagenPiedra();
        assertTrue(archivoPiedra.exists());

        // Obtiene la fecha de modificación
        long fechaOriginal = archivoPiedra.lastModified();

        // Espera un poco para asegurar diferencia de tiempo
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // Ignorar
        }

        // Intenta generar de nuevo
        GeneradorImagenes.generarImagenPiedra();

        // Verifica que no se modificó (misma fecha)
        assertEquals(fechaOriginal, archivoPiedra.lastModified());
    }

    @Test
    @DisplayName("La imagen generada tiene tamaño válido")
    void imagenTieneTamanioValido() {
        GeneradorImagenes.generarImagenPiedra();

        assertTrue(archivoPiedra.exists());
        assertTrue(archivoPiedra.length() > 0);
    }

    @Test
    @DisplayName("generarImagenesFaltantes genera la piedra")
    void generarImagenesFaltantesGeneraPiedra() {
        assertFalse(archivoPiedra.exists());

        GeneradorImagenes.generarImagenesFaltantes();

        assertTrue(archivoPiedra.exists());
    }
}
