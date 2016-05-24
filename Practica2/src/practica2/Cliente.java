/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Cliente.java
* TIEMPO: 1 h
* DESCRIPCION: escribe en un fichero los primos comprendidos en un intervalo. 
*/
//package Practica2;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Cliente implements Runnable{
	
	private int n=10;
	private int inf=0;
	private int sup=10000;
	private String ip;
	public Cliente(int inf,int sup,int n,String ip){
		this.inf=inf;
		this.sup=sup;
		this.n=n;
		this.ip=ip;
	}
	public void run() {
       try {
    	  
    	   Primos p=new Primos(inf,sup,n);
           Registry registry = LocateRegistry.getRegistry(ip);
           
           WorkerFactory stub=(WorkerFactory) registry.lookup("WorkerFactory");
           //registry.unbind("WorkerFactory");
           
           ArrayList<Worker> response = stub.dameWorkers(n);
           
           ArrayList<Integer> lista=new ArrayList<Integer>();
           Thread[] t=new Thread[n];
	       for(int i=0;i<n;i++){
	    	   t[i]=new Thread(new Servicio(p,lista,response.get(i)));
	    	   t[i].start();
	       }
	       for(int i=0;i<n;i++){
	    	   try {
	    		   t[i].join();
	    	   } catch (InterruptedException e) {
	    		   e.printStackTrace();
	    	   }   
	       }
	       Collections.sort(lista);
	       Iterator<Integer> i=lista.iterator();
	       PrintWriter pr=new PrintWriter("primos.txt");
	       while(i.hasNext()){
	    	  pr.println(i.next());
	       }
	       pr.close();
       } catch (RemoteException e) {
	       e.printStackTrace();
	   }catch (NotBoundException ex) {
	       ex.printStackTrace();
	   }catch (IOException exce){
		   
	   }
	}
}