<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="image-tools-ic-launcher" border="0" width="128" />
  </p>

  <p>Plugin de gestionnaire de fichiers. Modifier et convertir des images en toute sécurité</p>

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
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-Image-Tools/blob/master/.readme/README-ar.md)

******

### Introduction

******

Image Tools fournit des actions indépendantes pour modifier et convertir une seule image dans le gestionnaire de fichiers. Il lit la source sans la modifier et écrit uniquement dans une transaction de sortie appartenant à l'hôte.

******

### Fonctions

******

- Modifier avec recadrage, rotation, retournement, luminosité, contraste, saturation, température de couleur, pinceau, texte et annulation.
- Convertir en JPEG, PNG ou WebP avec qualité, redimensionnement, verrouillage du ratio et fond JPEG.
- Décoder via ContentResolver et ParcelFileDescriptor sans chemin brut ni BitmapFactory.decodeFile.
- Encoder uniquement vers l'URI exact fourni par l'hôte et renvoyer uniquement l'ID de transaction en cas de succès.

******

### Formats pris en charge

******

Le plugin valide le contenu par décodage Android au lieu de faire confiance à l'extension ou au type MIME déclaré:

```text
Input: Android-decodable images; output: JPEG, PNG, WebP
```

******

### Interface du plugin

******

L'hôte découvre et exécute le plugin avec les identités suivantes:

```text
service action: org.autojs.plugin.EXPLORER_ACTION
execute action: org.autojs.plugin.EXPLORER_ACTION_EXECUTE
plugin id: image-tools
engine: explorer-action
variant: default
Explorer action id: edit-image / convert-image
MIME type: input: image/*; output: image/jpeg, image/png, image/webp
required host build: 5269
```

La version 1 enregistre deux actions overflow du protocole v3 uniquement dans l'Explorer principal. Chaque action accepte une source en lecture seule et crée un nouveau fichier voisin via la transaction de l'hôte. La source n'est jamais remplacée.

La version 5269 ou ultérieure de l'hôte est requise.

******

### Sécurité

******

Le plugin ne demande aucune autorisation de stockage ou de réseau. Il rejette les requêtes non v3, les surfaces non principales, les actions inattendues, les URI parents, les éléments ClipData supplémentaires, les sources inscriptibles, les URI non content, les ID invalides, les MIME de sortie non pris en charge et les limites supérieures à 256 MiB. L'URI de sortie n'est jamais renvoyé.

******

### Limites de sécurité

******

- Une image source en lecture seule et un URI de sortie exact par action.
- Taille maximale de l'entrée déclarée et de la sortie encodée: `256 MiB`.
- Sortie limitée à JPEG, PNG ou WebP.
- Dimensions, pixels, échantillonnage, mémoire, historique et octets encodés sont limités.
- L'annulation renvoie `RESULT_CANCELED`; le succès renvoie uniquement l'ID correspondant.

******

### Historique des versions

******

# v1.0.1

###### 2026/08/08

* `Correctif` Renvoyer une liaison de service Explorer Action valide lors de l'activation depuis le centre des plugins
* `Amélioration` Raccourcir le nom et la description du plugin et rendre la documentation utilisateur plus naturelle

# v1.0.0

###### 2026/08/02

* `Fonctionnalité` Plugin Image Tools avec ID `image-tools`, actions `edit-image` et `convert-image`, moteur `explorer-action` et variante `default`
* `Fonctionnalité` Actions overflow du protocole Explorer Action v3 avec une image en lecture seule et une transaction create-sibling appartenant a l hote
* `Fonctionnalité` Editeur avec recadrage, rotation, retournement, reglages de couleur, pinceau, texte et annulation
* `Fonctionnalité` Conversion JPEG, PNG et WebP avec qualite, redimensionnement, ratio, fond JPEG et limites de memoire
* `Fonctionnalité` Entrees et sorties ContentResolver et ParcelFileDescriptor sans chemin brut, ecriture voisine directe, URI arbitraire, stockage ou reseau
* `Fonctionnalité` Métadonnées, interface, instructions, README et journaux localisés dans 10 langues
* `Amélioration` Conservation des sessions de l'éditeur et du convertisseur lors des changements de configuration, y compris les outils de canevas, les brouillons de dialogue, les options, l'historique d'annulation et les tâches en cours
* `Amélioration` Protection des transactions de sortie de l'hôte par une revendication persistante à usage unique, des gardes d'activité, des coroutines annulables et le retour du résultat après la fermeture du writer
* `Amélioration` Validation renforcée des types MIME de sortie, libération des bitmaps, cohérence des ressources anglaises, gestion lint des points de suspension et fermeture du flux de résumé de publication
* `Dépendance` Ajout d'AndroidX ExifInterface 1.4.2 pour l'analyse sécurisée des métadonnées d'image
* `Dépendance` Ajout de Robolectric 4.16.1 pour les tests du cycle de vie et des transactions persistantes

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

Les paramètres proviennent de `version.properties`. Le SDK minimal est 24 et le SDK cible est 36.

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

`strings.xml` localise les textes. `plugin_instruction.md` fournit les instructions. `.python/generate_markdown.py` génère les README et journaux depuis les sources JSON.

******

### Liens

******

- Documentation AutoJs6: https://docs.autojs6.com
- Partage sécurisé de fichiers Android: https://developer.android.com/training/secure-file-sharing
