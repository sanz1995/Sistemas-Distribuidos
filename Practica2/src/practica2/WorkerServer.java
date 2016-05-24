/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: WorkerServer.java
* TIEMPO: 2 h
* DESCRIPCION: Calcula los primos en un intervalo. 
*/
//package Practica2;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
public class WorkerServer extends UnicastRemoteObject 
									implements Worker{
	
	private static int n=0;
	
	private Registry registry;
	
	private static Semaphore s=new Semaphore(1);
	
	
	public WorkerServer(Registry r) throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
		this.registry=r;
	}


	//citar
	private static boolean esPrimo(int num) {
		if (num==0 | num==1){
			return false;
		}
        int i=2;
        boolean seguir=true;
        while((i*i<=num) & seguir){
        	if (num % i == 0){
        		seguir=false;
        	}
        	i++;
        }
        return seguir;
        
	}
	
	
	public ArrayList<Integer> encuentraPrimos(int a,int b) 
										throws RemoteException{
		ArrayList<Integer> l=new ArrayList<Integer>();
		while(a<b){
			if(esPrimo(a)){
				l.add(a);
			}
			a++;
		}
		registrar();
		return l;
	}
	
	public void registrar(){
		try{
			try {
				s.acquire();
			} catch (InterruptedException e) {}
			registry.bind("Worker"+n, this);
			n++;
			s.release();
		}catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("error");
		}catch (AlreadyBoundException e) {
			// TODO Auto-generated catch block
		}
	}
}
