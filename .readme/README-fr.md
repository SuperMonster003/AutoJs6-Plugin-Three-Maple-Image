<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>Edition et conversion securisees d'images pour AutoJs6 Explorer</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-Image-Tools?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-Image-Tools?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Langues

******

Le fichier README.md actuel prend en charge les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-en.md)
- Francais [fr] # actuel
- [Espanol [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### Introduction

******

Le plugin AutoJs6 Image Tools fournit des actions overflow independantes pour modifier et convertir une seule image dans l'Explorer principal. Il lit la source sans la modifier et ecrit uniquement dans une transaction de sortie appartenant a l'hote.

******

### Fonctions

******

- Modifier avec recadrage, rotation, retournement, luminosite, contraste, saturation, temperature de couleur, pinceau, texte et annulation.
- Convertir en JPEG, PNG ou WebP avec qualite, redimensionnement, verrouillage du ratio et fond JPEG.
- Decoder via ContentResolver et ParcelFileDescriptor sans chemin brut ni BitmapFactory.decodeFile.
- Encoder uniquement vers l'URI exact fourni par AutoJs6 et renvoyer uniquement l'ID de transaction en cas de succes.

******

### Formats pris en charge

******

Le plugin valide le contenu par decodage Android au lieu de faire confiance a l'extension ou au type MIME declare:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### Interface du plugin

******

AutoJs6 decouvre et execute le plugin avec les identites suivantes:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
supported ABIs: unrestricted (supportedAbis = emptyArray())
```

La version 1 enregistre deux actions overflow du protocole v3 uniquement dans l'Explorer principal. Chaque action accepte une source en lecture seule et cree un nouveau fichier voisin via la transaction de l'hote. La source n'est jamais remplacee.

Le plugin est entierement implemente sur la JVM et ne contient aucune bibliotheque native. Il declare `supportedAbis = emptyArray()` et fournit un APK independant de l'ABI. AutoJs6 build 5269 ou ulterieur est requis.

******

### Securite

******

Le plugin ne demande aucune autorisation de stockage ou de reseau. Il rejette les requetes non v3, les surfaces non principales, les actions inattendues, les URI parents, les elements ClipData supplementaires, les sources inscriptibles, les URI non content, les ID invalides, les MIME de sortie non pris en charge et les limites superieures a 256 MiB. L'URI de sortie n'est jamais renvoye.

******

### Limites de securite

******

- Une image source en lecture seule et un URI de sortie exact par action.
- Taille maximale de l'entree declaree et de la sortie encodee: `256 MiB`.
- Sortie limitee a JPEG, PNG ou WebP.
- Dimensions, pixels, echantillonnage, memoire, historique et octets encodes sont limites.
- L'annulation renvoie `RESULT_CANCELED`; le succes renvoie uniquement l'ID correspondant.

******

### Historique des versions

******

# v1.0.0

###### 2026/08/02

* `Fonction` Plugin Image Tools avec ID `image-tools`, actions `edit-image` et `convert-image`, moteur `explorer-action` et variante `default`
* `Fonction` Actions overflow du protocole Explorer Action v3 avec une image en lecture seule et une transaction create-sibling appartenant a l hote
* `Fonction` Editeur avec recadrage, rotation, retournement, reglages de couleur, pinceau, texte et annulation
* `Fonction` Conversion JPEG, PNG et WebP avec qualite, redimensionnement, ratio, fond JPEG et limites de memoire
* `Fonction` Entrees et sorties ContentResolver et ParcelFileDescriptor sans chemin brut, ecriture voisine directe, URI arbitraire, stockage ou reseau
* `Fonction` Implementation JVM pure, ABI sans restriction, un APK independant et ressources, README et journaux dans 10 langues
* `Amelioration` Conservation des sessions de l'éditeur et du convertisseur lors des changements de configuration, y compris les outils de canevas, les brouillons de dialogue, les options, l'historique d'annulation et les tâches en cours
* `Amelioration` Protection des transactions de sortie de l'hôte par une revendication persistante à usage unique, des gardes d'activité, des coroutines annulables et le retour du résultat après la fermeture du writer
* `Amelioration` Validation renforcée des types MIME de sortie, libération des bitmaps, cohérence des ressources anglaises, gestion lint des points de suspension et fermeture du flux de résumé de publication
* `Dependance` Ajout d'AndroidX ExifInterface 1.4.2 pour l'analyse sécurisée des métadonnées d'image
* `Dependance` Ajout de Robolectric 4.16.1 pour les tests du cycle de vie et des transactions persistantes

##### Autres versions

* [CHANGELOG-fr.md](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/assets/doc/CHANGELOG-fr.md)

******

### Construction

******

```powershell
.\gradlew.bat :app:assembleDebug
```

Construction de publication:

```powershell
.\gradlew.bat :app:assembleRelease
```

Les parametres proviennent de `version.properties`. Le SDK minimal est 24 et le SDK cible est 36.

******

### Organisation des ressources

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localise les textes. `plugin_instruction.md` fournit les instructions. `.python/generate_markdown.py` genere les README et journaux depuis les sources JSON.

******

### Liens

******

- Documentation AutoJs6: https://docs.autojs6.com
- Partage securise de fichiers Android: https://developer.android.com/training/secure-file-sharing
