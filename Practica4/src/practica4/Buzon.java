/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Buzón.java
* TIEMPO: 30 min
* DESCRIPCION: cola de mensajes. 
*/

import java.util.ArrayList;
import java.util.List;




public class Buzon {
	
	
	final int MAX;
	
	private List<Envelope> buzon;
	private int n;
	public Buzon(int max){
		MAX=max;
		buzon=new ArrayList<Envelope>();
		n=0;
	}
	
	public synchronized Envelope remove(){
		
		while(n==0){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		n--;
		return buzon.remove(0);
	}
	
	
	public synchronized boolean add(Envelope e){
		if(n<MAX){
			buzon.add(e);
			n++;
			notify();
			return true;
		}else{
			System.out.println("Mensaje perdido");
			return false;
		}
		
	}
	
	
}
