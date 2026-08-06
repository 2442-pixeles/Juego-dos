#!/bin/bash
cd "$(dirname "$0")"
java -cp "out/production/Juego-Dos" ar.edu.unlu.poo.juegodos.red.ClienteJuego
read -p "Presione Enter para cerrar..."
