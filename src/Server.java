/*
* @author Giuseppe Tutino
* @version 1.1
* @java-version jdk15
* */
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Vector;

public class Server extends Thread
{
	private final Socket sock; //Mio socket
	private String user; //Mio nome utente
	private final Vector<Socket> sockets; //Lista di tutti i socket
	private Vector<String> utenti; //Lista di tutti gli utenti

	public Server(Socket s, Vector<Socket> sockets,Vector<String> utenti,String user){
		this.sock = s;
		this.user =  user;
		this.sockets = sockets;
		this.utenti = utenti;
	}

	
	
	public void run(){
		Scanner scan = null;
		try {
		        String messaggio;
			while (true) {
				scan = new Scanner(sock.getInputStream());
				messaggio = scan.nextLine();

				for (int i=0;i<sockets.size(); i++) {
					PrintWriter out = new PrintWriter(sockets.elementAt(i).getOutputStream());

					//Messaggio privato
					int str;
					String str2;
					try {

						//Se il messaggio comincia con !/ è un comando, es. -> !/name giuseppe (assegno questo nome)
						if(messaggio.contains("!/name")){
							messaggio = messaggio.replace("!/name ","");

							try {
								String[] s = messaggio.split("#");
								user = s[1];
							}catch (ArrayIndexOutOfBoundsException e){
								user = messaggio;
							}

							//Cambio nome nella lista
							for(int z=0;z<sockets.size();z++){
								if(sockets.elementAt(z) == sock){
									utenti.set(z," <"+user+">");
								}
							}

							//Aggiorno la lista a tutti gli utenti
							for (int z=0;z<sockets.size();z++) {
								PrintWriter out1 = new PrintWriter(sockets.elementAt(z).getOutputStream());

								StringBuilder utente = new StringBuilder();
								utente.append("+");
								for (int j=0;j<sockets.size();j++) {
									if (!(j==z)) {
										utente.append(j).append(utenti.elementAt(j)).append("#");
									}
								}
								out1.println(utente);
								out1.flush();
							}
							break;
						}

						String[] s = messaggio.split("#");
						str = Integer.parseInt(s[0]);
						str2 = s[1];

						//TROVO Il destinatario(mess privato)
						if (i == str){

							//Invio al destinatario
							//&tl = "<" e &gt = ">"
							out.println("<html><font color='red'>[Private] &lt;"+ user +"&gt; "+str2+"</font>");
							out.flush();

							//chats dalla i risalgo al nome del destinatario
							String u =utenti.elementAt(i);
							u = u.replace("<","");
							u = u.replace(">","");
							u = u.replace(" ","");
							//Invio a me
							PrintWriter out1 = new PrintWriter(sock.getOutputStream());
							out1.println("<html><font color='red'>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;[PrivateTo] &lt;"+ u + "&gt; " + str2 +"</font>");
							out1.flush();
							break;
						}
					}
					catch (NumberFormatException e){
						//messaggio globale
						if (sockets.elementAt(i) == sock) {
							out.println("<html><font>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; " + messaggio + "</font>");
							out.flush();
						}
						else{
							out.println("<"+user +"> "+messaggio);
							out.flush();
						}
					}

				}


			}

		}catch(UnknownHostException e){
			System.err.println("Host sconosciuto" + e.getMessage());
		}catch(IOException e){
			System.err.println("Connessione non riuscita" + e.getMessage());
		}catch (NoSuchElementException e){
			try {
				//rimuovo la mia connessione/utenza dalla lista e la chiudo
				for(int i=0;i<sockets.size();i++){
					if(sockets.elementAt(i) == sock){
						sockets.removeElementAt(i);
						utenti.removeElementAt(i);
					}
				}
				scan.close();
				sock.close();

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

			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}


}
