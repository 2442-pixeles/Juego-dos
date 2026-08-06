package ar.edu.unlu.poo.juegodos.vista;

import ar.edu.unlu.poo.juegodos.modelo.Carta;
import ar.edu.unlu.poo.juegodos.modelo.ColorCarta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Vista grafica, implementa lo mismo que VistaConsola asi que se puede usar
// cualquiera de las dos sin tocar el Controlador ni el Modelo.
// Todo pasa en la misma ventana: los mensajes/cartas van al log y a los paneles de
// arriba, y los pedidos de input se resuelven en el panel de "Acciones" de abajo,
// nunca abro una ventana nueva para preguntar algo.
// Lo del hilo: el Controlador corre en su propio hilo (no el de Swing) y necesita
// quedarse bloqueado esperando un clic, asi que uso una BlockingQueue - el Controlador
// hace colaRespuestas.take() (bloquea) y cada boton hace colaRespuestas.offer(valor)
// cuando lo clickean. Es el patron productor-consumidor de siempre.
public class VistaSwing implements IVista {

    private final JFrame frame;
    private final JTextArea areaLog = new JTextArea();
    private final JPanel panelMesa = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JPanel panelMano = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JLabel etiquetaTurno = new JLabel("Esperando...");
    private final JLabel etiquetaPrompt = new JLabel(" ");
    private final JPanel filaControles = new JPanel(new FlowLayout());
    private final BlockingQueue<Object> colaRespuestas = new LinkedBlockingQueue<>();
    private final JButton botonRanking = new JButton("Ver Ranking");
    private final JButton botonGuardar = new JButton("Guardar y salir");
    private final JButton botonSalir = new JButton("Salir del juego");

    private List<Carta> ultimasCartasMostradas;

    public VistaSwing (String nombreJugador){
        this.frame = crearVentana(nombreJugador);
    }

    // el Controlador llama esto despues de armar la vista, para que estos botones
    // (que se pueden apretar en cualquier momento, no siguen el flujo normal) sepan que hacer
    public void configurarAccionesGlobales (Runnable accionVerRanking, Runnable accionGuardarYSalir, Runnable accionSalir){
        botonRanking.addActionListener(e -> accionVerRanking.run());
        botonGuardar.addActionListener(e -> accionGuardarYSalir.run());
        botonSalir.addActionListener(e -> accionSalir.run());
    }

    private JFrame crearVentana (String nombreJugador){
        return ejecutarEnEDT(() -> {
            JFrame f = new JFrame("DOS — " + nombreJugador);
            f.setLayout(new BorderLayout(8, 8));

            etiquetaTurno.setFont(new Font("Arial", Font.BOLD, 16));
            etiquetaTurno.setHorizontalAlignment(SwingConstants.CENTER);
            etiquetaTurno.setOpaque(true);
            etiquetaTurno.setBackground(new Color(230, 230, 230));

            JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panelSuperior.add(botonRanking);
            panelSuperior.add(botonGuardar);
            panelSuperior.add(botonSalir);

            JPanel arriba = new JPanel();
            arriba.setLayout(new BoxLayout(arriba, BoxLayout.Y_AXIS));
            arriba.add(panelSuperior);
            arriba.add(etiquetaTurno);
            arriba.add(conTitulo("Mesa", panelMesa, new Dimension(660, 150)));
            arriba.add(conTitulo("Tu mano", panelMano, new Dimension(660, 200)));
            f.add(arriba, BorderLayout.NORTH);

            areaLog.setEditable(false);
            areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            f.add(new JScrollPane(areaLog), BorderLayout.CENTER);

            etiquetaPrompt.setFont(new Font("Arial", Font.PLAIN, 14));
            JPanel panelAcciones = new JPanel(new BorderLayout(4, 4));
            panelAcciones.setBorder(BorderFactory.createTitledBorder("Acciones"));
            panelAcciones.add(etiquetaPrompt, BorderLayout.NORTH);
            panelAcciones.add(filaControles, BorderLayout.CENTER);
            panelAcciones.setPreferredSize(new Dimension(660, 170));
            f.add(panelAcciones, BorderLayout.SOUTH);

            f.setSize(700, 800);
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
            return f;
        });
    }

    private JPanel conTitulo (String titulo, JPanel contenido, Dimension tamanoVisible){
        JPanel envoltorio = new JPanel(new BorderLayout());
        envoltorio.setBorder(BorderFactory.createTitledBorder(titulo));

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setPreferredSize(tamanoVisible); // tamaño de la "ventanita" visible, no del contenido
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        envoltorio.add(scroll, BorderLayout.CENTER);
        return envoltorio;
    }

    @Override
    public void mostrarMensaje (String mensaje){
        SwingUtilities.invokeLater(() -> {
            areaLog.append(mensaje + "\n");
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        });
    }

    @Override
    public void mostrarCartas (String titulo, List<Carta> cartas){
        ultimasCartasMostradas = cartas;
        mostrarMensaje(titulo);

        JPanel destino = titulo.toLowerCase().contains("mesa") ? panelMesa : panelMano;
        SwingUtilities.invokeLater(() -> {
            destino.removeAll();
            for (Carta carta : cartas){
                destino.add(new PanelCarta(carta));
            }
            destino.revalidate();
            destino.repaint();
        });
    }

    @Override
    public void mostrarTurno (boolean esMiTurno){
        SwingUtilities.invokeLater(() -> {
            if (esMiTurno){
                etiquetaTurno.setText("★ ES TU TURNO ★");
                etiquetaTurno.setBackground(new Color(255, 235, 59));
            } else {
                etiquetaTurno.setText("Esperando el turno del otro jugador...");
                etiquetaTurno.setBackground(new Color(230, 230, 230));
            }
        });
    }

    @Override
    public int pedirOpcion (String mensaje, int min, int max){
        boolean esSeleccionDeCarta = min == 0
                && ultimasCartasMostradas != null
                && !ultimasCartasMostradas.isEmpty()
                && max == ultimasCartasMostradas.size() - 1;

        if (esSeleccionDeCarta){
            return pedirPosicionDeCartas(mensaje, ultimasCartasMostradas);
        }

        List<JButton> botones = new ArrayList<>();
        String[] lineas = mensaje.split("\n");
        for (int valor = min; valor <= max; valor++){
            String etiqueta = String.valueOf(valor);
            String prefijo = valor + ".";
            for (String linea : lineas){
                String limpia = linea.trim();
                if (limpia.startsWith(prefijo)){
                    etiqueta = limpia.substring(prefijo.length()).trim();
                    break;
                }
            }
            JButton boton = new JButton(etiqueta);
            int valorFinal = valor;
            String etiquetaFinal = etiqueta;
            boton.addActionListener(e -> {
                mostrarMensaje("Elegiste: " + etiquetaFinal);
                colaRespuestas.offer(valorFinal);
            });
            botones.add(boton);
        }

        mostrarControles("Elegí una opción:", botones);
        int resultado = (Integer) tomarRespuesta();
        limpiarAcciones();
        return resultado;
    }

    @Override
    public boolean pedirSiNo (String mensaje){
        JButton si = new JButton("Sí");
        JButton no = new JButton("No");
        si.addActionListener(e -> {
            mostrarMensaje("Elegiste: Sí");
            colaRespuestas.offer(Boolean.TRUE);
        });
        no.addActionListener(e -> {
            mostrarMensaje("Elegiste: No");
            colaRespuestas.offer(Boolean.FALSE);
        });

        mostrarControles(mensaje, List.of(si, no));
        boolean resultado = (Boolean) tomarRespuesta();
        limpiarAcciones();
        return resultado;
    }

    @Override
    public String pedirTexto (String mensaje){
        JTextField campo = new JTextField(15);
        JButton aceptar = new JButton("Aceptar");
        aceptar.addActionListener(e -> colaRespuestas.offer(campo.getText()));

        mostrarControles(mensaje, List.of(campo, aceptar));
        String resultado = (String) tomarRespuesta();
        limpiarAcciones();
        return resultado;
    }

    /** Elegir una carta haciendo clic, dentro del mismo panel de Acciones (no en una ventana nueva). */
    private int pedirPosicionDeCartas (String mensaje, List<Carta> cartas){
        List<PanelCarta> paneles = new ArrayList<>();
        for (int i = 0; i < cartas.size(); i++){
            int indice = i;
            Carta carta = cartas.get(i);
            PanelCarta panelCarta = new PanelCarta(carta);
            panelCarta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            panelCarta.addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked (MouseEvent e){
                    mostrarMensaje("Elegiste: " + carta.descripcion());
                    colaRespuestas.offer(indice);
                }
            });
            paneles.add(panelCarta);
        }

        mostrarControles(mensaje, paneles);
        int resultado = (Integer) tomarRespuesta();
        limpiarAcciones();
        return resultado;
    }

    private void mostrarControles (String mensaje, List<? extends JComponent> controles){
        colaRespuestas.clear(); // descarta cualquier clic viejo sin consumir (ej: doble click accidental)
        SwingUtilities.invokeLater(() -> {
            etiquetaPrompt.setText(mensaje);
            filaControles.removeAll();
            for (JComponent control : controles){
                filaControles.add(control);
            }
            filaControles.revalidate();
            filaControles.repaint();
        });
    }

    private void limpiarAcciones (){
        SwingUtilities.invokeLater(() -> {
            etiquetaPrompt.setText(" ");
            filaControles.removeAll();
            filaControles.revalidate();
            filaControles.repaint();
        });
    }

    private Object tomarRespuesta (){
        try {
            return colaRespuestas.take();
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    private <T> T ejecutarEnEDT (java.util.concurrent.Callable<T> tarea){
        final Object[] resultado = new Object[1];
        final Exception[] error = new Exception[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    resultado[0] = tarea.call();
                } catch (Exception e){
                    error[0] = e;
                }
            });
        } catch (Exception e){
            throw new RuntimeException("Error mostrando la ventana", e);
        }
        if (error[0] != null){
            throw new RuntimeException(error[0]);
        }
        @SuppressWarnings("unchecked")
        T valor = (T) resultado[0];
        return valor;
    }

    /** Dibuja una carta (color de fondo, número o comodín) con Java2D, sin usar ninguna imagen externa. */
    private static class PanelCarta extends JPanel {

        private final Carta carta;

        PanelCarta (Carta carta){
            this.carta = carta;
            setPreferredSize(new Dimension(80, 120));
            setOpaque(false);
        }

        @Override
        protected void paintComponent (Graphics g){
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(2, 2, w - 4, h - 4, 15, 15);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(2, 2, w - 4, h - 4, 15, 15);

            if (carta.esMulticolor()){
                Color[] franjas = {new Color(220, 50, 50), new Color(230, 200, 40), new Color(50, 160, 70), new Color(50, 90, 200)};
                int alturaFranja = (h - 16) / franjas.length;
                for (int i = 0; i < franjas.length; i++){
                    g2.setColor(franjas[i]);
                    g2.fillRoundRect(8, 8 + i * alturaFranja, w - 16, alturaFranja, 6, 6);
                }
            } else {
                g2.setColor(mapearColor(carta.getColorCarta()));
                g2.fillRoundRect(8, 8, w - 16, h - 16, 10, 10);
            }

            String texto = carta.getComodin() ? "*" : String.valueOf(carta.getNumeroCarta());
            g2.setFont(new Font("Arial", Font.BOLD, 30));
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(texto)) / 2;
            int ty = (h + fm.getAscent()) / 2 - 4;

            g2.setColor(Color.BLACK);
            g2.drawString(texto, tx + 1, ty + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(texto, tx, ty);
        }

        private Color mapearColor (ColorCarta color){
            switch (color){
                case ROJO:
                    return new Color(220, 50, 50);
                case AMARILLO:
                    return new Color(230, 200, 40);
                case VERDE:
                    return new Color(50, 160, 70);
                case AZUL:
                    return new Color(50, 90, 200);
                default:
                    return Color.GRAY;
            }
        }
    }
}
