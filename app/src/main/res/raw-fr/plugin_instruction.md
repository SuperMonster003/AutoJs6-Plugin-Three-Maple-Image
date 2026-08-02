# Visionneuse d'images

Image Viewer fournit l'action principale pour les images BMP, GIF, JFIF, JPE, JPEG, JPG, PNG et WEBP dans l'explorateur AutoJs6.

La visionneuse adapte l'image à l'écran et prend en charge le zoom focal par pincement, le déplacement, la réinitialisation par double appui, les métadonnées, le partage et l'ouverture avec une autre application. Appuyez sur l'image pour masquer ou afficher les commandes.

Le plugin nécessite AutoJs6 version 5269+. Il est entièrement implémenté sur la JVM et ne dépend pas de l'ABI de l'appareil.

Limites de sécurité et de confidentialité:

- La source est ouverte avec un accès temporaire en lecture seule à un URI `content`.
- Les fichiers de plus de 8 TiB sont refusés.
- Le plugin ne demande aucune autorisation de stockage ou de réseau.
- La modification, la conversion, la suppression, le déplacement et le renommage restent des fonctions de l'hôte.
