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
