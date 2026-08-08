# Outils d'image

Image Tools fournit les actions `Modifier l'image` et `Convertir l'image` dans le gestionnaire de fichiers.

L'éditeur prend en charge le recadrage, la rotation, les retournements, la luminosité, le contraste, la saturation, la température de couleur, le pinceau, le texte et l'annulation. Le convertisseur prend en charge JPEG, PNG et WebP, la qualité, le redimensionnement, le verrouillage du ratio et le fond JPEG.

Le plugin nécessite la version 5269 ou ultérieure de l'hôte.

Limites de sécurité et de confidentialité:

- La source est ouverte uniquement via un URI `content` exact en lecture seule.
- La sortie est encodée uniquement vers l'URI exact de transaction appartenant à l'hôte.
- Le plugin n'écrit jamais à côté de la source et ne renvoie aucun URI arbitraire.
- La sortie est limitée à JPEG, PNG ou WebP et à 256 MiB.
- Le plugin ne demande aucune autorisation de stockage ou de réseau.
