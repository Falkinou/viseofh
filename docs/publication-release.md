# Publication release - Orange Drone Compagnon

## Commande recommandee

```bash
./scripts/prepare-release.sh
```

La commande :

- compile l'APK release ;
- copie `Orange-Drone-Compagnon-<version>.apk` ;
- copie `Orange-Drone-Compagnon.apk` ;
- prepare le dossier web `odc` ;
- copie la documentation utile ;
- conserve les deux derniers dossiers de release.

## Dossier produit

Le dossier genere se trouve dans :

```text
release-packages/odc-<version>/
```

Contenu :

```text
apk/
odc/
docs/
```

## GitHub Release

Dans la release GitHub, publier de preference les deux APK :

```text
Orange-Drone-Compagnon-<version>.apk
Orange-Drone-Compagnon.apk
```

Le site pointe d'abord vers le fichier stable :

```text
Orange-Drone-Compagnon.apk
```

Cela evite les erreurs 404 quand le nom versionne change.

## GitHub Pages / site web

Copier le contenu du dossier :

```text
release-packages/odc-<version>/odc/
```

vers le dossier publie :

```text
odc/
```

Le workflow GitHub Actions à la racine du projet construit désormais l'APK,
met à jour la release, génère `odc/version.json` avec la taille et l'empreinte
SHA-256, puis publie le site sur la branche `gh-pages`. La source Pages doit
être configurée sur `gh-pages` à la racine `/`.

## Warnings SDK DJI

Pendant la compilation, Android peut afficher des warnings de ressources DJI du type :

```text
removing resource DJI SDK without required default value
```

Ces messages viennent des ressources embarquees du SDK DJI. Ils ne bloquent pas la compilation et ne correspondent pas a une erreur Kotlin ou Compose de l'application.

Action :

- surveiller qu'ils restent des warnings ;
- verifier que `BUILD SUCCESSFUL` est present ;
- ne pas bloquer la release uniquement pour ces warnings.
