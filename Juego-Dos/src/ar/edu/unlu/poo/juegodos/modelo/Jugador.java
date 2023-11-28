package ar.edu.unlu.poo.juegodos.modelo;

import java.util.ArrayList;
import java.util.Scanner;

public class Jugador {
    private String nombre;
    private ArrayList<Carta> cartasEnPosesion;
    private int puntos;

    public Jugador (String nombre){
        this.cartasEnPosesion = new ArrayList<>();
        this.nombre = nombre;
        this.puntos = 0;
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

    public void mostrarCartas() {
        System.out.println("Cartas en posesión del jugador: \n");
        int i = 0;
        for (Carta carta : cartasEnPosesion) {
            System.out.println("Posicion: " + i);
            carta.describirCarta();
            i+=1;
        }
        System.out.println("\n");
    }

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

    public int jugarTurno(Tablero tablero){
        int retorno = -1;
        System.out.println(" ");
        System.out.println("Las cartas en mesa son las siguientes: \n");

        for (Carta cartasEnMesa: tablero.getCartasEnMesa()){
            cartasEnMesa.describirCarta();
        }

        if (this.cuantasCartasLeQuedan()==0){                       //chequeo si le quedan 0 cartas
            return 0;
        }
        System.out.println(" ");
        this.mostrarCartas();

        System.out.println("1. Robar carta del mazo \n" + "2. Crear combinaciones \n ");

        Scanner scanner = new Scanner(System.in);
        int opcion = scanner.nextInt();
        scanner.nextLine(); // Consumir la nueva línea después de nextInt()

        switch (opcion){
            case (1):
                this.accionIrAlMazo(tablero);
                break;
            case (2):
                this.crearCombinacion(tablero);
                break;

        }
        if (this.cuantasCartasLeQuedan()==0){                       //chequeo si le quedan 0 cartas
            retorno = 0;
        }
        if (this.cuantasCartasLeQuedan()==2){                       //chequeo si le quedan 0 cartas
            retorno = 2 ;
        }
    return retorno;
    }


    public void accionIrAlMazo (Tablero tablero){
        Scanner scanner = new Scanner(System.in);
        boolean yaRobo = true;
        tablero.getMazoDeCartas().robarCarta(this,tablero);
        System.out.println("Ahora sus cartas son las siguientes: \n");
        this.mostrarCartas();
        System.out.println("desea saltear su turno o jugar? (0/1) \n");
        int saltarTurno = scanner.nextInt();
        scanner.nextLine(); // Consumir la nueva línea después de nextInt()
        if (saltarTurno == 0){
            System.out.println("como no usara la carta que levanto del mazo debe descartarse de una, ingrese la posicion de la misma  \n");
            this.mostrarCartas();
            int cartaADescartar = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea después de nextInt()
            tablero.agregarCartaAMesa(this.getCartasEnPosesion().remove(cartaADescartar));
        };
        if (saltarTurno == 1){
            this.crearCombinacion(tablero);
        }
    }

    public void crearCombinacion (Tablero tablero){
        Scanner scanner = new Scanner(System.in);
        ArrayList<Carta> copiaCartasEnMesa = new ArrayList<>(tablero.getCartasEnMesa()); //me copio el arraylist de cartas en mesa pq cuando hago juegos voy eliminando del original y cambirian los indices

        for (int i = 0; i < copiaCartasEnMesa.size(); i++){

            Carta cartaActualMesa = copiaCartasEnMesa.get(i);     //copio la carta actual de la mesa
            cartaActualMesa.describirCarta();                     //y la describo

            System.out.println("Desea crear combinaciones para esta carta (0/1) \n");
            int desicionCrearCombinacion = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea después de nextInt()

            if (desicionCrearCombinacion == 0){
                //no hago nada
            }
            else if (desicionCrearCombinacion == 1){
                System.out.println("1.Crear combinacion simple \n" + "2.Crear combinacion doble \n");
                int tipoCombinacion = scanner.nextInt();
                scanner.nextLine(); // Consumir la nueva línea después de nextInt()

                if (tipoCombinacion == 1){
                    this.combinacionSimple(tablero,cartaActualMesa,i); //llamo a hacer una combinacion simple
                }
                if (tipoCombinacion == 2){
                    this.combinacionDoble(tablero,cartaActualMesa,i);
                }

            }
        }

    }
    public void combinacionSimple (Tablero tablero,Carta cartaActualMesa,int i){
        boolean cartaValida = false;
        Scanner scanner = new Scanner(System.in);
        if (!this.getCartasEnPosesion().isEmpty()){
            System.out.println("Ingrese la posicion de la carta que quiere seleccionar \n");
            this.mostrarCartas();
            int opcionCarta = scanner.nextInt();
            scanner.nextLine(); // Consumir la nueva línea después de nextInt()
            Carta cartaSeleccionada = this.getCartasEnPosesion().get(opcionCarta);

            //Bloque de código común
            int finalI = i;

            Runnable codigoCartaValida = () -> {

                tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(opcionCarta)); //le saco la carta que jugo y la pongo al descarte
                tablero.agregarCartaADescarte(tablero.getCartasEnMesa().remove(finalI));  //saco la carta de la mesa con la que combino el jugador
                tablero.getMazoDeCartas().ponerUnaCartaSobreMesa(tablero); //repongo la carta que saque de la mesa

                if (cartaSeleccionada.getColorCarta() == cartaActualMesa.getColorCarta() || (cartaSeleccionada.esMulticolor() || cartaActualMesa.esMulticolor())) {
                    if (!this.getCartasEnPosesion().isEmpty()){
                        System.out.println("Hizo una bonificacion de color, ingrese la posicion de una de sus carta que pasara a la fila central \n");
                        this.mostrarCartas();
                        int cartaADescartar = scanner.nextInt();
                        scanner.nextLine(); // Consumir la nueva línea después de nextInt()
                        tablero.agregarCartaAMesa(this.getCartasEnPosesion().remove(cartaADescartar));
                    }
                }
            };

            if (cartaSeleccionada.getNumeroCarta()==cartaActualMesa.getNumeroCarta()){
                codigoCartaValida.run();
                cartaValida = true;
            }
            else if (cartaSeleccionada.getComodin()||cartaActualMesa.getComodin()){
                codigoCartaValida.run();
                cartaValida = true;
            }
            else {System.out.println("carta seleccionada invalida, perdio la oportunidad de hacer una combinacion para la carta en mesa \n");}

        }else{System.out.println("No tiene suficientes cartas \n");}
    }

    public void combinacionDoble (Tablero tablero,Carta cartaActualMesa,int i){
        boolean cartaValida = false;
        Scanner scanner = new Scanner(System.in);

        if (this.getCartasEnPosesion().size()>=2) {

            System.out.println("Ingrese las posiciones de las cartas que quiere seleccionar separadas por comas \n");
            this.mostrarCartas();

            String seleccion = scanner.nextLine();
            String[] opciones = seleccion.split(",");

            Carta cartaSeleccionada1 = this.getCartasEnPosesion().get(Integer.parseInt(opciones[0]));
            Carta cartaSeleccionada2 = this.getCartasEnPosesion().get(Integer.parseInt(opciones[1]));

            int suma = cartaSeleccionada1.getNumeroCarta() + cartaSeleccionada2.getNumeroCarta();
            boolean unaEsComodin = cartaSeleccionada1.getComodin() || cartaSeleccionada2.getComodin();

            //Bloque de código común
            int finalI = i;
            int pos1 = Integer.parseInt(opciones[0]);
            int pos2 = Integer.parseInt(opciones[1]);

            Runnable codigoCartasValidas = () -> {
                if (pos1 < pos2) {
                    tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos2)); //le saco la carta que jugo y la pongo al descarte
                    tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos1));

                } else {
                    tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos1)); //le saco la carta que jugo y la pongo al descarte
                    tablero.agregarCartaADescarte(this.cartasEnPosesion.remove(pos2));
                }

                tablero.agregarCartaADescarte(tablero.getCartasEnMesa().remove(finalI));
                tablero.getMazoDeCartas().ponerUnaCartaSobreMesa(tablero);
            };

            if (cartaActualMesa.getComodin() && suma < 10) {  //la carta de la mesa es comodin (#)
                cartaValida = true;
                codigoCartasValidas.run();
            } else if (unaEsComodin && suma < cartaActualMesa.getNumeroCarta()) {  // al menos una de las que tiro es comodin
                cartaValida = true;
                codigoCartasValidas.run();
            } else if (!unaEsComodin && (suma == cartaActualMesa.getNumeroCarta())) {
                cartaValida = true;
                codigoCartasValidas.run();
            }

            if (cartaValida) {
                //Bloque de código común
                Runnable codigoBonificacionColor = () -> {
                    if (!this.getCartasEnPosesion().isEmpty()) {
                        System.out.println("Hizo una bonificacion de color, ingrese la posicion de una de sus carta que pasara a la fila central \n");
                        this.mostrarCartas();
                        int cartaADescartar = scanner.nextInt();
                        scanner.nextLine(); // Consumir la nueva línea después de nextInt()
                        tablero.agregarCartaAMesa(this.getCartasEnPosesion().remove(cartaADescartar));
                        tablero.cartaExtraAlosDemasBonificacionColor(this);
                    }
                };

                                                //todas las cartas de la combinacion con el mismo color
                if (cartaSeleccionada1.getColorCarta() == cartaSeleccionada2.getColorCarta() && cartaSeleccionada2.getColorCarta() == cartaActualMesa.getColorCarta()) {
                    codigoBonificacionColor.run();
                }                               //todas las cartas que tiro multicolor
                else if (cartaSeleccionada1.getColorCarta() == cartaSeleccionada2.getColorCarta() && cartaSeleccionada2.getColorCarta() == ColorCarta.MULTICOLOR) {
                    codigoBonificacionColor.run();
                }                               //una carta de las que tiro coincide con el color de la carta en mesa y la otra es multicolor
                else if (cartaSeleccionada1.getColorCarta() == cartaActualMesa.getColorCarta() && cartaSeleccionada2.getColorCarta() == ColorCarta.MULTICOLOR) {
                    codigoBonificacionColor.run();
                }                               //una carta de las que tiro coincide con el color de la carta en mesa y la otra es multicolor
                else if (cartaSeleccionada2.getColorCarta() == cartaActualMesa.getColorCarta() && cartaSeleccionada1.getColorCarta() == ColorCarta.MULTICOLOR) {
                    codigoBonificacionColor.run();
                }

            } else {
                System.out.println("cartas seleccionadas invalidas, perdio la oportunidad de hacer una combinacion para la carta en mesa \n");
            }
        }else{System.out.println("No tiene suficientes cartas \n");}
    }
}
