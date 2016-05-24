/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Worker.java
* TIEMPO: 10 min
* DESCRIPCION: interfaz de Worker. 
*/
//package Practica2;


import java.rmi.Remote;
import java.util.ArrayList;

public interface Worker extends Remote{
	//Devuelve un vector con los primos entre min y max.
	ArrayList<Integer> encuentraPrimos(int min, int max) 
						throws java.rmi.RemoteException;
	
}
