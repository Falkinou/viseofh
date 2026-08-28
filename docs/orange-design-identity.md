# Identite visuelle Orange Drone Compagnon

Reference principale : Orange Design System
https://system.design.orange.com/0c1af118d/p/0127c5-orange-design-system

## Principes

- Orange sert d'accent d'action, pas de couleur de remplissage permanente.
- Les fonds restent sombres et sobres pour les radiocommandes en exterieur.
- Les etats sont toujours coherents :
  - Vert : pret, connecte, export reussi.
  - Jaune : attention, configuration incomplete, action requise.
  - Rouge : erreur, indisponible, non connecte critique.
  - Bleu : information, meteo, donnees.
- Les modules commencent par un en-tete commun avec :
  - icone du module,
  - titre lisible,
  - statut court,
  - trois indicateurs maximum.
- Une seule action principale par bloc. Les actions secondaires restent en boutons neutres.
- Les textes doivent rester courts, utiles terrain, sans explication decorative.

## Composants de base

- `ModuleHero` : entree visuelle des modules.
- `GlassCard` : carte sombre translucide avec bordure fine.
- `OrangeButton` : bouton d'action ou action importante.
- `SecondaryFieldButton` : action secondaire ou reglage.
- `GlassTextField` : champ formulaire harmonise.

## Regles d'ecran

- Accueil : navigation et etats rapides uniquement.
- Module : pas de grand header app, seulement le retour et le module.
- Reglages : regroupe les autorisations, diagnostics et preferences.
- Info : consignes, diagnostic admin et aide terrain.
- Export : parcours guide, progression visible, jamais d'action ambigue.

## A surveiller

- Pas de cartes imbriquees inutilement.
- Pas de gros boutons orange partout.
- Pas de couleurs aleatoires par ecran.
- Pas de texte trop petit sur RC.
- Pas de scroll obligatoire sur les vues cockpit paysage, sauf contenu long.
