package ar.edu.unlu.poo.juegodos.modelo;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {

        Jugador leandro = new Jugador("LEANDRO");
        Jugador jeremias = new Jugador("JEREMIAS");


        Tablero nuevoJuego = new Tablero();

        nuevoJuego.agregarJugador(leandro);
        nuevoJuego.agregarJugador(jeremias);

        nuevoJuego.crearPartida();

        Jugador jugadorActual = nuevoJuego.getJugadores().get(0);
        int ronda = 1;
        int retornoJugada = -1;


        while (!nuevoJuego.algunJugadorLlegoA200()){


            while (retornoJugada != 0){
                System.out.println("Ronda numero: "+ronda+", jugador: "+ jugadorActual.getNombre());
                retornoJugada = jugadorActual.jugarTurno(nuevoJuego)
                ;

                if ((retornoJugada == 2) ){
                    System.out.println("EL JUGADOR  " + jugadorActual.getNombre() + " A CANTADO ¡¡DOS!! \n");

                }

                jugadorActual=nuevoJuego.obtenerSiguienteJugador(jugadorActual);

            }

            Jugador ganadorRonda = nuevoJuego.asignarPuntaje();
            System.out.println("ESTA RONDA LA GANO: \n"+ ganadorRonda.getNombre());
            System.out.println("Y su cantidad de puntos es: "+ ganadorRonda.getPuntos()+"\n");
            nuevoJuego.reiniciarRonda();
            ronda +=1;
            retornoJugada = -1;


        }
        Jugador ganadorPartida = nuevoJuego.quienLlegoA200();
        System.out.println("EL GANADOR ES: \n"+ ganadorPartida.getNombre());
        System.out.println("CON LA CANTIDAD DE "+ ganadorPartida.getPuntos()+" PUNTOS");








    }
}