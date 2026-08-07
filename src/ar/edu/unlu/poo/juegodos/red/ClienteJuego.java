package ar.edu.unlu.poo.juegodos.red;

import ar.edu.unlu.poo.juegodos.controlador.Controlador;
import ar.edu.unlu.poo.juegodos.vista.IVista;
import ar.edu.unlu.poo.juegodos.vista.VistaConsola;
import ar.edu.unlu.poo.juegodos.vista.VistaSwing;
import ar.edu.unlu.poo.rmimvc.RMIMVCException;
import ar.edu.unlu.poo.rmimvc.cliente.Cliente;

import javax.swing.JOptionPane;
import java.rmi.RemoteException;

public class ClienteJuego {

    private static final int PUERTO_SERVIDOR = 1099;

    public static void main (String[] args){
        String hostServidor = JOptionPane.showInputDialog(null,
                "IP del servidor (vacío = localhost):", "DOS", JOptionPane.QUESTION_MESSAGE);
        if (hostServidor == null){
            return; // canceló
        }
        hostServidor = hostServidor.trim();
        if (hostServidor.isEmpty()){
            hostServidor = "localhost";
        }

        String nombre = JOptionPane.showInputDialog(null, "Ingrese su nombre:", "DOS", JOptionPane.QUESTION_MESSAGE);
        if (nombre == null || nombre.trim().isEmpty()){
            return;
        }
        nombre = nombre.trim();

        Object[] opciones = {"Consola", "Gráfica"};
        int eleccion = JOptionPane.showOptionDialog(null, "¿Qué interfaz quiere usar?", "DOS",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);

        // El Modelo y el Controlador son SIEMPRE los mismos, sin importar
        // qué interfaz se elija acá — solo cambia qué IVista se le pasa
        // al Controlador.
        IVista vista = (eleccion == 1) ? new VistaSwing(nombre) : new VistaConsola();
        Controlador controlador = new Controlador(vista, nombre);

        if (vista instanceof VistaSwing){
            VistaSwing vistaSwing = (VistaSwing) vista;
            vistaSwing.configurarAccionesGlobales(controlador::verRankingAhora, controlador::guardarYSalir, controlador::salirDelJuego);
        }

        // host/puerto locales en 0: que el sistema elija un puerto libre para este cliente.
        Cliente cliente = new Cliente("localhost", 0, hostServidor, PUERTO_SERVIDOR);

        try {
            cliente.iniciar(controlador);
            controlador.registrarse();
            controlador.jugar();
        } catch (RMIMVCException e) {
            vista.mostrarMensaje("No se pudo conectar al servidor: " + e.getMessage());
        } catch (RemoteException e) {
            vista.mostrarMensaje("Error de comunicación con el servidor: " + e.getMessage());
        }
    }
}
