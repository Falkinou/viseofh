# Viseo FH

Site statique de Viseo FH et page de distribution d'Orange Drone Compagnon.

## Hébergement

Le site de production est hébergé sur le VPS derrière Traefik. GitHub conserve le
code et sert de solution de repli pendant la migration.

Les fichiers d'exploitation sont dans `deploy/viseofh`. Le site public déployé est
limité à `index.html` et au dossier `odc` : le code source Android présent dans ce
dépôt n'est jamais copié dans la racine web.

## Publication d'Orange Drone Compagnon

Une version est identifiée par un numéro, un code Android, une taille et une somme
SHA-256. L'APK est publié sous une URL immuable, puis `odc/version.json` est remplacé
atomiquement en dernier. Voir `deploy/viseofh/README.md`.
