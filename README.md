# Orange Drone Compagnon

Boite a outils Android pour radiocommande DJI Enterprise type RC Plus 2.
La priorite actuelle est l'usage terrain sans infrastructure reseau Orange :
export USB des logs et medias, depot MSurvey, lecture de logs, fonds d'ecran RC,
consignes internes, points d'interet KML et aide conditions de vol.

Modules disponibles :

- `Export logs et medias` : export USB ou depot MSurvey ;
- `Consultation des logs` : lecture et controle des logs de vol ;
- `Fond d'ecran` : preparation de fonds d'ecran pour radiocommande ;
- `Consignes internes` : acces aux informations utiles au telepilote ;
- `Point d'interet` : export KML de sites Orange vers l’application de vol DJI ;
- `Conditions de vol` : aide meteo, METAR/TAF proche et profils drone.

La version actuelle fait volontairement peu de choses, mais les fait de facon fiable :

- detection automatique du dossier DJI `FlightRecord` connu ;
- choix manuel du dossier logs uniquement en secours si Android bloque l'acces direct ;
- detection des fichiers `.txt` uniquement ;
- aucun fichier DJI n'est modifie, deplace ou supprime ;
- export USB d'un fichier par log, sans infra reseau ;
- copie optionnelle des photos/videos DJI prises pendant le meme vol ;
- historique local Room avec statut `En attente`, `Envoye` ou `Erreur` ;
- journal local des evenements de synchronisation ;
- reessai automatique via WorkManager toutes les 15 minutes minimum ;
- relance automatique de la synchronisation apres demarrage Android et apres mise a jour APK ;
- relance d'une synchronisation quand Android signale le branchement d'un peripherique USB ;
- verification de mise a jour depuis `viseofh.fr/odc/version.json` ;
- module `Consultation des logs` avec decodage local des metadonnees DJI ;
- module `Fond d'ecran` avec champs et apercu de fond d'ecran ;
- bouton `Synchroniser maintenant` ;
- bouton `Reessayer erreurs` ;
- bouton `Tester la connexion`.

## Stack

- Kotlin
- Jetpack Compose
- WorkManager
- Room
- DataStore Preferences
- Storage Access Framework
- DJI Mobile SDK V5
- Open-Meteo pour la meteo sans cle API
- AviationWeather.gov pour METAR/TAF publics
- USB + MSurvey comme canaux terrain prioritaires
- Min SDK 26, target/compile SDK 35

Le projet utilise DJI Mobile SDK V5.18.0, qui prend en charge la famille
Matrice 4E/4T. Le diagnostic affiche le type de produit, le firmware du drone,
la radiocommande et son firmware afin de repérer rapidement une incompatibilité
de version sur le terrain.

## Configuration USB recommandee

Ce mode est a privilegier quand aucune infrastructure Orange simple n'est
disponible.

Dans `Reglages`, configurer d'abord les dossiers essentiels :

1. brancher la cle USB sur la radiocommande ;
2. appuyer sur `Choisir dossier sur cle USB` ;
3. choisir le dossier racine de la cle ou un dossier dedie ;
4. optionnellement appuyer sur `Choisir dossier photos/videos DJI` ;
5. laisser `Inclure medias du vol` active si les photos/videos doivent etre copiees ;
6. appuyer sur `Enregistrer`, puis `Tester la connexion`.

Android demandera une autorisation au premier choix du dossier. Orange Drone Compagnon
memorise cette autorisation via le Storage Access Framework.

Les exports sont ranges sous cette forme :

```text
OrangeDroneCompagnon/
  UAS-FR-01/
    2026-06-30/
      DJIFlightRecord_xxx/
        FlightRecord/
          DJIFlightRecord_xxx.txt
        Media/
          DJI_0001.JPG
          DJI_0002.MP4
```

Les medias sont selectionnes par date : l'application utilise l'heure de depart
du vol, sa duree, puis ajoute une marge de securite avant/apres le vol. Les
originaux ne sont jamais modifies, deplaces ou supprimes.

## Menu outils

Le menu deroulant en haut a droite permet de passer entre :

1. `Export logs et medias`
2. `Consultation des logs`
3. `Fond d'ecran`
4. `Consignes internes`
5. `Point d'interet`
6. `Conditions de vol`

## Conditions de vol

Le module `Conditions de vol` est une aide terrain rapide. Il ne remplace pas
l'analyse reglementaire ni la preparation de mission dans Orange Drone.

Fonctions actuelles :

- recherche par ville ;
- meteo actuelle via Open-Meteo ;
- estimation vent, rafales, temperature, precipitation, nebulosite,
  visibilite et nuit aeronautique ;
- choix du profil drone : Mavic 3E, Matrice 4D ou Matrice 400 ;
- comparaison des vents et rafales aux limites du profil choisi ;
- recherche de la station aviation proche dans une liste embarquee ;
- recuperation du METAR et du TAF publics via AviationWeather.gov ;
- affichage brut et resume lisible du METAR/TAF.

Sources appelees :

```text
https://geocoding-api.open-meteo.com/v1/search
https://api.open-meteo.com/v1/forecast
https://aviationweather.gov/api/data/metar
https://aviationweather.gov/api/data/taf
```

## Consultation des logs

Le lecteur decode localement les metadonnees disponibles dans les fichiers DJI
`FlightRecord` :

- version du format DJI ;
- date de depart ;
- duree ;
- distance ;
- hauteur maximale ;
- vitesses maximales ;
- point de depart ;
- modele detecte ;
- numeros de serie drone, radiocommande, camera et batterie quand disponibles.

L'analyse se lance log par log depuis la liste `Logs de vol`. L'application ne
force pas l'analyse de tout l'historique, afin de laisser le technicien choisir
le vol exact a controler.

Pour les logs DJI recents, renseigner l'`App Key DJI FlightRecord` dans
les reglages avances. L'application extrait la demande `keychainsArray` du log choisi
et appelle l'API DJI :

```text
POST https://dev.dji.com/openapi/v1/flight-records/keychains
```

La cle DJI est stockee localement avec les autres reglages. Ne pas la diffuser
publiquement.

Les logs DJI recents, version 13 et plus, protegent les trajectoires detaillees
par chiffrement. Avec une App Key DJI valide, l'application tente de recuperer
la keychain officielle et affiche le nombre de points trajectoire exploitables.

## OrangeScreen

Le module `OrangeScreen` prepare un fond d'ecran RC a partir de :

- modeles visuels Orange integres ;
- photo personnalisee optionnelle ;
- format RC Plus 2 ou RC 2 ;
- QR code affichable, deplacable et redimensionnable.

La version actuelle fournit l'apercu interactif, la sauvegarde du modele et
l'export JPG direct vers un dossier choisi ou via le selecteur Android.

## Installation developpement

Option sans Android Studio, deja preparee sur cette machine :

```bash
./gradlew assembleDebug
```

Outils installes localement :

- JDK 17 via Homebrew ;
- Gradle wrapper dans le projet ;
- Android command-line tools ;
- Android SDK 35 ;
- Android platform-tools pour `adb`.

Option avec Android Studio :

1. Ouvrir ce dossier dans Android Studio.
2. Laisser Android Studio synchroniser Gradle.
3. Verifier que le SDK Android 35 est installe.
4. Brancher la DJI RC Plus 2 en USB avec le debogage USB active, ou utiliser un emulateur Android pour test.
5. Lancer la configuration `app`.

## Options reseau masquees

Les briques FTP/FTPS et SMTP existent encore dans le code, mais elles sont
masquees dans l'interface pour simplifier l'usage terrain. Le mode prioritaire
est l'export USB.

## Détection des logs de vol

Au premier lancement, aucun choix de dossier logs n'est requis dans le parcours normal.
Orange Drone Compagnon scanne automatiquement l'emplacement DJI Enterprise connu.

L'emplacement principal est :

```text
/sdcard/DJI/com.dji.industry.pilot/FlightRecord/
```

L'application essaie aussi la variante Android courante :

```text
/storage/emulated/0/DJI/com.dji.industry.pilot/FlightRecord/
```

Si Android ou DJI bloque l'acces direct, le choix manuel du dossier reste
disponible dans les reglages comme solution de secours. Dans ce cas,
l'application conserve une permission de lecture persistante via le Storage
Access Framework.

## Fonctionnement

WorkManager lance une synchronisation periodique toutes les 15 minutes minimum,
limite imposee par Android.

A chaque synchronisation :

1. l'emplacement DJI FlightRecord est scanne automatiquement ;
2. seuls les fichiers dont le nom termine par `.txt` sont pris en compte ;
3. chaque log est identifie par `nom + taille + date de modification` ;
4. les nouveaux logs sont ajoutes en base locale avec le statut `En attente` ;
5. leurs metadonnees DJI sont analysees localement quand le format le permet ;
6. les logs `En attente` ou `Erreur` sont transferes vers la destination choisie ;
7. en mode USB, seul le dossier USB autorise est requis ;
8. un log passe a `Envoye` uniquement apres confirmation d'export USB ;
9. en cas d'erreur USB, le log reste reessayable.

En mode USB, le chemin local est :

```text
OrangeDroneCompagnon/{identifiant_radio}/{date}/{nom_du_vol}/FlightRecord/{nom_fichier}
```

## Compilation APK

Depuis Android Studio :

1. menu `Build` ;
2. `Build Bundle(s) / APK(s)` ;
3. `Build APK(s)`.

L'APK standard Android sera genere dans :

```text
app/build/outputs/apk/debug/app-debug.apk
```

Le projet fournit aussi une tache qui copie l'APK avec le nom produit :

```bash
./gradlew :app:packageOrangeDroneCompagnonApk
./gradlew :app:packageOrangeDroneCompagnonLatestApk
```

Fichier final a diffuser :

```text
dist/Orange-Drone-Compagnon-<version>.apk
dist/Orange-Drone-Compagnon.apk
```

Publier aussi le fichier de version pour la verification de mise a jour :

```text
dist/odc/version.json
```

Pour une version signee :

1. menu `Build` ;
2. `Generate Signed Bundle / APK` ;
3. choisir `APK` ;
4. creer ou selectionner un keystore ;
5. compiler en `release`.

## Installation sur DJI RC Plus 2

### Depuis la page web interne

L'APK peut etre depose dans le dossier web OVH :

```text
www/odc/
```

La page fournie dans `dist/odc/index.html` expose un bouton de telechargement.
Une fois publiee, ouvrir depuis la radiocommande :

```text
https://votre-domaine/odc/
```

Puis telecharger et installer `Orange-Drone-Compagnon.apk`. Android peut demander
d'autoriser l'installation depuis le navigateur utilise sur la radiocommande.

### Depuis ADB

1. Activer les options developpeur et le debogage USB sur la radiocommande.
2. Brancher la RC Plus 2 au poste Android Studio.
3. Installer depuis Android Studio, ou via ADB :

```bash
adb install dist/Orange-Drone-Compagnon.apk
```

4. Lancer Orange Drone Compagnon.
5. Verifier que l'assistant indique les logs DJI en mode automatique.
6. Choisir le dossier sur la cle USB.
7. Tester la connexion USB.
8. Appuyer sur `Synchroniser maintenant` pour forcer un premier scan.

Apres cette configuration initiale, Orange Drone Compagnon conserve les parametres et
la permission d'ecriture USB. Au redemarrage de la radiocommande, Android
relance la planification WorkManager et une synchronisation est demandee en
arriere-plan. L'interface peut rester fermee : les fichiers sont detectes et
exportes automatiquement si la cle USB autorisee est disponible.

Pour la page web de telechargement GitHub Pages, publier dans la branche
`gh-pages` :

```text
odc/index.html
odc/logo.png
odc/version.json
```

## Securite

- L'application n'envoie que les fichiers `.txt` du dossier selectionne.
- L'application ne supprime jamais les logs originaux.
- Les parametres sont stockes localement dans le sandbox Android de l'application.

## Limites connues V1

- Pas de regroupement : un fichier log reste un transfert individuel.
- Le delai periodique minimum depend d'Android et WorkManager, environ 15 minutes.
- Les restrictions batterie Android/DJI peuvent retarder les taches en arriere-plan.
