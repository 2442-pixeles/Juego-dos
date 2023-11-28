package ar.edu.unlu.poo.juegodos.modelo;

import java.util.ArrayList;
import java.util.Collections;

public class Mazo {
    private ArrayList<Carta> mazoDeCartas;

    public Mazo(){                                             //constuctor
        this.mazoDeCartas = new ArrayList<>();
    }

    public ArrayList<Carta> getMazoDeCartas() {
        return mazoDeCartas;
    }

    public void construirMazo(){
        int k,j,i;

        for (k = 0;k < 4; k++){             //selector de color
            for (j = 1; j < 12; j++){       //selector de numero
                Carta nueva = null;
                ColorCarta colorCartaSeleccionado = null;

                switch (k) {                       //se decodifica el color
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


                if (j < 6) {                                     //si el numero de la carta es menor a 6 hay que crear 3 cartas identicas de esa combinacion
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

                if (j >= 6){                                   //si el numero de la carta es mayor a 6 hay que crear 2 cartas identicas de esa combinacion
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

                                     //agrega la carta al mazo
            }
        }
    }

    public void mezclarMazo(){
        Collections.shuffle(mazoDeCartas);
    }


    public void repartirMazo(Tablero tablero) {
        ArrayList<Jugador> jugadores = tablero.getJugadores();
        // Itera sobre el array de jugadores del tablero
        for (Jugador jugador : jugadores) {
            // Saco del array mazo 7 cartas
            ArrayList<Carta> cartasRepartidas = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (mazoDeCartas.size()>=1) {
                    cartasRepartidas.add(mazoDeCartas.remove(0));
                    if (mazoDeCartas.size()==0){
                        System.out.println("Quedan pocas cartas en el mazo, se procede a mezclar la pila de descarte e integrarla al mazo");
                        this.anadirDescarte(tablero);
                    }
                } else {
                    System.out.println("No hay más cartas en el mazo.");
                    break;
                }
            }
            // al jugador actual del for, le agrego las 7 cartas que saqué
            jugador.agregarCartas(cartasRepartidas);
        }
    }

    public Carta robarCarta(Tablero tablero) {
        if (mazoDeCartas.size() >= 1) {
            Carta cartaRobada = mazoDeCartas.remove(0); // Saco del array mazo 1 carta
            if (mazoDeCartas.size() == 0){
                System.out.println("Quedan pocas cartas en el mazo, se procede a mezclar la pila de descarte e integrarla al mazo");
                this.anadirDescarte(tablero);
            }
            return cartaRobada;

        } else {
            System.out.println("No hay más cartas en el mazo.");
            return null;
        }
    }

    public void robarCarta(Jugador jugador,Tablero tablero) {
        if (mazoDeCartas.size() >= 1) {
            Carta cartaRobada = mazoDeCartas.remove(0); // Saco del array mazo 1 carta
            ArrayList<Carta> cartaLista = new ArrayList<>();
            cartaLista.add(cartaRobada);
            jugador.agregarCartas(cartaLista); // modelo.Jugador: array de cartas en posesión, le agrego la carta que saqué
            if (mazoDeCartas.size() == 0){
                System.out.println("Quedan pocas cartas en el mazo, se procede a mezclar la pila de descarte e integrarla al mazo");
                this.anadirDescarte(tablero);
            }
        } else {
            System.out.println("No hay más cartas en el mazo.");
        }
    }

    public void ponerDosCartasSobreMesa(Tablero tablero) {
        if (mazoDeCartas.size() >= 2) {
            Carta carta1 = mazoDeCartas.remove(0);
            Carta carta2 = mazoDeCartas.remove(0);
            tablero.agregarCartaAMesa(carta1);
            tablero.agregarCartaAMesa(carta2);
            if (mazoDeCartas.size() == 0){
                System.out.println("Quedan pocas cartas en el mazo, se procede a mezclar la pila de descarte e integrarla al mazo");
                this.anadirDescarte(tablero);
            }
        } else {
            System.out.println("No hay suficientes cartas en el mazo para poner dos en la mesa.");
        }
    }

    public void ponerUnaCartaSobreMesa(Tablero tablero) {
        if (mazoDeCartas.size() >= 1) {
            Carta carta = mazoDeCartas.remove(0);
            tablero.agregarCartaAMesa(carta);
            if (mazoDeCartas.size() == 0){
                System.out.println("Quedan pocas cartas en el mazo, se procede a mezclar la pila de descarte e integrarla al mazo");
                this.anadirDescarte(tablero);
            }

        } else {
            System.out.println("No hay suficientes cartas en el mazo para poner una en la mesa.");
        }
    }

    public void anadirDescarte(Tablero tablero){
        ArrayList<Carta> mazoDescarte = tablero.getMazoDescarte();
        Collections.shuffle(mazoDescarte);
        this.getMazoDeCartas().addAll(mazoDescarte);
    }






}
