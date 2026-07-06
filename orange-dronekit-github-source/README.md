# Orange DroneKit

Boite a outils Android pour radiocommande DJI Enterprise type RC Plus 2.
La priorite actuelle est l'usage terrain sans infrastructure reseau Orange :
export USB des logs, lecture de logs et preparation de fonds d'ecran RC.

Modules disponibles :

- `OrangeSyncLog` : export USB automatique des logs DJI ;
- `OrangePlayLog` : lecture et controle de logs de vol ;
- `OrangeScreen` : preparation de fonds d'ecran pour radiocommande.

La version actuelle fait volontairement peu de choses, mais les fait de facon fiable :

- choix manuel du dossier DJI `FlightRecord` au premier lancement ;
- detection des fichiers `.txt` uniquement ;
- aucun fichier DJI n'est modifie, deplace ou supprime ;
- export USB d'un fichier par log, sans infra reseau ;
- copie optionnelle des photos/videos DJI prises pendant le meme vol ;
- historique local Room avec statut `En attente`, `Envoye` ou `Erreur` ;
- journal local des evenements de synchronisation ;
- reessai automatique via WorkManager toutes les 15 minutes minimum ;
- relance automatique de la synchronisation apres demarrage Android et apres mise a jour APK ;
- relance d'une synchronisation quand Android signale le branchement d'un peripherique USB ;
- verification de mise a jour depuis `viseofh.fr/orange-dronekit/version.json` ;
- module `OrangePlayLog` avec decodage local des metadonnees DJI ;
- module `OrangeScreen` avec champs et apercu de fond d'ecran ;
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
- Apache Commons Net pour FTP/FTPS
- JavaMail Android (`com.sun.mail:android-mail`)
- Min SDK 26, target/compile SDK 35

## Configuration USB recommandee

Ce mode est a privilegier quand aucune infrastructure Orange simple n'est
disponible.

Dans le menu deroulant, selectionner `OrangeSyncLog`, puis :

1. brancher la cle USB sur la radiocommande ;
2. appuyer sur `Choisir dossier sur cle USB` ;
3. choisir le dossier racine de la cle ou un dossier dedie ;
4. optionnellement appuyer sur `Choisir dossier photos/videos DJI` ;
5. laisser `Inclure medias du vol` active si les photos/videos doivent etre copiees ;
6. appuyer sur `Enregistrer`, puis `Tester la connexion`.

Android demandera une autorisation au premier choix du dossier. Orange DroneKit
memorise cette autorisation via le Storage Access Framework.

Les exports sont ranges sous cette forme :

```text
OrangeDroneKit/
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

1. `OrangeSyncLog`
2. `OrangePlayLog`
3. `OrangeScreen`

## OrangePlayLog

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
OrangePlayLog. L'application extrait la demande `keychainsArray` du log choisi
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

- l'identifiant radiocommande ;
- le nom d'equipe ;
- un contact ;
- une mention courte.

La version actuelle fournit les champs et l'apercu. L'export PNG vers cle USB
sera raccorde dans une prochaine etape.

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

## Choix du dossier de logs de vol

Au premier lancement, appuyer sur `Choisir dossier log de vol`.

Le dossier probable sur DJI Enterprise Pilot est :

```text
/sdcard/DJI/com.dji.industry.pilot/FlightRecord/
```

Selon la version Android/DJI, le selecteur peut afficher ce chemin sous une forme
legere differente. Il faut choisir le dossier qui contient directement les fichiers
de logs DJI `.txt`.

L'application conserve une permission de lecture persistante via le Storage Access
Framework. Elle ne demande pas l'acces complet au stockage.

## Fonctionnement

WorkManager lance une synchronisation periodique toutes les 15 minutes minimum,
limite imposee par Android.

A chaque synchronisation :

1. le dossier choisi est scanne ;
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
OrangeDroneKit/{identifiant_radio}/{date}/{nom_du_vol}/FlightRecord/{nom_fichier}
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
./gradlew :app:packageOrangeDroneKitApk
```

Fichier final a diffuser :

```text
dist/Orange-DroneKit.apk
```

Publier aussi le fichier de version pour la verification de mise a jour :

```text
dist/version.json
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
www/orange-dronekit/
```

La page fournie dans `dist/index.html` expose un bouton de telechargement.
Une fois publiee, ouvrir depuis la radiocommande :

```text
https://votre-domaine/orange-dronekit/
```

Puis telecharger et installer `Orange-DroneKit.apk`. Android peut demander
d'autoriser l'installation depuis le navigateur utilise sur la radiocommande.

### Depuis ADB

1. Activer les options developpeur et le debogage USB sur la radiocommande.
2. Brancher la RC Plus 2 au poste Android Studio.
3. Installer depuis Android Studio, ou via ADB :

```bash
adb install dist/Orange-DroneKit.apk
```

4. Lancer Orange DroneKit.
5. Choisir le dossier `FlightRecord`.
6. Choisir le dossier sur la cle USB.
7. Tester la connexion USB.
8. Appuyer sur `Synchroniser maintenant` pour forcer un premier scan.

Apres cette configuration initiale, Orange DroneKit conserve les parametres et
la permission de lecture du dossier. Au redemarrage de la radiocommande, Android
relance la planification WorkManager et une synchronisation est demandee en
arriere-plan. L'interface peut rester fermee : les fichiers sont detectes et
exportes automatiquement si la cle USB autorisee est disponible.

Pour la page web de telechargement GitHub Pages, publier dans la branche
`gh-pages` :

```text
orange-dronekit/index.html
orange-dronekit/Orange-DroneKit.apk
orange-dronekit/version.json
```

## Securite

- L'application n'envoie que les fichiers `.txt` du dossier selectionne.
- L'application ne supprime jamais les logs originaux.
- Les parametres sont stockes localement dans le sandbox Android de l'application.

## Limites connues V1

- Export PNG OrangeScreen pas encore raccorde.
- Pas de regroupement : un fichier log reste un transfert individuel.
- Le delai periodique minimum depend d'Android et WorkManager, environ 15 minutes.
- Les restrictions batterie Android/DJI peuvent retarder les taches en arriere-plan.
