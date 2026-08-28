# Matrice de reponse au cadrage national

## RC-01 Consultation des logs de vol

Statut : couvert.

Fonction app : `Consultation des logs`

Reponse :
- liste des logs DJI presents sur la RC ;
- recherche par nom ou date ;
- ouverture d'un log precis ;
- decodage FlightRecord ;
- synthese et rapport PDF.

Reste a renforcer :
- filtres mission/drone/duree plus avances.

## RC-02 Export des logs sur cle USB

Statut : couvert.

Fonction app : `Export logs et medias`

Reponse :
- selection du dernier log ou d'un log choisi ;
- export USB ;
- dossier structure ;
- progression visible ;
- original DJI non modifie.

Reste a renforcer :
- validation sur vraie RC avec plusieurs cles USB.

## RC-03 Consultation des medias de mission

Statut : partiellement couvert.

Fonction app : `Export logs et medias`

Reponse :
- association des medias au creneau horaire du log ;
- integration prevue via SDK DJI et dossier media local.

Reste a renforcer :
- galerie complete ;
- filtres type/date/drone ;
- confirmation terrain avec drone connecte.

## RC-04 Export des medias sur cle USB

Statut : couvert en premiere version.

Fonction app : `Export logs et medias`

Reponse :
- export log + medias associes ;
- progression transfert ;
- USB prioritaire.

Reste a renforcer :
- robustesse du transfert radio RC/drone.

## RC-05 Personnalisation des radiocommandes Orange

Statut : couvert.

Fonction app : `Fond d'ecran`

Reponse :
- choix RC Plus 2 / RC 2 ;
- modeles Orange ;
- QR code ;
- export JPG.

Reste a renforcer :
- ergonomie finale de l'editeur ;
- bibliotheque de modeles valides.

## RC-06 Affichage des consignes internes

Statut : couvert.

Fonction app : `Consignes internes`

Reponse :
- acces rapide aux consignes ;
- QR code documentaire ;
- diagnostic admin et guide terrain.

Reste a renforcer :
- contenu documentaire Orange final.

## RC-07 Referentiel embarque des sites mobiles Orange

Statut : couvert.

Fonction app : `Point d'interet`

Reponse :
- dataset embarque ;
- selection departement ;
- recherche commune/GPS ;
- rayon 15 km ;
- carte France coloree.

Reste a renforcer :
- mise a jour periodique du referentiel.

## RC-08 Export de sites Orange vers l’application de vol DJI

Statut : couvert.

Fonction app : `Point d'interet`

Reponse :
- export KML ;
- departements selectionnes ;
- sites autour de la position ;
- fichier exploitable dans l’application de vol DJI.

Reste a renforcer :
- validation import l’application de vol DJI sur RC reelle.

## Hors cadrage mais utile

### Conditions de vol

Aide terrain : meteo, METAR/TAF, nuit aeronautique, limites par drone.

Important : ne remplace pas l'analyse reglementaire ni Orange Drone.

### Diagnostic

Outil support pour comprendre les problemes de droits Android, USB, logs, SDK DJI et erreurs recentes.
