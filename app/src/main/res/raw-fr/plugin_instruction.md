3-Maple Image permet d'afficher et de modifier des images et d'en convertir le format. Il s'ouvre seul pour parcourir et organiser les images de l'appareil par album; toucher un fichier image dans l'explorateur de fichiers d'AutoJs6 l'ouvre aussi dans 3-Maple Image, et le menu du fichier propose la modification et la conversion. Les scripts AutoJs6 peuvent l'appeler via le module `gallery`.

### Prise en main

1. Ouvrez 3-Maple Image et accordez l'accès aux photos lorsqu'il est demandé; la page d'accueil liste alors les images de l'appareil par album. Sans cet accès, utilisez "Choisir une image" ou "Choisir un dossier" pour ouvrir des images via le sélecteur du système.
2. Activez 3-Maple Image dans le "Centre des plugins" d'AutoJs6. Toucher ensuite un fichier image dans l'explorateur de fichiers d'AutoJs6 l'affiche, et "Modifier l'image" et "Convertir l'image" dans le menu du fichier servent à le modifier et à le convertir.

### Fonctionnalités principales

- **Albums**: la page d'accueil liste les images par album, "Favoris" en tête. Vous pouvez rechercher les noms d'album et de fichier, modifier le tri, le nombre de colonnes et le regroupement par date; un appui long sur un album propose "Épingler", "Exclure" ou "Masquer".
- **Visionneuse**: zoom, défilement, bande de vignettes et "Pivoter", avec lecture en boucle des GIF animés. Le menu en haut à droite propose "Diaporama", "Définir comme fond d'écran", "Partager une copie", "Imprimer / enregistrer en PDF", etc.
- **Détails**: touchez "Détails" pour voir le nom, la taille, la résolution et les informations de prise de vue EXIF.
- **Organisation**: un appui long dans la grille d'images lance la sélection multiple pour partager, ajouter aux favoris, déplacer, copier ou supprimer. Les images supprimées vont d'abord dans la "Corbeille", et le message permet d'annuler.
- **Modification**: les outils de l'éditeur forment six groupes, dont "Transformer", "Ajuster", "Filtres" et "Texte", avec annulation et rétablissement. La modification et la conversion enregistrent par défaut un nouveau fichier sans toucher l'original; l'original n'est remplacé que si "Remplacer l'original" est activé, et ce remplacement peut être annulé.
- **Conversion**: "Convertir l'image" change le format (JPEG, PNG, WebP), la qualité et la taille, avec un aperçu de la taille du résultat. Sélectionnez plusieurs images dans un album ou dans l'explorateur de fichiers d'AutoJs6 pour les convertir par lot.

### Utilisation dans les scripts

Les scripts AutoJs6 utilisent ce plugin via le module `gallery`. L'exemple ci-dessous laisse l'utilisateur choisir jusqu'à 5 images, les convertit en WebP avec un grand côté de 1080 pixels, puis affiche les résultats dans la visionneuse:

```js
let images = gallery.pick({ multiple: true, maxCount: 5 });
if (images) {
    let outputs = images.map((image) => gallery.convert(image.path, { format: 'webp', longEdge: 1080 }).path);
    gallery.view(outputs);
}
```

- Chaque méthode a aussi une forme asynchrone qui renvoie une Promise, par exemple `gallery.pickAsync`. Les méthodes synchrones `pick`, `edit` et `convert` peuvent attendre longtemps et ne doivent pas être appelées sur le thread UI.
- Les images sont désignées par leur chemin de fichier, et les chemins relatifs sont résolus par rapport au répertoire de travail du script. Le résultat est écrit par défaut à côté de l'original (le nom de fichier reçoit `_converted` ou `_edited`), et l'option `output` permet de choisir un autre emplacement.
- Le choix d'images nécessite l'accès aux photos du plugin, que le sélecteur demande d'abord. Le choix, l'affichage et la modification ouvrent les écrans du plugin; les scripts sont donc plus fiables lorsque l'écran d'AutoJs6 est visible.
- Appelez `gallery.isAvailable()` avant l'exécution pour vérifier que le plugin est disponible. Toutes les options et les codes d'erreur figurent dans [Documentation AutoJs6 > gallery](https://docs.autojs6.com/#/gallery).

### Prérequis

- Android 7.0 ou version ultérieure. Les images HEIC et HEIF nécessitent Android 9 ou version ultérieure, et les images AVIF Android 12 ou version ultérieure.
- L'utilisation dans AutoJs6 (explorateur de fichiers et module `gallery`) nécessite AutoJs6 6.8.0 ou version ultérieure, avec ce plugin activé dans le "Centre des plugins"; utilisé seul, il ne nécessite pas AutoJs6.
- L'accès aux photos sert à parcourir les images par album sur la page d'accueil et à laisser les scripts choisir des images; ouvrir une image depuis l'explorateur de fichiers d'AutoJs6 ou une autre application ne demande aucune autorisation.
- "Accès à tous les fichiers" et "Gestion des médias" sont des autorisations facultatives qui permettent de masquer des albums et de supprimer ou déplacer des images sans confirmation à chaque fois; activez-les dans "Paramètres" > "Autorisation de stockage".

### Questions fréquentes

**Utilisation sans accès aux photos**

La page d'accueil affiche "Choisir une image" et "Choisir un dossier", ce qui permet encore d'afficher, de modifier et de convertir des images via le sélecteur du système. L'accès peut être accordé plus tard dans "Paramètres" > "Autorisation de stockage"; sous Android 14 et versions ultérieures, si seules certaines photos sont autorisées, la page d'accueil n'affiche que ces photos, et "Gérer les photos sélectionnées" permet d'en ajouter ou d'en retirer.

**"Modifier l'image" et "Convertir l'image" absents du menu du fichier**

AutoJs6 6.8.1 et versions ultérieures placent ces deux entrées dans le sous-menu 3-Maple Image du menu du fichier. Si elles restent introuvables, vérifiez que le plugin est activé dans le "Centre des plugins" et que le fichier est dans un format d'image pris en charge, puis mettez AutoJs6 à jour vers la dernière version.

**Restauration des images supprimées**

Les images supprimées dans un album vont d'abord dans la "Corbeille" du menu en haut à droite de la page d'accueil, où elles peuvent être restaurées; elles sont effacées automatiquement après environ 30 jours. Les images supprimées après une ouverture depuis AutoJs6 vont dans la corbeille d'AutoJs6 et se restaurent dans AutoJs6.

**Affichage et conservation de la position des photos**

Les coordonnées ne sont pas affichées par défaut, et les images produites par la modification, la conversion et "Partager une copie" perdent leur position. Avec "Afficher la position" activé dans "Paramètres", les détails affichent les coordonnées et proposent "Ouvrir dans une carte", et "Conserver la position" peut être coché lors de l'enregistrement ou de la conversion. Ce plugin n'a pas d'autorisation réseau.

Consultez la [page du projet](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image) pour la description complète des fonctionnalités, les formats pris en charge et les questions fréquentes.
