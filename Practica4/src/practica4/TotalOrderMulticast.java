/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: TotalOrderMulticast.java
* TIEMPO: 4 horas
* DESCRIPCION: Envia y recibe mensajes multicast de forma ordenada. 
*/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class TotalOrderMulticast {
	
	private MessageSystem msystem;
	private int nProcesos;
	private int nConfirmaciones;
	private boolean interes;
	private int tiempoDelIntento;
	//private Queue<Serializable> cola;
	private Serializable mensaje;
	private List<Integer> peticiones;
	private Semaphore s;
	private Semaphore mutex;
	
	public TotalOrderMulticast(MessageSystem ms){
		msystem=ms;
		nProcesos=msystem.numProcesos();
		nConfirmaciones=0;
		//cola=new ConcurrentLinkedQueue<Serializable>();
		peticiones=new ArrayList<Integer>();
		interes=false;
		tiempoDelIntento=0;
		s=new Semaphore(1);
		mutex=new Semaphore(1);
	}
	
	public void sendMulticast(Serializable message){
		//cola.offer(message);
		try {
			s.acquire();
			mutex.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mensaje=message;
		msystem.setTime(msystem.getTime()+1);
		tiempoDelIntento=msystem.getTime();
		interes=true;
		for(int i=0;i<nProcesos;i++){
			//if((i+1)!=msystem.getSource()){
				//System.out.println("req ("+msystem.getTime()+
				//		","+msystem.getSource()+")");
				msystem.send(i+1,"request", 0);
			//}
		}
		mutex.release();
	}
	
	public Envelope receiveMulticast(){
		while (true) {
			Envelope e = msystem.receive();
			if(e.isRequest()){
				if(e.getSource()!=e.getDestination()){
					try{
						mutex.acquire();
					}catch (InterruptedException ex) {
					}
					if(e.getTime()<tiempoDelIntento | !interes){
						//System.out.println("ACK ("+msystem.getTime()+
						//	","+msystem.getSource()+")");
						msystem.send(e.getSource(), "ACK", 1);
					}else if(e.getTime()==tiempoDelIntento 
							&& e.getSource()>e.getDestination()){
						//System.out.println("ACK ("+msystem.getTime()
						//+","+msystem.getSource()+")");
						msystem.send(e.getSource(), "ACK", 1);
					}else{
						peticiones.add(e.getSource());
					}
					mutex.release();
				}
			}else if(e.isACK()){
				nConfirmaciones++;
				if(nConfirmaciones==(nProcesos-1)){
					//Entro en sección crítica
					//System.out.println("mc ("+msystem.getTime()+")");
					//Serializable message=cola.poll();
					for(int i=0;i<nProcesos;i++){
						msystem.send(i+1, mensaje, 2);
					}
					//Termino sección crítica
					Iterator<Integer> i=peticiones.iterator();
					while(i.hasNext()){
						//System.out.println("ACK ("+msystem.getTime()
						//+","+msystem.getSource()+")");
						msystem.send(i.next(), "ACK", 1);
					}
					peticiones.clear();
					nConfirmaciones=0;
					try{
						mutex.acquire();
					}catch (InterruptedException ex) {
					}
					interes=false;
					mutex.release();
					s.release();
				}
			}else{

				
				msystem.setTime(Math.max(msystem.getTime(),e.getTime())+1);
				mutex.release();
				return e;
			}
		}
	}
}
