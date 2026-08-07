package ar.edu.unlu.poo.juegodos.modelo;

import ar.edu.unlu.poo.juegodos.persistencia.EstadoPartida;
import ar.edu.unlu.poo.juegodos.persistencia.GuardadoPartida;
import ar.edu.unlu.poo.juegodos.persistencia.Ranking;
import ar.edu.unlu.poo.rmimvc.observer.ObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Tablero extends ObservableRemoto implements IModelo {

    private static final int MIN_JUGADORES = 2; // reglas oficiales de DOS: de 2 a 4 jugadores
    private static final int MAX_JUGADORES = 4;

    private final int cantidadJugadores;
    private final ArrayList<Jugador> jugadores;
    private final ArrayList<Carta> cartasEnMesa;
    private final ArrayList<Carta> mazoDescarte;
    private final Mazo mazoDeCartas;

    private int indiceJugadorEnTurno;
    private int numeroRonda;
    private boolean partidaTerminada;
    private String ganadorPartida;

    public Tablero (int cantidadJugadores){
        if (cantidadJugadores < MIN_JUGADORES || cantidadJugadores > MAX_JUGADORES){
            throw new IllegalArgumentException("La cantidad de jugadores debe ser entre " + MIN_JUGADORES + " y " + MAX_JUGADORES);
        }
        this.cantidadJugadores = cantidadJugadores;
        this.jugadores = new ArrayList<>();
        this.cartasEnMesa = new ArrayList<>();
        this.mazoDescarte = new ArrayList<>();
        this.mazoDeCartas = new Mazo();
        this.mazoDeCartas.construirMazo();
        this.indiceJugadorEnTurno = 0;
        this.numeroRonda = 1;
        this.partidaTerminada = false;
        this.ganadorPartida = null;
    }

    /** Reconstruye el Tablero a partir de una partida guardada previamente. */
    public Tablero (EstadoPartida estado){
        this.cantidadJugadores = estado.cantidadJugadores;
        this.jugadores = new ArrayList<>(estado.jugadores);
        this.cartasEnMesa = new ArrayList<>(estado.cartasEnMesa);
        this.mazoDescarte = new ArrayList<>(estado.mazoDescarte);
        this.mazoDeCartas = new Mazo();
        this.mazoDeCartas.getMazoDeCartas().addAll(estado.mazoRestante);
        this.indiceJugadorEnTurno = estado.indiceJugadorEnTurno;
        this.numeroRonda = estado.numeroRonda;
        this.partidaTerminada = false;
        this.ganadorPartida = null;
    }

    // métodos remotos (IModelo)

    // si el nombre ya estaba en la lista (partida cargada de un guardado) no creo un Jugador
    // nuevo, solo reconozco la reconexion. ojo que no espero a que TODOS se reconecten antes
    // de arrancar - si la partida quedo guardada ya completa, arranca apenas el de turno actua
    @Override
    public synchronized void registrarJugador (String nombre) throws RemoteException {
        for (Jugador existente : jugadores){
            if (existente.getNombre().equals(nombre)){
                notificarObservadores(nombre + " se reconectó a la partida guardada.");
                return;
            }
        }

        if (jugadores.size() >= cantidadJugadores){
            notificarObservadores("La partida ya está completa.");
            return;
        }

        jugadores.add(new Jugador(nombre));
        notificarObservadores(nombre + " se conectó (" + jugadores.size() + "/" + cantidadJugadores + ").");

        if (jugadores.size() == cantidadJugadores){
            crearPartida();
            notificarObservadores("La partida arrancó. Turno de: " + getJugadorEnTurno());
        }
    }

    // guarda una foto de la partida para poder seguirla despues
    @Override
    public synchronized boolean guardarPartida () throws RemoteException {
        EstadoPartida estado = new EstadoPartida();
        estado.jugadores = new ArrayList<>(jugadores);
        estado.cartasEnMesa = new ArrayList<>(cartasEnMesa);
        estado.mazoDescarte = new ArrayList<>(mazoDescarte);
        estado.mazoRestante = new ArrayList<>(mazoDeCartas.getMazoDeCartas());
        estado.indiceJugadorEnTurno = indiceJugadorEnTurno;
        estado.numeroRonda = numeroRonda;
        estado.cantidadJugadores = cantidadJugadores;

        GuardadoPartida.guardar(estado);
        notificarObservadores("Partida guardada.");
        return true;
    }

    @Override
    public boolean partidaCompleta (){
        return jugadores.size() == cantidadJugadores;
    }

    @Override
    public boolean partidaTerminada (){
        return partidaTerminada;
    }

    @Override
    public String getGanadorPartida (){
        return ganadorPartida;
    }

    @Override
    public int getPuntosDe (String nombreJugador) throws RemoteException {
        return buscarJugador(nombreJugador).getPuntos();
    }

    @Override
    public String getJugadorEnTurno (){
        if (jugadores.isEmpty()){
            return null;
        }
        return jugadores.get(indiceJugadorEnTurno).getNombre();
    }

    @Override
    public int getNumeroRonda (){
        return numeroRonda;
    }

    @Override
    public synchronized ArrayList<Carta> getCartasEnMesa (){
        return new ArrayList<>(cartasEnMesa);
    }

    @Override
    public synchronized ArrayList<Carta> getMiMano (String nombreJugador) throws RemoteException {
        return new ArrayList<>(buscarJugador(nombreJugador).getCartasEnPosesion());
    }

    @Override
    public int cuantasCartasLeQuedanDe (String nombreJugador) throws RemoteException {
        return buscarJugador(nombreJugador).cuantasCartasLeQuedan();
    }

    @Override
    public synchronized boolean robarCarta (String nombreJugador) throws RemoteException {
        if (!esTurnoDe(nombreJugador)){
            return false;
        }
        Jugador jugador = buscarJugador(nombreJugador);
        mazoDeCartas.robarCarta(jugador, this);
        notificarObservadores(null); // solo un "algo cambió", sin mensaje para imprimir
        return true;
    }

    @Override
    public synchronized boolean descartarCarta (String nombreJugador, int posicion) throws RemoteException {
        if (!esTurnoDe(nombreJugador)){
            return false;
        }
        buscarJugador(nombreJugador).descartarCartaAMesa(posicion, this);
        notificarObservadores(null);
        return true;
    }

    @Override
    public synchronized boolean intentarCombinacionSimple (String nombreJugador, int posCartaPropia, int posCartaMesa) throws RemoteException {
        if (!esTurnoDe(nombreJugador)){
            return false;
        }
        Jugador jugador = buscarJugador(nombreJugador);
        Carta cartaMesa = cartasEnMesa.get(posCartaMesa);
        boolean valida = jugador.intentarCombinacionSimple(posCartaPropia, cartaMesa, posCartaMesa, this);
        if (valida){
            reponerMesaSiFalta();
            notificarObservadores(null);
        }
        return valida;
    }

    @Override
    public synchronized boolean intentarCombinacionDoble (String nombreJugador, int pos1, int pos2, int posCartaMesa) throws RemoteException {
        if (!esTurnoDe(nombreJugador)){
            return false;
        }
        Jugador jugador = buscarJugador(nombreJugador);
        Carta cartaMesa = cartasEnMesa.get(posCartaMesa);
        boolean valida = jugador.intentarCombinacionDoble(pos1, pos2, cartaMesa, posCartaMesa, this);
        if (valida){
            reponerMesaSiFalta();
            notificarObservadores(null);
        }
        return valida;
    }

    @Override
    public synchronized void aplicarBonificacionColor (String nombreJugador, int posicionCartaPropia) throws RemoteException {
        buscarJugador(nombreJugador).aplicarBonificacionColor(posicionCartaPropia, this);
        notificarObservadores(null);
    }

    @Override
    public synchronized void aplicarBonificacionColorDoble (String nombreJugador, int posicionCartaPropia) throws RemoteException {
        Jugador jugador = buscarJugador(nombreJugador);
        jugador.aplicarBonificacionColor(posicionCartaPropia, this);
        cartaExtraAlosDemasBonificacionColor(jugador);
        notificarObservadores(null);
    }

    // esto se usa cuando la doble con bonificacion de color deja al jugador sin cartas: no
    // tiene ninguna para poner en la fila central, pero los demas igual levantan del mazo
    @Override
    public synchronized void darCartaExtraAOtrosPorBonificacion (String nombreJugador) throws RemoteException {
        Jugador jugador = buscarJugador(nombreJugador);
        cartaExtraAlosDemasBonificacionColor(jugador);
        notificarObservadores(null);
    }

    @Override
    public synchronized boolean terminarTurno (String nombreJugador) throws RemoteException {
        if (!esTurnoDe(nombreJugador)){
            return false;
        }
        Jugador jugador = buscarJugador(nombreJugador);
        int cartasRestantes = jugador.cuantasCartasLeQuedan();

        // el DOS ya no se anuncia solo, si no lo cantaste te arriesgas a que te agarren
        if (cartasRestantes != 2){
            jugador.resetearDosCantado();
        }

        if (cartasRestantes == 0){
            finalizarRonda();
            if (partidaTerminada){
                return true; // ya se notificó el fin de la partida en finalizarRonda()
            }
        }

        avanzarTurno();
        notificarObservadores("Turno de: " + getJugadorEnTurno());
        return true;
    }

    // solo funciona si de verdad tiene 2 cartas en ese momento, para que no se pueda
    // "cantar DOS" en falso como para curarse por si te llegan a acusar despues
    @Override
    public synchronized boolean cantarDos (String nombreJugador) throws RemoteException {
        Jugador jugador = buscarJugador(nombreJugador);
        if (jugador.cuantasCartasLeQuedan() != 2){
            return false;
        }
        jugador.marcarDosCantado();
        notificarObservadores(nombreJugador + " cantó ¡¡DOS!!");
        return true;
    }

    // si la acusacion es correcta, el acusado roba 2 de penalizacion. si es incorrecta
    // (ya habia cantado, o no tiene 2) no pasa nada mas que quedar la acusacion publica
    @Override
    public synchronized boolean acusarDos (String nombreAcusador, String nombreAcusado) throws RemoteException {
        Jugador acusado = buscarJugador(nombreAcusado);

        if (acusado.cuantasCartasLeQuedan() == 2 && !acusado.isDosCantado()){
            for (int i = 0; i < 2; i++){
                Carta carta = mazoDeCartas.robarCarta(this);
                if (carta != null){
                    acusado.agregarCartas(carta);
                }
            }
            acusado.resetearDosCantado();
            notificarObservadores(nombreAcusador + " agarró a " + nombreAcusado + " sin cantar DOS: roba 2 cartas de penalización.");
            return true;
        }

        notificarObservadores(nombreAcusador + " acusó a " + nombreAcusado + " de no cantar DOS, pero la acusación fue incorrecta.");
        return false;
    }

    // nombres nomas, informacion publica, esto NO incluye las manos de nadie
    @Override
    public synchronized List<String> getNombresJugadores (){
        List<String> nombres = new ArrayList<>();
        for (Jugador jugador : jugadores){
            nombres.add(jugador.getNombre());
        }
        return nombres;
    }

    @Override
    public String getRankingTexto (){
        return Ranking.formatoTexto();
    }

    // lógica interna, nada de esto es remoto

    private void crearPartida () throws RemoteException {
        mazoDeCartas.mezclarMazo();
        mazoDeCartas.repartirMazo(this);
        mazoDeCartas.ponerDosCartasSobreMesa(this);
    }

    private void finalizarRonda () throws RemoteException {
        Jugador ganadorRonda = asignarPuntaje();
        notificarObservadores("Ganó la ronda: " + ganadorRonda.getNombre() + " (" + ganadorRonda.getPuntos() + " puntos totales)");
        reiniciarRonda();
        numeroRonda++;

        if (algunJugadorLlegoA200()){
            Jugador ganador = quienLlegoA200();
            partidaTerminada = true;
            ganadorPartida = ganador.getNombre();
            Ranking.registrarResultado(ganador.getNombre(), ganador.getPuntos());
            notificarObservadores("GANADOR DE LA PARTIDA: " + ganadorPartida + "\n" + Ranking.formatoTexto());
        }
    }

    // para jugar otra sin tener que levantar el servidor de nuevo. el chequeo de abajo
    // es para que no se pise si dos clientes confirman "jugar otra" casi al mismo tiempo
    @Override
    public synchronized void reiniciarPartida () throws RemoteException {
        if (!jugadores.isEmpty() && !partidaTerminada){
            return; // ya hay una partida nueva en marcha, no hay que reiniciar de nuevo
        }
        jugadores.clear();
        cartasEnMesa.clear();
        mazoDescarte.clear();
        mazoDeCartas.getMazoDeCartas().clear();
        mazoDeCartas.construirMazo();
        indiceJugadorEnTurno = 0;
        numeroRonda = 1;
        partidaTerminada = false;
        ganadorPartida = null;
        notificarObservadores("Se está armando una partida nueva...");
    }

    private Jugador asignarPuntaje (){
        int puntajeAcumulado = 0;
        Jugador ganadorRonda = null;
        for (Jugador jugador : jugadores){
            if (jugador.getCartasEnPosesion().isEmpty()){
                ganadorRonda = jugador;
            } else {
                for (Carta carta : jugador.getCartasEnPosesion()){
                    if (carta.getNumeroCarta() == 0){
                        puntajeAcumulado += 40;
                    } else if (carta.getColorCarta() == ColorCarta.MULTICOLOR){
                        puntajeAcumulado += 20;
                    } else {
                        puntajeAcumulado += carta.getNumeroCarta();
                    }
                }
            }
        }
        ganadorRonda.sumarPuntos(puntajeAcumulado);
        return ganadorRonda;
    }

    private void reiniciarRonda () throws RemoteException {
        for (Jugador jugador : jugadores){
            mazoDescarte.addAll(jugador.getCartasEnPosesion());
            jugador.getCartasEnPosesion().clear();
        }
        mazoDescarte.addAll(cartasEnMesa);
        cartasEnMesa.clear();
        mazoDeCartas.anadirDescarte(this);
        mazoDeCartas.repartirMazo(this);
        mazoDeCartas.ponerDosCartasSobreMesa(this);
    }

    private boolean algunJugadorLlegoA200 (){
        for (Jugador jugador : jugadores){
            if (jugador.getPuntos() >= 200){
                return true;
            }
        }
        return false;
    }

    private Jugador quienLlegoA200 (){
        for (Jugador jugador : jugadores){
            if (jugador.getPuntos() >= 200){
                return jugador;
            }
        }
        return null;
    }

    private void cartaExtraAlosDemasBonificacionColor (Jugador jugadorBonificado) throws RemoteException {
        for (Jugador jugador : jugadores){
            if (jugador != jugadorBonificado){
                Carta carta = mazoDeCartas.robarCarta(this);
                if (carta != null){
                    jugador.agregarCartas(carta);
                }
            }
        }
    }

    // solo repone si la mesa quedo con menos de 2, no reemplaza 1 a 1 automatico
    // despues de cada combinacion (asi la mesa no crece sola cada vez que combinas bien)
    private void reponerMesaSiFalta () throws RemoteException {
        while (cartasEnMesa.size() < 2 && (!mazoDeCartas.getMazoDeCartas().isEmpty() || !mazoDescarte.isEmpty())){
            mazoDeCartas.ponerUnaCartaSobreMesa(this);
        }
    }

    private void avanzarTurno (){
        indiceJugadorEnTurno = (indiceJugadorEnTurno + 1) % jugadores.size();
    }

    private boolean esTurnoDe (String nombre){
        return !jugadores.isEmpty() && jugadores.get(indiceJugadorEnTurno).getNombre().equals(nombre);
    }

    private Jugador buscarJugador (String nombre) throws RemoteException {
        for (Jugador jugador : jugadores){
            if (jugador.getNombre().equals(nombre)){
                return jugador;
            }
        }
        throw new RemoteException("Jugador no encontrado: " + nombre);
    }

    // estos son de uso interno para Mazo/Jugador, no estan en IModelo asi que ningun cliente los puede llamar

    public ArrayList<Jugador> getJugadores (){
        return jugadores;
    }

    public ArrayList<Carta> getMazoDescarte (){
        return mazoDescarte;
    }

    public Mazo getMazoDeCartas (){
        return mazoDeCartas;
    }

    public void agregarCartaAMesa (Carta carta){
        cartasEnMesa.add(carta);
    }

    // esta es la mesa real, no la copia que devuelve getCartasEnMesa() para los
    // clientes remotos. Jugador usa esta cuando ejecuta una combinacion.
    public Carta quitarCartaDeMesa (int posicion){
        return cartasEnMesa.remove(posicion);
    }

    public void agregarCartaADescarte (Carta carta){
        mazoDescarte.add(carta);
    }
}
