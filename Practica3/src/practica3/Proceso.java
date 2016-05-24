/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Proceso.java
* TIEMPO: 10 min
* DESCRIPCION: recibe o envia mensajes en funcion del parametro recibir. 
*/
//package practica3;

import java.io.FileNotFoundException;

public class Proceso implements Runnable{

	private int id;
	private String networkFile;
	private boolean debug;
	private boolean recibir;
	private int n;
	public Proceso(int id,String networkFile,boolean debug,
								boolean recibir,int n){
		this.id=id;
		this.networkFile=networkFile;
		this.debug=debug;
		this.recibir=recibir;
		this.n=n;
	}
	public void run(){
		try{
			if(recibir){
				MessageSystem ms = new MessageSystem(id, networkFile, debug);
				int resultado=0;
				Envelope e;
				int i=0;
				while(i<n){
					e = ms.receive();
					resultado += (Integer)e.getPayload()*(i+1);
					i++;
				}
				System.out.println("El resultado de la operacion es "+ resultado);
				ms.stopMailbox();
			}else{
				MessageSystem ms = new MessageSystem(id, networkFile, debug);
				ms.send(n, new Integer(id));
				ms.stopMailbox();
			}
		}catch (FileNotFoundException e) {
			System.err.println("El fichero " + networkFile + " no existe.");
		}
	}
	
}
