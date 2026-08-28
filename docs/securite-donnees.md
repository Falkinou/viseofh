# Securite et donnees

## Donnees lues

Orange Drone Compagnon lit uniquement les donnees selectionnees ou autorisees par l'utilisateur :

- dossier des logs DJI FlightRecord ;
- racine de la cle USB ;
- dossier media local si configure ;
- donnees meteo publiques pour le module Conditions de vol ;
- referentiel embarque des sites Orange pour le module Point d'interet.

## Donnees modifiees

L'application ne modifie jamais les logs DJI originaux.

Les actions d'ecriture sont limitees a :

- copie de logs sur cle USB ;
- copie de medias sur cle USB ;
- export JPG de fond d'ecran ;
- export PDF de rapport ;
- export KML de points d'interet ;
- export diagnostic texte.

## Canaux de partage

Canaux actifs :

- USB ;
- MSurvey ;
- GitHub Release pour telechargement APK ;
- Open-Meteo / AviationWeather pour meteo.

Canaux historiques neutralises pour l'usage terrain :

- FTP ;
- SMTP embarque.

## Secrets

Le depot ne doit contenir aucun mot de passe.

Les mots de passe FTP/SMTP historiques et la clé DJI locale sont chiffrés dans
Android Keystore avant leur stockage dans DataStore. Les anciennes valeurs
restées en clair sont migrées au prochain chargement des réglages.

La configuration DJI Mobile SDK impose de conserver l'`applicationId` Android historique tant qu'une nouvelle cle DJI n'est pas generee.

Les APK de mise à jour sont téléchargés uniquement depuis les domaines
autorisés en HTTPS, puis contrôlés par taille et empreinte SHA-256 avant de
proposer l'installation Android.

## Permissions Android

L'acces aux fichiers passe par le Storage Access Framework quand c'est possible.

Le technicien doit autoriser :

- le dossier FlightRecord ;
- la racine de la cle USB ;
- eventuellement le dossier media local.
