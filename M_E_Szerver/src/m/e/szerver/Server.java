
package m.e.szerver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


class Jatekos {
    String UID;
    
    Socket socket;
    public PrintWriter writer;
    
    public String nev;
    public int pontszam;
    
    public Jatekos(Socket socket, String nev, int pontszam, String UID) {
        this.socket = socket;
        try{
            writer = new PrintWriter(socket.getOutputStream());
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        this.nev = nev;
        this.pontszam = pontszam;
        
        this.UID = UID;
    }
}

public class Server {
    ArrayList<Jatekos> jatekosok;
    
    public class ClientHandler implements Runnable {
       BufferedReader reader;
       Socket socket;
        
       public ClientHandler(Socket clientSocket) {
           try {
               socket = clientSocket;
               InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
               reader = new BufferedReader(isReader);
           } catch(Exception e) {
               e.printStackTrace();
           }
       } 
       
        @Override
        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                    //tellToEveryone(message);
                }
                
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /*
                DataInputStream din = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
            
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String msgin = "", msgout="";
                
                while(!msgin.equals("end")){
                msgout = br.readLine();
                dout.writeUTF(msgout);
                msgin = din.readUTF();
                System.out.println(msgin);      // üzenet kiírása
                dout.flush();
                }*/
    
    public static void main(String[] args) {
        new Server().go();
    }
    
    public void go() {
        jatekosok = new ArrayList();
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            
            while(true) {
                Socket clientSocket = serverSocket.accept();
                
                //Itt generálj egy pl. 64bit hosszú karakterláncot.
                String uid = "dsjlfjaslfjasdkfjaslkfjskldajfalks";
                jatekosok.add(new Jatekos(clientSocket, "", 0, uid));
                
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                System.out.println("Új játékos kapcsolódott!");
                tellToSomebody(uid,"Üdv a szerveren!");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void tellToSomebody(String uid, String message) {
        for(int i=0; i<jatekosok.size(); i++) {
            if(jatekosok.get(i).UID == uid) {
                jatekosok.get(i).writer.println(message);
                jatekosok.get(i).writer.flush();
                break;
            }
        }
    }
    
    public void tellToEveryone(String message) {
        Iterator it = jatekosok.iterator();
        
        while((it.hasNext())) {
            try {
                PrintWriter w = (PrintWriter) it.next();
                w.println(message);
                w.flush();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
    }
}

