/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stibor.jatek;

//import java.awt.event.ActionEvent;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.json.JSONObject;

/**
 *
 * @author svard_iwxbnec
 */


/*

            Hogyan tudok a reader osztályból kiiratni a JFrame-re??
                http://www.javamex.com/tutorials/threads/invokelater.shtml
            Hogyan tudok gombnyomásra üzenetet küldeni a szervernek??
            Hogyan néznek ki az egyes üzenet csoportok?? (azonosítók)

*/

public class Client extends JFrame implements ActionListener {  
    
    JPanel contentPane;
    JButton[][] gombok = new JButton[10][10];
    JPanel jatekmezo;
    JButton btnconnect;
    
    JLabel lblteszt;
    
    public Client(){
        initComponents();
        ConnectToServer();
    }
    
    private void initComponents() {
        
        // alap
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 600);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5,5,5,5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        
        // játékmező
        jatekmezo = new JPanel();
        jatekmezo.setBackground(java.awt.Color.yellow);
        jatekmezo.setBounds(75, 100, 448, 448);
        jatekmezo.setLayout(null);
        contentPane.add(jatekmezo);
        
        // 10x10-es pálya
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                gombok[i][j] = new JButton();
                gombok[i][j].setBackground(java.awt.Color.LIGHT_GRAY);
                gombok[i][j].setBounds(45*j,45*i, 43, 43);
                gombok[i][j].addActionListener(this);
                jatekmezo.add(gombok[i][j]);
            }
        }
        
        // csatlakozás gomb
        btnconnect = new JButton("Külés");
        btnconnect.setBounds(500,10,80,25);
        btnconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                
            }
        });
        contentPane.add(btnconnect);
        
        // teszt szöveg kiírás
        lblteszt = new JLabel();
        //lblteszt.setLocation(10, 10);
        lblteszt.setBounds(10, 10, 150, 20);
        lblteszt.setVisible(true);
        //lblteszt.setForeground(Color.GREEN);
        lblteszt.setText("Kiválasztott gomb: , ");
        contentPane.add(lblteszt);
    }
    
    public void setMessage(String message){
        this.lblteszt.setText(message);
    }
    
    static Socket socket;
    static BufferedReader reader;
    static PrintWriter writer;
    static String uid;
    static Thread t;
    
    private void ConnectToServer(){
        try {
            socket = new Socket("localhost", 5000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            
            t = new Thread(new Reader());
            t.start();
            
            //while(true);
            
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) {
        new Client().setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if(ae.getSource().equals(gombok[i][j])){
                    lblteszt.setText("Kiválasztott gomb: "+i+", "+j);
                }
            }
        }
    }
  
    public static class Reader implements Runnable {
        
        @Override
        public void run() {
           
           String message;
           try {
               while(true){
                while((message = reader.readLine()) != null) {
                    System.out.println(message);
                    JSONObject obj = new JSONObject(message);
                    switch(obj.getInt("status_code")) {
                        case 500:
                            uid = obj.getString("uid");
                            System.out.println("UID megváltozott: " + uid);
                            BufferedReader buffr = new BufferedReader(new InputStreamReader(System.in));
                            System.out.println("Mi a neved?");
                            
                            //JOptionPane.showMessageDialog(null,"Mi a neved?", "Kérés", JOptionPane.QUESTION_MESSAGE);
                            
                            JSONObject obj1 = new JSONObject();
                            obj1.put("status_code", 100);
                            obj1.put("uid", uid);
                            obj1.put("name", buffr.readLine());
                            writer.println(obj1.toString());
                            writer.flush();
                            break;
                        case 101:
                            String m = obj.getString("message");
                            System.out.println(m);
                            break;
                        default:
                             System.out.println("Ismeretlen utasítás: " + obj.getInt("status_code"));
                             break;
                    }
                }
               }
           } catch(Exception e) {
               e.printStackTrace();
           }
        }
        
    }
}

