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
