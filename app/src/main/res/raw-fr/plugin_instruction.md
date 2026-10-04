# AutoJs6 3-Maple Image

Image Viewer et Image Tools sont réunis dans 3-Maple Image pour afficher, retoucher et convertir les images.
Les paramètres communs proposent la langue, le mode sombre, la couleur et quatre icônes de lanceur.
L'identifiant passe de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools à io.github.supermonster003.autojs6.plugin.three.maple.image. Android installe une application distincte; les anciennes applications et leurs données peuvent être conservées, sans migration automatique des paramètres.


3-Maple Image fournit l'action principale pour les images AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG et WEBP dans le gestionnaire de fichiers.

HEIC et HEIF nécessitent Android 9 ou version ultérieure, tandis qu'AVIF nécessite Android 12 ou version ultérieure. Si le décodeur de la plateforme est indisponible, la visionneuse affiche l'exigence précise au lieu d'échouer silencieusement.

Les très grandes images JPEG, PNG et HEIC/HEIF statiques utilisent un aperçu sous-échantillonné et borné; pendant le zoom, seules les zones visibles en haute résolution sont décodées. Le cache de tuiles est libéré au changement de page ou sous pression mémoire.

Pour n'ouvrir qu'un groupe explicite, passez en mode de sélection dans AutoJs6, choisissez de 1 à 128 images prises en charge dans un même dossier puis touchez `Afficher l'image`. La visionneuse conserve l'ordre de sélection et limite la pagination à ce groupe; chaque URI sélectionné est validé séparément et reste en lecture seule.

La visionneuse adapte l'image à l'écran et prend en charge la pagination gauche/droite du même dossier à l'échelle 1x, le zoom focal par pincement, le déplacement, le basculement par double appui entre l'ajustement et un zoom 2,5x centré sur le point touché, ainsi que les métadonnées par page. Le facteur actuel apparaît pendant le pincement et brièvement après un double appui. Les GIF animés bouclent avec une commande de pause/reprise qui reste masquée pour les images statiques. L'image remplit tout l'écran sous des barres translucides en surimpression; appuyez sur l'image pour les masquer ou les afficher, et la barre de titre ajoute un compteur de pages du type `3 / 12` lorsque plusieurs images sont ouvertes. Le partage et l'ouverture avec une autre application sont disponibles pour l'image ouverte initialement et désactivés sur les pages voisines de session.

`Pivoter` ne fait pivoter que la vue actuelle et conserve le zoom actif. `Réinitialiser le zoom`, dans le menu en haut à droite, rétablit l'orientation d'origine et l'ajustement à l'écran.

Utilisez `Détails` pour ouvrir un panneau inférieur avec le nom du fichier, le type MIME, la taille, la résolution ainsi que la date de prise de vue, l'appareil, l'exposition et l'orientation EXIF disponibles. Si des métadonnées GPS existent, la visionneuse signale leur présence mais masque les coordonnées.

La barre d'informations affiche toujours le type MIME, la taille du fichier et la résolution décodée. Sous Android 8.0 ou version ultérieure, elle affiche aussi la profondeur de pixel décodée (bpp) et l'espace colorimétrique de sortie lorsque le décodeur les fournit.

Les photos sont automatiquement pivotées ou mises en miroir selon leur orientation EXIF. `Pivoter` ajoute ensuite une rotation de la vue à cet affichage corrigé.

`Imprimer / enregistrer en PDF`, dans le menu en haut à droite, envoie l'image actuelle complète vers la feuille d'impression système d'Android en conservant la correction EXIF et la rotation manuelle de la vue. Pour un GIF animé, l'image visible au toucher est utilisée. Le zoom et le déplacement ne recadrent pas la sortie, et le plugin ne crée aucun fichier image ou PDF temporaire.

La version 5276 ou ultérieure de l'hôte est requise.

Limites de sécurité et de confidentialité:

- La source sélectionnée utilise un URI `content` temporaire en lecture seule; les voisins directs ne sont ouverts que par la session `readSiblings` limitée de l'hôte.
- Les fichiers de plus de 8 TiB sont refusés.
- Le plugin ne demande aucune autorisation de stockage ou de réseau.
- La modification permanente, la conversion, la suppression, le déplacement et le renommage restent des fonctions de l'hôte.
