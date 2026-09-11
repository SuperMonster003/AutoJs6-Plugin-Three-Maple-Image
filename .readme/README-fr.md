<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-viewer-ic-launcher" border="0" width="128" />
  </p>

  <p>Plugin de gestionnaire de fichiers. Affichage des images avec zoom, métadonnées, partage et ouverture externe sécurisée</p>

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

Image Viewer fournit l'action principale d'affichage des images prises en charge dans le gestionnaire de fichiers. Il ouvre un content URI temporaire en lecture seule dans une visionneuse dédiée sans modifier le fichier source.

******

### Fonctionnalités

******

- Enregistre une action principale de l'explorateur avec le protocole v2 pour les huit extensions d'image auparavant affichées par l'hôte.
- Adapte l'image à l'écran et prend en charge le zoom focal par pincement, le déplacement, la réinitialisation par double appui et le masquage des commandes par appui.
- Affiche le nom du fichier, le type MIME, la taille et la résolution décodée.
- Partage l'image ou l'ouvre dans une autre application compatible en excluant ce plugin de sa propre solution de repli.
- Fournit une passerelle Android `ACTION_VIEW` indépendante pour les URI `content` en lecture seule avec des types MIME `image/*`.

******

### Formats pris en charge

******

L'action principale de l'explorateur correspond exactement à ces extensions:

```text
BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP
```

******

### Interface du plugin

******

L'hôte découvre et exécute le plugin avec les identités suivantes:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-viewer
engine: explorer-action
variant: default
Explorer action id: view-image
MIME type: Explorer: bmp/gif/jfif/jpe/jpeg/jpg/png/webp; ACTION_VIEW: image/*
required host build: 5269
```

La version 1 fournit l'action principale pour les images dans le gestionnaire de fichiers. La modification, la conversion, les informations, la suppression, le déplacement et le renommage restent des fonctions de l'hôte. Sans le plugin, l'hôte utilise une requête externe `ACTION_VIEW` en lecture seule.

La version 5269 ou ultérieure de l'hôte est requise.

******

### Sécurité

******

Le plugin ne demande aucune autorisation de stockage ou de réseau. L'hôte accorde un accès temporaire en lecture seule au content URI cible. La passerelle de l'explorateur vérifie l'action exacte, l'URI, ClipData, le nom, le type MIME, la taille déclarée et la relation au dossier parent, refuse les droits d'écriture ou persistants et ne modifie jamais la source. La passerelle externe `ACTION_VIEW` est séparée, accepte uniquement les URI `content` d'image en lecture seule et transmet seulement la cible validée.

******

### Limites de sécurité

******

- Taille maximale de l'entrée: `8 TiB`.
- Un fichier cible par action.
- Catalogue de l'explorateur: `BMP, GIF, JFIF, JPE, JPEG, JPG, PNG, WEBP`.
- `ACTION_VIEW` externe: URI `content` en lecture seule avec types MIME `image/*`.
- Le décodage réel dépend toujours d'Android et de Glide.
- La modification, la conversion, la suppression, le déplacement et le renommage sont hors du périmètre de ce plugin.

******

### Historique des versions

******

# v1.0.1

###### 2026/08/08

* `Correctif` Liaison de service nulle qui empêchait l'activation dans le centre de plugins
* `Amélioration` Nom, description et documentation utilisateur plus clairs

# v1.0.0

###### 2026/08/02

* `Fonctionnalité` Plugin Image Viewer avec l'ID `image-viewer`, l'ID d'action `view-image`, le moteur `explorer-action` et la variante `default`
* `Fonctionnalité` Affichage principal des images par le protocole Explorer Action v2 pour les fichiers BMP, GIF, JFIF, JPE, JPEG, JPG, PNG et WEBP
* `Fonctionnalité` Adaptation à l'écran avec zoom focal par pincement, déplacement, réinitialisation par double appui et commandes masquables par appui
* `Fonctionnalité` Métadonnées de nom, type MIME, taille et résolution décodée, avec partage et ouverture externe sécurisée
* `Fonctionnalité` Passerelles distinctes pour l'explorateur protégé et Android `ACTION_VIEW` public avec accès URI temporaire en lecture seule et limite de 8 TiB
* `Fonctionnalité` Version 5269 ou ultérieure de l'hôte requise
* `Fonctionnalité` Métadonnées, interface, instructions, README et historiques localisés en espagnol, français, russe, arabe, japonais, coréen, anglais, chinois simplifié, chinois traditionnel de Hong Kong et chinois traditionnel de Taïwan
* `Dépendance` Ajout de Glide version 5.0.5

# v1.2.0

###### 2026/09/11

* `Amélioration` La vérification de compilation rejette les dépendances natives involontaires et produit un rapport JSON

##### Pour consulter davantage de versions

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

Les paramètres de compilation proviennent de `version.properties`. Le SDK minimal actuel est 24 et le SDK cible est 36.

******

### Structure des ressources

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/assets/doc/CHANGELOG-*.md
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
```

`strings.xml` localise les métadonnées du plugin et le texte de l'interface. `plugin_instruction.md` fournit les instructions visibles depuis l'hôte. `.python/generate_markdown.py` génère les fichiers README et les historiques localisés depuis les sources JSON.

******

### Liens

******

- Documentation AutoJs6: https://docs.autojs6.com
- Partage sécurisé de fichiers Android: https://developer.android.com/training/secure-file-sharing

[16 KB page alignment and verification](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Viewer/blob/master/docs/16kb.md)
