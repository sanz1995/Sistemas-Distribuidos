/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Primos.java
* TIEMPO: 1 h
* DESCRIPCION: Se encarga de dividir los intervalos en las peticiones. 
*/
//package Practica2;

public class Primos {

	private int inf;
	private int sup;
	private int total;
	private int numWorkers;
	private int solicitado;
	private int tamPeticion;
	
	
	public Primos(int inf,int sup,int numWorkers){
		this.inf=inf;
		this.sup=sup;
		this.total=sup-inf;
		this.numWorkers=numWorkers;
		tamPeticion=(sup-inf)/(numWorkers*3);
		solicitado=0;
	}
	public synchronized Peticion solicitar(){
		if(inf>=sup){
			return new Peticion(0,0,false);
		}else if((total-solicitado)<total/(numWorkers*5)){
			tamPeticion=total-solicitado;
		}else if((total-solicitado)<total/3){
			tamPeticion=total/(numWorkers*5);
		}
		solicitado+=tamPeticion;
		inf+=tamPeticion;
		return new Peticion(inf-tamPeticion,inf,true);
	}
	
	
}
