<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>Modifie les images et convertit leurs formats</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Langues (Languages)

******

Le fichier README.md actuel prend en charge les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### Introduction

******

Image Tools est un plugin de traitement d'images pour le gestionnaire de fichiers d'AutoJs6. Une fois activé, chaque fichier image du gestionnaire de fichiers propose deux actions dans son menu secondaire: `Modifier l'image` ouvre un éditeur avec un canevas et une barre d'outils pour les retouches courantes (recadrage, rotation, réglages de couleur, dessin), tandis que `Convertir l'image` ouvre une boîte de dialogue qui enregistre l'image en JPEG, PNG ou WebP, avec un redimensionnement optionnel.

Le résultat est toujours enregistré comme un nouveau fichier à côté de la source (son nom porte le suffixe `edited` ou `converted`). Le fichier source reste en lecture seule du début à la fin et n'est jamais modifié, écrasé ni supprimé. Le plugin ne demande aucune permission de stockage ni de réseau et ne peut accéder qu'au seul fichier d'entrée et au seul emplacement de sortie autorisés par l'hôte.

******

### Points forts

******

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

<table>
  <tr>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/file-menu-action.png?raw=true" alt="Actions du menu du fichier" width="300" />
      <br />
      <sub>Actions du menu du fichier</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/editor.png?raw=true" alt="Éditeur d'images" width="300" />
      <br />
      <sub>Éditeur d'images</sub>
    </td>
    <td align="center" valign="top" width="33%">
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/images/screenshots/converter-dialog.png?raw=true" alt="Options de conversion JPEG" width="300" />
      <br />
      <sub>Options de conversion JPEG</sub>
    </td>
  </tr>
</table>

******

### Installation et utilisation

******

Avant de commencer, vérifiez les prérequis suivants:

```text
host app: AutoJs6 (org.autojs.autojs6)
minimum host build: 5269
minimum android: 7.0 (API 24)
plugin package: io.github.supermonster003.autojs6.plugin.imagetools
```

Il faut 4 étapes entre l'installation et la première image traitée:

1. Téléchargez et installez l'APK du plugin. Le plugin n'a pas d'icône de lanceur; après l'installation, il est entièrement géré par AutoJs6.
2. Ouvrez AutoJs6, accédez au `Centre des plugins`, repérez `Outils d'image` et activez-le.
3. Dans le gestionnaire de fichiers d'AutoJs6, localisez un fichier image (par exemple `photo.jpg`) et ouvrez son menu secondaire.
4. Sélectionnez `Modifier l'image` pour entrer dans l'éditeur, ou `Convertir l'image` pour ouvrir la boîte de dialogue de conversion.

La barre de l'éditeur propose `Recadrer`, `Tourner à gauche`, `Tourner à droite`, `Rotation précise`, `Retourner horizontalement`, `Retourner verticalement`, `Luminosité`, `Contraste`, `Saturation`, `Température de couleur`, `Pinceau` et `Texte`. Le recadrage comprend des proportions libres, fixes et d'origine; la rotation précise va de -45° à +45°. Les pinceaux sont stylo, surligneur, mosaïque et gomme; le texte accepte plusieurs lignes, contour et ombre. La barre supérieure fournit `Annuler`, `Rétablir` et `Enregistrer`, et le menu supplémentaire `Restaurer l'original`. `Enregistrer` ouvre un dialogue pour conserver le format source ou choisir JPEG / PNG / WebP, régler la qualité avec perte et activer WebP sans perte sous Android 11+. L'hôte publie un nouveau fichier voisin suffixé `edited`; les métadonnées source sont supprimées et l'original n'est jamais écrasé.

Le dialogue de conversion propose JPEG / PNG / WebP (PNG par défaut), la qualité 1-100 (92 par défaut), WebP sans perte sous Android 11+ et `Taille de fichier cible`, qui choisit automatiquement la qualité pour JPEG ou WebP avec perte. `Redimensionner` offre `Original`, `Pourcentage` (1-1000), `Côté long` (1920 px par défaut, sans agrandissement) et `Personnalisé` avec verrouillage facultatif des proportions. JPEG peut remplir la transparence en blanc ou noir; PNG utilise automatiquement une palette indexée si le résultat compte au plus 256 couleurs. `Conserver les métadonnées EXIF sûres` est désactivé par défaut et supprime toujours le GPS, les aperçus intégrés et les métadonnées impossibles à inspecter sûrement. Le dialogue affiche résolution, taille estimée et suffixe. `Convertir` demande à l'hôte de publier un fichier voisin suffixé `converted`; `Annuler` ou retour ne crée aucun fichier.

******

### Formats pris en charge

******

Le gestionnaire de fichiers affiche les actions du plugin pour les fichiers portant les extensions suivantes (et tout type MIME `image/*`):

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

Le plugin identifie les images par leur contenu et ne fait pas confiance aux extensions: l'ouverture dépend de la capacité d'Android à décoder le fichier. Les fichiers animés comme les GIF et les WebP animés sont traités sur leur première image uniquement; les fichiers d'entrée et de sortie sont chacun plafonnés à 256 MiB.

******

### Questions fréquentes

******

**Le menu du fichier n'affiche pas `Modifier l'image` et `Convertir l'image`?**

Vérifiez dans l'ordre: le code de version d'AutoJs6 est au moins 5269; le plugin est activé dans le `Centre des plugins`; l'extension du fichier ou son type MIME figure dans la liste prise en charge. Si l'une des trois conditions manque, les actions n'apparaissent pas.

**L'ouverture échoue avec `Impossible de lire les informations de l'image` ou l'écran se ferme aussitôt?**

Causes courantes: le fichier est corrompu ou n'est pas une vraie image (le plugin vérifie le contenu, renommer l'extension ne sert à rien); le système ne peut pas décoder le format (HEIC / HEIF est généralement non pris en charge avant Android 9); le fichier dépasse 256 MiB; ou l'appel ne provient pas du gestionnaire de fichiers d'AutoJs6. Pour des raisons de sécurité, le plugin rejette les appels de toute autre origine.

**Où le résultat est-il enregistré? Écrase-t-il l'original?**

Il n'écrase jamais rien. L'hôte publie le résultat comme un nouveau fichier à côté de la source avec le suffixe `edited` ou `converted` et résout automatiquement les conflits de noms; le fichier source reste en lecture seule pour le plugin du début à la fin.

**La conversion d'une grande image signale une mémoire insuffisante ou trop de pixels?**

La taille de sortie est bornée de trois façons: aucun côté ne peut dépasser 16384 px, le total ne peut pas dépasser 40 millions de pixels (40 MP), et le tout doit tenir dans le budget mémoire de l'appareil. Si la source dépasse les limites, passez `Redimensionner` sur `Pourcentage`, `Côté long` ou `Personnalisé` pour réduire la sortie; en cas de problème de mémoire, fermer d'autres applications ou réduire encore la résolution suffit généralement.

**Pourquoi une image éditée ressort-elle avec une résolution plus basse?**

Pour garder l'édition fluide et stable, les images au-dessus du budget de pixels d'édition (jusqu'à environ 16 MP selon la mémoire de l'appareil) sont sous-échantillonnées avant d'entrer dans l'éditeur, et le résultat enregistré correspond au canevas d'édition. Si vous voulez seulement changer le format ou la taille sans retoucher les pixels, utilisez `Convertir l'image`: il décode précisément à la taille de sortie et n'est pas soumis à ce budget.

**Peut-il traiter plusieurs images à la fois, ou enregistrer le résultat dans un autre dossier?**

Pas encore. Le protocole explorer-action v3 ne prend en charge que les actions sur un seul fichier avec une sortie voisine, et le plugin ne peut pas choisir lui-même l'emplacement de sortie. Les actions multi-fichiers et les modes de sortie supplémentaires dépendent des versions futures du protocole et sont suivis dans la feuille de route.

******

### Sécurité

******

Le plugin applique le principe du refus par défaut. Toutes les mesures suivantes sont toujours actives et ne peuvent pas être désactivées:

- Le fichier source est strictement en lecture seule: le plugin ouvre l'entrée uniquement via un content URI en lecture seule à usage unique accordé par l'hôte, ne reçoit aucun chemin du système de fichiers et ne demande aucune permission de stockage ni de réseau.
- La sortie n'est écrite que dans l'emplacement exact pré-créé par l'hôte, et en cas de succès le plugin ne renvoie que l'identifiant de transaction; il ne peut choisir, créer ni renvoyer aucun autre URI.
- Chaque transaction de sortie est à usage unique: les identifiants consommés sont enregistrés de façon persistante, et les requêtes rejouées ou dupliquées sont rejetées d'emblée.
- Chaque appel est entièrement validé: toute discordance de version de protocole, de surface d'origine, d'identifiant d'action, de mode d'autorisation, de type MIME, de nom d'affichage ou d'identifiant de transaction interrompt l'exécution, et les autorisations en écriture sur la source sont également rejetées.
- L'entrée et la sortie sont chacune plafonnées à 256 MiB, la résolution de sortie à 16384 px par côté et 40 MP au total, et le nombre d'octets encodés est contrôlé pendant l'écriture.
- La sortie fraîchement encodée supprime les métadonnées de la source par défaut. La conservation EXIF sûre et facultative utilise une liste limitée; l'orientation est normalisée, tandis que la position GPS, les aperçus intégrés et les métadonnées impossibles à contrôler en toute sécurité sont toujours supprimés.

******

### Interface du plugin (pour les développeurs)

******

L'hôte détecte et invoque le plugin avec les identités suivantes:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
explorer action ids: edit-image / convert-image
input MIME types: image/*
output MIME types: image/jpeg, image/png, image/webp
required host build: 5269
```

L'implémentation actuelle vise le protocole explorer-action v3: deux actions de menu secondaire pour un seul fichier image sur la surface principale du gestionnaire de fichiers, chacune prenant une entrée en lecture seule et écrivant un nouveau fichier via une transaction de sortie create-sibling détenue par l'hôte, en ne renvoyant que l'identifiant de transaction en cas de succès. Les actions multi-fichiers et au niveau des dossiers dépendent des versions futures du protocole et sont suivies dans la feuille de route.

******

### Feuille de route

******

Les capacités achevées et les projets à venir sont tenus sous forme de liste cochable dans ROADMAP.md. Les cases non cochées expriment une intention et ne décrivent pas les capacités actuelles.

- [Ouvrir le ROADMAP.md cochable](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/ROADMAP.md)

******

### Historique des versions

******

#### v1.2.1

###### 2026/09/15

* `Amélioration` compileSdk et targetSdk passent à 37 (Android 17) ; le comportement du plugin ne dépend pas de la nouvelle cible

#### v1.2.0

###### 2026/09/13

* `Fonctionnalité` Historique local accessible depuis l'interface, avec traductions et repli en anglais
* `Amélioration` Vérification de la signature complète, des APK attendus et de la reproductibilité de la documentation

#### v1.1.0

###### 2026/09/12

* `Fonctionnalité` Ajout d'un historique symétrique Annuler / Rétablir, de la restauration réversible de l'original, de formats de recadrage prédéfinis et d'une rotation fine de -45° à +45° sans modifier les dimensions de sortie
* `Fonctionnalité` Extension du dessin avec Stylo, Surligneur, Mosaïque et Gomme, ainsi que du texte multiligne déplaçable avec contour, ombre et rotation
* `Fonctionnalité` Ajout dans l'éditeur des formats Source / JPEG / PNG / WebP, de la qualité avec perte réglable et du WebP sans perte sous Android 11+
* `Fonctionnalité` Ajout au convertisseur du WebP sans perte, du PNG indexé jusqu'à 256 couleurs, d'une taille de fichier cible pour JPEG / WebP avec perte et d'un redimensionnement par côté long sans agrandissement
* `Fonctionnalité` Ajout de la conservation facultative des EXIF sûres, avec suppression systématique du GPS, des aperçus intégrés et des métadonnées opaques, puis normalisation de l'orientation
* `Correctif` Correction du rejet d'images valides lorsque l'inspection des seules limites par BitmapFactory ne renvoyait correctement aucun bitmap
* `Correctif` Correction des libellés d'outils illisibles dans l'éditeur en mode sombre
* `Amélioration` Extension de la restauration d'état, des limites mémoire / sortie et de la couverture de régression à 23 suites / 99 tests
* `Amélioration` Mise à jour des README et instructions hôte en 10 langues depuis des sources partagées, avec trois captures réelles sans données personnelles
* `Amélioration` La vérification de compilation rejette les dépendances natives involontaires et produit un rapport JSON

##### Historique complet

* [CHANGELOG-fr.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-fr.md)

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
.readme/template_plugin_instruction.md
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localise les informations du plugin et l'interface de l'éditeur et du convertisseur. Les fichiers README, CHANGELOG et `plugin_instruction.md` côté hôte sont générés depuis des sources JSON et des modèles Markdown par `.python/generate_markdown.py`: pour modifier la documentation, éditez les sources sous `.readme` et `.changelog` puis relancez le script au lieu de modifier les fichiers Markdown générés.

******

### Liens

******

- Documentation AutoJs6: https://docs.autojs6.com
- Partage de fichiers sécurisé Android: https://developer.android.com/training/secure-file-sharing


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/docs/16kb.md)
