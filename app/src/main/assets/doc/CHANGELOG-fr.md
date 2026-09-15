******

### Historique des versions

******

## v1.3.1

###### 2026/09/15

* `Amélioration` compileSdk et targetSdk passent à 37 (Android 17) ; le comportement du plugin ne dépend pas de la nouvelle cible

## v1.3.0

###### 2026/09/13

* `Fonctionnalité` Historique local accessible depuis l'interface, avec traductions et repli en anglais
* `Amélioration` Vérification de la signature complète, des APK attendus et de la reproductibilité de la documentation

## v1.2.0

###### 2026/09/12

* `Fonctionnalité` Visionneuse repensée en écran immersif bord à bord : l'image remplit désormais toute la fenêtre sous les barres d'état et de navigation, et les barres supérieure et inférieure sont des calques translucides qui se masquent ou reviennent d'un simple appui
* `Fonctionnalité` Barre de titre en surimpression avec le nom du fichier, un compteur de pages du type `3 / 12` lors du parcours d'un dossier ou d'une sélection, et un menu en haut à droite regroupant `Réinitialiser le zoom` et `Imprimer / enregistrer en PDF`
* `Fonctionnalité` Barre d'actions inférieure avec des boutons à icône `Détails`, `Pivoter`, `Partager` et `Ouvrir avec`, plus un bouton flottant pause / reprise qui n'apparaît que pour les GIF animés
* `Fonctionnalité` Les détails de l'image s'ouvrent désormais dans un panneau inférieur glissant avec le nom du fichier, le type MIME, la taille, la résolution, les informations de couleur décodées et les champs EXIF ; balayez vers le bas, appuyez sur l'image ou sur retour pour le fermer
* `Amélioration` Prise en charge bord à bord des barres système et des encoches d'écran, afin que l'interface reste entièrement visible sur Android 15 et versions ultérieures au lieu d'être recouverte par les barres système
* `Amélioration` Chaque commande à icône porte une description d'accessibilité correspondant à son ancien libellé textuel
* `Amélioration` La vérification de compilation rejette les dépendances natives involontaires et produit un rapport JSON

## v1.1.0

###### 2026/08/31

* `Note` La navigation dans un même dossier et la sélection multiple explicite via explorer-action v12 nécessitent la build 5276 ou ultérieure de l'hôte AutoJs6
* `Fonctionnalité` Parcours des images prises en charge d'un même dossier par balayage dans l'ordre naturel des noms, ou ouverture d'une sélection explicite de 128 images maximum en conservant l'ordre choisi dans l'hôte
* `Fonctionnalité` Gestes et commandes enrichis avec zoom par pincement centré sur le toucher, zoom 2.5x par double toucher, rotation de la vue de 90 degrés, indicateur temporaire du zoom et pause/reprise des GIF animés
* `Fonctionnalité` Détails EXIF à la demande avec correction automatique des 8 orientations et coordonnées GPS toujours masquées, informations de profondeur de pixel et d'espace colorimétrique si disponibles, puis impression ou enregistrement de l'image complète en PDF
* `Fonctionnalité` Prise en charge de HEIC / HEIF sous Android 9 ou ultérieur et d'AVIF sous Android 12 ou ultérieur, avec de vraies sondes de décodage et des messages explicites en cas d'indisponibilité
* `Fonctionnalité` Affichage par tuiles des très grandes images JPEG / PNG et HEIC / HEIF statiques, avec aperçu borné et tuiles haute résolution de la zone visible dans un budget mémoire limité
* `Amélioration` Validation renforcée des requêtes explorer-action v12, cibles, contenus, tailles et enfants directs sur tous les points d'entrée, avec accès temporaire en lecture seule et sans permission de stockage ou de réseau
* `Amélioration` Interface et documentation utilisateur enrichies dans 10 langues, avec une galerie de cinq captures provenant de l'interface Android réelle
* `Dépendance` Ajout d'AndroidX ExifInterface 1.4.2 pour l'analyse en lecture seule des métadonnées EXIF

## v1.0.1

###### 2026/08/08

* `Correctif` L'activation du plugin dans le centre des plugins d'AutoJs6 échouait car le service renvoyait une liaison vide (onNullBinding)
* `Amélioration` Nom et description du plugin allégés, avec une formulation cohérente dans tous les documents traduits

## v1.0.0

###### 2026/08/02

* `Fonctionnalité` Première version d'Image Viewer: une action principale `Afficher l'image` pour le gestionnaire de fichiers d'AutoJs6, ouvrant les fichiers images pris en charge dans une visionneuse dédiée d'un simple toucher
* `Fonctionnalité` Prise en charge de 8 extensions, BMP / GIF / JFIF / JPE / JPEG / JPG / PNG / WEBP, décodées par Android et Glide, avec lecture automatique des GIF animés
* `Fonctionnalité` Visionneuse avec ajustement à l'écran, zoom par pincement jusqu'à 5x centré sur les doigts, déplacement à un doigt, restauration par double toucher et masquage des commandes au toucher
* `Fonctionnalité` Nom du fichier dans la barre de titre, type MIME, taille et résolution décodée dans la barre d'informations, plus les actions `Réinitialiser le zoom`, `Partager` et `Ouvrir avec une autre application` (excluant le plugin lui-même)
* `Fonctionnalité` Entrées isolées pour le gestionnaire de fichiers et pour `ACTION_VIEW` externe, toutes deux protégées par des URI de contenu temporaires en lecture seule et une validation point par point, avec un plafond de 8 TiB par fichier et aucune permission de stockage ni de réseau
* `Fonctionnalité` Service du plugin enregistré sur la version 2 du protocole explorer-action, nécessitant la build 5269 ou ultérieure de l'hôte
* `Fonctionnalité` Métadonnées du plugin, interface, instructions et documents en chinois simplifié, chinois traditionnel (Hong Kong / Taïwan), anglais, français, espagnol, japonais, coréen, russe et arabe
* `Dépendance` Introduction de Glide 5.0.5 comme moteur de décodage et de rendu d'images
