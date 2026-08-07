package ar.edu.unlu.poo.juegodos.persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// guarda y recupera una partida completa en un archivo binario con serializacion
// comun de Java. Es como Ranking pero para el estado entero del juego
public class GuardadoPartida {

    private static final String ARCHIVO = "partida_guardada.dat";

    private GuardadoPartida (){
    }

    public static void guardar (EstadoPartida estado){
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ARCHIVO))){
            salida.writeObject(estado);
        } catch (IOException e){
            System.out.println("No se pudo guardar la partida: " + e.getMessage());
        }
    }

    public static boolean existeGuardado (){
        return new File(ARCHIVO).exists();
    }

    public static EstadoPartida cargar (){
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ARCHIVO))){
            return (EstadoPartida) entrada.readObject();
        } catch (IOException | ClassNotFoundException e){
            System.out.println("No se pudo cargar la partida guardada: " + e.getMessage());
            return null;
        }
    }

    public static void borrar (){
        new File(ARCHIVO).delete();
    }
}
