/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: WorkerFactoryServer.java
* TIEMPO: 10 min
* DESCRIPCION: devuelve una lista con los workers solicitados. 
*/
//package Practica2;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class WorkerFactoryServer extends UnicastRemoteObject 
										implements WorkerFactory{

	Registry registry;
	public WorkerFactoryServer(Registry r) throws RemoteException {
		super();
		registry=r;
	}
	public ArrayList<Worker> dameWorkers(int n) throws RemoteException{
		System.setProperty("java.rmi.server.codebase","file:/media/datos/algo");
		Worker w;
		String[] names=registry.list();
		ArrayList<Worker> l=new ArrayList<Worker>();
		if(names.length>=n){
			for(int i=0;i<n;i++){
				try {
					if(!names[i].equals("WorkerFactory")){
						w=(Worker) registry.lookup(names[i]);
						l.add(w);
					}
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			}
		}
		return l;
	}

}
