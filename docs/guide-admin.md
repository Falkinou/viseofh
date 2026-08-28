# Guide admin - Orange Drone Compagnon

## Installation

1. Publier la derniere APK sur GitHub Releases.
2. Publier le dossier `odc` sur GitHub Pages ou le site interne.
3. Depuis la radiocommande, ouvrir la page de telechargement.
4. Installer `Orange-Drone-Compagnon.apk`.

## Fichiers de publication

Le dossier a publier contient :

- `odc/index.html` ;
- `odc/version.json` ;
- `odc/logo.png` si present.

Le lien de telechargement principal doit pointer vers :

```text
https://github.com/Falkinou/viseofh/releases/latest/download/Orange-Drone-Compagnon.apk
```

Le fichier `version.json` permet la verification de mise a jour dans l'application.

## Version

La version Android est definie dans :

```text
app/build.gradle.kts
```

Variables :

- `orangeDroneCompagnonVersionCode` ;
- `orangeDroneCompagnonVersionName`.

## Compilation

```bash
./gradlew --no-daemon :app:packageOrangeDroneCompagnonApk :app:packageOrangeDroneCompagnonLatestApk
```

Sorties :

```text
dist/Orange-Drone-Compagnon-<version>.apk
dist/Orange-Drone-Compagnon.apk
```

## Securite

- L’identifiant Android technique est conservé pour la compatibilité avec la clé DJI Mobile SDK, mais le nom affiché reste Orange Drone Compagnon.
- Le nom visible utilisateur est Orange Drone Compagnon.
- FTP et SMTP sont des canaux historiques : l'usage terrain prioritaire est USB + MSurvey.
- Aucun mot de passe SMTP/FTP ne doit etre publie dans le depot.
- Les logs DJI originaux ne doivent jamais etre modifies ni supprimes.

## Diagnostic

Dans l'application :

1. ouvrir le bouton information ;
2. aller dans Diagnostic ;
3. exporter le diagnostic texte si necessaire.

Elements utiles pour analyser une anomalie :

- modele RC ;
- modele drone ;
- version application ;
- etat USB ;
- etat SDK DJI ;
- dossier logs ;
- messages d'erreur recents.

## Points sensibles

- Autorisations Android Storage Access Framework ;
- detection USB selon version DJI/Android ;
- connexion SDK DJI si l’application de vol DJI occupe deja certaines ressources ;
- taille de l'APK due au SDK DJI.
