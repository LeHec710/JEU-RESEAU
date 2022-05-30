import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;

/**
 * @author Hector BIDAN
 *
 */
public class TPClient extends Frame {
	int team;
	int x;
	int y;
	int number;
	int port = 8082;
	boolean can_move = true;
	Socket socket = null;
	DataInputStream in;
	DataOutputStream out;
	TPPanel tpPanel;
	TPCanvas tpCanvas;
	int gridSize;
	int size;
	byte[] etat;
	

	public TPClient(int pnumber, int pteam, int px, int py) {
		
		System.out.printf("%n%n");
		System.out.println("----- CLIENT #" + pnumber + " -----");
		System.out.printf("%n");

		try {
			socket = new Socket("127.0.0.1", port);
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			System.out.println("connexion etablie");

			// on defini la taille de la grille
			// definie par le serveur
			int gridSize = in.readInt();
			size = in.readInt();
			etat = new byte[gridSize * gridSize * 2];

			// creation de l'environnement de jeu
			setLayout(new BorderLayout());
			tpPanel = new TPPanel(this);
			add("North", tpPanel);
			tpCanvas = new TPCanvas(etat, gridSize, size);
			add("Center", tpCanvas);

			in.readFully(etat);

			// initialisation des attributs du joueur
			number = pnumber;
			team = pteam;
			x = px;
			y = py;

			// Pour fermeture
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					exit("", true);
				}
			});

			// envoie des infos
			out.writeInt(number);
			out.writeInt(team);
			out.writeInt(x);
			out.writeInt(y);
			
			// on quitte si l'id existe deja
			int canEnterId = in.read();
			if(canEnterId == 255) {
				exit("Votre identifiant est deja utilise", false);
			}
			
			// on quitte si la position est en dehors de la grille
			int canEnterPosGrid = in.read();
			if(canEnterPosGrid == 255) {
				exit("Votre postion n'est pas sur la grille " + gridSize + "x" + gridSize + ": (0 <= x, y <= " + (gridSize-1) + ")", false);
			}

			// on quitte si la position existe deja
			int canEnterPos = in.read();
			if(canEnterPos == 255) {
				exit("Votre position est deja utilisee par un autre joueur", false);
			}

			// lancement du thread
			new TPClientThread(socket).start();
			
			// creation de la fenetre
			pack();
			setSize(size + 16, size + 72);
			setVisible(true);
			setResizable(false);
			setTitle("Client #" + number);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/** Action vers droit */
	public synchronized void droit() {
		try {
			// envoi des infos au serveur
			if(can_move) {
				out.writeInt(20); // 20 -> code action mouvement
				out.writeInt(21); // 21 -> code action direction droite
				sendInfo(number, team, x, y);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/** Action vers gauche */
	public synchronized void gauche() {
		try {
			// envoi des infos au serveur
			if(can_move) {
				out.writeInt(20); // 20 -> code action mouvement
				out.writeInt(22); // 22 -> code action direction gauche
				sendInfo(number, team, x, y);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/** Action vers gauche */
	public synchronized void haut() {
		try {
			// envoi des infos au serveur
			if(can_move) {
				out.writeInt(20); // 20 -> code action mouvement
				out.writeInt(23); // 23 -> code action direction gauche
				sendInfo(number, team, x, y);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/** Action vers bas */
	public synchronized void bas() {
		try {
			// envoi des infos au serveur
			if(can_move) {
				out.writeInt(20); // 20 -> code action mouvement
				out.writeInt(24); // 24 -> code action direction gauche
				sendInfo(number, team, x, y);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void sendInfo(int pid, int pteam, int px, int py) {
		try {
			out.writeInt(pid);
			out.writeInt(team);
			out.writeInt(px);
			out.writeInt(py);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/** Pour rafraichir la situation */
	public synchronized void refresh() {
		try {
			in.readFully(etat);
		} catch (IOException e) {
			e.printStackTrace();
		}
		tpCanvas.repaint();
	}

	public void exit(String message, boolean updatePos) {
		System.out.println("Fermeture en cours...");
		if(message != "") {
			System.out.println(message);
		}
		System.out.printf("%n");
		System.out.println("----- FIN CLIENT #" + number + " -----");
		System.out.printf("%n");
		try {
			if(updatePos) {
				out.writeInt(254);
				out.writeInt(x);
				out.writeInt(y);
			} else {
				out.writeInt(253);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// verification des parametres
		if (args.length != 4) {
			System.out.println("Usage : java TPClient id couleur positionX positionY");
			System.exit(0);
		}

		// l'argument de l'équipe doit valoir "bleu" ou "rouge"
		int team = -1;
		if (args[1].equals("blanc")) {
			team = 1;
		} else if (args[1].equals("noir")) {
			team = 2;
		} else {
			System.out.println("L'argument couleur doit valoir 'noir' ou 'blanc'");
			System.exit(0);
		}

		// conversion des paramètres (string -> int)
		int number = Integer.parseInt(args[0]);
		int x = Integer.parseInt(args[2]);
		int y = Integer.parseInt(args[3]);

		try {
			// creation d'un nouveau client
			new TPClient(number, team, x, y);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class TPClientThread extends Thread {
		Socket socket;
		DataOutputStream out;
		DataInputStream in;

		public TPClientThread(Socket psocket) {
			socket = psocket;
		}

		public void run() {
			try {
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());

				// CODE
				// 255 -> ne rien faire
				// 10 -> refresh la page
				boolean refresh = false;
				while (!socket.isClosed()) {
					int msg = in.read();
					// refresh
					if (msg == 10) {
						refresh = true;
					}

					// deplacement
					switch(msg) {
						case 21:
							x++;
							refresh = true;
							break;
						case 22:
							x--;
							refresh = true;
							break;
						case 23:
							y--;
							refresh = true;
							break;
						case 24:
							y++;
							refresh = true;
							break;
					}

					// a perdu
					if (msg == 11) {
						// 0 = posEtat
						// 1 = posX
						// 2 = posY
						byte[] perdant = new byte[3];
						in.readFully(perdant);

						if ((int) perdant[1] == x+1 && (int) perdant[2] == y) {
							// perdant
							etat[perdant[0] + 1] = 4;
							System.out.println("Partie terminee, vous avez perdu");
							can_move = false;
						} else {
							// gagnant (kill)
							etat[perdant[0] + 1] = 3;
						}
						try {
							out.writeInt(10);
							out.write(etat);
						} catch(IOException e) {
							e.printStackTrace();
						}
						tpCanvas.repaint();
					}

					if (refresh) {
						refresh();
						refresh = false;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
