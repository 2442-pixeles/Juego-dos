package ar.edu.unlu.poo.juegodos.vista;

import ar.edu.unlu.poo.juegodos.modelo.Carta;

import java.util.List;
import java.util.Scanner;

public class VistaConsola implements IVista {

    private final Scanner scanner;

    public VistaConsola (){
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void mostrarMensaje (String mensaje){
        System.out.println(mensaje);
    }

    @Override
    public void mostrarCartas (String titulo, List<Carta> cartas){
        System.out.println("\n" + titulo);
        int i = 0;
        for (Carta carta : cartas){
            System.out.println("Posicion: " + i + " - " + carta.descripcion());
            i++;
        }
    }

    @Override
    public void mostrarTurno (boolean esMiTurno){
        if (esMiTurno){
            System.out.println("\n===== ES TU TURNO =====");
        } else {
            System.out.println("\n(Esperando el turno del otro jugador...)");
        }
    }

    @Override
    public int pedirOpcion (String mensaje, int min, int max){
        int opcion;
        while (true){
            System.out.println(mensaje);
            if (scanner.hasNextInt()){
                opcion = scanner.nextInt();
                scanner.nextLine();
                if (opcion >= min && opcion <= max){
                    return opcion;
                }
            } else {
                scanner.nextLine();
            }
            System.out.println("Opción inválida, ingrese un valor entre " + min + " y " + max + ".");
        }
    }

    @Override
    public boolean pedirSiNo (String mensaje){
        int respuesta = pedirOpcion(mensaje + " (0 = No / 1 = Si)", 0, 1);
        return respuesta == 1;
    }

    @Override
    public String pedirTexto (String mensaje){
        System.out.println(mensaje);
        return scanner.nextLine();
    }
}
