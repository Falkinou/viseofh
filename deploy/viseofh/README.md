# Publication de viseofh.fr

Le VPS héberge uniquement les fichiers publics du site. Le projet Android, les
clés de signature et les secrets restent sur le Mac.

## Première installation

Depuis la racine du dépôt :

```sh
./deploy/viseofh/deploy-site.sh
```

Le script copie le site et la configuration dans `/opt/viseofh`, démarre le
conteneur isolé puis contrôle sa santé. Il ne modifie aucun autre service du VPS.

## Publier une version ODC

```sh
./deploy/viseofh/publish-odc.sh \
  /chemin/Orange-Drone-Compagnon-1.35.apk 51 1.35
```

L'APK est transféré sous une URL versionnée. Sa taille et son SHA-256 sont vérifiés
sur le VPS avant que le manifeste public ne soit remplacé atomiquement.

Une copie de la release GitHub peut être conservée comme sauvegarde, mais elle
n'est pas utilisée par la mise à jour en production.
