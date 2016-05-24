/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Servicio.java
* TIEMPO: 40 min
* DESCRIPCION: realiza las peticiones al servidor y las añade a una lista. 
*/
//package Practica2;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Servicio implements Runnable{

	private List<Integer> l;
	private Primos p;
	private Worker w;
	public Servicio(Primos p,List<Integer> list,Worker w){
		this.p=p;
		l=list;
		this.w=w;
	}
	
	public void run(){
		try {
			Peticion pet=p.solicitar();
			while(pet.seguir()){
				l.addAll(w.encuentraPrimos(pet.getInf(), pet.getSup()));
				pet=p.solicitar();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
