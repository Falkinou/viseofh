# Roadmap Orange Drone Compagnon - 19 etapes restantes

## 1. Responsive RC parfait

Objectif : chaque ecran doit etre utilisable sans perte de lisibilite sur RC Plus 2, RC 2 et emulateur.

Etat : en cours.

Actions :
- privilegier les vues paysage sans scroll quand l'information est courte ;
- garder les listes longues en scroll uniquement ;
- verifier les boutons en bas d'ecran et les retours accueil ;
- limiter les headers dans les modules.

## 2. Fond d'ecran final

Objectif : outil simple pour generer un JPG compatible RC.

Etat : fonctionnel, ergonomie a finaliser.

Actions :
- apercu compact ;
- choix RC Plus 2 / RC 2 ;
- QR code deplacable et zoomable ;
- modeles Orange valides ;
- export JPG fiable.

## 3. Export USB solide

Objectif : exporter log + medias associes au vol selectionne.

Etat : fonctionnel avec parcours guide.

Actions :
- dernier vol propose automatiquement ;
- choix manuel obligatoire possible ;
- progression visible ;
- popup de confirmation avec debut, fin et duree du vol ;
- ne jamais modifier les originaux DJI.

## 4. Diagnostic terrain

Objectif : savoir en 10 secondes pourquoi un test ne marche pas.

Etat : present dans Reglages et Info.

Actions :
- logs, USB, SDK DJI, droits Android, erreurs recentes ;
- export diagnostic texte ;
- messages clairs pour technicien non developpeur.

## 5. Mode test technicien

Objectif : pre-check avant intervention.

Etat : present dans Reglages.

Actions :
- dossier logs ;
- cle USB ;
- SDK DJI ;
- MSurvey ;
- export USB ;
- medias.

## 6. Point d'interet V1

Objectif : generer des KML carte DJI par departement ou zone.

Etat : fonctionnel.

Actions :
- carte France par departement ;
- recherche par departement ;
- recherche commune / GPS RC ;
- rayon 15 km ;
- export KML propre.

## 7. Conditions de vol V1

Objectif : aide terrain lisible, non substitutive a Orange Drone.

Etat : fonctionnel.

Actions :
- choix ville / position RC ;
- choix drone ;
- vent, rafales, temperature, pluie, visibilite, nuit aero ;
- METAR/TAF en clair ;
- style proche SkyGo, mais compatible Orange.

## 8. Lecture logs V1 stable

Objectif : retrouver et lire les FlightRecord DJI.

Etat : fonctionnel.

Actions :
- filtre par nom/date ;
- selection d'un log precis ;
- detail vol ;
- graphes utiles ;
- rapport PDF.

## 9. Rapport PDF professionnel

Objectif : produire une synthese exploitable SAV/REX.

Etat : present, a enrichir.

Actions :
- resume vol ;
- drone / batterie ;
- duree / distance / altitude ;
- points de vigilance ;
- signature application/version.

## 10. Consignes internes

Objectif : donner les bons documents terrain dans la RC.

Etat : present.

Actions :
- prevol ;
- post-vol ;
- batterie ;
- incident ;
- controle ;
- QR code doc interne.

## 11. MSurvey integre

Objectif : depot securise sans infrastructure dediee.

Etat : present via WebView.

Actions :
- ouvrir le formulaire ;
- garder le log selectionne comme contexte utilisateur ;
- eviter stockage de mot de passe.

## 12. Medias dans Export

Objectif : ne pas multiplier les tuiles.

Etat : regroupe dans Export logs et medias.

Actions :
- afficher les medias associes au vol ;
- preparer transfert depuis drone si SDK disponible ;
- afficher progression.

## 13. Automatisation USB

Objectif : brancher la cle et ne presque rien faire.

Etat : detection/autorisation en cours.

Actions :
- detecter branchement ;
- demander autorisation racine si necessaire ;
- proposer le dernier vol ;
- exporter apres confirmation.

## 14. Journal admin discret

Objectif : aider le support sans polluer l'usage terrain.

Etat : present dans Info.

Actions :
- filtres erreurs / USB / DJI / mail ;
- export diagnostic ;
- logs decode et evenements.

## 15. Mise a jour APK propre

Objectif : page web stable + version.json.

Etat : present.

Actions :
- lien stable `Orange-Drone-Compagnon.apk` ;
- release versionnee en option ;
- statistiques via GitHub Releases ;
- controle de mise a jour in-app.

## 16. Page web de telechargement

Objectif : installation facile depuis RC.

Etat : present.

Actions :
- nom Orange Drone Compagnon ;
- lien stable ;
- infos version ;
- stats telechargement ;
- future restriction Orange a definir.

## 17. Securite et conformite

Objectif : minimiser les risques.

Etat : en cours.

Actions :
- pas de FTP/SMTP visible par defaut ;
- pas de mot de passe en dur dans les sources ;
- permissions SAF ;
- aucun fichier DJI modifie ;
- exports explicites.

## 18. Preparation beta terrain

Objectif : test reproductible.

Etat : protocole a suivre.

Actions :
- installation ;
- dossier logs ;
- USB ;
- drone ;
- export ;
- rapport ;
- retour bug.

## 19. Documentation finale

Objectif : rendre le projet transmissible.

Etat : en cours.

Actions :
- README ;
- cahier des charges ;
- guide telepilote ;
- guide admin ;
- fiche installation RC.
