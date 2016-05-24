/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: NewWorker.java
* TIEMPO: 10 min
* DESCRIPCION: registra un Worker. 
*/
//package Practica2;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NewWorker implements Runnable{

	private String ip;
	public NewWorker(String ip){
		this.ip=ip;
	}
	public void run(){
		Registry r;
		try {
			r = LocateRegistry.getRegistry(ip);
			WorkerServer w=new WorkerServer(r);
			w.registrar();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
