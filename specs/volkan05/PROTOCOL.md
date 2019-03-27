### What transport protocol do we use ?

TCP

### How does the client find the server (addresses and ports) ?

En tapant une adresse ip comme 192.168.2.5 et le port 2020

### Who speaks first ?

Client initie la communication en envoyant une requête INIT

Le serveur lui renvoie une liste de commandes (options) à entrer par l'utilisateur

### What is the sequence of messages exchanged by the client and the server ?

Le client envoie le INIT -> le serveur renvoie la liste des options -> le client envoie sa requête de calcul -> le serveur renvoie la réponse

S'il y a une erreur, le serveur envoit un message d'erreur et demande une nouvelle saisie.

Le client peut faire autant de requête qu'il le souhaite.

### What happens when a message is received from the other party ?

Si c'est le serveur qui reçoit: si c'est une initalisation, il renvoie la liste sinon il calcule et envoit le résultat

Si c'est le client, il lit le message reçu

### What is the syntax of the messages? How we generate and parse them ? 

**Exemple sans erreur:** 

Client: INIT -> 					DIV nbre1 nbre2 ->						recup result

Serveur:		liste d'options->				         Result = nbre1 / nbre2 ->

**Exemple avec erreur:**

Client: INIT -> 					DIV 2 0 ->					   Entrer à nouveau

Serveur:		liste d'options->			Result = 2/0 ->Erreur->

### Who closes the connection and when ?

Le client arrête la connexion lorsqu'il a terminé et le serveur fermera la connexion établie au départ.