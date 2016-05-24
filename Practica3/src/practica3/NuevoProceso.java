/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: NuevoProceso.java
* TIEMPO: 1 h
* DESCRIPCION: Lanza procesos de envio o de recepcion. 
*/
//package practica3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class NuevoProceso {
	
	private static Semaphore s=new Semaphore(1);
	
	
	public static void main(String[] args){
		
		try{
			boolean debug = false;
			if (args[0].equals("-d")){
				debug = true;
				args[0]=args[1];
				args[1]=args[2];
			}else{
				debug=false;
			}
			
			Scanner escaner=new Scanner(new File(args[1]));
			int i=0;
			while(escaner.hasNext()){
				escaner.nextLine();
				i++;
			}
			
			int id=Integer.parseInt(args[0]);
			if(id>i || id<1){
				System.out.println("Identificador de proceso fuera"
						+ " del intervalo[1-"+i+"]");
			}else{
				boolean recibir;
				int n=0;
				if(id%2==0){
					recibir=false;
					Scanner teclado=new Scanner(System.in);
					System.out.println("Selecciona el proceso al que "
							+ "enviar el mensaje");
					n=teclado.nextInt();
					while(n>i || n<1){
						System.out.println("Identificador de proceso "
								+ "fuera del intervalo[1-"+i+"]");
						n=teclado.nextInt();
					}
				}else{
					recibir=true;
					Scanner teclado=new Scanner(System.in);
					System.out.println("Selecciona el numero de mensajes a recibir");
					n=teclado.nextInt();
					while(n<1){
						System.out.println("El numero debe ser superior a 0");
						n=teclado.nextInt();
					}
				}

				Thread t=new Thread(new Proceso(id,args[1],debug,recibir,n)); 
				t.start();
			}
		}catch(FileNotFoundException e){
			System.out.println("El fichero no ha podido ser abierto");
		}
	}
}
