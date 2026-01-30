import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Clase que maneja la ventana principal y el renderizado del juego.
 * Extiende Frame de AWT y contiene el canvas de dibujo.
 */
public class Display extends Frame {

    // Canvas para dibujar el juego
    private final MyCanvas canvas;

    private final Juego juego;

    public Function<Void, Integer> terminadoFunc;

    // Fonts pre-creados para evitar creación cada frame
    static final Font FONT_STATUS = new Font("SansSerif", Font.BOLD, 20);
    static final Font FONT_FIN_JUEGO = new Font("SansSerif", Font.BOLD, 100);

    // RenderingHints pre-configurados
    static final RenderingHints RENDER_HINTS;
    static {
        HashMap<RenderingHints.Key, Object> hints = new HashMap<>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        RENDER_HINTS = new RenderingHints(hints);
    }

    public void bufferedDraw(){
        this.canvas.draw();
    }

    public void trackeaPersonajes(ArrayList<Character> personajes, Frame f){
        // Crea el tracker de imágenes para precargar
        MediaTracker tracker = new MediaTracker(f);
        int i = 0;
        for (Character c : personajes) {
            tracker.addImage(c.img, i);
            i++;
        }
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Display(Juego juego){
        super("Zorrito 1.0");

        // Configura pantalla completa si se usa sin fondo
        if (juego.sinFondo){
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = env.getDefaultScreenDevice();

            if (device.isFullScreenSupported()) {
                device.setFullScreenWindow(this);
            }
        }

        // Crea el canvas de dibujo
        this.canvas = new MyCanvas(this);

        setBackground(Color.BLACK);
        setExtendedState(Frame.MAXIMIZED_BOTH);
        add(this.canvas);
        setVisible(true);

        // Setea el ícono de la ventana
        setIconImage(Toolkit.getDefaultToolkit().getImage("zorro.png"));

        // Espera a que se maximize la pantalla
        if (!juego.sinFondo){
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        this.juego = juego;

        // Listener para las teclas de movimiento y acciones
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                juego.pressedKeys.add(e.getKeyCode());
                juego.accionDeTeclaPresionada();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                juego.pressedKeys.remove(e.getKeyCode());
            }
        });

        // Listener para cerrar la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                juego.timer.cancel();
                System.out.println("End.");
                System.exit(0);
            }
        });
    }

    /**
     * Canvas interno que maneja el double-buffering y renderizado.
     */
    private class MyCanvas extends Canvas {

        // AffineTransform reutilizable para evitar crear objetos cada frame
        private final AffineTransform reuseTransform = new AffineTransform();
        private final AffineTransform identityTransform = new AffineTransform();

        Display rootDisplay;

        public MyCanvas(Display d) {
            this.rootDisplay = d;
        }

        /**
         * Dibuja un personaje con transformaciones (traslación, rotación, escala).
         */
        private void drawImageCanvas(boolean drawFromCenter, Image imgTemp, int centroX, int centroY,
                int angulo, int newWidth, int newHeight, Graphics2D g2d, double zoom,
                int general_x, int general_y, int radio){

            reuseTransform.setToIdentity();

            // Translada la imagen a la posición del mapa
            reuseTransform.translate((general_x+centroX)*zoom, (general_y+centroY)*zoom);

            // Rota la imagen
            reuseTransform.rotate(Math.toRadians(angulo));

            // Aplica el zoom
            reuseTransform.scale(zoom, zoom);

            g2d.setTransform(reuseTransform);

            // Dibuja la imagen centrada o desde esquina
            g2d.drawImage(imgTemp, drawFromCenter?-(newWidth/2):0, drawFromCenter?-(newHeight/2):0,
                    newWidth, newHeight, this);
        }

        /**
         * Dibuja un fondo infinito usando mosaico (tiling).
         */
        private void drawTiledBackground(Character c, Graphics2D g2d, double zoom,
                int general_x, int general_y) {
            Image imgTemp = c.getImagen();
            int imgWidth = c.getWidth(canvas);
            int imgHeight = c.getHeight(canvas);

            if (imgWidth <= 0 || imgHeight <= 0) return;

            int screenWidth = getWidth();
            int screenHeight = getHeight();

            // Calcula el offset considerando el zoom
            double offsetX = general_x * zoom;
            double offsetY = general_y * zoom;

            double scaledWidth = imgWidth * zoom;
            double scaledHeight = imgHeight * zoom;

            // Calcula posición inicial del tile
            double startX = offsetX % scaledWidth;
            double startY = offsetY % scaledHeight;

            if (startX > 0) startX -= scaledWidth;
            if (startY > 0) startY -= scaledHeight;

            // Dibuja tiles para cubrir toda la pantalla
            for (double tileY = startY; tileY < screenHeight; tileY += scaledHeight) {
                for (double tileX = startX; tileX < screenWidth; tileX += scaledWidth) {
                    reuseTransform.setToIdentity();
                    reuseTransform.translate(tileX, tileY);
                    reuseTransform.scale(zoom, zoom);
                    g2d.setTransform(reuseTransform);

                    g2d.drawImage(imgTemp, 0, 0, imgWidth, imgHeight, this);
                }
            }
        }

        /**
         * Dibuja todos los elementos del juego: personajes, HUD, mensajes.
         */
        private void drawElementosComunes(Graphics2D g){
            // Dibuja los personajes
            for (Character c : this.rootDisplay.juego.personajes){
                if (c.img!=null){
                    if (c.esFondoInfinito) {
                        drawTiledBackground(c, g, this.rootDisplay.juego.zoom,
                                this.rootDisplay.juego.general_x, this.rootDisplay.juego.general_y);
                    } else {
                        drawImageCanvas(c.drawFromCenter, c.getImagen(), c.centroX, c.centroY,
                                c.angulo, c.getWidth(canvas), c.getHeight(canvas), g,
                                this.rootDisplay.juego.zoom, this.rootDisplay.juego.general_x,
                                this.rootDisplay.juego.general_y, c.radio);
                    }
                }
            }

            // Resetea transformación para dibujar HUD
            g.setTransform(identityTransform);
            g.setFont(FONT_STATUS);

            // Dibuja el status del jugador
            String text;
            if (this.rootDisplay.juego.principal.colisionado){
                g.setColor(Color.RED);
                text = "Status: COME";
            } else {
                g.setColor(Color.WHITE);
                text = "Status: CAZANDO ";
            }
            g.drawString(text, 50, 50);

            // Dibuja el cronómetro (cuenta regresiva)
            g.setColor(Color.WHITE);
            String[] tiempoYPorcentaje = obtenerCronometro();
            g.drawString(tiempoYPorcentaje[0], 500, 50);

            // Dibuja la barra de progreso del tiempo
            double porcentaje = Double.parseDouble(tiempoYPorcentaje[1]);
            int barraAncho = 150;
            int barraAlto = 20;
            int barraX = 640;
            int barraY = 35;

            // Fondo de la barra
            g.setColor(new Color(60, 60, 60));
            g.fillRect(barraX, barraY, barraAncho, barraAlto);

            // Barra de progreso con color según tiempo restante
            int anchoRestante = (int) (barraAncho * porcentaje);
            if (porcentaje > 0.5) {
                g.setColor(new Color(50, 205, 50));
            } else if (porcentaje > 0.25) {
                g.setColor(new Color(255, 200, 0));
            } else {
                g.setColor(new Color(220, 50, 50));
            }
            g.fillRect(barraX, barraY, anchoRestante, barraAlto);

            // Borde de la barra
            g.setColor(Color.WHITE);
            g.drawRect(barraX, barraY, barraAncho, barraAlto);

            // Dibuja el nivel de zoom
            String printZoom = String.format("%.2f", this.rootDisplay.juego.zoom);
            g.drawString("Zoom: "+printZoom+"x", 820, 50);

            // Verifica si el juego terminó y muestra mensaje
            int codigoTerminado = terminadoFunc.apply(null);
            if (codigoTerminado==2) {
                g.setFont(FONT_FIN_JUEGO);
                g.setColor(Color.RED);
                text = "¡¡CAZADO!!";
                g.drawString(text, 600, 300);
            }
            if (codigoTerminado==1) {
                g.setFont(FONT_FIN_JUEGO);
                g.setColor(Color.YELLOW);
                text = "¡¡GANO!!";
                g.drawString(text, 600, 300);
            }
            if (codigoTerminado==3) {
                g.setFont(FONT_FIN_JUEGO);
                g.setColor(new Color(255, 140, 0));
                text = "¡TERMINÓ EL TIEMPO!";
                g.drawString(text, 300, 300);
            }
        }

        /**
         * Método principal de dibujo con double-buffering.
         */
        public void draw() {
            BufferStrategy bs = getBufferStrategy();
            if (bs == null) {
                createBufferStrategy(3);
                return;
            }

            Graphics2D g = (Graphics2D) bs.getDrawGraphics();
            g.setRenderingHints(RENDER_HINTS);

            // Limpia el fondo
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Dibuja los elementos del juego
            drawElementosComunes(g);

            g.dispose();
            bs.show();

            Toolkit.getDefaultToolkit().sync();
        }

        /**
         * Calcula el tiempo restante y el porcentaje para la barra.
         */
        private String[] obtenerCronometro() {
            long tiempoActual = System.currentTimeMillis();
            long tiempoTranscurrido = tiempoActual - this.rootDisplay.juego.initTimeMillis;
            long tiempoLimite = this.rootDisplay.juego.TIEMPO_LIMITE_MS;

            long tiempoRestante = Math.max(0, tiempoLimite - tiempoTranscurrido);

            long segundosTotales = tiempoRestante / 1000;
            long minutos = segundosTotales / 60;
            long segundos = segundosTotales % 60;

            double porcentaje = Math.max(0, Math.min(1, (double) tiempoRestante / tiempoLimite));

            String textoTiempo = String.format("Tiempo: %d:%02d", minutos, segundos);
            return new String[] { textoTiempo, String.valueOf(porcentaje) };
        }
    }
}
