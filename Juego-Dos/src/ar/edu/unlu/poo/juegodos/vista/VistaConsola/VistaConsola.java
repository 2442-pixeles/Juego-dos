package ar.edu.unlu.poo.juegodos.vista.VistaConsola;

import ar.edu.unlu.poo.juegodos.controlador.Controlador;
import ar.edu.unlu.poo.juegodos.modelo.*;
import ar.edu.unlu.poo.juegodos.vista.IVista;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class VistaConsola implements IVista {
    private final JFrame frame;
    private JTextArea areaTexto;
    private JTextField campoEntrada;
    private Controlador controlador;


    public VistaConsola() {
        frame = new JFrame();

        // Configuración de la ventana
        frame.setTitle("Consola Gráfica");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear el área de texto y el campo de entrada
        areaTexto = new JTextArea();
        campoEntrada = new JTextField();

        // Configurar el área de texto como no editable
        areaTexto.setEditable(false);

        // Crear un JScrollPane para el área de texto
        JScrollPane scrollPane = new JScrollPane(areaTexto);

        // Configurar el layout
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        // Agregar componentes a la ventana
        frame.add(scrollPane);
        frame.add(campoEntrada);

        // Configurar el ActionListener para el campo de entrada
        campoEntrada.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener el texto del campo de entrada
                String comando = campoEntrada.getText();

                // Mostrar el comando en el área de texto
                areaTexto.append(">> " + comando);

                // Aquí puedes agregar la lógica para procesar el comando

                // Limpiar el campo de entrada
                campoEntrada.setText("");
            }
        });
    }

    public void setControlador(Controlador controlador) {
        this.controlador = controlador;
    }

    private void mostrarMensaje(String mensaje) {
        areaTexto.append(mensaje + "\n");
    }
}