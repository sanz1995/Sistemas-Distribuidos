/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Envelope.java
* TIEMPO: 0 h
* DESCRIPCION: sobre. 
*/


import java.io.Serializable;

public class Envelope implements Serializable {
	
	private int source;
	
	private int destination;
	
	private Serializable payload;
	
	private int timeStamp;
	
	private int type;
	
	public Envelope(int s, int d, Serializable p,int time,int t) {
			source = s;
			destination = d;
			payload = p;
			timeStamp = time;
			type=t;
	}
	
	public int getSource() { return source; }
	
	public int getDestination() { return destination; }
	
	public Serializable getPayload() { return payload; }
	
	public int getTime(){ return timeStamp; }
	
	public boolean isRequest(){
		return type==0;
	}
	
	public boolean isACK(){
		return type==1;
	}
	
	public boolean isMulticast(){
		return type==2;
	}
}