# Image Tools

Image Tools est un plugin de traitement d'images pour le gestionnaire de fichiers d'AutoJs6. Une fois activé, chaque fichier image du gestionnaire de fichiers propose deux actions dans son menu secondaire: `Modifier l'image` ouvre un éditeur avec un canevas et une barre d'outils pour les retouches courantes (recadrage, rotation, réglages de couleur, dessin), tandis que `Convertir l'image` ouvre une boîte de dialogue qui enregistre l'image en JPEG, PNG ou WebP, avec un redimensionnement optionnel.

Le résultat est toujours enregistré comme un nouveau fichier à côté de la source (son nom porte le suffixe `edited` ou `converted`). Le fichier source reste en lecture seule du début à la fin et n'est jamais modifié, écrasé ni supprimé. Le plugin ne demande aucune permission de stockage ni de réseau et ne peut accéder qu'au seul fichier d'entrée et au seul emplacement de sortie autorisés par l'hôte.

## Installation et utilisation

Avant de commencer, vérifiez les prérequis suivants:

```text
minimum host build: 5269
minimum android: 7.0 (API 24)
```

1. Téléchargez et installez l'APK du plugin. Le plugin n'a pas d'icône de lanceur; après l'installation, il est entièrement géré par AutoJs6.
2. Ouvrez AutoJs6, accédez au `Centre des plugins`, repérez `Outils d'image` et activez-le.
3. Dans le gestionnaire de fichiers d'AutoJs6, localisez un fichier image (par exemple `photo.jpg`) et ouvrez son menu secondaire.
4. Sélectionnez `Modifier l'image` pour entrer dans l'éditeur, ou `Convertir l'image` pour ouvrir la boîte de dialogue de conversion.

La barre de l'éditeur propose `Recadrer`, `Tourner à gauche`, `Tourner à droite`, `Rotation précise`, `Retourner horizontalement`, `Retourner verticalement`, `Luminosité`, `Contraste`, `Saturation`, `Température de couleur`, `Pinceau` et `Texte`. Le recadrage comprend des proportions libres, fixes et d'origine; la rotation précise va de -45° à +45°. Les pinceaux sont stylo, surligneur, mosaïque et gomme; le texte accepte plusieurs lignes, contour et ombre. La barre supérieure fournit `Annuler`, `Rétablir` et `Enregistrer`, et le menu supplémentaire `Restaurer l'original`. `Enregistrer` ouvre un dialogue pour conserver le format source ou choisir JPEG / PNG / WebP, régler la qualité avec perte et activer WebP sans perte sous Android 11+. L'hôte publie un nouveau fichier voisin suffixé `edited`; les métadonnées source sont supprimées et l'original n'est jamais écrasé.

Le dialogue de conversion propose JPEG / PNG / WebP (PNG par défaut), la qualité 1-100 (92 par défaut), WebP sans perte sous Android 11+ et `Taille de fichier cible`, qui choisit automatiquement la qualité pour JPEG ou WebP avec perte. `Redimensionner` offre `Original`, `Pourcentage` (1-1000), `Côté long` (1920 px par défaut, sans agrandissement) et `Personnalisé` avec verrouillage facultatif des proportions. JPEG peut remplir la transparence en blanc ou noir; PNG utilise automatiquement une palette indexée si le résultat compte au plus 256 couleurs. `Conserver les métadonnées EXIF sûres` est désactivé par défaut et supprime toujours le GPS, les aperçus intégrés et les métadonnées impossibles à inspecter sûrement. Le dialogue affiche résolution, taille estimée et suffixe. `Convertir` demande à l'hôte de publier un fichier voisin suffixé `converted`; `Annuler` ou retour ne crée aucun fichier.

## Formats pris en charge

Le gestionnaire de fichiers affiche les actions du plugin pour les fichiers portant les extensions suivantes (et tout type MIME `image/*`):

```text
input:  bmp, gif, heic, heif, jpg, jpeg, png, webp (image/*)
output: JPEG (jpg), PNG (png), WebP (webp)
```

Le plugin identifie les images par leur contenu et ne fait pas confiance aux extensions: l'ouverture dépend de la capacité d'Android à décoder le fichier. Les fichiers animés comme les GIF et les WebP animés sont traités sur leur première image uniquement; les fichiers d'entrée et de sortie sont chacun plafonnés à 256 MiB.

## Sécurité

Le plugin applique le principe du refus par défaut. Toutes les mesures suivantes sont toujours actives et ne peuvent pas être désactivées:

- Le fichier source est strictement en lecture seule: le plugin ouvre l'entrée uniquement via un content URI en lecture seule à usage unique accordé par l'hôte, ne reçoit aucun chemin du système de fichiers et ne demande aucune permission de stockage ni de réseau.
- La sortie n'est écrite que dans l'emplacement exact pré-créé par l'hôte, et en cas de succès le plugin ne renvoie que l'identifiant de transaction; il ne peut choisir, créer ni renvoyer aucun autre URI.
- Chaque transaction de sortie est à usage unique: les identifiants consommés sont enregistrés de façon persistante, et les requêtes rejouées ou dupliquées sont rejetées d'emblée.
- Chaque appel est entièrement validé: toute discordance de version de protocole, de surface d'origine, d'identifiant d'action, de mode d'autorisation, de type MIME, de nom d'affichage ou d'identifiant de transaction interrompt l'exécution, et les autorisations en écriture sur la source sont également rejetées.
- L'entrée et la sortie sont chacune plafonnées à 256 MiB, la résolution de sortie à 16384 px par côté et 40 MP au total, et le nombre d'octets encodés est contrôlé pendant l'écriture.
- La sortie fraîchement encodée supprime les métadonnées de la source par défaut. La conservation EXIF sûre et facultative utilise une liste limitée; l'orientation est normalisée, tandis que la position GPS, les aperçus intégrés et les métadonnées impossibles à contrôler en toute sécurité sont toujours supprimés.
