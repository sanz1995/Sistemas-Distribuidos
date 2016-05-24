/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: SerAsig.java
* TIEMPO: 3 h
* DESCRIPCION: crea un Registro y le asigna un servidor de asignacion. 
*/
//package Practica2;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class SerAsig implements Runnable{
 
	private String ip;
	public SerAsig(String ip){
		this.ip=ip;
	}

	public void run (){
		try {
			//Semaphore s=new Semaphore(1);
			WorkerServer w;
			Registry r=LocateRegistry.getRegistry(ip);
			WorkerFactory wF=new WorkerFactoryServer(r);
			/**
			for (int i=0;i<30;i++){
				w=new WorkerServer(r);
				w.registrar();
			}
			*/
			r.bind("WorkerFactory", wF);
		} catch (RemoteException e) {
			e.printStackTrace();
		}catch (AlreadyBoundException ex){
			
		}
		
	}
}
