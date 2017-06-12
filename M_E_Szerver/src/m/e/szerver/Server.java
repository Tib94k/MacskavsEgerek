
package m.e.szerver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONObject;


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
    SessionIdentifierGenerator sig;
    
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
                    JSONObject object = new JSONObject(message);
                    switch(object.getInt("status_code")) {
                        case 200:
                            String uid = object.getString("uid");
                            String nev = object.getString("name");
                            for(int i=0; i<jatekosok.size(); i++) {
                                if(jatekosok.get(i).UID.equals(uid)) {
                                    jatekosok.get(i).nev = nev;
                                    System.out.println("Játékos: " + uid + " neve megváltozott: " + nev);
                                    break;
                                }
                            }
                            break;
                        case 100:
                            uid = object.getString("uid");
                            nev = object.getString("name");
                            int db = 4 - jatekosok.size()%4;
                            System.out.println("Teszt!!!");
                            JSONObject jo = new JSONObject();
                            jo.put("status_code", 102);
                            jo.put("message", "Üdv a szerveren "+nev+"!!\n\nVárakozás még "+db+" darab játékosra...");
                            tellToSomebody(uid, jo.toString());
                            break;
                        default:
                            System.out.println("Ismeretlen utasítás!");
                    }
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
        sig = new SessionIdentifierGenerator();
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            
            while(true) {
                Socket clientSocket = serverSocket.accept();
                
                //Itt generálj egy pl. 64bit hosszú karakterláncot.
                //String uid = "dsjlfjaslfjasdkfjaslkfjskldajfalks";
                String uid = sig.nextSessionId();
                jatekosok.add(new Jatekos(clientSocket, "", 0, uid));
                JSONObject obj = new JSONObject();
                obj.put("status_code", 500);
                obj.put("uid", uid);
                PrintWriter p = new PrintWriter(clientSocket.getOutputStream());
                p.println(obj.toString());
                p.flush();
                System.out.println("Új játékos kapcsolódott: " + uid);
                
                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();
                /*System.out.println("Új játékos kapcsolódott!");
                tellToSomebody(uid,"Üdv a szerveren!");*/
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void tellToSomebody(String uid, String message) {
        for(int i=0; i<jatekosok.size(); i++) {
            if(jatekosok.get(i).UID.equals(uid)) {
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
    
    public final class SessionIdentifierGenerator {
    private SecureRandom random = new SecureRandom();

    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
    }
}

