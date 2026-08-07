package ar.edu.unlu.poo.juegodos.persistencia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

// guarda el top 5 en un archivo de texto plano (una linea por entrada, "nombre,puntos").
// no hace falta nada mas sofisticado para guardar 5 lineas.
// vive del lado servidor, el Tablero llama a registrarResultado() cuando termina una partida
public class Ranking {

    private static final String ARCHIVO = "ranking_top5.txt";
    private static final int CANTIDAD_TOP = 5;

    private Ranking (){
        // clase de métodos estáticos, no se instancia
    }

    public static synchronized void registrarResultado (String nombreGanador, int puntos){
        List<String[]> entradas = leerEntradas();
        entradas.add(new String[]{nombreGanador, String.valueOf(puntos)});
        entradas.sort((a, b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));

        if (entradas.size() > CANTIDAD_TOP){
            entradas = entradas.subList(0, CANTIDAD_TOP);
        }

        guardarEntradas(entradas);
    }

    public static synchronized String formatoTexto (){
        List<String[]> top5 = leerEntradas();
        if (top5.isEmpty()){
            return "Todavía no hay resultados registrados en el ranking.";
        }

        StringBuilder sb = new StringBuilder("\n===== TOP 5 =====\n");
        int puesto = 1;
        for (String[] entrada : top5){
            sb.append(puesto).append(". ").append(entrada[0]).append(" — ").append(entrada[1]).append(" puntos\n");
            puesto++;
        }
        return sb.toString();
    }

    private static List<String[]> leerEntradas (){
        List<String[]> lista = new ArrayList<>();
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()){
            return lista;
        }

        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))){
            String linea;
            while ((linea = lector.readLine()) != null){
                if (linea.trim().isEmpty()){
                    continue;
                }
                String[] partes = linea.split(",");
                if (partes.length == 2){
                    lista.add(partes);
                }
            }
        } catch (IOException e){
            System.out.println("No se pudo leer el ranking: " + e.getMessage());
        }
        return lista;
    }

    private static void guardarEntradas (List<String[]> entradas){
        try (PrintWriter escritor = new PrintWriter(new FileWriter(ARCHIVO))){
            for (String[] entrada : entradas){
                escritor.println(entrada[0] + "," + entrada[1]);
            }
        } catch (IOException e){
            System.out.println("No se pudo guardar el ranking: " + e.getMessage());
        }
    }
}
