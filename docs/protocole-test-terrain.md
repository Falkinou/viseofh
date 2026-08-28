# Protocole de test terrain - Orange Drone Compagnon

Version cible : 1.27

## Objectif

Verifier que l'application est utilisable par un telepilote sur radiocommande DJI sans manipulation technique.

## Materiel

- Radiocommande DJI compatible.
- Drone DJI Enterprise compatible.
- Cle USB formatee et vide de preference.
- Au moins un vol recent dans le dossier DJI FlightRecord.
- Connexion internet pour MSurvey, meteo et mise a jour.

## Preparation

1. Installer l'APK `Orange-Drone-Compagnon.apk`.
2. Verifier que l'application s'appelle bien Orange Drone Compagnon.
3. Ouvrir l'application.
4. Aller dans Reglages.
5. Choisir le dossier log de vol DJI.
6. Brancher la cle USB.
7. Choisir la racine de la cle USB.
8. Revenir a l'accueil.

## Tests obligatoires

### Accueil

- Le header doit afficher le dernier vol si un log est trouve.
- Drone doit etre `Non connecte` si le drone est eteint.
- USB doit etre rouge si non autorise, vert si autorise.
- Les tuiles doivent rentrer dans l'ecran paysage sans manipulation excessive.

### Export logs et medias

- Le dernier vol doit etre selectionne automatiquement.
- L'utilisateur doit pouvoir choisir un autre log.
- Le bouton principal doit proposer l'export log + medias si USB prete.
- La popup doit afficher :
  - nom du log ;
  - debut du vol ;
  - fin du vol ;
  - duree ;
  - destination USB.
- Le transfert doit afficher une progression.
- Les fichiers DJI originaux ne doivent jamais etre modifies.

### Depot MSurvey

- Le module doit ouvrir `https://msurvey.orange.com/dronelog`.
- Aucun mot de passe applicatif ne doit etre demande par Orange Drone Compagnon.

### Consultation des logs

- La liste des logs doit etre visible.
- La recherche par nom/date doit fonctionner.
- Un log selectionne doit ouvrir une fiche detail.
- Le PDF doit pouvoir etre exporte.

### Conditions de vol

- La recherche par ville doit fonctionner si internet disponible.
- Le choix du drone doit modifier les limites vent/rafales.
- METAR/TAF doivent etre affiches en clair si disponibles.
- La page doit rappeler que ce n'est pas une analyse reglementaire.

### Point d'interet

- Selectionner un departement.
- Verifier que le nombre de sites evolue.
- Tester une recherche autour d'une commune.
- Exporter un KML.

### Fond d'ecran

- Choisir RC 2 ou RC Plus 2.
- Selectionner un modele.
- Deplacer le QR code.
- Exporter en JPG.

### Info / Diagnostic

- Ouvrir le bouton information.
- Acceder au diagnostic.
- Verifier les erreurs recentes.
- Exporter le diagnostic texte.

## Criteres de validation

- Aucun crash pendant 15 minutes d'utilisation.
- Aucun ecran illisible sur RC en paysage.
- Aucun fichier DJI original modifie ou supprime.
- Export USB fonctionne avec au moins un log.
- MSurvey s'ouvre correctement.
- Diagnostic explique les problemes USB ou DJI.

## Remontee d'anomalie

Pour chaque anomalie, relever :

- modele RC ;
- modele drone ;
- version application ;
- action realisee ;
- message affiche ;
- capture ecran si possible ;
- diagnostic exporte depuis l'application.
