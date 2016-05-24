/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: MessageSystem.java
* TIEMPO: 30 min
* DESCRIPCION: Clase gestora de la comunicación. 
*/


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
	
	private int time;
	
	
	public MessageSystem(int source, String networkFile, boolean debug)
			throws FileNotFoundException { 
		this.source=source;
		this.networkFile=new File(networkFile);
		this.debug=debug;
		buzon=new Buzon(5000);
		buzonManager=new BuzonManager(getPort(),buzon);
		(new Thread(buzonManager)).start();
		time=0;
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
	
	
	public void send(int dst, Serializable message,int type) {
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
				Envelope sobre=new Envelope(source,dst,message,time,type);
				if(debug){
					System.out.println(sobre.getSource()+"ha enviado \""+
							sobre.getPayload()+"\" a "+sobre.getDestination() 
							+ " en "+sobre.getTime());
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
			System.out.println(sobre.getDestination()+"ha recibido \""+
				sobre.getPayload()+"\" de "+sobre.getSource() + " en "+sobre.getTime());
		}
		return sobre;
	}
	
	public void stopMailbox() { 
		buzonManager.cerrar();
		debug=false;
		send(source,null,0);
	}
	
	public void setTime(int newTime){
		time=newTime;
	}
	
	public int getTime(){
		return time;
	}
	
	public int getSource(){
		return source;
	}
	
	public int numProcesos(){
		int n=0;
		try{
			Scanner scanner=new Scanner(networkFile);
			while(scanner.hasNext()){
				scanner.nextLine();
				n++;
			}
		}catch(IOException e){
			n=-1;
		}
		return n;
	}
	
}