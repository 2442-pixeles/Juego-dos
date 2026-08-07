package ar.edu.unlu.poo.juegodos.controlador;

import ar.edu.unlu.poo.juegodos.modelo.Carta;
import ar.edu.unlu.poo.juegodos.modelo.IModelo;
import ar.edu.unlu.poo.juegodos.vista.IVista;
import ar.edu.unlu.poo.rmimvc.cliente.IControladorRemoto;
import ar.edu.unlu.poo.rmimvc.observer.IObservableRemoto;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Controlador implements IControladorRemoto {

    private final IVista vista;
    private final String nombreJugador;
    private final Object cerrojo = new Object();

    private IModelo modelo;
    private List<Carta> cartasElegibles; // cartas de la mesa contra las que se puede combinar EN EL TURNO ACTUAL

    public Controlador (IVista vista, String nombreJugador){
        this.vista = vista;
        this.nombreJugador = nombreJugador;
    }

    // ==================== IControladorRemoto / IObservadorRemoto ====================

    @Override
    public <T extends IObservableRemoto> void setModeloRemoto (T modeloRemoto){
        this.modelo = (IModelo) modeloRemoto;
    }

    // esto lo llama la libreria cada vez que el Tablero notifica algo. "o" es el
    // mensaje que el Tablero mando (o null si es solo un "algo cambio" sin nada para decir)
    @Override
    public void actualizar (IObservableRemoto observable, Object o){
        if (o instanceof String && !((String) o).isEmpty()){
            vista.mostrarMensaje((String) o);
        }
        synchronized (cerrojo){
            cerrojo.notifyAll();
        }
    }

    // ==================== Flujo del juego ====================

    public void registrarse () throws RemoteException {
        modelo.registrarJugador(nombreJugador);
    }

    // en un hilo aparte pq si lo hago directo desde el boton (que corre en el
    // Event Dispatch Thread) la ventana se me congela hasta que responda el server
    public void verRankingAhora (){
        new Thread(() -> {
            try {
                vista.mostrarMensaje(modelo.getRankingTexto());
            } catch (RemoteException e){
                vista.mostrarMensaje("No se pudo consultar el ranking: " + e.getMessage());
            }
        }).start();
    }

    public void salirDelJuego (){
        System.exit(0);
    }

    // ojo: esto no avisa a los demas clientes, la partida queda "pausada" del lado
    // del servidor pero los otros se quedan esperando el turno hasta que salgan tambien
    public void guardarYSalir (){
        try {
            modelo.guardarPartida();
            vista.mostrarMensaje("Partida guardada. Podés continuarla la próxima vez que arranques el servidor.");
        } catch (RemoteException e){
            vista.mostrarMensaje("No se pudo guardar la partida: " + e.getMessage());
        }
        System.exit(0);
    }

    // espera su turno, juega, y al final pregunta si quiere otra.
    // limitacion: no hay coordinacion entre clientes, si uno dice que no y se va
    // el otro se queda esperando un segundo jugador que nunca llega. no me dio
    // el tiempo para armar un lobby que sincronice esa decision entre los dos
    public void jugar () throws RemoteException {
        boolean seguirJugando = true;

        while (seguirJugando){
            jugarUnaPartida();

            seguirJugando = vista.pedirSiNo("¿Querés jugar otra partida?");
            if (seguirJugando){
                modelo.reiniciarPartida();
                registrarse();
            }
        }

        vista.mostrarMensaje("¡Gracias por jugar!");
    }

    private void jugarUnaPartida () throws RemoteException {
        vista.mostrarMensaje("Conectado. Esperando a los demás jugadores...");

        while (!modelo.partidaCompleta()){
            esperarNotificacion();
        }

        while (!modelo.partidaTerminada()){
            if (nombreJugador.equals(modelo.getJugadorEnTurno())){
                jugarMiTurno();
            } else {
                esperarNotificacion();
            }
        }

        vista.mostrarMensaje("Partida finalizada.");
    }

    private void esperarNotificacion () {
        synchronized (cerrojo){
            try {
                cerrojo.wait(2000); // el timeout es solo una red de seguridad por si se pierde algún notify
            } catch (InterruptedException ignored){
            }
        }
    }

    private void jugarMiTurno () throws RemoteException {
        vista.mostrarTurno(true);
        ofrecerAcusarDos();

        List<Carta> mesaAlEmpezar = modelo.getCartasEnMesa();
        vista.mostrarCartas("Cartas en mesa:", mesaAlEmpezar);
        // Congelamos esta lista: solo contra estas cartas se puede combinar
        // este turno, aunque durante el turno se agreguen otras a la mesa
        // (por una bonificación, un descarte, o una reposición del mazo).
        cartasElegibles = new ArrayList<>(mesaAlEmpezar);

        List<Carta> miMano = modelo.getMiMano(nombreJugador);
        if (miMano.isEmpty()){
            modelo.terminarTurno(nombreJugador);
            vista.mostrarTurno(false);
            return;
        }

        vista.mostrarCartas("Tus cartas:", miMano);

        int opcion;
        if (hayJugadaValidaDisponible(miMano, cartasElegibles)){
            opcion = vista.pedirOpcion("1. Robar carta del mazo\n2. Crear combinaciones", 1, 2);
        } else {
            vista.mostrarMensaje("No tenés ninguna combinación posible contra la mesa actual: hay que robar del mazo.");
            opcion = 1;
        }

        if (opcion == 1){
            accionIrAlMazo();
        } else {
            crearCombinacion();
        }

        if (modelo.cuantasCartasLeQuedanDe(nombreJugador) == 2){
            boolean cantar = vista.pedirSiNo("Te quedan 2 cartas. ¿Querés cantar DOS?");
            if (cantar){
                modelo.cantarDos(nombreJugador);
            }
        }

        modelo.terminarTurno(nombreJugador);
        vista.mostrarTurno(false);
    }

    // en tu turno te avisa si alguien tiene 2 cartas, para que puedas acusarlo.
    // no sabes si ya canto o no, te arriesgas
    private void ofrecerAcusarDos () throws RemoteException {
        for (String nombre : modelo.getNombresJugadores()){
            if (nombre.equals(nombreJugador)){
                continue;
            }
            if (modelo.cuantasCartasLeQuedanDe(nombre) == 2){
                boolean acusar = vista.pedirSiNo(nombre + " tiene 2 cartas en la mano. ¿Querés acusarlo de no haber cantado DOS?");
                if (acusar){
                    boolean exito = modelo.acusarDos(nombreJugador, nombre);
                    if (exito){
                        vista.mostrarMensaje("¡Acusación correcta! " + nombre + " roba 2 cartas de penalización.");
                    } else {
                        vista.mostrarMensaje("Acusación incorrecta: " + nombre + " ya había cantado DOS (o ya no tiene 2 cartas).");
                    }
                }
            }
        }
    }

    /** Chequeo puro (sin tocar el modelo): ¿hay alguna combinación simple o doble posible con lo que hay ahora? */
    private boolean hayJugadaValidaDisponible (List<Carta> mano, List<Carta> mesa){
        for (Carta cartaMesa : mesa){
            for (Carta cartaMano : mano){
                if (esCombinacionSimpleValida(cartaMano, cartaMesa)){
                    return true;
                }
            }
            for (int i = 0; i < mano.size(); i++){
                for (int j = i + 1; j < mano.size(); j++){
                    if (esCombinacionDobleValida(mano.get(i), mano.get(j), cartaMesa)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean esCombinacionSimpleValida (Carta cartaMano, Carta cartaMesa){
        return cartaMano.mismoNumero(cartaMesa) || cartaMano.algunaEsComodin(cartaMesa);
    }

    private boolean esCombinacionDobleValida (Carta carta1, Carta carta2, Carta cartaMesa){
        int suma = carta1.getNumeroCarta() + carta2.getNumeroCarta();
        boolean unaEsComodin = carta1.getComodin() || carta2.getComodin();

        if (cartaMesa.getComodin() && suma < 10){
            return true;
        }
        if (unaEsComodin && suma < cartaMesa.getNumeroCarta()){
            return true;
        }
        return !unaEsComodin && suma == cartaMesa.getNumeroCarta();
    }

    private void accionIrAlMazo () throws RemoteException {
        modelo.robarCarta(nombreJugador);
        vista.mostrarMensaje("Ahora sus cartas son las siguientes:");
        actualizarEstadoVisible();

        boolean juega = vista.pedirSiNo("¿Desea jugar la carta que acaba de robar en vez de saltear el turno?");

        if (!juega){
            List<Carta> miMano = modelo.getMiMano(nombreJugador);
            vista.mostrarMensaje("Debe descartarse de una carta. Ingrese la posición:");
            vista.mostrarCartas("Tus cartas:", miMano);
            int posicion = vista.pedirOpcion("Posición a descartar:", 0, miMano.size() - 1);
            modelo.descartarCarta(nombreJugador, posicion);
            actualizarEstadoVisible();
        } else {
            crearCombinacion();
        }
    }

    /** Vuelve a pedirle al modelo el estado actual y lo muestra, para que la pantalla nunca se quede desactualizada. */
    private void actualizarEstadoVisible () throws RemoteException {
        vista.mostrarCartas("Cartas en mesa:", modelo.getCartasEnMesa());
        vista.mostrarCartas("Tus cartas:", modelo.getMiMano(nombreJugador));
    }

    // solo se puede combinar contra las cartas que estaban en mesa al arrancar TU turno,
    // nunca contra algo que se agrego durante el mismo turno. y una vez que probaste
    // con una carta de mesa (te haya salido bien o mal) ya no la podes reintentar
    private void crearCombinacion () throws RemoteException {
        boolean seguir = true;
        while (seguir){
            if (cartasElegibles.isEmpty()){
                vista.mostrarMensaje("Ya no quedan cartas (de las que había al empezar tu turno) para combinar.");
                break;
            }

            vista.mostrarCartas("Cartas de la mesa contra las que podés combinar este turno:", cartasElegibles);
            boolean quiereCombinar = vista.pedirSiNo("¿Desea intentar una combinación contra alguna de esas cartas?");
            if (!quiereCombinar){
                break;
            }

            int indiceElegible = vista.pedirOpcion("¿Contra cuál? (posición en esta lista)", 0, cartasElegibles.size() - 1);
            Carta cartaElegida = cartasElegibles.remove(indiceElegible); // se saca ya, se intente o no la combinación

            int posMesaReal = ubicarEnMesaActual(cartaElegida);
            if (posMesaReal == -1){
                vista.mostrarMensaje("Esa carta ya no está en la mesa.");
                seguir = vista.pedirSiNo("¿Desea intentar otra combinación?");
                continue;
            }

            int tipo = vista.pedirOpcion("1. Combinación simple\n2. Combinación doble", 1, 2);
            boolean valida = (tipo == 1) ? intentarSimple(posMesaReal) : intentarDoble(posMesaReal);
            if (!valida){
                vista.mostrarMensaje("Combinación inválida, perdió la oportunidad con esa carta.");
            }

            seguir = vista.pedirSiNo("¿Desea intentar otra combinación?");
        }
    }

    /** Busca en la mesa REAL (fresca) la carta cuyo id coincide con la elegida, ya que las posiciones se mueven. */
    private int ubicarEnMesaActual (Carta cartaBuscada) throws RemoteException {
        List<Carta> mesaActual = modelo.getCartasEnMesa();
        for (int i = 0; i < mesaActual.size(); i++){
            if (mesaActual.get(i).getId() == cartaBuscada.getId()){
                return i;
            }
        }
        return -1;
    }

    private boolean intentarSimple (int posMesa) throws RemoteException {
        List<Carta> mano = modelo.getMiMano(nombreJugador);
        if (mano.isEmpty()){
            vista.mostrarMensaje("No tiene cartas.");
            return false;
        }

        vista.mostrarCartas("Tus cartas:", mano);
        int posMano = vista.pedirOpcion("Posición de su carta:", 0, mano.size() - 1);
        Carta miCarta = mano.get(posMano);
        Carta cartaMesa = modelo.getCartasEnMesa().get(posMesa);

        boolean valida = modelo.intentarCombinacionSimple(nombreJugador, posMano, posMesa);
        if (valida && miCarta.coincideColorOMulticolor(cartaMesa)){
            aplicarBonificacionSimple();
        }
        actualizarEstadoVisible();
        return valida;
    }

    private boolean intentarDoble (int posMesa) throws RemoteException {
        List<Carta> mano = modelo.getMiMano(nombreJugador);
        if (mano.size() < 2){
            vista.mostrarMensaje("No tiene suficientes cartas.");
            return false;
        }

        vista.mostrarCartas("Tus cartas: elegí la PRIMERA para la combinación doble:", mano);
        int pos1 = vista.pedirOpcion("Elegí la primera carta:", 0, mano.size() - 1);
        Carta carta1 = mano.get(pos1);

        List<Carta> manoSinPrimera = new ArrayList<>(mano);
        manoSinPrimera.remove(pos1);

        vista.mostrarCartas("Tus cartas: elegí la SEGUNDA para la combinación doble:", manoSinPrimera);
        int indiceEnResto = vista.pedirOpcion("Elegí la segunda carta:", 0, manoSinPrimera.size() - 1);
        Carta carta2 = manoSinPrimera.get(indiceEnResto);

        // carta2 viene de una copia sin la primera, así que hay que volver a
        // ubicar su posición real en "mano" (Carta.equals compara por id,
        // así que esto funciona aunque sean copias serializadas distintas).
        int pos2 = mano.indexOf(carta2);

        Carta cartaMesa = modelo.getCartasEnMesa().get(posMesa);

        boolean valida = modelo.intentarCombinacionDoble(nombreJugador, pos1, pos2, posMesa);
        if (valida && hayBonificacionColorDoble(carta1, carta2, cartaMesa)){
            aplicarBonificacionDoble();
        }
        actualizarEstadoVisible();
        return valida;
    }

    private void aplicarBonificacionSimple () throws RemoteException {
        List<Carta> manoRestante = modelo.getMiMano(nombreJugador);
        if (manoRestante.isEmpty()){
            vista.mostrarMensaje("¡Bonificación de color! Ya no te quedan cartas para poner en la mesa (en la simple no hay otro efecto además de eso).");
            return;
        }
        vista.mostrarMensaje("Hizo una bonificación de color, elija una carta para la fila central:");
        vista.mostrarCartas("Tus cartas:", manoRestante);
        int posBonus = vista.pedirOpcion("Posición:", 0, manoRestante.size() - 1);
        modelo.aplicarBonificacionColor(nombreJugador, posBonus);
    }

    private void aplicarBonificacionDoble () throws RemoteException {
        List<Carta> manoRestante = modelo.getMiMano(nombreJugador);
        if (manoRestante.isEmpty()){
            // No le queda ninguna carta para poner en la mesa, pero el resto
            // de los jugadores igual debe levantar una carta del mazo.
            vista.mostrarMensaje("¡Bonificación de color! Ya no te quedan cartas para poner en la mesa, pero los demás jugadores levantan una carta del mazo igual.");
            modelo.darCartaExtraAOtrosPorBonificacion(nombreJugador);
            return;
        }
        vista.mostrarMensaje("Hizo una bonificación de color, elija una carta para la fila central:");
        vista.mostrarCartas("Tus cartas:", manoRestante);
        int posBonus = vista.pedirOpcion("Posición:", 0, manoRestante.size() - 1);
        modelo.aplicarBonificacionColorDoble(nombreJugador, posBonus);
    }

    private boolean hayBonificacionColorDoble (Carta c1, Carta c2, Carta cartaMesa){
        if (c1.getColorCarta() == c2.getColorCarta() && c2.getColorCarta() == cartaMesa.getColorCarta()){
            return true;
        }
        if (c1.getColorCarta() == c2.getColorCarta() && c2.esMulticolor()){
            return true;
        }
        if (c1.getColorCarta() == cartaMesa.getColorCarta() && c2.esMulticolor()){
            return true;
        }
        if (c2.getColorCarta() == cartaMesa.getColorCarta() && c1.esMulticolor()){
            return true;
        }
        return false;
    }
}
