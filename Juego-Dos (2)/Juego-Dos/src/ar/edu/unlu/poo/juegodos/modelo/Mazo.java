package ar.edu.unlu.poo.juegodos.modelo;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

public class Mazo {
    private ArrayList<Carta> mazoDeCartas;

    public Mazo(){
        this.mazoDeCartas = new ArrayList<>();
    }

    public ArrayList<Carta> getMazoDeCartas() {
        return mazoDeCartas;
    }

    public void construirMazo(){
        int k,j,i;

        for (k = 0;k < 4; k++){
            for (j = 1; j < 12; j++){
                Carta nueva = null;
                ColorCarta colorCartaSeleccionado = null;

                switch (k) {
                    case (0):
                        colorCartaSeleccionado = ColorCarta.AZUL;
                        break;
                    case (1):
                        colorCartaSeleccionado = ColorCarta.VERDE;
                        break;
                    case (2):
                        colorCartaSeleccionado = ColorCarta.ROJO;
                        break;
                    case (3):
                        colorCartaSeleccionado = ColorCarta.AMARILLO;
                        break;
                }

                if (j < 6) {
                    for (i = 0; i < 3; i++) {
                        nueva = new Carta();
                        if (j == 2) {
                            nueva.crearCartaMulticolor();
                        } else {
                            nueva.crearCartaComun(colorCartaSeleccionado, j);
                        }
                        mazoDeCartas.add (nueva);
                    }
                }

                if (j >= 6){
                    for (i = 0; i < 2; i++) {
                        nueva = new Carta();
                        if (j == 11) {
                            nueva.crearCartaComodin(colorCartaSeleccionado);
                        } else {
                            nueva.crearCartaComun(colorCartaSeleccionado, j);
                        }
                        mazoDeCartas.add (nueva);
                    }
                }
            }
        }
    }

    public void mezclarMazo(){
        Collections.shuffle(mazoDeCartas);
    }

    public void repartirMazo(Tablero tablero) throws RemoteException {
        ArrayList<Jugador> jugadores = tablero.getJugadores();
        for (Jugador jugador : jugadores) {
            ArrayList<Carta> cartasRepartidas = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (mazoDeCartas.size()>=1) {
                    cartasRepartidas.add(mazoDeCartas.remove(0));
                    if (mazoDeCartas.size()==0){
                        this.anadirDescarte(tablero);
                    }
                } else {
                    break;
                }
            }
            jugador.agregarCartas(cartasRepartidas);
        }
    }

    public Carta robarCarta(Tablero tablero) throws RemoteException {
        if (mazoDeCartas.size() >= 1) {
            Carta cartaRobada = mazoDeCartas.remove(0);
            if (mazoDeCartas.size() == 0){
                this.anadirDescarte(tablero);
            }
            return cartaRobada;
        } else {
            return null;
        }
    }

    public void robarCarta(Jugador jugador, Tablero tablero) throws RemoteException {
        if (mazoDeCartas.size() >= 1) {
            Carta cartaRobada = mazoDeCartas.remove(0);
            ArrayList<Carta> cartaLista = new ArrayList<>();
            cartaLista.add(cartaRobada);
            jugador.agregarCartas(cartaLista);
            if (mazoDeCartas.size() == 0){
                this.anadirDescarte(tablero);
            }
        }
    }

    public void ponerDosCartasSobreMesa(Tablero tablero) throws RemoteException {
        if (mazoDeCartas.size() >= 2) {
            Carta carta1 = mazoDeCartas.remove(0);
            Carta carta2 = mazoDeCartas.remove(0);
            tablero.agregarCartaAMesa(carta1);
            tablero.agregarCartaAMesa(carta2);
            if (mazoDeCartas.size() == 0){
                this.anadirDescarte(tablero);
            }
        }
    }

    public void ponerUnaCartaSobreMesa(Tablero tablero) throws RemoteException {
        if (mazoDeCartas.size() >= 1) {
            Carta carta = mazoDeCartas.remove(0);
            tablero.agregarCartaAMesa(carta);
            if (mazoDeCartas.size() == 0){
                this.anadirDescarte(tablero);
            }
        }
    }

    public void anadirDescarte(Tablero tablero) throws RemoteException {
        ArrayList<Carta> mazoDescarte = tablero.getMazoDescarte();
        Collections.shuffle(mazoDescarte);
        this.getMazoDeCartas().addAll(mazoDescarte);
        mazoDescarte.clear();
    }
}
