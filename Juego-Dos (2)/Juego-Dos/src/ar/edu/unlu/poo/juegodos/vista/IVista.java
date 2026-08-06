package ar.edu.unlu.poo.juegodos.vista;

import ar.edu.unlu.poo.juegodos.modelo.Carta;

import java.util.List;

// vista de CADA cliente, no es remota. el Controlador la usa para pedir inputs
// y mostrar info, tanto la propia (mano) como la publica (mesa)
public interface IVista {

    void mostrarMensaje (String mensaje);

    void mostrarCartas (String titulo, List<Carta> cartas);

    // avisa si es o no tu turno, para que la vista lo destaque de alguna forma
    void mostrarTurno (boolean esMiTurno);

    int pedirOpcion (String mensaje, int min, int max);

    boolean pedirSiNo (String mensaje);

    String pedirTexto (String mensaje);
}
