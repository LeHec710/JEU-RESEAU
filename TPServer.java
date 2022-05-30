import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.DataInputStream;

/**
 * @author Hector BIDAN
 *
 */
public class TPServer extends Frame {

    int port = 8082;
    ServerSocket serverSocket = null;
    DataInputStream in;
    DataOutputStream out;

    int size = 800;
    int gridSize = 10;
    byte etat[] = new byte[2 * gridSize * gridSize];
    Map<String, Socket> sockets = new HashMap<String, Socket>();

    /** Constructeur */
    public TPServer() {
        System.out.printf("%n%n");
        System.out.println("----- SERVEUR -----");
        System.out.printf("%n");

        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("En attente de joueurs...");
            while (!serverSocket.isClosed()) {
                Socket socket = this.serverSocket.accept();
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                // pour creer la grille du joueur en fonction
                // de la taille definie par le serveur
                out.writeInt(gridSize);
                out.writeInt(size);
                out.write(etat);

                // on recupere l'id du joueur et on
                // l'ajoute a la liste des joueurs connectés
                Integer playerId = in.readInt();
                int team = in.readInt();
                int x = in.readInt();
                int y = in.readInt();

                // lors d'un foreach, je ne peux pas acceder a l'element du tableau directement
                // j'ai donc trouver du code permettant de boucler sur un HashMap
                // source:
                // https://www.geeksforgeeks.org/remove-an-entry-using-key-from-hashmap-while-iterating-over-it/
                Iterator<Map.Entry<String, Socket>> socketsIt = sockets.entrySet().iterator();
                boolean idExist = false;
                while (socketsIt.hasNext()) {
                    Map.Entry<String, Socket> joueur = socketsIt.next();
                    if (Integer.parseInt(joueur.getKey()) == playerId) {
                        idExist = true;
                    }
                }
                if (idExist) {
                    out.write(255);
                    close(socket, -1, -1);
                    continue;
                } else {
                    out.write(0);
                    sockets.put(playerId.toString(), socket);
                }

                // on verifie aussi que la position est sur la grille
                if(x >= gridSize || y >= gridSize || x < 0 || y < 0) {
                    out.write(255);
                    close(socket, -1, -1);
                    continue;
                } else {
                    out.write(0);
                }

                // initialisation de la position
                // on envoie 255 si la position est deja utilisee,
                // sinon on envoie 0
                if (etat[(y * gridSize + x) * 2] == 0) {
                    etat[(y * gridSize + x) * 2] = 1;
                    etat[((y * gridSize + x) * 2) + 1] = (byte) team;
                    out.write(0);
                } else {
                    out.write(255);
                }


                System.out.println("Joueur #" + playerId + " connecte");

                // on verifie si le joueur ne bloque pas un adversaire 
                // en se connectant
                checkWinner();

                // on met a jour tous les clients
                sockets.forEach((pid, psock) -> {
                    try {
                        DataOutputStream pout = new DataOutputStream(psock.getOutputStream());
                        pout.write(10);
                        pout.write(etat);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                // creation du thread serveur
                TPServerThread TPServerthread = new TPServerThread(socket);
                TPServerthread.start();
            }
            System.out.println("FERMETURE DU SERVEUR");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkWinner() {
        // pour map la position de l'etat avec la position en x,y
        int _x = 0;
        int _y = 0;
        // on parcours toutes les cases
        for(int i=0; i< (gridSize * gridSize-1); i++) {
            // si on rencontre un joueur
            if(etat[2*i] == 1) {
                // on verifie si bloque par haut bas
                if(i >= gridSize && i < (gridSize-1) * gridSize) {
                    if(etat[2*(i - gridSize)] == 1 && etat[2*(i + gridSize)] == 1) {
                        // test equipe
                        if(etat[(2*(i - gridSize)) + 1] != etat[2*i+1] 
                        && etat[(2*(i + gridSize)) + 1] != etat[2*i+1]) {
                            byte[] perdant = {(byte) (2*i), (byte) (_x), (byte) _y};
                            
                            refreshAll();
                            sendAll(11);
                            sendAll(perdant);
                        }
                    }
                }

                // on verifie si bloque pas gauche droite
                if(i % gridSize != 0 && i % gridSize != gridSize - 1) {
                    if(etat[2*(i - 1)] == 1 && etat[2*(i + 1)] == 1) {
                        if(etat[(2*(i - 1)) + 1] != etat[2*i+1]
                        && etat[(2*(i + 1)) + 1] != etat[2*i+1]) {
                            byte[] perdant = { (byte) (2 * i), (byte) _x, (byte) _y };

                            refreshAll();
                            sendAll(11);
                            sendAll(perdant);
                        }
                    }
                }
            }

            // on incremente la position
            _x++;
            if(i % gridSize == gridSize - 2) {
                _x = 0;
                _y++;
            }
        }
    }

    public void close(Socket socket, int px, int py) {
        try {
            // lors d'un foreach, je ne peux pas acceder a l'element du tableau directement
            // j'ai donc trouver du code permettant de boucler sur un HashMap
            // source:
            // https://www.geeksforgeeks.org/remove-an-entry-using-key-from-hashmap-while-iterating-over-it/
            Iterator<Map.Entry<String, Socket>> socketsIt = sockets.entrySet().iterator();
            while (socketsIt.hasNext()) {
                Map.Entry<String, Socket> joueur = socketsIt.next();
                if (joueur.getValue().equals(socket)) {
                    socketsIt.remove();
                    System.out.println("Joueur #" + joueur.getKey() + " deconnecte");

                    // on reffraichit si necessaire
                    if (px != -1 && py != -1) {
                        etat[(py * gridSize + px) * 2] = 0;
                        etat[(py * gridSize + px) * 2 + 1] = 0;
                    }
                    refreshAll();
                }
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void quit() {
            System.out.println("Tous les clients sont deconnectes, fermeture du serveur");
            System.out.printf("%n");
            System.out.println("----- FIN SERVEUR -----");
            System.exit(0);
    }

    public void refreshAll() {
        sockets.forEach((pid, psock) -> {
            try {
                DataOutputStream pout = new DataOutputStream(psock.getOutputStream());
                pout.write(10);
                pout.write(etat);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void sendAll(byte[] bytes) {
        sockets.forEach((pid, psock) -> {
            try {
                DataOutputStream pout = new DataOutputStream(psock.getOutputStream());
                pout.write(bytes);
            } catch(IOException e) {
                e.printStackTrace();
            }

        });
    }
    public void sendAll(int msg) {
        sockets.forEach((pid, psock) -> {
            try {
                DataOutputStream pout = new DataOutputStream(psock.getOutputStream());
                pout.write(msg);
            } catch(IOException e) {
                e.printStackTrace();
            }

        });
    }

    public static void main(String[] args) {
        try {
            TPServer TPServer = new TPServer();

            // Pour fermeture
            TPServer.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class TPServerThread extends Thread {
        Socket socket;
        DataOutputStream out;
        DataInputStream in;

        public TPServerThread(Socket psocket) {
            socket = psocket;
        }

        public void run() {
            try {
                out = new DataOutputStream(socket.getOutputStream());
                in = new DataInputStream(socket.getInputStream());

                boolean send = false;
                int pdirection = -1;
                int pid = -1;
                int pteam = -1;
                int px = -1;
                int py = -1;

                // CODE
                // 20 -> mouvement
                // 21 -> direction droite
                while (!socket.isClosed()) {
                    int msg = in.readInt();

                    // fermeture
                    if (msg == 254) {
                        int x = in.readInt();
                        int y = in.readInt();
                        close(socket, x, y);
                        return;
                    }
                    if (msg == 253) {
                        close(socket, -1, -1);
                        return;
                    }

                    // refresh
                    if(msg == 10) {
                        in.readFully(etat);
                        refreshAll();
                    }

                    // deplacement
                    if (msg == 20) {
                        pdirection = in.readInt();
                        pid = in.readInt();
                        pteam = in.readInt();
                        px = in.readInt();
                        py = in.readInt();

                        // direction droit

                        if (pdirection == 21 && px != gridSize-1) {
                            if (etat[(py * gridSize + px + 1) * 2] == 0) {
                                // on eleve l'ancien emplacement du joueur
                                etat[(py * gridSize + px) * 2] = 0;
                                etat[((py * gridSize + px) * 2) + 1] = 0;

                                // on met le nouveau
                                etat[(py * gridSize + px + 1) * 2] = 1;
                                etat[((py * gridSize + px + 1) * 2) + 1] = (byte) pteam;

                                send = true;
                            }
                        }

                        // direction gauche
                        if (pdirection == 22 && px != 0) {
                            if (etat[(py * gridSize + px - 1) * 2] == 0) {
                                // on eleve l'ancien emplacement du joueur
                                etat[(py * gridSize + px) * 2] = 0;
                                etat[((py * gridSize + px) * 2) + 1] = 0;

                                // on met le nouveau
                                etat[(py * gridSize + px - 1) * 2] = 1;
                                etat[((py * gridSize + px - 1) * 2) + 1] = (byte) pteam;

                                send = true;
                            }
                        }

                        // direction haut
                        if (pdirection == 23 && py != 0) {
                            if (etat[(py * gridSize + px - gridSize) * 2] == 0) {
                                // on eleve l'ancien emplacement du joueur
                                etat[(py * gridSize + px) * 2] = 0;
                                etat[((py * gridSize + px) * 2) + 1] = 0;

                                // on met le nouveau
                                etat[(py * gridSize + px - gridSize) * 2] = 1;
                                etat[((py * gridSize + px - gridSize) * 2) + 1] = (byte) pteam;

                                send = true;
                            }
                        }

                        // direction bas
                        if (pdirection == 24 && py != gridSize-1) {
                            if (etat[(py * gridSize + px + gridSize) * 2] == 0) {
                                // on eleve l'ancien emplacement du joueur
                                etat[(py * gridSize + px) * 2] = 0;
                                etat[((py * gridSize + px) * 2) + 1] = 0;
                                // on met le nouveau
                                etat[(py * gridSize + px + gridSize) * 2] = 1;
                                etat[((py * gridSize + px + gridSize) * 2) + 1] = (byte) pteam;

                                send = true;
                            }
                        }

                        if (send) {
                            final int fpid = pid;
                            final int fpdirection = pdirection;
                            
                            // on recupere le socket du joueur
                            sockets.forEach((_pid, _psock) -> {
                                try {
                                    DataOutputStream pout = new DataOutputStream(_psock.getOutputStream());
                                    if (fpid == Integer.parseInt(_pid)) {
                                        pout.write(fpdirection);
                                        pout.write(etat);
                                    } else {
                                        pout.write(10);
                                        pout.write(etat);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            send = false;

                            //lorsque quelqu'un bouge, on verifie si il y a une equipe gagnante
                            checkWinner();                          
                        }

                        // reinitialisation des variables
                        pdirection = -1;
                        pid = -1;
                        pteam = -1;
                        px = -1;
                        py = -1;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
