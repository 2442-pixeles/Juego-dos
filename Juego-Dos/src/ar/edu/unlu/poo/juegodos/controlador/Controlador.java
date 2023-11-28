package ar.edu.unlu.poo.juegodos.controlador;

import ar.edu.unlu.poo.juegodos.modelo.*;
import ar.edu.unlu.poo.juegodos.vista.IVista;

public class Controlador {
    private final IVista vista;
    private Tablero modelo;

    public Controlador(IVista vista) {
        this.vista = vista;
        vista.setControlador(this);
    }

    public void setModelo(Tablero modelo) {
        this.modelo = modelo;
    }
}
