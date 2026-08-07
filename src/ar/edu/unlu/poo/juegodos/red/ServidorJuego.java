package ar.edu.unlu.poo.juegodos.red;

import ar.edu.unlu.poo.juegodos.modelo.Tablero;
import ar.edu.unlu.poo.juegodos.persistencia.EstadoPartida;
import ar.edu.unlu.poo.juegodos.persistencia.GuardadoPartida;
import ar.edu.unlu.poo.rmimvc.RMIMVCException;
import ar.edu.unlu.poo.rmimvc.servidor.Servidor;

import javax.swing.JOptionPane;
import java.rmi.RemoteException;

public class ServidorJuego {

    private static final int PUERTO = 1099;

    public static void main (String[] args){
        try {
            Tablero modelo = armarModelo();
            Servidor servidor = new Servidor("localhost", PUERTO);
            servidor.iniciar(modelo);

            JOptionPane.showMessageDialog(null, "Servidor RMIMVC listo en el puerto " + PUERTO + ".", "DOS", JOptionPane.INFORMATION_MESSAGE);
        } catch (RemoteException | RMIMVCException e) {
            JOptionPane.showMessageDialog(null, "No se pudo iniciar el servidor: " + e.getMessage(), "DOS", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Tablero armarModelo (){
        if (GuardadoPartida.existeGuardado()){
            int resp = JOptionPane.showConfirmDialog(null, "Hay una partida guardada. ¿Querés continuarla?", "DOS", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION){
                EstadoPartida estado = GuardadoPartida.cargar();
                if (estado != null){
                    GuardadoPartida.borrar();
                    JOptionPane.showMessageDialog(null, "Partida cargada. Reconectá los clientes con los mismos nombres de antes.", "DOS", JOptionPane.INFORMATION_MESSAGE);
                    return new Tablero(estado);
                }
                JOptionPane.showMessageDialog(null, "No se pudo leer la partida guardada, arranca una nueva.", "DOS", JOptionPane.WARNING_MESSAGE);
            }
        }

        int cantidadJugadores = pedirCantidadJugadores();
        return new Tablero(cantidadJugadores);
    }

    private static int pedirCantidadJugadores (){
        while (true){
            String linea = JOptionPane.showInputDialog(null, "¿Cuántos jugadores van a jugar esta partida? (2 a 4)", "DOS", JOptionPane.QUESTION_MESSAGE);
            if (linea == null){
                continue;
            }
            try {
                int cantidad = Integer.parseInt(linea.trim());
                if (cantidad >= 2 && cantidad <= 4){
                    return cantidad;
                }
            } catch (NumberFormatException ignored){
            }
            JOptionPane.showMessageDialog(null, "Ingrese un número entre 2 y 4.", "DOS", JOptionPane.WARNING_MESSAGE);
        }
    }
}
