/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: BuzónManager.java
* TIEMPO: 30 min
* DESCRIPCION: Recibe mensajes y los guarda en una cola. 
*/


import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;

public class BuzonManager implements Runnable{

	final int MAX=50;
	
	private Buzon buzon;
	
	private boolean abierto;
	
	private int port;
	
	public BuzonManager(int port,Buzon buzon){
		abierto=true;
		this.port=port;
		this.buzon=buzon;
	}
	
	public void cerrar(){
		abierto=false;
	}
	
	
	public void run(){
		try{
			ServerSocket s=new ServerSocket(port);
			ObjectInputStream ois=null;
			while(abierto){
				ois=new ObjectInputStream(s.accept().getInputStream());
				buzon.add((Envelope)ois.readObject());
			}
			ois.close();
			s.close();
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
}
