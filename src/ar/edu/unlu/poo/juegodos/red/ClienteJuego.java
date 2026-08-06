package ar.edu.unlu.poo.juegodos.red;

import ar.edu.unlu.poo.juegodos.controlador.Controlador;
import ar.edu.unlu.poo.juegodos.vista.IVista;
import ar.edu.unlu.poo.juegodos.vista.VistaConsola;
import ar.edu.unlu.poo.juegodos.vista.VistaSwing;
import ar.edu.unlu.poo.rmimvc.RMIMVCException;
import ar.edu.unlu.poo.rmimvc.cliente.Cliente;

import java.rmi.RemoteException;
import java.util.Scanner;

public class ClienteJuego {

    private static final int PUERTO_SERVIDOR = 1099;

    public static void main (String[] args){
        Scanner scanner = new Scanner(System.in);

        System.out.println("IP del servidor (Enter para localhost):");
        String hostServidor = scanner.nextLine().trim();
        if (hostServidor.isEmpty()){
            hostServidor = "localhost";
        }

        System.out.println("Ingrese su nombre:");
        String nombre = scanner.nextLine().trim();

        System.out.println("¿Qué interfaz quiere usar? 1 = Consola, 2 = Gráfica");
        String tipoInterfaz = scanner.nextLine().trim();

        // El Modelo y el Controlador son SIEMPRE los mismos, sin importar
        // qué interfaz se elija acá — solo cambia qué IVista se le pasa
        // al Controlador.
        IVista vista = "2".equals(tipoInterfaz) ? new VistaSwing(nombre) : new VistaConsola();
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
            System.out.println("No se pudo conectar al servidor: " + e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Error de comunicación con el servidor: " + e.getMessage());
        }
    }
}
