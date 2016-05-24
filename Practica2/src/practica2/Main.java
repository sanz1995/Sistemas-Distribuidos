/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Main.java
* TIEMPO: 10 min
* DESCRIPCION: Lanza un thead en funcion de los paramentros. 
*/
//package Practica2;

public class Main {

	
	public static void main(String[] args){
		Thread t;
		if(args[0].equals("-c")){
			t=new Thread(new NewWorker(args[1]));
			t.start();
		}else if(args[0].equals("-a")){
			t=new Thread(new SerAsig(args[1]));
			t.start();
		}if(args[0].equals("-u")){
			t=new Thread(new Cliente(Integer.parseInt(args[1]),
						Integer.parseInt(args[2]),Integer.parseInt(args[3]),args[4]));
			t.start();
		}
	}
}
