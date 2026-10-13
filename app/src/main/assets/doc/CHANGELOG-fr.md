******

### Historique des versions

******

## v3.1.0

###### 2026/10/14

* `Note` Les images supprimées vont d'abord dans une corbeille: sur Android 11+ celle du système (visible aussi dans les autres galeries), vidée par le système après environ 30 jours; sur Android 7-10 celle de l'application, conservée 30 jours et limitée à un dixième de l'espace libre (2 GiB au plus), supprimée avec l'application. Elle peut être désactivée dans Paramètres > Corbeille; la suppression demande alors une confirmation et ne peut pas être annulée
* `Note` Sur Android 11+ le système confirme chaque lot qui supprime, déplace ou renomme des images enregistrées par d'autres applications; activer Gestion des médias (Android 12+) dans Paramètres > Autorisation de stockage supprime ces confirmations
* `Fonctionnalité` La grille d'images permet la sélection multiple: appui long sur une image pour commencer, appui pour ajouter ou retirer, garder le doigt posé après l'appui long et glisser pour sélectionner une plage (la grille défile près du haut ou du bas), tout sélectionner ou inverser; partager, ajouter aux favoris, déplacer, copier ou supprimer plusieurs images à la fois, renommer une image ou afficher ses détails
* `Fonctionnalité` Une suppression peut être annulée depuis la barre du bas; le menu de l'accueil gagne Corbeille, qui indique les jours restants de chaque image et permet de restaurer, supprimer définitivement ou vider
* `Fonctionnalité` Déplacer et copier proposent un album ou un nouvel album dans Pictures, les albums récents en premier; si la cible contient déjà des images du même nom, une seule question: conserver les deux (un numéro est ajouté au nouveau nom), ignorer, ou remplacer (les images existantes vont dans la corbeille); les lots affichent la progression et peuvent être annulés; déplacer et renommer peuvent être annulés
* `Fonctionnalité` Favoris: les images favorites sont réunies dans l'album Favoris en tête de la liste des albums; sur Android 11+ Synchroniser les favoris du système dans les paramètres les rend visibles aux autres galeries
* `Fonctionnalité` Ouverte depuis un album, la visionneuse gagne un bouton cœur dans la barre du haut, et Supprimer remplace Ouvrir avec dans la barre du bas; Ouvrir avec, Renommer, Déplacer et Copier sont dans le menu en haut à droite
* `Fonctionnalité` Les images des dossiers autorisés peuvent être supprimées (définitivement, après confirmation, sans corbeille) et renommées
* `Dépendance` Ajout de Room 2.8.1 pour conserver les favoris et les entrées de la corbeille d'Android 7-10

## v3.0.0

###### 2026/10/13

* `Note` Lancée seule, l'application demande une fois l'accès aux photos (photos et vidéos sur Android 13+, stockage sur les versions antérieures) pour afficher les albums; en cas de refus elle ne le redemande plus d'elle-même, l'affichage, la retouche et la conversion restent possibles via le sélecteur du système, et l'accès peut être accordé plus tard depuis l'accueil ou Paramètres > Autorisation de stockage. Ouvrir des images depuis AutoJs6 ou d'autres applications ne demande jamais d'autorisation
* `Fonctionnalité` L'écran d'accueil devient une vue en albums: images de l'appareil regroupées par dossier (couverture, nom, nombre) ou vue à plat du contenu de tous les dossiers; la grille propose 3 à 6 colonnes (menu ou pincement), un tri par nom, date de modification, date de prise de vue ou taille mémorisé par album, une barre de défilement rapide avec indication de date et l'actualisation par glissement
* `Fonctionnalité` La visionneuse parcourt tout l'album par glissement à gauche et à droite, une bande de miniatures saute à une page au toucher et la grille revient à la dernière image vue
* `Fonctionnalité` Faites pivoter une image à deux doigts; au relâchement elle se cale sur le quart de tour le plus proche, ne change que l'affichage, peut être annulée et se désactive dans Paramètres > Visionneuse
* `Fonctionnalité` Une nouvelle page Autorisation de stockage dans les Paramètres affiche l'accès actuel, l'accorde de nouveau, gère les photos sélectionnées sur Android 14+, ajoute ou retire des dossiers autorisés (affichés comme albums même sans accès aux photos) et propose l'option Accès à tous les fichiers
* `Fonctionnalité` Le menu de dossier du gestionnaire de fichiers d'AutoJs6 gagne Afficher comme album, qui montre les images du dossier en grille et ouvre ses sous-dossiers
* `Dépendance` Ajout de Navigation Compose 2.10.2 pour la navigation entre l'accueil et les pages d'album

## v2.1.0

###### 2026/10/13

* `Note` L'APK contient désormais libandroidx.graphics.path.so, une bibliothèque native fournie avec Jetpack Compose (environ 10 KB pour chacune des ABI arm64-v8a, armeabi-v7a, x86_64 et x86) ; le plugin lui-même ne contient toujours aucun code natif et reste un APK universel unique
* `Note` Les premiers lancements après l'installation sont un peu plus lents sur les appareils anciens jusqu'à ce que le système termine son optimisation en arrière-plan
* `Fonctionnalité` Toute l'interface passe à Material 3 (Jetpack Compose): accueil, paramètres, à propos, historique des versions, visionneuse, éditeur et convertisseur sont refaits, suivent la couleur du thème et le mode sombre, et prennent en charge les grandes polices et la disposition de droite à gauche
* `Fonctionnalité` La barre d'outils de l'éditeur devient des bandes d'outils groupées avec icônes et libellés (transformer, ajuster, dessiner, texte); le panneau de paramètres suit l'outil actif et les dialogues de texte, d'enregistrement et de palette sont refaits
* `Fonctionnalité` Le convertisseur passe d'un dialogue à une page complète qui n'affiche que les champs du mode de redimensionnement actuel, avec la résolution de sortie, la taille estimée et le suffixe fixés en bas
* `Fonctionnalité` La barre inférieure de la visionneuse propose désormais Détails, Pivoter, Modifier, Partager et Ouvrir avec; les images ouvertes depuis l'accueil autonome ou depuis une autre application passent directement à l'éditeur et enregistrent le résultat
* `Correctif` La page d'historique des versions dans l'application était vide (les titres de version ## du fichier généré n'étaient pas reconnus)
* `Correctif` Après le passage de la langue de l'application à l'arabe, les pages déjà ouvertes gardaient une disposition de gauche à droite
* `Correctif` En mode clair, les icônes de la barre d'état de la visionneuse et de l'éditeur suivaient le thème de la page et devenaient sombres, donc invisibles sur le fond sombre
* `Amélioration` Les icônes du centre de plugins utilisent les tailles, positions, images claires et sombres et fonds circulaires réglés dans Icon Studio, avec les sources et paramètres permettant de les reproduire
* `Amélioration` Les icônes des informations d'application Android utilisent les illustrations et les fonds clairs et sombres d'Icon Studio, en conservant les images transparentes du centre de plugins et les choix du lanceur
* `Amélioration` Les icônes sont unifiées en vecteurs Material 24 dp; les icônes bitmap et les ressources de couleur, de style et de texte inutilisées sont supprimées
* `Amélioration` Un profil de référence de démarrage intégré réduit le coût du démarrage à froid de l'accueil après la migration de l'interface une fois que le système a effectué son optimisation en arrière-plan
* `Dépendance` Ajout de Jetpack Compose BOM 2026.09.00 pour la migration de l'interface
* `Dépendance` Ajout de l'intégration Glide Compose 1.0.0-beta10 pour le chargement des images dans les écrans Compose
* `Dépendance` Mise à jour de Glide 5.0.5 -> 5.0.9
* `Dépendance` Mise à jour des plugins AutoJs6 Gradle Platform Versions et Native Alignment 1.9.0 -> 1.9.1

## v2.0.0

###### 2026/10/04

* `Note` L'identifiant passe de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools à io.github.supermonster003.autojs6.plugin.three.maple.image. Android installe une application distincte; les anciennes applications et leurs données peuvent être conservées, sans migration automatique des paramètres
* `Fonctionnalité` Image Viewer et Image Tools sont réunis dans 3-Maple Image pour afficher, retoucher et convertir les images
* `Fonctionnalité` Un écran d'accueil autonome permet de choisir une image, de la retoucher ou convertir et d'enregistrer le résultat à l'emplacement choisi
* `Fonctionnalité` Les paramètres communs proposent la langue, le mode sombre, la couleur et quatre icônes de lanceur

## v1.3.1

###### 2026/09/19

* `Correctif` Avertissements de lecture SDK XML v4 avec AGP 9.1 et contrôles d'alignement natif des APK déclenchés par erreur lors de l'assemblage des tests unitaires JVM, avec les plugins de compilation partagés 1.8.3
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
