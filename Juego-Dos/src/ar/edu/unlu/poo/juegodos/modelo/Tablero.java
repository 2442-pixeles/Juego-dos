package ar.edu.unlu.poo.juegodos.modelo;

import java.util.ArrayList;

public class Tablero {
    private ArrayList<Jugador> jugadores;
    private ArrayList<Carta> cartasEnMesa;
    private ArrayList<Carta> mazoDescarte;
    private Mazo mazoDeCartas;



    public Tablero (){
        this.jugadores = new ArrayList<>();
        this.cartasEnMesa = new ArrayList<>();
        this.mazoDescarte = new ArrayList<>();
        Mazo nuevoMazo = new Mazo();
        nuevoMazo.construirMazo();
        mazoDeCartas = nuevoMazo;
    }

    public ArrayList<Carta> getMazoDescarte() { return mazoDescarte; }

    public Mazo getMazoDeCartas() {
        return mazoDeCartas;
    }

    public ArrayList<Carta> getCartasEnMesa() {
        return cartasEnMesa;
    }

    public ArrayList<Jugador> getJugadores() {
        return jugadores;
    }

    public void agregarCartaAMesa(Carta carta) {
        cartasEnMesa.add(carta);
    }

    public void agregarCartaADescarte(Carta carta) {
        mazoDescarte.add(carta);
    }

    public void agregarJugador (Jugador jugador){
        jugadores.add(jugador);
    }

    public void crearPartida (){       /////static o final
        if (jugadores.size() >= 2 ){
            mazoDeCartas.mezclarMazo();
            mazoDeCartas.repartirMazo(this);
            mazoDeCartas.ponerDosCartasSobreMesa(this);
        }
        else {
            System.out.println("jugadores insuficientes");
        }
    }

    public void mostrarCartasEnMesa(){
        for (Carta carta: cartasEnMesa){

        }
    }

    public void cartaExtraAlosDemasBonificacionColor(Jugador jugadorBonificado){
        for (Jugador jugador:this.getJugadores()){
            if (jugador != jugadorBonificado){
                jugador.agregarCartas(this.getMazoDeCartas().robarCarta(this));
            }
        }
    }

    public boolean algunJugadorLlegoA200 (){
        boolean resultado = false;
        for (Jugador jugador:this.getJugadores()){
            if (jugador.getPuntos()>=200){
                resultado = true;
            }
        }
        return resultado;
    }

    public Jugador quienLlegoA200 (){
        Jugador resultado = null;
        for (Jugador jugador:this.getJugadores()){
            if (jugador.getPuntos()>=200){
                resultado = jugador;
            }
        }
        return resultado;
    }



    public Jugador asignarPuntaje(){
        int puntajeAcumulado = 0;
        Jugador ganadorRonda = null;
        for (Jugador jugador:this.getJugadores()){
            if (jugador.getCartasEnPosesion().isEmpty()){
                ganadorRonda = jugador;
            }
            else {
                for(Carta carta:jugador.getCartasEnPosesion()){
                    if (carta.getNumeroCarta()==0){
                        puntajeAcumulado += 40;
                    }
                    else if (carta.getColorCarta()==ColorCarta.MULTICOLOR){
                        puntajeAcumulado += 20;
                    }
                    else {puntajeAcumulado += carta.getNumeroCarta();}
                }
            }
        }
        ganadorRonda.sumarPuntos(puntajeAcumulado);
        return ganadorRonda;
    }

    public Jugador obtenerSiguienteJugador(Jugador jugadorActual) {
        int indiceActual = jugadores.indexOf(jugadorActual);
        int indiceSiguiente = (indiceActual + 1) % jugadores.size();
        return jugadores.get(indiceSiguiente);
    }

    public void reiniciarRonda (){
        for (Jugador jugador:this.getJugadores()){
            this.mazoDescarte.addAll(jugador.getCartasEnPosesion());
            jugador.getCartasEnPosesion().clear();
            }
        this.mazoDescarte.addAll(this.cartasEnMesa);
        this.cartasEnMesa.clear();
        this.getMazoDeCartas().anadirDescarte(this);
        mazoDeCartas.repartirMazo(this);
        mazoDeCartas.ponerDosCartasSobreMesa(this);
    }



}
