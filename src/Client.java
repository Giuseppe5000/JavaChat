/*
 * @author Giuseppe Tutino
 * @version 1.1
 * @java-version jdk15
 * */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {

        private JFrame f;
        private Socket sock;

        private final JTextField text = new JTextField();

        //Prefisso viene attivato in chat privata
        //Se chatto in global non c'è prefisso
        //Il prefisso lo attivo quando clicco su un utende sulla Jlist
        private String prefix = "";

        //private final int currentChat = 0;
        private Map<String, JPanel> chats = new HashMap<String, JPanel>(); //Array associativo tra nomi utenti e chat

        public Client()
        {

                //Mi connetto al server
                while (true) {
                        String ip = JOptionPane.showInputDialog("Inserisci l'indirizzo ip del server");

                        try {
                                sock = new Socket(ip,3000);
                                break;

                        }catch(IOException e) {
                                int i = JOptionPane.showOptionDialog(f,"Server non trovato!\nVuoi cambiare indirizzo ip?","Errore",
                                        JOptionPane.YES_NO_OPTION,JOptionPane.ERROR_MESSAGE,null,null,null);

                                if (i == JOptionPane.NO_OPTION) {
                                        System.exit(0);
                                }
                        }
                }

                //Creo il frame
                f = new JFrame("Chat");
                f.setSize(700,700);
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                f.setLayout(new BorderLayout(10,10));



                //Messaggi inviati e ricevuti
                //Cambio chats
                chats.put("global",new JPanel(new GridLayout(30,1)));
                chats.get("global").setBackground(Color.WHITE);
                JScrollPane chat = new JScrollPane();
                chat.setViewportView(chats.get("global"));


                //Lista utenti attivi
                DefaultListModel l = new DefaultListModel<String>();
                JList lista = new JList<>(l);
                l.addElement("global");
                JScrollPane utenti = new JScrollPane();
                utenti.setViewportView(lista);

                //Pannello principale
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(text,BorderLayout.CENTER);
                JButton send = new JButton("Invio");
                panel.add(send,BorderLayout.LINE_END);

                f.add(chat,BorderLayout.CENTER);
                f.add(utenti,BorderLayout.LINE_END);
                f.add(panel,BorderLayout.PAGE_END);
                f.setVisible(true);

                //Quando chiudo la finestra chiudo anche la connessione
                f.addWindowListener(new WindowAdapter()
                {
                        @Override
                        public void windowClosing(WindowEvent e)
                        {
                                try {
                                        sock.close();
                                } catch (IOException ioException) {
                                        ioException.printStackTrace();
                                }
                        }
                });

                //Quando premo il button invia il messaggio
                send.addActionListener(e -> {
                        PrintWriter p;
                        try {
                                p = new PrintWriter(sock.getOutputStream());
                                p.println(prefix + text.getText());
                                p.flush();
                                text.setText("");
                        } catch (IOException ioException) {
                                ioException.printStackTrace();
                        }

                });

                //Quando premo Enter invio il messaggio
                text.addKeyListener(new KeyAdapter()
                {
                        @Override
                        public void keyPressed(KeyEvent e)
                        {
                                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                                        PrintWriter p;
                                        try {
                                                p = new PrintWriter(sock.getOutputStream());
                                                p.println(prefix + text.getText());
                                                p.flush();
                                                text.setText("");
                                        } catch (IOException ioException) {
                                                ioException.printStackTrace();
                                        }
                                }
                        }
                });

                //Cambio il prefisso del messaggio, se seleziono un utente, il messaggio sarà inviato soltanto a lui, se scelgo global sarà inviato a tutti
                lista.addMouseListener(new MouseAdapter()
                {
                        @Override
                        public void mouseClicked(MouseEvent e)
                        {
                                if (lista.getSelectedIndex() == 0){
                                       prefix = "";
                                       chat.setViewportView(chats.get("global"));
                                }
                                else{
                                        String[] str = ((String) lista.getSelectedValue()).split(" "); // "1 utente" -> ["1"], ["utente"]
                                        prefix = str[0]+"#";

                                        str[1] = str[1].replace("<","");
                                        str[1] = str[1].replace(">","");
                                        chat.setViewportView(chats.get(str[1]));
                                        System.out.println(str[1]);
                                        chat.revalidate();
                                        chats.get(str[1]).revalidate();
                                }

                        }
                });



                //Sempre in attesa di messaggi
                while (true) {
                        Scanner scan;
                        try {
                                scan = new Scanner(sock.getInputStream());
                                String messaggio = scan.nextLine();


                                //Controllo se il messaggio è una rischiesta per aggiornare gli utenti cona stringa
                                //Se il messaggio inizia con + allora è una richiesta di aggiunta utenti
                                if (messaggio.charAt(0) == '+') {
                                        l.clear();
                                        l.addElement("global");

                                        String[] s = messaggio.split("#");

                                        //Nel primo carattere è presente "+", quindi lo tolgo
                                        s[0] = s[0].replace("+","");

                                        for(int i=0; i < s.length;i++) {
                                                l.addElement(s[i]);

                                                if(!s[i].equals("")){
                                                        String[] s2 = s[i].split("<");
                                                        s2[1] = s2[1].replace(">","");
                                                        //TODO eliminazione username vecchi
                                                        chats.put(s2[1],new JPanel(new GridLayout(30,1)));
                                                        chats.get(s2[1]).setBackground(Color.WHITE);
                                                        System.out.println(s2[1]);
                                                }

                                        }
                                        lista.revalidate();
                                        utenti.revalidate();

                                }


                                else{
                                        if(messaggio.contains("[Private]")){
                                                messaggio = messaggio.replace("[Private]",""); //Rimuovo il PRIVATE
                                                String[] s = messaggio.split(";");
                                                s[1] = s[1].replace("&gt",""); // Tiro fuori l'username
                                                chat.setViewportView(chats.get(s[1]));
                                                chats.get(s[1]).add(new JLabel(messaggio));

                                        }
                                        else if (messaggio.contains("[PrivateTo]")){
                                                messaggio = messaggio.replace("[PrivateTo]",""); //Rimuovo il PRIVATE
                                                String[] s = messaggio.split(";");

                                                //prendo 7 prechè il messaggio contiene &emsp; <--
                                                s[7] = s[7].replace("&gt",""); // Tiro fuori l'username
                                                chat.setViewportView(chats.get(s[7]));
                                                chats.get(s[7]).add(new JLabel(messaggio));
                                        }
                                        else {
                                                chats.get("global").add(new JLabel(messaggio));
                                                f.revalidate();
                                        }

                                }


                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }


        }

        public static void main(String[] args)
        {
                new Client();
        }
}
