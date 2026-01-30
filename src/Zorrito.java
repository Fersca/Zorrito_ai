import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Clase principal del juego Zorrito.
 * Punto de entrada que procesa argumentos CLI y crea la instancia del juego.
 */
public class Zorrito {

    // Referencias a los objetos principales
    Juego juego;
    Display display;

    public Zorrito(boolean buffer, int cantMalos, boolean centrar, boolean sinFondo, int aguilas) {
        // Crea el objeto del juego
        this.juego = new Juego();

        // Configura si usa fondo invisible
        this.juego.sinFondo = sinFondo;

        if (sinFondo){
            capturaPantalla();
        }

        // Crea el display y lo conecta al juego
        this.display = new Display(juego);
        this.juego.setDisplay(this.display);

        // Configura cantidad de enemigos
        this.juego.cantidadMalos = cantMalos;
        this.juego.cantidadAguilas = aguilas;

        // Configura si el personaje se centra
        this.juego.centrar = centrar;

        // Crea los personajes del juego
        this.juego.crearPersonajes();

        // Conecta la función de estado terminado
        this.display.terminadoFunc = juego.terminadoFunc();

        // Inicia el game loop
        this.juego.comenzar();
    }

    public static void main(String[] args) {
        // Configura escala de UI para evitar problemas de resolución
        System.setProperty("sun.java2d.uiScale", "1");
        System.out.println("Inicia Zorrito 1.0");
        System.out.println("------------------");
        System.out.println("");

        // Muestra ayuda si se solicita
        if(args.length > 0 && "-help".equals(args[0])) {
            String ayuda = """
            -help       : Muestra esta ayuda en pantalla
            -pajaros    : Indica la cantidad de pájaros. Ej: -pajaros:25
            -aguilas    : Indica la cantidad de enemigos. Ej: -aguilas:10
            -no-centrar : No centra al personaje en la pantalla
            -sin-fondo  : El juego se da sobre la pantalla actual

            """;
            System.out.println(ayuda);
            System.exit(0);
        }

        // Valores por defecto
        boolean conBuffer = true;
        int size = 20;
        int aguilas = 0;
        boolean centrar = true;
        boolean sinFondo = false;

        // Procesa los argumentos de línea de comando
        for (String s : args) {
            if ("-no-centrar".equals(s)){
                centrar = false;
                System.out.println("- Personaje no centrado");
            }

            if (s.contains("-pajaros:")){
                String[] partes = s.split(":");
                size = Integer.parseInt(partes[1]);
                System.out.println("- Pajaros: " + size);
            }

            if (s.contains("-aguilas:")){
                String[] partes = s.split(":");
                aguilas = Integer.parseInt(partes[1]);
                System.out.println("- Aguilas: " + aguilas);
            }

            if ("-sin-fondo".equals(s)){
                sinFondo = true;
                System.out.println("- Fondo invisible");
            }
        }

        // Crea la instancia del juego
        new Zorrito(conBuffer, size, centrar, sinFondo, aguilas - 1);
    }

    /**
     * Captura la pantalla actual para usarla como fondo transparente.
     */
    private void capturaPantalla(){
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
            return;
        }

        // Obtiene el tamaño de la pantalla
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        // Captura la imagen
        BufferedImage screenFullImage = robot.createScreenCapture(screenRect);

        // Guarda la captura como archivo PNG
        try {
            ImageIO.write(screenFullImage, "PNG", new File("assets/screenshot.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
