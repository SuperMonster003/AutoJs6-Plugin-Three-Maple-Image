# AutoJs6 3-Maple Image

Image Viewer et Image Tools sont réunis dans 3-Maple Image pour afficher, retoucher et convertir les images.
Les paramètres communs proposent la langue, le mode sombre, la couleur et quatre icônes de lanceur.
L'identifiant passe de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools à io.github.supermonster003.autojs6.plugin.three.maple.image. Android installe une application distincte; les anciennes applications et leurs données peuvent être conservées, sans migration automatique des paramètres.


3-Maple Image fournit l'action principale pour les images AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG et WEBP dans le gestionnaire de fichiers.

HEIC et HEIF nécessitent Android 9 ou version ultérieure, tandis qu'AVIF nécessite Android 12 ou version ultérieure. Si le décodeur de la plateforme est indisponible, la visionneuse affiche l'exigence précise au lieu d'échouer silencieusement.

Les très grandes images JPEG, PNG et HEIC/HEIF statiques utilisent un aperçu sous-échantillonné et borné; pendant le zoom, seules les zones visibles en haute résolution sont décodées. Le cache de tuiles est libéré au changement de page ou sous pression mémoire.

Pour n'ouvrir qu'un groupe explicite, passez en mode de sélection dans AutoJs6, choisissez de 1 à 128 images prises en charge dans un même dossier puis touchez `Afficher l'image`. La visionneuse conserve l'ordre de sélection et limite la pagination à ce groupe; chaque URI sélectionné est validé séparément et reste en lecture seule. Chaque image sélectionnée peut être envoyée dans la Corbeille d'AutoJs6 avec `Supprimer` dans la barre du bas et récupérée avec Annuler; l'image ouverte en touchant un seul fichier se supprime de la même façon, mais pas les autres images de son dossier.

La visionneuse adapte l'image à l'écran et prend en charge la pagination gauche/droite du même dossier à l'échelle 1x, le zoom focal par pincement, le déplacement, le basculement par double appui entre l'ajustement et un zoom 2,5x centré sur le point touché, ainsi que les métadonnées par page. Le facteur actuel apparaît pendant le pincement et brièvement après un double appui. Les GIF animés bouclent avec une commande de pause/reprise qui reste masquée pour les images statiques. L'image remplit tout l'écran sous des barres translucides en surimpression; appuyez sur l'image pour les masquer ou les afficher, et la barre de titre ajoute un compteur de pages du type `3 / 12` lorsque plusieurs images sont ouvertes. Le partage et l'ouverture avec une autre application sont disponibles pour l'image ouverte initialement et désactivés sur les pages voisines de session. Le zoom du double toucher et le zoom maximal, le zoom à un doigt, le toucher des bords pour tourner les pages et le balayage vers le bas pour fermer se règlent dans `Paramètres` > `Visionneuse` de 3-Maple Image.

`Pivoter` ne fait pivoter que la vue actuelle et conserve le zoom actif. `Réinitialiser le zoom`, dans le menu en haut à droite, rétablit l'orientation d'origine et l'ajustement à l'écran.

Utilisez `Détails` pour ouvrir un panneau inférieur avec le nom du fichier, le type MIME, la taille, la résolution ainsi que la date de prise de vue, l'appareil, l'exposition et l'orientation EXIF disponibles. Si des métadonnées GPS existent, la visionneuse signale par défaut seulement leur présence; avec `Paramètres` > `Visionneuse` > `Afficher la position` activé, elle montre les coordonnées du lieu de prise de vue (en décimal et en degrés, minutes, secondes) et propose `Ouvrir dans une carte`.

La barre d'informations affiche toujours le type MIME, la taille du fichier et la résolution décodée. Sous Android 8.0 ou version ultérieure, elle affiche aussi la profondeur de pixel décodée (bpp) et l'espace colorimétrique de sortie lorsque le décodeur les fournit.

Les photos sont automatiquement pivotées ou mises en miroir selon leur orientation EXIF. `Pivoter` ajoute ensuite une rotation de la vue à cet affichage corrigé.

`Imprimer / enregistrer en PDF`, dans le menu en haut à droite, envoie l'image actuelle complète vers la feuille d'impression système d'Android en conservant la correction EXIF et la rotation manuelle de la vue. Pour un GIF animé, l'image visible au toucher est utilisée. Le zoom et le déplacement ne recadrent pas la sortie, et le plugin ne crée aucun fichier image ou PDF temporaire.

Dans le gestionnaire de fichiers, `Afficher comme album` dans le menu d'un dossier ouvre ce dossier comme album via la session de l'hôte. Les sous-dossiers jusqu'à 8 niveaux apparaissent comme cellules de dossier, les images s'ouvrent dans la visionneuse avec pagination dans ce dossier et seules les 4096 premières entrées de chaque dossier sont listées. Ce mode ne requiert aucune permission de stockage et ne divulgue aucun chemin de fichier; les actions de gestion n'y sont pas disponibles.

`Modifier l'image` et `Convertir l'image` dans le menu du fichier enregistrent par défaut un nouveau fichier à côté de l'original; avec `Remplacer l'original` activé dans la boîte d'enregistrement ou sur la page de conversion, ils remplacent l'original (même format), et le remplacement peut être annulé pendant 8 secondes. Chaque sortie est préparée et relue avant d'être publiée, si bien qu'un échec ne laisse aucun fichier partiel.

La version 5352 ou ultérieure de l'hôte est requise.

Les scripts AutoJs6 peuvent aussi utiliser 3-Maple Image avec l'objet `gallery`: `gallery.pick` ouvre le sélecteur et renvoie les images choisies, `gallery.view` affiche des images dans la visionneuse, `gallery.edit` ouvre l'éditeur et AutoJs6 enregistre le résultat, et `gallery.convert` convertit des images sans rien afficher. AutoJs6 ouvre les images et écrit les résultats lui-même, un script n'atteint donc que les fichiers auxquels il a accès; la sélection a besoin de l'accès aux photos du plugin, que le sélecteur demande d'abord. Cette fonction nécessite la version 5439 ou ultérieure de l'hôte AutoJs6 6.8.0.

Limites de sécurité et de confidentialité:

- La source sélectionnée utilise un URI `content` temporaire en lecture seule; les voisins directs ne sont ouverts que par la session `readSiblings` limitée de l'hôte.
- Les fichiers de plus de 8 TiB sont refusés.
- Ouvert depuis AutoJs6, le plugin ne demande aucune autorisation et ne déclare pas d'autorisation réseau.
- La suppression n'envoie que les fichiers sélectionnés dans la Corbeille d'AutoJs6; le déplacement, le renommage et les détails de fichiers restent des fonctions de l'hôte.
