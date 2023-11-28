package ar.edu.unlu.poo.juegodos.modelo;

public class Carta {
    private ColorCarta colorCarta;
    private int numeroCarta;
    private boolean comodin;

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
        if (this.getColorCarta()==ColorCarta.MULTICOLOR){
            return true;
        }else return false;
    }

    public void describirCarta (){
        String tipoCarta;

        if (this.getComodin()) {
            tipoCarta = "Comodin";                                                  //deberia mandarle este texto al controlador, y este a la vista
        } else if (this.getColorCarta() == ColorCarta.MULTICOLOR) {
            tipoCarta = "Multicolor";
        } else {
            tipoCarta = "Normal";
        }

        System.out.println("Color: " + this.getColorCarta() +
                ", Número: " + this.getNumeroCarta() +
                ", Tipo: " + tipoCarta);

    }
}
