package ar.edu.unlu.poo.juegodos.red;

import ar.edu.unlu.poo.juegodos.modelo.Tablero;
import ar.edu.unlu.poo.juegodos.persistencia.EstadoPartida;
import ar.edu.unlu.poo.juegodos.persistencia.GuardadoPartida;
import ar.edu.unlu.poo.rmimvc.RMIMVCException;
import ar.edu.unlu.poo.rmimvc.servidor.Servidor;

import java.rmi.RemoteException;
import java.util.Scanner;

public class ServidorJuego {

    private static final int PUERTO = 1099;

    public static void main (String[] args){
        Scanner scanner = new Scanner(System.in);

        try {
            Tablero modelo = armarModelo(scanner);
            Servidor servidor = new Servidor("localhost", PUERTO);
            servidor.iniciar(modelo);

            System.out.println("Servidor RMIMVC listo en el puerto " + PUERTO + ".");
        } catch (RemoteException | RMIMVCException e) {
            System.out.println("No se pudo iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Tablero armarModelo (Scanner scanner){
        if (GuardadoPartida.existeGuardado()){
            System.out.println("Hay una partida guardada. ¿Querés continuarla? (0 = No / 1 = Sí)");
            String respuesta = scanner.nextLine().trim();
            if ("1".equals(respuesta)){
                EstadoPartida estado = GuardadoPartida.cargar();
                if (estado != null){
                    GuardadoPartida.borrar(); // ya se cargó, no queremos volver a cargarla la próxima vez sin querer
                    System.out.println("Partida cargada. Reconectá los clientes con los mismos nombres de antes.");
                    return new Tablero(estado);
                }
                System.out.println("No se pudo leer la partida guardada, arranca una nueva.");
            }
        }

        int cantidadJugadores = pedirCantidadJugadores(scanner);
        Tablero modelo = new Tablero(cantidadJugadores);
        System.out.println("Esperando " + cantidadJugadores + " jugadores...");
        return modelo;
    }

    private static int pedirCantidadJugadores (Scanner scanner){
        while (true){
            System.out.println("¿Cuántos jugadores van a jugar esta partida? (2 a 4)");
            String linea = scanner.nextLine().trim();
            try {
                int cantidad = Integer.parseInt(linea);
                if (cantidad >= 2 && cantidad <= 4){
                    return cantidad;
                }
            } catch (NumberFormatException ignored){
            }
            System.out.println("Ingrese un número entre 2 y 4.");
        }
    }
}
