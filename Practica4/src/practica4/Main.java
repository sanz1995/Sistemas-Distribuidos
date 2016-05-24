/*
* AUTOR: Jorge Sanz Alcaine
* NIA: 680182
* FICHERO: Main.java
* TIEMPO: 30 min
* DESCRIPCION: Lanza un proceso y una interfaz gráfica. 
*/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

public class Main {
	private static ChatDialog v;
	private static TotalOrderMulticast t;
	
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
			
			t=new TotalOrderMulticast(new MessageSystem(
					Integer.parseInt(args[0]),args[1],debug));
			
			
			v = new ChatDialog(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String m = v.text();
						//if (!m.isEmpty())
							//v.addMessage("Yo: " + m);
						t.sendMulticast(m);
					}
			});
			v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			Envelope en;
			while(true){
				en=t.receiveMulticast();
				v.addMessage(en.getSource()+": "+en.getPayload());
			}
			
		}catch(FileNotFoundException e){}
	}
}
