package ar.edu.unlu.poo.juegodos.persistencia;

import ar.edu.unlu.poo.juegodos.modelo.Carta;
import ar.edu.unlu.poo.juegodos.modelo.Jugador;

import java.io.Serializable;
import java.util.ArrayList;

// una "foto" de la partida en curso, para guardarla y reconstruirla despues.
// es un DTO nomas, sin logica, por eso los campos son publicos directo
public class EstadoPartida implements Serializable {
    private static final long serialVersionUID = 1L;

    public ArrayList<Jugador> jugadores;
    public ArrayList<Carta> cartasEnMesa;
    public ArrayList<Carta> mazoDescarte;
    public ArrayList<Carta> mazoRestante;
    public int indiceJugadorEnTurno;
    public int numeroRonda;
    public int cantidadJugadores;
}
