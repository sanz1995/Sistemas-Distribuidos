/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Peticion.java
* TIEMPO: 10 min
* DESCRIPCION: contenido de la peticion a un Worker. 
*/
//package Practica2;

public class Peticion {

	private int inf;
	private int sup;
	private boolean seguir;
	public Peticion(int inf,int sup,boolean seguir){
		this.inf=inf;
		this.sup=sup;
		this.seguir=seguir;
	}
	
	public int getInf(){
		return inf;
	}
	
	public int getSup(){
		return sup;
	}
	public boolean seguir(){
		return seguir;
	}
}
