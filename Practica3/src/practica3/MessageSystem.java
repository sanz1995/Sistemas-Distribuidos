/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: MessageSystem.java
* TIEMPO: 30 min
* DESCRIPCION: Clase gestora de la comunicación. 
*/
//package practica3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.Scanner;

public class MessageSystem {

	private int source;
	
	private File networkFile;
	
	private boolean debug;
	
	private BuzonManager buzonManager;
	
	private Buzon buzon;
	
	
	public MessageSystem(int source, String networkFile, boolean debug)
			throws FileNotFoundException { 
		this.source=source;
		this.networkFile=new File(networkFile);
		this.debug=debug;
		buzon=new Buzon(50);
		buzonManager=new BuzonManager(getPort(),buzon);
		(new Thread(buzonManager)).start();
	}
	private int getPort(){
		try{
			Scanner scanner=new Scanner(networkFile);
			scanner.useDelimiter(":");
			int port=0;
			int i=0;
			while(scanner.hasNext() & i<source){
				scanner.next();
				port=Integer.parseInt(scanner.nextLine().substring(1));
				i++;
			}
			return port;
		}catch(IOException e){
			return -1;
		}
	}
	
	
	public void send(int dst, Serializable message) {
		try{
			Scanner scanner=new Scanner(networkFile);
			scanner.useDelimiter(":");
			String machine="";
			int port=0;
			int i=0;
			while(scanner.hasNext() & i<dst){
				machine=scanner.next();
				port=Integer.parseInt(scanner.nextLine().substring(1));
				i++;
			}
			if(i==dst){
				Socket s=new Socket(machine,port);
				ObjectOutputStream oos=
						new ObjectOutputStream(s.getOutputStream());
				Envelope sobre=new Envelope(source,dst,message);
				if(debug){
					System.out.println("Has enviado \""+
							sobre.getPayload()+"\" a "+sobre.getDestination());
				}
				oos.writeObject(sobre);
				oos.close();
				scanner.close();
			}
		}catch(IOException e){
			
		}
	}
	
	public Envelope receive() { 
		Envelope sobre=buzon.remove();
		if(debug){
			System.out.println("Has recibido \""+
					sobre.getPayload()+"\" de "+sobre.getSource());
		}
		return sobre;
	}
	
	public void stopMailbox() { 
		buzonManager.cerrar();
		debug=false;
		send(source,null);
	}
}