import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Clase utilitaria para generar imágenes del juego programáticamente.
 * Crea assets que no existen para evitar dependencias de archivos externos.
 */
public class GeneradorImagenes {

    /**
     * Genera la imagen de una piedra (proyectil) si no existe.
     * Crea una piedra gris con efecto de volumen.
     */
    public static void generarImagenPiedra() {
        File archivo = new File("assets/piedra.png");

        // Si ya existe, no la regenera
        if (archivo.exists()) {
            return;
        }

        // Tamaño de la imagen de la piedra (más grande para que sea visible)
        int tamanio = 80;
        BufferedImage imagen = new BufferedImage(tamanio, tamanio, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagen.createGraphics();

        // Activa antialiasing para bordes suaves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fondo transparente
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, tamanio, tamanio);

        // Gradiente para efecto de volumen (luz desde arriba-izquierda)
        GradientPaint gradiente = new GradientPaint(
            10, 10, new Color(160, 150, 140),    // Color claro arriba-izquierda
            70, 70, new Color(80, 70, 60)        // Color oscuro abajo-derecha
        );
        g2d.setPaint(gradiente);

        // Dibuja la piedra como una elipse
        g2d.fillOval(4, 4, tamanio - 8, tamanio - 8);

        // Borde más oscuro para definir la forma
        g2d.setColor(new Color(50, 45, 40));
        g2d.drawOval(4, 4, tamanio - 9, tamanio - 9);

        // Punto de luz (reflejo) en la esquina superior izquierda
        g2d.setColor(new Color(200, 195, 190));
        g2d.fillOval(18, 18, 15, 15);

        // Detalles de textura para que parezca más roca
        g2d.setColor(new Color(100, 90, 80));
        g2d.fillOval(40, 45, 12, 8);
        g2d.fillOval(25, 55, 8, 6);

        g2d.dispose();

        // Guarda la imagen como PNG
        try {
            ImageIO.write(imagen, "PNG", archivo);
            System.out.println("Imagen de piedra generada: " + archivo.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al generar imagen de piedra: " + e.getMessage());
        }
    }

    /**
     * Genera todas las imágenes necesarias que no existan.
     */
    public static void generarImagenesFaltantes() {
        generarImagenPiedra();
    }
}
