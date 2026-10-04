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
- Le dialogue prévisualise la résolution et la taille estimée en temps réel. La conservation EXIF sûre est désactivée par défaut; activée, elle garde des champs d'appareil bornés, normalise l'orientation et supprime toujours le GPS et les aperçus intégrés.
- Les changements de configuration conservent le canevas, les brouillons, l'historique annuler/rétablir et les tâches en cours; chaque action utilise toujours une entrée en lecture seule et une transaction de sortie voisine, à usage unique et appartenant à l'hôte.

******

### Captures d'écran

******

Ces captures montrent l'interface réellement exécutée avec AutoJs6 6.8.0 sur un émulateur Android 13. Toutes les images, tous les noms de fichiers et tous les dossiers affichés ont été générés spécialement pour la documentation et ne contiennent aucune donnée personnelle.

<table>
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

1. Un écran d'accueil autonome permet de choisir une image, de la retoucher ou convertir et d'enregistrer le résultat à l'emplacement choisi.
2. Ouvrez AutoJs6, accédez au `Centre des plugins`, repérez `Visionneuse d'images` et activez-la.
3. Dans le gestionnaire de fichiers d'AutoJs6, localisez un fichier image pris en charge (par exemple `screenshot.png`).
4. Touchez le fichier. L'image s'ouvre dans la visionneuse dédiée.

Dans la visionneuse: à l'échelle 1x, glissez à gauche ou à droite pour passer à l'image prise en charge précédente ou suivante du même dossier. Pincez pour zoomer autour de vos doigts (1x à 5x), faites glisser un doigt pour vous déplacer et double-touchez pour basculer entre l'ajustement à l'écran et un zoom 2,5x centré sur le point touché. Le facteur actuel apparaît pendant le pincement et brièvement après un double toucher. Touchez `Pivoter` pour ne faire pivoter que la vue; le zoom actif est conservé, tandis que `Réinitialiser le zoom`, dans le menu en haut à droite, rétablit l'orientation d'origine et l'ajustement. Les GIF animés affichent un bouton flottant `Mettre l'animation en pause` / `Reprendre l'animation`, absent pour les images statiques. Toucher l'image masque ou affiche les barres en surimpression, et `Détails` ouvre un panneau inférieur avec les informations du fichier et les champs EXIF. `Partager` et `Ouvrir avec` restent disponibles sur l'image ouverte initialement; ces actions sont désactivées pour les pages voisines de session, qui ne possèdent volontairement aucun content URI transférable.

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

**Le menu du fichier n'affiche pas `Modifier l'image` et `Convertir l'image`?**

Vérifiez dans l'ordre: le code de version d'AutoJs6 est au moins 5276; le plugin est activé dans le `Centre des plugins`; l'extension du fichier ou son type MIME figure dans la liste prise en charge. Si l'une des trois conditions manque, les actions n'apparaissent pas.

**L'ouverture échoue avec `Impossible de lire les informations de l'image` ou l'écran se ferme aussitôt?**

La lecture utilise des entrées en lecture seule. La retouche et la conversion créent un fichier distinct en conservant la source. L'application autonome utilise le sélecteur de documents Android.

**Où le résultat est-il enregistré? Écrase-t-il l'original?**

La lecture accepte une sélection d'images; la retouche et la conversion traitent une image à la fois. Depuis l'accueil autonome vous choisissez la destination; les appels AutoJs6 créent un nouveau fichier à côté de la source.

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

#### v2.0.1

###### 2026/10/04

* `Amélioration` Les icônes du centre de plugins utilisent les tailles, positions, images claires et sombres et fonds circulaires réglés dans Icon Studio, avec les sources et paramètres permettant de les reproduire

#### v2.0.0

###### 2026/10/04

* `Note` L'identifiant passe de io.github.supermonster003.autojs6.plugin.imageviewer / io.github.supermonster003.autojs6.plugin.imagetools à io.github.supermonster003.autojs6.plugin.three.maple.image. Android installe une application distincte; les anciennes applications et leurs données peuvent être conservées, sans migration automatique des paramètres
* `Fonctionnalité` Image Viewer et Image Tools sont réunis dans 3-Maple Image pour afficher, retoucher et convertir les images
* `Fonctionnalité` Un écran d'accueil autonome permet de choisir une image, de la retoucher ou convertir et d'enregistrer le résultat à l'emplacement choisi
* `Fonctionnalité` Les paramètres communs proposent la langue, le mode sombre, la couleur et quatre icônes de lanceur

#### v1.3.1

###### 2026/09/19

* `Correctif` Avertissements de lecture SDK XML v4 avec AGP 9.1 et contrôles d'alignement natif des APK déclenchés par erreur lors de l'assemblage des tests unitaires JVM, avec les plugins de compilation partagés 1.8.3
* `Amélioration` compileSdk et targetSdk passent à 37 (Android 17) ; le comportement du plugin ne dépend pas de la nouvelle cible

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
