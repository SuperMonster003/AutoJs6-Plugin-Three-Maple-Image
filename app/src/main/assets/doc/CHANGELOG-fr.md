# Historique des versions

## v1.2.0

###### 2026/09/13

* `Fonctionnalité` Historique local accessible depuis l'interface, avec traductions et repli en anglais
* `Amélioration` Vérification de la signature complète, des APK attendus et de la reproductibilité de la documentation

## v1.1.0

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

## v1.0.1

###### 2026/08/08

* `Correctif` Le plugin ne pouvait pas être activé depuis le centre des plugins AutoJs6 car le service renvoyait une liaison vide (onNullBinding)
* `Amélioration` Nom et description du plugin raccourcis et formulation de la documentation utilisateur unifiée entre les langues

## v1.0.0

###### 2026/08/02

* `Fonctionnalité` Première version d'Image Tools : deux actions de menu contextuel, `Modifier l'image` et `Convertir l'image`, pour une image unique dans le gestionnaire de fichiers AutoJs6, le résultat étant enregistré comme nouveau fichier à côté de la source qui reste en lecture seule
* `Fonctionnalité` Éditeur avec recadrage, rotation, retournement, luminosité, contraste, saturation, température de couleur, pinceau, texte et jusqu'à 8 étapes d'annulation, avec application automatique de l'orientation EXIF
* `Fonctionnalité` Convertisseur avec sortie JPEG / PNG / WebP : qualité réglable de 1 à 100, trois modes de redimensionnement (Original / Pourcentage / Personnalisé), verrouillage des proportions, choix de la couleur de fond JPEG et estimation en direct de la taille de sortie
* `Fonctionnalité` Reconnaissance des extensions bmp / gif / heic / heif / jpg / jpeg / png / webp et de tous les types MIME `image/*`
* `Fonctionnalité` Service du plugin enregistré sur le protocole explorer-action v3 : entrée en lecture seule à usage unique associée à des transactions de sortie détenues par l'hôte, sans demande de permission de stockage ni de réseau
* `Fonctionnalité` Métadonnées du plugin, interface, instructions, README et journal des modifications en 10 langues : chinois simplifié, chinois traditionnel (Hong Kong / Taïwan), anglais, français, espagnol, japonais, coréen, russe et arabe
* `Amélioration` Sessions de l'éditeur et du convertisseur préservées lors des changements de configuration comme la rotation de l'écran, y compris les outils du canevas, les brouillons de dialogues, les options de conversion, l'historique d'annulation et les tâches en cours
* `Amélioration` Écritures de sortie protégées par des déclarations de transaction à usage unique, une protection contre les actions simultanées et des coroutines sûres à l'annulation, évitant les soumissions en double et les fichiers partiels résiduels
* `Amélioration` Validation MIME de sortie, recyclage mémoire des bitmaps et cohérence des ressources multilingues renforcés
* `Dépendance` Ajout d'AndroidX ExifInterface 1.4.2 pour lire en toute sécurité les métadonnées d'orientation des images
* `Dépendance` Ajout de Robolectric 4.16.1 pour les tests unitaires du cycle de vie et des transactions de sortie
