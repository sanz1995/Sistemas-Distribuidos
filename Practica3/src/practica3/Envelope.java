/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Envelope.java
* TIEMPO: 0 h
* DESCRIPCION: sobre. 
*/
//package practica3;

import java.io.Serializable;

public class Envelope implements Serializable {
	
	private int source;
	
	private int destination;
	
	private Serializable payload;
	
	
	public Envelope(int s, int d, Serializable p) {
			source = s;
			destination = d;
			payload = p;
	}
	
	public int getSource() { return source; }
	
	public int getDestination() { return destination; }
	
	public Serializable getPayload() { return payload; }
}