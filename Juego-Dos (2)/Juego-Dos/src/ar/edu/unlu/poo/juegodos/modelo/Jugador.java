package ar.edu.unlu.poo.juegodos.modelo;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Jugador implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private ArrayList<Carta> cartasEnPosesion;
    private int puntos;
    private boolean dosCantado;

    public Jugador (String nombre){
        this.cartasEnPosesion = new ArrayList<>();
        this.nombre = nombre;
        this.puntos = 0;
        this.dosCantado = false;
    }

    public boolean isDosCantado (){
        return dosCantado;
    }

    public void marcarDosCantado (){
        this.dosCantado = true;
    }

    public void resetearDosCantado (){
        this.dosCantado = false;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public int getPuntos() {
        return puntos;
    }

    public void sumarPuntos (int cantASumar){
        this.puntos = puntos + cantASumar;
    }

    public ArrayList<Carta> getCartasEnPosesion() {
        return cartasEnPosesion;
    }

    public void agregarCartas(ArrayList<Carta> cartas) {
        cartasEnPosesion.addAll(cartas);
    }

    public void agregarCartas (Carta carta){cartasEnPosesion.add(carta);}

    public int cuantasCartasLeQuedan (){
        int resultado = -1;
        if (this.getCartasEnPosesion().size()==2){
            resultado = 2;
        }
        if (this.getCartasEnPosesion().isEmpty()){
            resultado = 0;
        }
        return resultado;
    }

    public void descartarCartaAMesa (int posicion, Tablero tablero) throws RemoteException {
        tablero.agregarCartaAMesa(this.cartasEnPosesion.remove(posicion));
    }

    // si es valida ejecuta el movimiento y devuelve true, si no, no toca nada y devuelve false
    public boolean intentarCombinacionSimple (int posCartaPropia, Carta cartaMesa, int posCartaMesa, Tablero tablero) throws RemoteException {
        Carta cartaSeleccionada = this.cartasEnPosesion.get(posCartaPropia);

        boolean esValida = cartaSeleccionada.mismoNumero(cartaMesa) || cartaSeleccionada.algunaEsComodin(cartaMesa);
        if (!esValida) {
            return false;
        }

        tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(posCartaPropia));
        tablero.agregarCartaADescarte(tablero.quitarCartaDeMesa(posCartaMesa));
        return true;
    }

    public boolean intentarCombinacionDoble (int pos1, int pos2, Carta cartaMesa, int posCartaMesa, Tablero tablero) throws RemoteException {
        Carta carta1 = this.cartasEnPosesion.get(pos1);
        Carta carta2 = this.cartasEnPosesion.get(pos2);

        int suma = carta1.getNumeroCarta() + carta2.getNumeroCarta();
        boolean unaEsComodin = carta1.getComodin() || carta2.getComodin();

        boolean esValida;
        if (cartaMesa.getComodin() && suma < 10) {
            esValida = true;
        } else if (unaEsComodin && suma < cartaMesa.getNumeroCarta()) {
            esValida = true;
        } else if (!unaEsComodin && suma == cartaMesa.getNumeroCarta()) {
            esValida = true;
        } else {
            esValida = false;
        }

        if (!esValida) {
            return false;
        }

        if (pos1 < pos2) {
            tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos2));
            tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos1));
        } else {
            tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos1));
            tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos2));
        }

        tablero.agregarCartaADescarte(tablero.quitarCartaDeMesa(posCartaMesa));
        return true;
    }

    public void aplicarBonificacionColor (int posicionCartaPropia, Tablero tablero) throws RemoteException {
        tablero.agregarCartaAMesa(this.cartasEnPosesion.remove(posicionCartaPropia));
    }
}
