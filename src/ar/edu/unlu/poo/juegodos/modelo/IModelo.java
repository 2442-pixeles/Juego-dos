package ar.edu.unlu.poo.juegodos.modelo;

import ar.edu.unlu.poo.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

// interfaz remota del modelo (el Tablero). extiende IObservableRemoto de la libreria
// para que los controladores remotos lo puedan observar.
// ojo con la privacidad: aca solo van consultas seguras. getMiMano() esta atada a un
// nombre y devuelve SOLO esa mano - nunca hay un metodo que devuelva todos los jugadores
// con sus cartas juntos
public interface IModelo extends IObservableRemoto {

    void registrarJugador (String nombre) throws RemoteException;

    boolean partidaCompleta () throws RemoteException;

    boolean partidaTerminada () throws RemoteException;

    String getGanadorPartida () throws RemoteException;

    int getPuntosDe (String nombreJugador) throws RemoteException;

    String getJugadorEnTurno () throws RemoteException;

    int getNumeroRonda () throws RemoteException;

    ArrayList<Carta> getCartasEnMesa () throws RemoteException;

    ArrayList<Carta> getMiMano (String nombreJugador) throws RemoteException;

    int cuantasCartasLeQuedanDe (String nombreJugador) throws RemoteException;

    boolean robarCarta (String nombreJugador) throws RemoteException;

    boolean descartarCarta (String nombreJugador, int posicion) throws RemoteException;

    boolean intentarCombinacionSimple (String nombreJugador, int posCartaPropia, int posCartaMesa) throws RemoteException;

    boolean intentarCombinacionDoble (String nombreJugador, int pos1, int pos2, int posCartaMesa) throws RemoteException;

    void aplicarBonificacionColor (String nombreJugador, int posicionCartaPropia) throws RemoteException;

    void aplicarBonificacionColorDoble (String nombreJugador, int posicionCartaPropia) throws RemoteException;

    void darCartaExtraAOtrosPorBonificacion (String nombreJugador) throws RemoteException;

    boolean terminarTurno (String nombreJugador) throws RemoteException;

    void reiniciarPartida () throws RemoteException;

    boolean cantarDos (String nombreJugador) throws RemoteException;

    boolean acusarDos (String nombreAcusador, String nombreAcusado) throws RemoteException;

    List<String> getNombresJugadores () throws RemoteException;

    // consulta el ranking en cualquier momento, no hace falta que termine la partida
    String getRankingTexto () throws RemoteException;

    // guarda la partida en disco para poder seguirla en otra sesion del servidor
    boolean guardarPartida () throws RemoteException;
}
