import java.awt.Canvas;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Function;
import javax.imageio.ImageIO;

/**
 * Clase que guarda el personaje (objeto del juego)
 * para ser transportado entre el juego y el display
 * además de contener la info del personaje.
 */
public class Character {
    public Image img;
    public Image img_colision;
    public int x = 500;
    public int y = 500;
    public int fixed_witdh;
    public int fixed_heigth;
    public int width = 0;
    public int height = 0;
    public boolean fixedSize= false;
    public boolean drawFromCenter=true;
    private int scale;
    public Function<Character, Void> movimiento;
    public String name;
    public int centroX;
    public int centroY;
    public int radio=0;
    public boolean colisionado;
    public Direccion avanzando_x = Direccion.Derecha;
    public Direccion avanzando_y = Direccion.Abajo;
    public int velocidadX = 1;
    public int velocidadY = 1;
    public int rotaAngulo = 0;
    public int angulo = 0;
    public boolean colisiona = true;
    public int numImagen = 0;
    public boolean cazado = false;
    public boolean hasSprites = false;
    private int spritesIndex = 0;
    public Sprite[] spritesArray;

    // Setea otro caracter para que lo siga con el movimiento
    public Character follow;

    // Propiedades para movimiento en arco y aleatorio
    public double anguloMovimiento = 0;
    public double velocidadAngular = 0;
    public int contadorCambio = 0;
    public int frecuenciaCambio = 50;
    public int tipoMovimiento = 0;

    // Propiedad para fondo infinito (tiling)
    public boolean esFondoInfinito = false;

    // Caché de imágenes
    private static HashMap<String, Image> imagenes = new HashMap<String, Image>();

    // Caché de sprites pre-recortados
    private BufferedImage[] cachedSpritesNormal;
    private BufferedImage[] cachedSpritesMirror;
    private boolean spritesCached = false;

    // Record para definir regiones de sprites
    public record Sprite(int x, int y, int w, int h){}

    public Character(String name, String imageFile, int scale, Function<Character, Void> movimientoPersonaje){
        // Carga la imagen del personaje desde archivo
        try {
            this.img = ImageIO.read(new File(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
            this.img = null;
        }

        this.scale = scale;
        this.movimiento = movimientoPersonaje;
        this.name = name;
    }

    public void setColision(boolean colision){
        this.colisionado = colision;

        // Si está colisionado lo hace girar (excepto Zorrito)
        if (colision & !name.equals("Zorrito")){
            this.rotaAngulo = 5;
        } else {
            this.rotaAngulo = 0;
        }
    }

    public int getWidth(Canvas canvas){
        if (fixedSize){
            return fixed_witdh;
        } else {
            this.width = img.getWidth(canvas) / scale;
            return this.width;
        }
    }

    public int getHeight(Canvas canvas){
        if (fixedSize){
            return fixed_heigth;
        } else {
            this.height = img.getHeight(canvas) / scale;
            return this.height;
        }
    }

    public void setImagenColision(String imageFileColision){
        if (imageFileColision!=null)
            this.img_colision = Toolkit.getDefaultToolkit().getImage(imageFileColision);
    }

    /**
     * Pre-cachea todos los sprites recortados para evitar crear BufferedImage cada frame.
     * Debe llamarse después de configurar spritesArray.
     */
    public void cacheSprites() {
        if (!hasSprites || spritesArray == null || spritesCached) return;

        BufferedImage bimg = SpriteUtils.toBufferedImage(img);
        if (bimg == null) return;

        int numSprites = spritesArray.length;
        cachedSpritesNormal = new BufferedImage[numSprites];
        cachedSpritesMirror = new BufferedImage[numSprites];

        // Recorta y cachea cada sprite (normal y espejado)
        for (int i = 0; i < numSprites; i++) {
            Sprite s = spritesArray[i];
            BufferedImage cropped = SpriteUtils.cropImage(bimg, s.x, s.y, s.w, s.h);
            if (cropped != null) {
                cachedSpritesNormal[i] = cropped;
                cachedSpritesMirror[i] = SpriteUtils.espejarHorizontal(cropped);
            }
        }
        spritesCached = true;
    }

    public Image getImagen(){
        // Determina qué imagen mostrar según estado de colisión
        Image imgTemp;
        if (colisionado){
            if (img_colision!=null)
                imgTemp = img_colision;
            else
                imgTemp = img;
        } else {
            imgTemp = img;
        }

        // Alterna imagen según numImagen
        if (numImagen==0)
            imgTemp = img;
        else {
            if (img_colision!=null)
                imgTemp = img_colision;
            else
                imgTemp = img;
        }

        // Si tiene sprites, usa el sistema de animación
        if (hasSprites){
            // Si los sprites están cacheados, usarlos directamente
            if (spritesCached && cachedSpritesNormal != null) {
                int index;
                // Verifica si se está moviendo
                if (this.avanzando_x == Direccion.Quieto && this.avanzando_y == Direccion.Quieto){
                    index = 0;
                    spritesIndex = 1;
                } else {
                    index = spritesIndex;
                    spritesIndex++;
                    if (spritesIndex==spritesArray.length) spritesIndex = 0;
                }

                // Retorna sprite cacheado según dirección
                if (avanzando_x == Direccion.Izquierda) {
                    return cachedSpritesMirror[index];
                } else {
                    return cachedSpritesNormal[index];
                }
            }

            // Fallback al método original si no hay caché
            Image croppedImage;
            if (this.avanzando_x == Direccion.Quieto && this.avanzando_y == Direccion.Quieto){
                Sprite s = spritesArray[0];
                croppedImage = cropImage(imgTemp, s.x, s.y, s.w, s.h);
                spritesIndex = 1;
            } else {
                Sprite s = spritesArray[spritesIndex];
                croppedImage = cropImage(imgTemp, s.x, s.y, s.w, s.h);
                spritesIndex++;
                if (spritesIndex==spritesArray.length) spritesIndex = 0;
            }
            return croppedImage;

        } else {
            return imgTemp;
        }
    }

    public BufferedImage espejarImagen(BufferedImage img) {
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

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    private Image cropImage(Image img, int x, int y, int width, int height) {
        BufferedImage bimg = toBufferedImage(img);
        BufferedImage subImg = bimg.getSubimage(x, y, width, height);

        // Espeja la imagen si el personaje va hacia la izquierda
        if (avanzando_x == Direccion.Izquierda)
            return espejarImagen(subImg);
        else
            return subImg;
    }

    /**
     * Ejecuta la función de movimiento y actualiza centro/radio.
     */
    public void seMueve(){
        // Aplica el algoritmo de movimiento
        this.movimiento.apply(this);

        // El fondo no necesita calcular centro ni radio
        if (name.equals("Bosque")){
            return;
        }

        // Calcula el centro del personaje
        centroX = x+width/2;
        centroY = y+height/2;

        // Calcula el radio para colisiones (primera vez)
        if (radio==0)
            radio = (width>height)?width/2:height/2;

        // Si está colisionado, rota
        if (this.colisionado){
            this.angulo = angulo + rotaAngulo;
        }
    }

    /**
     * Verifica si hay una colisión con otro personaje.
     * Usa CollisionUtils optimizado (sin sqrt).
     */
    public boolean verificaColision(Character c){
        // Si no colisiona (como la jaula), sale
        if (!c.colisiona) return false;

        return CollisionUtils.verificaColisionCircular(
            this.centroX, this.centroY, this.radio,
            c.centroX, c.centroY, c.radio
        );
    }
}
