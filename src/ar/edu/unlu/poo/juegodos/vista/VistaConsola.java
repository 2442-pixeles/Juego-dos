package ar.edu.unlu.poo.juegodos.vista;

import ar.edu.unlu.poo.juegodos.modelo.Carta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// misma pinta que una consola de texto (mismo formato de mensajes de antes), pero
// sin Scanner ni System.out: la salida va a un JTextArea y la entrada se escribe
// en un campo de texto abajo, como si fuera una terminal metida en una ventana.
public class VistaConsola implements IVista {

    private final JFrame frame;
    private final JTextArea areaLog = new JTextArea();
    private final JTextField campoInput = new JTextField();
    private final BlockingQueue<String> colaLineas = new LinkedBlockingQueue<>();

    public VistaConsola (){
        this.frame = crearVentana();
    }

    private JFrame crearVentana (){
        return ejecutarEnEDT(() -> {
            JFrame f = new JFrame("DOS — consola");
            f.setLayout(new BorderLayout(6, 6));

            areaLog.setEditable(false);
            areaLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            f.add(new JScrollPane(areaLog), BorderLayout.CENTER);

            campoInput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            campoInput.addActionListener((ActionEvent e) -> {
                String linea = campoInput.getText();
                campoInput.setText("");
                areaLog.append("> " + linea + "\n");
                colaLineas.offer(linea);
            });
            f.add(campoInput, BorderLayout.SOUTH);

            f.setSize(650, 600);
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
            campoInput.requestFocusInWindow();
            return f;
        });
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
        StringBuilder sb = new StringBuilder("\n").append(titulo).append("\n");
        int i = 0;
        for (Carta carta : cartas){
            sb.append("Posicion: ").append(i).append(" - ").append(carta.descripcion()).append("\n");
            i++;
        }
        mostrarMensaje(sb.toString());
    }

    @Override
    public void mostrarTurno (boolean esMiTurno){
        if (esMiTurno){
            mostrarMensaje("\n===== ES TU TURNO =====");
        } else {
            mostrarMensaje("\n(Esperando el turno del otro jugador...)");
        }
    }

    @Override
    public int pedirOpcion (String mensaje, int min, int max){
        mostrarMensaje(mensaje);
        while (true){
            String linea = leerLinea();
            try {
                int valor = Integer.parseInt(linea.trim());
                if (valor >= min && valor <= max){
                    return valor;
                }
            } catch (NumberFormatException ignored){
            }
            mostrarMensaje("Opción inválida, ingrese un valor entre " + min + " y " + max + ".");
        }
    }

    @Override
    public boolean pedirSiNo (String mensaje){
        int respuesta = pedirOpcion(mensaje + " (0 = No / 1 = Si)", 0, 1);
        return respuesta == 1;
    }

    @Override
    public String pedirTexto (String mensaje){
        mostrarMensaje(mensaje);
        return leerLinea();
    }

    private String leerLinea (){
        try {
            return colaLineas.take();
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
}
