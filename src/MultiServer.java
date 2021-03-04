/*
 * @author Giuseppe Tutino
 * @version 1.1
 * @java-version jdk15
 * */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class MultiServer
{

        public static Vector<Socket> sockets = new Vector<>();
        public static Vector<String> utenti = new Vector<>();

        public static void main(String[] args){
                try {

                        ServerSocket server = new ServerSocket(3000);
                        int z = 0;

                        while(true) {

                                //Attesa di una connessione
                                Socket sock = server.accept();

                                //Metto i socket nel vector cosi posso utilizzarli ovuque
                                sockets.add(sock);
                                utenti.add(" <user"+ z +">"); //Nome utente di base


                                //Ad ogni nuova connessione si attiva un nuovo thread
                                Thread t = new Server(sock,sockets,utenti,"user"+z++);
                                t.start();

                                //Aggiorno la lista utenti
                                for (int i=0;i<sockets.size();i++) {
                                        PrintWriter out = new PrintWriter(sockets.elementAt(i).getOutputStream());

                                        StringBuilder utente = new StringBuilder();
                                        utente.append("+");
                                        for (int j=0;j<sockets.size();j++) {
                                                if (!(j==i)) {
                                                        utente.append(j).append(utenti.elementAt(j)).append("#");
                                                }
                                        }
                                        out.println(utente);
                                        out.flush();
                                }
                        }

                }catch(IOException e) {
                        System.err.println("Connessione non riuscita: " + e.getMessage());
                }
        }



}
