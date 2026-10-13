<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <source srcset="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap-night/ic_launcher.png?raw=true" media="(prefers-color-scheme: dark)" />
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>Affichage, retouche et conversion d'images</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Langues (Languages)

******

Le fichier README.md actuel prend en charge les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-en.md)
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/.readme/README-ar.md)

******

### Commencer

Un écran d'accueil autonome permet de choisir une image, de la retoucher ou convertir et d'enregistrer le résultat à l'emplacement choisi. Les paramètres communs proposent la langue, le mode sombre, la couleur et quatre icônes de lanceur.

L'identifiant passe de `io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools` à `io.github.supermonster003.autojs6.plugin.three.maple.image`. Android installe une application distincte; les anciennes applications et leurs données peuvent être conservées, sans migration automatique des paramètres.

******

### Introduction

******

Image Viewer et Image Tools sont réunis dans 3-Maple Image pour afficher, retoucher et convertir les images.

La lecture utilise des entrées en lecture seule. La retouche et la conversion créent un fichier distinct en conservant la source. L'application autonome utilise le sélecteur de documents Android.

******

### Points forts

******

- Accueil en albums: lancée seule, l'application affiche une grille d'albums des images de l'appareil (couverture, nom, nombre), avec les dossiers autorisés dans leur propre groupe; `Afficher le contenu de tous les dossiers` dans le menu ouvre une vue à plat. La grille propose 3 à 6 colonnes (menu ou pincement), un tri par nom, date de modification, date de prise de vue ou taille mémorisé par album, une barre de défilement rapide avec bulle de date et l'actualisation par glissement.
- Visionneuse paginée avec bande de miniatures: les images ouvertes depuis la grille se parcourent dans tout l'album, la bande met en évidence la page courante et y saute au toucher, et la grille revient à la dernière image vue. Faites pivoter l'image à deux doigts; au relâchement elle se cale sur le quart de tour le plus proche (affichage seulement; désactivable dans `Paramètres` > `Visionneuse` > `Rotation à deux doigts`).
- Autorisations seulement si nécessaire: l'accueil autonome demande l'accès aux photos une seule fois, au premier lancement, puis plus jamais de lui-même; sans cet accès, `Choisir une image`, `Choisir un dossier`, `Modifier l'image` et `Convertir l'image` fonctionnent toujours via le sélecteur du système. Android 14+ permet de n'autoriser que certaines photos (`Gérer les photos sélectionnées`). `Paramètres` > `Autorisation de stockage` permet d'accorder à nouveau l'accès, de gérer les dossiers autorisés (affichés comme albums même sans accès aux photos) et l'option `Accès à tous les fichiers`. Ouvrir des images depuis AutoJs6 ou d'autres applications ne demande jamais d'autorisation.
- Afficher comme album: dans le menu d'un dossier du gestionnaire de fichiers d'AutoJs6, choisissez `Afficher comme album` pour voir ses images en grille et ouvrir ses sous-dossiers, en lecture seule via la session de l'hôte, sans demande d'autorisation ni chemin absolu affiché.
- Ouvrir une sélection précise: dans le mode de sélection d'AutoJs6, choisissez jusqu'à 128 images prises en charge dans un même dossier puis touchez `Afficher l'image`; la visionneuse conserve l'ordre de sélection de l'hôte et limite le balayage à ce groupe.
- Toucher et parcourir: toucher un fichier image pris en charge ouvre directement la visionneuse; à l'échelle 1x, glissez à gauche ou à droite pour parcourir les images prises en charge du même dossier selon l'ordre naturel des noms.
- Gestes naturels: zoom par pincement centré sur les doigts (jusqu'à 5x), déplacement à un doigt, double toucher pour basculer entre l'ajustement et un zoom 2,5x centré sur le point touché, rotation de la vue de 90° à droite et toucher simple pour masquer ou afficher les commandes. Un indicateur compact affiche le facteur actuel pendant le pincement et brièvement après un double toucher.
- Zoom détaillé pour les très grandes images: lorsqu'une image JPEG, PNG ou HEIC/HEIF statique dépasse la limite de texture de l'appareil ou le budget de décodage borné, la visionneuse affiche un aperçu sous-échantillonné puis ne décode à fort grossissement que les tuiles haute résolution de la zone visible. La mémoire des tuiles est plafonnée et libérée au changement de page ou sous pression mémoire.
- Informations clés en un coup d'oeil: la barre de titre en surimpression affiche le nom du fichier et, lorsque plusieurs images sont ouvertes, un compteur de pages du type `3 / 12`, tandis que la barre inférieure indique le type MIME, la taille du fichier et la résolution décodée (largeur x hauteur). Sous Android 8.0 ou version ultérieure, lorsque le décodeur les fournit, elle affiche aussi la profondeur de pixel décodée (bpp) et l'espace colorimétrique de sortie.
- Panneau inférieur de détails: touchez `Détails` pour ouvrir un panneau glissant avec le nom du fichier, le type MIME, la taille, la résolution et, lorsque des données EXIF existent, la date de prise de vue, l'appareil, l'exposition et l'orientation. Si des métadonnées GPS existent, la visionneuse signale leur présence mais masque les coordonnées. Les photos sont automatiquement pivotées ou mises en miroir selon leur orientation EXIF avant toute rotation manuelle de la vue.
- Imprimer ou enregistrer au format PDF: `Imprimer / enregistrer en PDF`, dans le menu en haut à droite, envoie l'image actuelle complète vers la feuille d'impression système d'Android en conservant la correction EXIF et la rotation manuelle de la vue. Pour un GIF animé, l'image visible au toucher est utilisée; le zoom et le déplacement ne recadrent pas la sortie, et le plugin ne crée aucun fichier image ou PDF temporaire.
- Formats courants immédiatement pris en charge: la famille JPEG (JPG / JPEG / JPE / JFIF), PNG, WEBP, BMP, GIF, HEIC, HEIF et AVIF, soit 11 extensions, avec lecture en boucle des GIF animés et une commande dédiée de pause/reprise.
- Partage et relais: ouvrez la feuille de partage du système d'un geste, ou utilisez `Ouvrir avec` pour éditer ou annoter, le plugin s'excluant lui-même de la liste pour éviter les boucles.
- Utilisable comme visionneuse d'images du système: une entrée Android `ACTION_VIEW` distincte sert en toute sécurité les demandes d'affichage en lecture seule venant d'autres applications.
- L'éditeur propose des préréglages de recadrage; des rotations à 90 degrés et une rotation précise de -45° à +45°; des retournements horizontal et vertical; luminosité, contraste, saturation et température de couleur; pinceaux et texte stylé, avec aperçu en direct pendant le réglage.
- Les pinceaux comprennent stylo, surligneur, mosaïque de confidentialité et gomme, avec mémorisation de la couleur et de l'épaisseur; le texte accepte plusieurs lignes, taille, couleur, contour, ombre et placement par glissement.
- Annuler et rétablir conservent jusqu'à 8 instantanés dans un budget de 192 MiB; `Restaurer l'original` est lui-même réversible et quitter avec des modifications non enregistrées exige une confirmation.
- Le dialogue d'enregistrement peut conserver le format source ou choisir JPEG, PNG ou WebP, régler la qualité avec perte et activer WebP sans perte sous Android 11+; l'hôte publie un nouveau fichier voisin sans écraser la source.
- Le convertisseur prend en charge JPEG / PNG / WebP, la qualité 1-100 (92 par défaut), une taille cible pour JPEG et WebP avec perte, WebP sans perte sous Android 11+, et l'optimisation PNG indexée automatique lorsque l'image compte au plus 256 couleurs.
- Quatre modes de taille couvrent `Original`, `Pourcentage` (1-1000), `Personnalisé` avec verrouillage des proportions et `Côté long` (1920 px par défaut, sans agrandissement); JPEG peut remplir la transparence en blanc ou noir.
- La page du convertisseur prévisualise en temps réel, en bas, la résolution de sortie, la taille estimée et le suffixe. La conservation EXIF sûre est désactivée par défaut; activée, elle garde des champs d'appareil bornés, normalise l'orientation et supprime toujours le GPS et les aperçus intégrés.
- Les changements de configuration conservent le canevas, les brouillons, l'historique annuler/rétablir et les tâches en cours; chaque action utilise toujours une entrée en lecture seule et une transaction de sortie voisine, à usage unique et appartenant à l'hôte.

******

### Captures d'écran

******

Ce sont de véritables captures de l'application autonome et d'AutoJs6 6.8.0 sur un émulateur Android 13. Toutes les images, noms de fichiers et dossiers affichés ont été générés spécialement pour la documentation et ne contiennent aucune donnée personnelle.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-albums.png?raw=true" alt="Accueil en albums" width="360" />
      <br />
      <sub>Accueil en albums</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-media-grid.png?raw=true" alt="Grille d'images d'un album" width="360" />
      <br />
      <sub>Grille d'images d'un album</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-thumbnail-strip.png?raw=true" alt="Visionneuse paginée avec la bande de miniatures" width="360" />
      <br />
      <sub>Visionneuse paginée avec la bande de miniatures</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/gallery-no-permission.png?raw=true" alt="Accueil sans accès aux photos" width="360" />
      <br />
      <sub>Accueil sans accès aux photos</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="Action d'affichage d'un seul fichier" width="360" />
      <br />
      <sub>Action d'affichage d'un seul fichier</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="Groupe exact de deux images sélectionnées" width="360" />
      <br />
      <sub>Groupe exact de deux images sélectionnées</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="Vue principale et métadonnées en direct" width="360" />
      <br />
      <sub>Vue principale et métadonnées en direct</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="Zoom immersif à 2,5x" width="360" />
      <br />
      <sub>Zoom immersif à 2,5x</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="Panneau de partage du système" width="360" />
      <br />
      <sub>Panneau de partage du système</sub>
    </td>
  </tr>
</table>

******

### Installation et utilisation

******

Avant de commencer, vérifiez les prérequis suivants:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5276
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.three.maple.image
```

Il faut 4 étapes entre l'installation et la première image affichée:

1. Lancez 3-Maple Image seule pour parcourir les albums de l'appareil (l'accès aux photos est demandé une fois au premier lancement); le menu en haut à droite permet toujours d'ouvrir une image choisie pour l'afficher, la retoucher ou la convertir et d'enregistrer le résultat à l'emplacement choisi.
2. Ouvrez AutoJs6, accédez au `Centre des plugins`, repérez `3-Maple Image` et activez-la.
3. Dans le gestionnaire de fichiers d'AutoJs6, localisez un fichier image pris en charge (par exemple `screenshot.png`).
4. Touchez le fichier. L'image s'ouvre dans la visionneuse dédiée. Choisissez `Afficher comme album` dans le menu d'un dossier pour voir ses images en grille.

Dans la visionneuse: à l'échelle 1x, glissez à gauche ou à droite pour passer à l'image prise en charge précédente ou suivante du même dossier. Pincez pour zoomer autour de vos doigts (1x à 5x), faites glisser un doigt pour vous déplacer et double-touchez pour basculer entre l'ajustement à l'écran et un zoom 2,5x centré sur le point touché. Le facteur actuel apparaît pendant le pincement et brièvement après un double toucher. Touchez `Pivoter` pour ne faire pivoter que la vue; le zoom actif est conservé, tandis que `Réinitialiser le zoom`, dans le menu en haut à droite, rétablit l'orientation d'origine et l'ajustement. Les GIF animés affichent un bouton flottant `Mettre l'animation en pause` / `Reprendre l'animation`, absent pour les images statiques. Toucher l'image masque ou affiche les barres en surimpression, et `Détails` ouvre un panneau inférieur avec les informations du fichier et les champs EXIF. `Partager` et `Ouvrir avec` restent disponibles sur l'image ouverte initialement; ces actions sont désactivées pour les pages voisines de session, qui ne possèdent volontairement aucun content URI transférable. Les images ouvertes depuis l'accueil autonome ou depuis une autre application proposent aussi `Editer`, qui ouvre l'éditeur et permet d'enregistrer le résultat; les entrées de l'hôte et les pages voisines ne le proposent pas.

******

### Formats pris en charge

******

L'action d'affichage du gestionnaire de fichiers correspond exactement aux extensions suivantes:

```text
AVIF, BMP, GIF, HEIC, HEIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

JFIF et JPE sont des extensions alias de la famille JPEG. HEIC et HEIF nécessitent Android 9 ou version ultérieure; AVIF nécessite Android 12 ou version ultérieure. La visionneuse exécute aussi une petite sonde locale de capacité de décodage et affiche un message précis si le décodeur de la plateforme est indisponible. L'entrée `ACTION_VIEW` distincte accepte les demandes par type MIME `image/*` et ne se limite pas à la liste ci-dessus. Un fichier ne peut pas dépasser 8 TiB; la prise en charge réelle du décodage dépend de la plateforme Android et de Glide.

******

### Questions fréquentes

******

**Pourquoi l'application autonome demande-t-elle l'accès aux photos, et fonctionne-t-elle si je refuse?**

L'accueil en albums a besoin de la liste des images de l'appareil: le premier lancement autonome la demande donc une fois (photos et vidéos sur Android 13+, stockage sur les versions antérieures) puis plus jamais de lui-même. Sans accès, `Choisir une image`, `Choisir un dossier`, `Modifier l'image` et `Convertir l'image` fonctionnent toujours via le sélecteur du système; vous pouvez accorder l'accès plus tard depuis le bouton de l'accueil ou dans `Paramètres` > `Autorisation de stockage`, et quand le système n'affiche plus la boîte de dialogue le bouton devient `Ouvrir les paramètres de l'application`. Ouvrir des images depuis AutoJs6 ou d'autres applications ne demande jamais d'autorisation.

**Sur Android 14+ je n'ai autorisé que certaines photos. Pourquoi d'autres albums manquent-ils?**

L'accès limité n'expose que les photos choisies dans le sélecteur de photos du système, donc seuls leurs albums apparaissent. Touchez `Gérer les photos sélectionnées` en haut de l'accueil pour modifier la sélection, ou accordez de nouveau l'accès complet dans `Paramètres` > `Autorisation de stockage`.

**À quoi sert `Accès à tous les fichiers`? En ai-je besoin?**

Non. C'est un interrupteur avancé facultatif qui ouvre la page des paramètres du système; parcourir les albums ne demande que l'accès aux photos. Il est réservé à des fonctions ultérieures comme les dossiers masqués (.nomedia) et les actions groupées sans confirmation, et peut être désactivé à tout moment dans les paramètres du système.

**Le menu du fichier n'affiche pas `Modifier l'image` et `Convertir l'image`?**

Vérifiez dans l'ordre: le code de version d'AutoJs6 est au moins 5276; le plugin est activé dans le `Centre des plugins`; l'extension du fichier ou son type MIME figure dans la liste prise en charge. Si l'une des trois conditions manque, les actions n'apparaissent pas.

**L'ouverture échoue avec `Impossible de lire les informations de l'image` ou l'écran se ferme aussitôt?**

Le fichier ne peut pas être lu pour le moment: il a peut-être été déplacé ou supprimé, l'application qui l'a partagé a peut-être retiré l'accès, ou son contenu ne correspond pas à l'extension (par exemple un fichier endommagé). Rouvrez-le depuis son emplacement actuel ou partagez-le de nouveau depuis l'application d'origine.

**Où le résultat est-il enregistré? Écrase-t-il l'original?**

L'original n'est jamais écrasé. Depuis AutoJs6, le résultat est enregistré comme un nouveau fichier à côté de la source; depuis l'accueil autonome, vous choisissez où l'enregistrer après la retouche ou la conversion.

**La conversion d'une grande image signale une mémoire insuffisante ou trop de pixels?**

La taille de sortie est bornée de trois façons: aucun côté ne peut dépasser 16384 px, le total ne peut pas dépasser 40 millions de pixels (40 MP), et le tout doit tenir dans le budget mémoire de l'appareil. Si la source dépasse les limites, passez `Redimensionner` sur `Pourcentage`, `Côté long` ou `Personnalisé` pour réduire la sortie; en cas de problème de mémoire, fermer d'autres applications ou réduire encore la résolution suffit généralement.

**Pourquoi une image éditée ressort-elle avec une résolution plus basse?**

Pour garder l'édition fluide et stable, les images au-dessus du budget de pixels d'édition (jusqu'à environ 16 MP selon la mémoire de l'appareil) sont sous-échantillonnées avant d'entrer dans l'éditeur, et le résultat enregistré correspond au canevas d'édition. Si vous voulez seulement changer le format ou la taille sans retoucher les pixels, utilisez `Convertir l'image`: il décode précisément à la taille de sortie et n'est pas soumis à ce budget.

**Peut-il traiter plusieurs images à la fois, ou enregistrer le résultat dans un autre dossier?**

La lecture accepte une sélection d'images; la retouche et la conversion traitent une image à la fois. Depuis l'accueil autonome vous choisissez la destination; les appels AutoJs6 créent un nouveau fichier à côté de la source.

******

### Sécurité

******

Le plugin applique le principe du refus par défaut. Toutes les mesures suivantes sont toujours actives et ne peuvent pas être désactivées:

- La lecture utilise des entrées en lecture seule. La retouche et la conversion créent un fichier distinct en conservant la source. L'application autonome utilise le sélecteur de documents Android.
- Ouvrir des images depuis AutoJs6 (`Afficher l'image`, `Afficher comme album`) ou d'autres applications ne demande jamais d'autorisation d'exécution; l'accueil autonome demande l'accès aux photos au plus une fois de lui-même, et seul vous pouvez activer `Accès à tous les fichiers` dans les paramètres du système.
- Le plugin ne déclare pas l'autorisation réseau (`INTERNET`): albums, miniatures et détails des images sont lus sur l'appareil et ne le quittent jamais.

******

### Interface du plugin (pour les développeurs)

******

L'hôte détecte et invoque le plugin avec les identités suivantes:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: three-maple-image
engine: explorer-action
variant: default
explorer action id: view-image
protocol version: 12
MIME type: Explorer: avif/bmp/gif/heic/heif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5276
```

L'implémentation actuelle vise la version 12 du protocole explorer-action: l'action principale déclare une cible mono-fichier, un accès en lecture seule et `readSiblings`. La session de l'hôte n'expose que les voisins directs; le plugin conserve les images prises en charge, lisibles et non symboliques, applique l'ordre naturel des noms et maintient une fenêtre limitée à 128 pages autour de l'image sélectionnée. Une seconde action en lecture seule dans la barre de sélection déclare plusieurs fichiers sans `readSiblings`; elle accepte de 1 à 128 images prises en charge sous un même parent, conserve l'ordre de sélection de l'hôte et ne transmet que les cibles explicitement autorisées. L'édition, la conversion, les détails de fichiers, la suppression, le déplacement et le renommage restent des fonctions de l'hôte; sans le plugin, l'hôte se rabat sur une demande externe `ACTION_VIEW` en lecture seule.

******

### Feuille de route

******

Les capacités achevées et les projets à venir sont tenus sous forme de liste cochable dans ROADMAP.md. Les cases non cochées expriment une intention et ne décrivent pas les capacités actuelles.

- [Ouvrir le ROADMAP.md cochable](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/ROADMAP.md)

******

### Historique des versions

******

#### v3.0.0

###### 2026/10/13

* `Note` Lancée seule, l'application demande une fois l'accès aux photos (photos et vidéos sur Android 13+, stockage sur les versions antérieures) pour afficher les albums; en cas de refus elle ne le redemande plus d'elle-même, l'affichage, la retouche et la conversion restent possibles via le sélecteur du système, et l'accès peut être accordé plus tard depuis l'accueil ou Paramètres > Autorisation de stockage. Ouvrir des images depuis AutoJs6 ou d'autres applications ne demande jamais d'autorisation
* `Fonctionnalité` L'écran d'accueil devient une vue en albums: images de l'appareil regroupées par dossier (couverture, nom, nombre) ou vue à plat du contenu de tous les dossiers; la grille propose 3 à 6 colonnes (menu ou pincement), un tri par nom, date de modification, date de prise de vue ou taille mémorisé par album, une barre de défilement rapide avec indication de date et l'actualisation par glissement
* `Fonctionnalité` La visionneuse parcourt tout l'album par glissement à gauche et à droite, une bande de miniatures saute à une page au toucher et la grille revient à la dernière image vue
* `Fonctionnalité` Faites pivoter une image à deux doigts; au relâchement elle se cale sur le quart de tour le plus proche, ne change que l'affichage, peut être annulée et se désactive dans Paramètres > Visionneuse
* `Fonctionnalité` Une nouvelle page Autorisation de stockage dans les Paramètres affiche l'accès actuel, l'accorde de nouveau, gère les photos sélectionnées sur Android 14+, ajoute ou retire des dossiers autorisés (affichés comme albums même sans accès aux photos) et propose l'option Accès à tous les fichiers
* `Fonctionnalité` Le menu de dossier du gestionnaire de fichiers d'AutoJs6 gagne Afficher comme album, qui montre les images du dossier en grille et ouvre ses sous-dossiers
* `Dépendance` Ajout de Navigation Compose 2.10.2 pour la navigation entre l'accueil et les pages d'album

#### v2.1.0

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

#### v2.0.0

###### 2026/10/04

* `Note` L'identifiant passe de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools à io.github.supermonster003.autojs6.plugin.three.maple.image. Android installe une application distincte; les anciennes applications et leurs données peuvent être conservées, sans migration automatique des paramètres
* `Fonctionnalité` Image Viewer et Image Tools sont réunis dans 3-Maple Image pour afficher, retoucher et convertir les images
* `Fonctionnalité` Un écran d'accueil autonome permet de choisir une image, de la retoucher ou convertir et d'enregistrer le résultat à l'emplacement choisi
* `Fonctionnalité` Les paramètres communs proposent la langue, le mode sombre, la couleur et quatre icônes de lanceur

##### Historique complet

* [CHANGELOG-fr.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/app/src/main/assets/doc/CHANGELOG-fr.md)

******

### Compilation

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Compilation Release:

```powershell
.\gradlew.bat :app:assembleRelease
```

Les paramètres de compilation proviennent de `version.properties`. Le SDK minimum actuel est 24 et le SDK cible est 36.

******

### Organisation des ressources

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
docs/images/screenshots/*.png
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localise les informations du plugin et l'interface de la visionneuse, tandis que `plugin_instruction.md` fournit les instructions affichées par l'hôte. Tous les fichiers README et CHANGELOG sont générés depuis des sources JSON par `.python/generate_markdown.py`: pour modifier la documentation, éditez les fichiers `lang_*.json` sous `.readme` et `.changelog` puis relancez le script au lieu de modifier les fichiers Markdown générés.

******

### Liens

******

- Documentation AutoJs6: https://docs.autojs6.com
- Partage de fichiers sécurisé sur Android: https://developer.android.com/training/secure-file-sharing
- Glide (moteur de chargement et de rendu d'images): https://github.com/bumptech/glide


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/docs/16kb.md)


### Sources et remerciements

[THIRD_PARTY_NOTICES.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/THIRD_PARTY_NOTICES.md) · [RIGHTS_AND_TAKEDOWN.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Three-Maple-Image/blob/master/RIGHTS_AND_TAKEDOWN.md)
