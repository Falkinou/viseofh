# Publication automatique Orange DroneKit

Workflow ajoute : `.github/workflows/release-apk.yml`.

## Ce que fait le workflow

À chaque push sur `main`, ou via lancement manuel `Actions > Publish Orange DroneKit APK > Run workflow` :

1. compile l'APK release ;
2. lit `versionCode` et `versionName` dans `app/build.gradle.kts` ;
3. crée ou met à jour le tag `v{versionName}` ;
4. publie `Orange-DroneKit.apk` dans les GitHub Releases ;
5. publie la page web dans la branche `gh-pages` ;
6. met à jour `version.json` avec l'URL de la dernière release.

## Réglage GitHub requis

Dans GitHub :

1. ouvrir le dépôt `Falkinou/viseofh` ;
2. aller dans `Settings > Actions > General` ;
3. dans `Workflow permissions`, choisir `Read and write permissions` ;
4. enregistrer.

## Important

Ne pas pousser l'APK dans les fichiers du dépôt : il est trop lourd.

Le fichier `Orange-DroneKit.apk` doit rester un asset de Release GitHub.

## Procédure obligatoire à chaque nouvelle version

À chaque nouvelle version Orange DroneKit :

1. incrémenter `versionCode` et `versionName` dans `app/build.gradle.kts` ;
2. mettre à jour `github-pages-upload/orange-dronekit/version.json` si la page locale doit refléter la version avant publication ;
3. compiler localement au moins une fois :

```bash
./gradlew --no-daemon :app:compileDebugKotlin
./gradlew --no-daemon :app:packageOrangeDroneKitApk
```

4. pousser les sources sur GitHub, sans ajouter l'APK ;
5. vérifier que le workflow `Publish Orange DroneKit APK` s'exécute ;
6. vérifier la Release `v{versionName}` et la présence de l'asset `Orange-DroneKit.apk` ;
7. ouvrir `https://viseofh.fr/orange-dronekit/version.json` pour vérifier que la version publiée est à jour ;
8. ouvrir la page de téléchargement depuis la RC.

Consigne projet : aucune version livrable ne doit être considérée terminée tant que ce workflow n'a pas tourné avec succès.
