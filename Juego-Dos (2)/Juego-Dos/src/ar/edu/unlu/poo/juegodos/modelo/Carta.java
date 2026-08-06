package ar.edu.unlu.poo.juegodos.modelo;

import java.io.Serializable;

public class Carta implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contadorId = 0;

    private final int id;
    private ColorCarta colorCarta;
    private int numeroCarta;
    private boolean comodin;

    public Carta (){
        this.id = contadorId++;
    }

    public int getId (){
        return id;
    }

    // dos cartas son "la misma" si tienen el mismo id, aunque sean objetos distintos -
    // pasa todo el tiempo porque cada llamada remota devuelve una copia serializada nueva
    @Override
    public boolean equals (Object o){
        if (this == o) return true;
        if (!(o instanceof Carta)) return false;
        return this.id == ((Carta) o).id;
    }

    @Override
    public int hashCode (){
        return id;
    }

    public Carta crearCartaComun (ColorCarta colordeCarta, int numero){
        this.setColorCarta(colordeCarta);
        this.setNumeroCarta(numero);
        this.setComodin(false);
        return this;
    }

    public Carta crearCartaMulticolor (){
        this.setColorCarta(ColorCarta.MULTICOLOR);
        this.setNumeroCarta(2);
        this.setComodin(false);
        return this;
    }

    public Carta crearCartaComodin (ColorCarta colordeCarta){
        this.setColorCarta(colordeCarta);
        this.setNumeroCarta(0);
        this.setComodin(true);
        return this;
    }

    public void setColorCarta(ColorCarta colorCarta) {
        this.colorCarta = colorCarta;
    }

    public void setComodin(boolean comodin) {
        this.comodin = comodin;
    }

    public void setNumeroCarta(int numeroCarta) {
        this.numeroCarta = numeroCarta;
    }

    public ColorCarta getColorCarta() {
        return colorCarta;
    }

    public int getNumeroCarta() {
        return numeroCarta;
    }

    public boolean getComodin() { return comodin; }

    public boolean esMulticolor() {
        return this.getColorCarta() == ColorCarta.MULTICOLOR;
    }

    // antes esto imprimia directo por consola, ahora devuelve el texto nomas
    // y que la vista decida como mostrarlo
    public String descripcion (){
        String tipoCarta;
        if (this.getComodin()) {
            tipoCarta = "Comodin";
        } else if (this.getColorCarta() == ColorCarta.MULTICOLOR) {
            tipoCarta = "Multicolor";
        } else {
            tipoCarta = "Normal";
        }
        return "Color: " + this.getColorCarta() +
                ", Número: " + this.getNumeroCarta() +
                ", Tipo: " + tipoCarta;
    }

    // reglas de combinacion (antes estaban mezcladas adentro de Jugador)

    public boolean mismoNumero (Carta otra){
        return this.getNumeroCarta() == otra.getNumeroCarta();
    }

    public boolean algunaEsComodin (Carta otra){
        return this.getComodin() || otra.getComodin();
    }

    public boolean coincideColorOMulticolor (Carta otra){
        return this.getColorCarta() == otra.getColorCarta() || this.esMulticolor() || otra.esMulticolor();
    }
}
