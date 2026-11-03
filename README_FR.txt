DUNEDESERT — plugin Paper 26.3
================================

Ce projet contient le CODE SOURCE du plugin. Il ne s'agit pas encore du fichier .jar compilé.

Fonctions :
- Monde normal avec biome désert partout.
- Grandes dunes douces et vallées, sans hautes montagnes.
- Aucun bruit de terrain vanilla, aucune grotte, aucune structure ni décoration vanilla.
- Sous-sol solide en grès, sans minerais générés.
- Sur les 4 couches supérieures, chaque bloc est choisi indépendamment :
  50 % sable, 20 % grès, 20 % sable suspect, 10 % End Stone.
- Monde créé via /dunes create <nom>, puis accès via /dunes tp <nom>.

COMPILATION
-----------
Prérequis : Java 25 et Gradle récent.
Dans le dossier du projet, lancer :
    gradle build

Le fichier généré sera :
    build/libs/DuneDesert-1.0.0.jar

INSTALLATION
------------
1. Fais une sauvegarde du serveur.
2. Arrête le serveur Paper 26.3.
3. Place DuneDesert-1.0.0.jar dans le dossier plugins/.
4. Redémarre le serveur.
5. En jeu avec les permissions OP : /dunes create desert
6. Pour revenir plus tard : /dunes tp desert

IMPORTANT
---------
- Le plugin crée un NOUVEAU monde nommé comme indiqué. Il ne transforme pas les mondes existants.
- Ne supprime pas un monde existant sans sauvegarde.
- Les 4 probabilités s'appliquent aux quatre couches supérieures du terrain,
  pas à tout le sous-sol.
- Le sable suspect généré en tant que bloc peut ne pas contenir automatiquement
  de butin archéologique. Dans cette première version, il sert surtout au rendu.
- Le plugin est préparé pour Paper 26.3 / Java 25, mais doit être compilé puis
  testé sur une copie de ton serveur avant utilisation en production.
