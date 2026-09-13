<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>Affiche les images et leurs détails</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Viewer?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Viewer?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Langues (Languages)

******

Le fichier README.md actuel prend en charge les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-en.md)
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/.readme/README-ar.md)

******

### Introduction

******

Image Viewer est un plugin de navigation d'images pour le gestionnaire de fichiers d'AutoJs6. Une fois activé, toucher un fichier JPG, PNG, GIF ou WEBP l'ouvre dans une visionneuse dédiée: l'image s'ajuste automatiquement à l'écran, un pincement permet d'inspecter les détails et, à l'échelle 1x, un glissement vers la gauche ou la droite parcourt les images prises en charge du même dossier. Le titre et les métadonnées se mettent à jour à chaque page, tandis que l'image ouverte initialement peut être partagée ou transférée vers une autre application.

Le plugin fait une seule chose et la fait en toute sécurité: l'affichage en lecture seule. Le fichier touché initialement arrive par un content URI temporaire en lecture seule, tandis que les fichiers voisins directs ne peuvent être énumérés et ouverts qu'au moyen d'une session readSiblings de courte durée détenue par l'hôte. Le plugin ne demande aucune permission de stockage ni de réseau, ne modifie ni ne déplace jamais les sources et ferme la session de l'hôte avec la visionneuse.

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
- Bac à sable en lecture seule: aucune permission de stockage ni de réseau, accès à exactement un fichier via une autorisation temporaire en lecture seule, et le fichier source n'est jamais écrit.

******

### Captures d'écran

******

Ces captures montrent l'interface réellement exécutée avec AutoJs6 6.8.0 sur un émulateur Android 13. Toutes les images, tous les noms de fichiers et tous les dossiers affichés ont été générés spécialement pour la documentation et ne contiennent aucune donnée personnelle.

<table>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-action.png?raw=true" alt="Action d'affichage d'un seul fichier" width="360" />
      <br />
      <sub>Action d'affichage d'un seul fichier</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/explorer-selection.png?raw=true" alt="Groupe exact de deux images sélectionnées" width="360" />
      <br />
      <sub>Groupe exact de deux images sélectionnées</sub>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-main.png?raw=true" alt="Vue principale et métadonnées en direct" width="360" />
      <br />
      <sub>Vue principale et métadonnées en direct</sub>
    </td>
    <td align="center">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/viewer-zoom-2.5x.png?raw=true" alt="Zoom immersif à 2,5x" width="360" />
      <br />
      <sub>Zoom immersif à 2,5x</sub>
    </td>
  </tr>
  <tr>
    <td align="center" colspan="2">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/images/screenshots/share-sheet.png?raw=true" alt="Panneau de partage du système" width="360" />
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
plugin package: io.github.supermonster003.autojs6.plugin.imageviewer
```

Il faut 4 étapes entre l'installation et la première image affichée:

1. Téléchargez et installez l'APK du plugin. Le plugin n'a pas d'icône de lanceur; après l'installation, il est entièrement géré par AutoJs6.
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

**Toucher un fichier image n'ouvre pas cette visionneuse?**

Vérifiez dans l'ordre: le code de version d'AutoJs6 est au moins 5276 (la version 6.8.0 ou ultérieure convient); le plugin est activé dans le `Centre des plugins`; l'extension du fichier figure dans la liste prise en charge. Si l'une des trois conditions manque, le toucher ne sera pas servi par ce plugin.

**La visionneuse affiche `L'image n'a pas pu être affichée`?**

Causes courantes: les données de l'image sont corrompues ou leur encodage n'est pas pris en charge par la plateforme Android actuelle; le fichier a été déplacé, renommé ou supprimé au moment de l'ouverture; ou la taille déclarée ne correspond pas à la taille réelle (les contrôles de sécurité rejettent ces demandes).

**Puis-je éditer, recadrer ou faire pivoter définitivement les images?**

Non. Ce plugin se concentre sur l'affichage en lecture seule. `Pivoter` ne modifie que la vue actuelle, jamais le fichier source. Pour éditer, touchez `Ouvrir avec` afin de confier l'image à une application d'édition; la suppression, le déplacement et le renommage restent disponibles dans le gestionnaire de fichiers d'AutoJs6.

**Les GIF animés sont-ils lus?**

Oui. Les GIF animés sont décodés par Glide et bouclent automatiquement. La visionneuse n'affiche la commande de pause/reprise que pour une image réellement animée; elle agit uniquement sur la lecture à l'écran sans modifier le fichier source.

**Que se passe-t-il si le plugin n'est pas installé?**

L'hôte se rabat sur une demande d'affichage externe en lecture seule, servie par les applications d'images déjà présentes sur l'appareil. Une fois ce plugin installé et activé, le toucher ouvre l'image dans la visionneuse intégrée.

**Pourquoi le plugin enregistre-t-il aussi une entrée d'affichage d'images au niveau du système?**

Il s'agit de l'entrée `ACTION_VIEW` distincte, qui n'accepte que les demandes `image/*` avec des URI `content` en lecture seule, afin que d'autres applications puissent utiliser cette visionneuse. Elle est isolée de l'entrée du gestionnaire de fichiers, passe par la même validation stricte et n'écrit jamais rien non plus.

******

### Sécurité

******

Le plugin applique le principe du refus par défaut. Toutes les mesures suivantes sont toujours actives et ne peuvent pas être désactivées:

- Groupes explicites bornés: la sélection multiple accepte de 1 à 128 fichiers directs pris en charge sous un même parent. Les ID, URI, noms, ClipData ordonné, types MIME et tailles doivent être uniques lorsque nécessaire et cohérents entre eux; le contenu de chaque image est revérifié avant l'ouverture de la visionneuse.
- Zéro permission sensible: aucune permission de stockage, de réseau ni d'exécution, avec le trafic en clair désactivé; l'entrée du gestionnaire de fichiers et l'entrée de réveil sont protégées par la permission de plugin de l'hôte et ne peuvent être appelées que par lui.
- Accès temporaire, limité et en lecture seule: le fichier sélectionné utilise un content URI temporaire; les voisins directs sont énumérés et ouverts uniquement par la v12 HOST_SESSION avec un ID de cible opaque et des noms relatifs directs validés. Le plugin ne reçoit aucun chemin du système de fichiers et rejette les autorisations d'écriture ou persistantes.
- Validation des points d'entrée: l'identité de l'action, la version du protocole, l'UUID de demande, la version de l'hôte, la surface d'appel, le Bundle cible, la structure de l'URI, le ClipData, le nom du fichier, le type MIME, la taille déclarée, la relation de parent direct et le descripteur Binder de la session sont vérifiés un par un; toute anomalie entraîne le refus.
- Double contrôle du contenu: avant l'ouverture, les limites de décodage de l'image sont sondées et la taille déclarée est comparée à la taille réelle, avec rejet en cas d'écart; un fichier est plafonné à 8 TiB.
- Entrées doubles isolées: l'entrée du gestionnaire de fichiers et l'entrée externe `ACTION_VIEW` sont indépendantes; cette dernière n'accepte que les demandes d'images par URI `content` en lecture seule et passe la même vérification de contenu.
- La visionneuse n'est pas exportée: l'écran d'affichage ne peut être lancé que depuis l'intérieur du plugin, le partage et l'ouverture externe ne transmettent que des autorisations temporaires en lecture seule, et le fichier source n'est jamais écrit.

******

### Interface du plugin (pour les développeurs)

******

L'hôte détecte et invoque le plugin avec les identités suivantes:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
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

- [Ouvrir le ROADMAP.md cochable](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/ROADMAP.md)

******

### Historique des versions

******

#### v1.3.0

###### 2026/09/13

* `Fonctionnalité` Historique local accessible depuis l'interface, avec traductions et repli en anglais
* `Amélioration` Vérification de la signature complète, des APK attendus et de la reproductibilité de la documentation

#### v1.2.0

###### 2026/09/12

* `Fonctionnalité` Visionneuse repensée en écran immersif bord à bord : l'image remplit désormais toute la fenêtre sous les barres d'état et de navigation, et les barres supérieure et inférieure sont des calques translucides qui se masquent ou reviennent d'un simple appui
* `Fonctionnalité` Barre de titre en surimpression avec le nom du fichier, un compteur de pages du type `3 / 12` lors du parcours d'un dossier ou d'une sélection, et un menu en haut à droite regroupant `Réinitialiser le zoom` et `Imprimer / enregistrer en PDF`
* `Fonctionnalité` Barre d'actions inférieure avec des boutons à icône `Détails`, `Pivoter`, `Partager` et `Ouvrir avec`, plus un bouton flottant pause / reprise qui n'apparaît que pour les GIF animés
* `Fonctionnalité` Les détails de l'image s'ouvrent désormais dans un panneau inférieur glissant avec le nom du fichier, le type MIME, la taille, la résolution, les informations de couleur décodées et les champs EXIF ; balayez vers le bas, appuyez sur l'image ou sur retour pour le fermer
* `Amélioration` Prise en charge bord à bord des barres système et des encoches d'écran, afin que l'interface reste entièrement visible sur Android 15 et versions ultérieures au lieu d'être recouverte par les barres système
* `Amélioration` Chaque commande à icône porte une description d'accessibilité correspondant à son ancien libellé textuel
* `Amélioration` La vérification de compilation rejette les dépendances natives involontaires et produit un rapport JSON

#### v1.1.0

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

##### Historique complet

* [CHANGELOG-fr.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/assets/doc/CHANGELOG-fr.md)

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
