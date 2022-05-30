![La Rochelle université](https://www.univ-larochelle.fr/wp-content/uploads/gif/loader2019-1.gif)

# PROJET TEA
Jeu en réseaux local pour un projet de cours (réseaux-transmission)
par Hector BIDAN 2022
hector.bidan@gmail.com
hector-bidan.fr

## Installation 

Dezippez l'archive et accédez au dossier dans la console
```sh
cd projet
```
Vérifiez que tous les fichiers soient bien présents
- TPPanel.java
- TPCanvas.java
- TPClient.java
- TPServer.java
 

Compilation et lancement du serveur
```
Javac TPServer.java
Java TPServer
```

Idem pour le joueur (après le lancement du serveur):
```
Javac TPClient.java
Java TPClient <id> <equipe> <px> <y>
```

| Paramètre | Nom | Valeur |
| ------ | ------ | ------ |
| id | identifiant | int |
| equipe | equipe | "noir", "blanc" |
| x | position x | int |
| y | position y | int |

 
## CODE DE COMMUNICATION
Voici la référence des codes de communication utilisées par le serveur et le client.


| Code | Action | 
| ------ | ------ | 
| 254 | Fermeture du client et le retirer du plateau| 
| 253 | Fermeture du client | 
| 10 | Rafraîchir tous les clients | 
| 11 | Élimination d’un joueur | 
| 20 | Demande de déplacement | 
| 21 | Déplacement à droite | 
| 22 | Déplacement à gauche | 
| 23 | Déplacement en haut | 
| 24 | Déplacement en bas | 


[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO -
    ![alt text](https://github.com/[username]/[reponame]/blob/[branch]/image.jpg?raw=true)

