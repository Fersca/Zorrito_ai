import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Utilidades para manipulación de sprites e imágenes.
 * Clase extraída para permitir testing unitario.
 */
public class SpriteUtils {

    /**
     * Representa un sprite dentro de un sprite sheet.
     */
    public static class SpriteRegion {
        public final int x;
        public final int y;
        public final int width;
        public final int height;

        public SpriteRegion(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Convierte una Image genérica a BufferedImage.
     *
     * @param img Imagen a convertir
     * @return BufferedImage resultante
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        int width = img.getWidth(null);
        int height = img.getHeight(null);

        if (width <= 0 || height <= 0) {
            return null;
        }

        BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    /**
     * Recorta una región de una imagen.
     *
     * @param source Imagen fuente (debe ser BufferedImage)
     * @param x Coordenada X del inicio del recorte
     * @param y Coordenada Y del inicio del recorte
     * @param width Ancho del recorte
     * @param height Alto del recorte
     * @return Imagen recortada o null si los parámetros son inválidos
     */
    public static BufferedImage cropImage(BufferedImage source, int x, int y, int width, int height) {
        if (source == null) {
            return null;
        }

        // Validar que el recorte esté dentro de los límites
        if (x < 0 || y < 0 || x + width > source.getWidth() || y + height > source.getHeight()) {
            return null;
        }

        return source.getSubimage(x, y, width, height);
    }

    /**
     * Recorta una región de una imagen usando SpriteRegion.
     *
     * @param source Imagen fuente
     * @param region Región a recortar
     * @return Imagen recortada
     */
    public static BufferedImage cropImage(BufferedImage source, SpriteRegion region) {
        return cropImage(source, region.x, region.y, region.width, region.height);
    }

    /**
     * Espeja una imagen horizontalmente.
     *
     * @param img Imagen a espejar
     * @return Imagen espejada
     */
    public static BufferedImage espejarHorizontal(BufferedImage img) {
        if (img == null) {
            return null;
        }

        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage espejada = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        AffineTransform at = AffineTransform.getScaleInstance(-1, 1);
        at.translate(-width, 0);

        Graphics2D g2d = espejada.createGraphics();
        g2d.transform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return espejada;
    }

    /**
     * Espeja una imagen verticalmente.
     *
     * @param img Imagen a espejar
     * @return Imagen espejada
     */
    public static BufferedImage espejarVertical(BufferedImage img) {
        if (img == null) {
            return null;
        }

        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage espejada = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        AffineTransform at = AffineTransform.getScaleInstance(1, -1);
        at.translate(0, -height);

        Graphics2D g2d = espejada.createGraphics();
        g2d.transform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return espejada;
    }

    /**
     * Rota una imagen por un ángulo dado.
     *
     * @param img Imagen a rotar
     * @param angulo Ángulo en grados
     * @return Imagen rotada
     */
    public static BufferedImage rotarImagen(BufferedImage img, double angulo) {
        if (img == null) {
            return null;
        }

        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage rotada = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = rotada.createGraphics();
        g2d.rotate(Math.toRadians(angulo), width / 2.0, height / 2.0);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotada;
    }

    /**
     * Escala una imagen a las dimensiones especificadas.
     *
     * @param img Imagen a escalar
     * @param nuevoAncho Nuevo ancho
     * @param nuevoAlto Nuevo alto
     * @return Imagen escalada
     */
    public static BufferedImage escalarImagen(BufferedImage img, int nuevoAncho, int nuevoAlto) {
        if (img == null || nuevoAncho <= 0 || nuevoAlto <= 0) {
            return null;
        }

        BufferedImage escalada = new BufferedImage(nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = escalada.createGraphics();
        g2d.drawImage(img, 0, 0, nuevoAncho, nuevoAlto, null);
        g2d.dispose();

        return escalada;
    }

    /**
     * Calcula el índice del sprite actual en una animación cíclica.
     *
     * @param frameActual Frame actual del juego
     * @param totalSprites Número total de sprites en la animación
     * @param framesPerSprite Cuántos frames de juego por cada sprite
     * @return Índice del sprite a mostrar
     */
    public static int calcularIndiceSprite(long frameActual, int totalSprites, int framesPerSprite) {
        if (totalSprites <= 0 || framesPerSprite <= 0) {
            return 0;
        }
        return (int) ((frameActual / framesPerSprite) % totalSprites);
    }

    /**
     * Genera un array de regiones de sprite para un sprite sheet con grid uniforme.
     *
     * @param sheetWidth Ancho total del sprite sheet
     * @param sheetHeight Alto total del sprite sheet
     * @param columnas Número de columnas
     * @param filas Número de filas
     * @return Array de SpriteRegion
     */
    public static SpriteRegion[] generarGridSprites(int sheetWidth, int sheetHeight,
                                                     int columnas, int filas) {
        int spriteWidth = sheetWidth / columnas;
        int spriteHeight = sheetHeight / filas;
        SpriteRegion[] sprites = new SpriteRegion[columnas * filas];

        int index = 0;
        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                sprites[index++] = new SpriteRegion(
                    col * spriteWidth,
                    fila * spriteHeight,
                    spriteWidth,
                    spriteHeight
                );
            }
        }

        return sprites;
    }

    /**
     * Valida que las dimensiones de una imagen sean válidas.
     *
     * @param img Imagen a validar
     * @return true si la imagen tiene dimensiones válidas
     */
    public static boolean esImagenValida(BufferedImage img) {
        return img != null && img.getWidth() > 0 && img.getHeight() > 0;
    }
}
