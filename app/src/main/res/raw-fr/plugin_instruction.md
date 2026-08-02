# Outils d'image

Image Tools fournit les actions overflow `Modifier l'image` et `Convertir l'image` dans AutoJs6 Explorer.

L'editeur prend en charge le recadrage, la rotation, les retournements, la luminosite, le contraste, la saturation, la temperature de couleur, le pinceau, le texte et l'annulation. Le convertisseur prend en charge JPEG, PNG et WebP, la qualite, le redimensionnement, le verrouillage du ratio et le fond JPEG.

Le plugin necessite AutoJs6 build 5269+. Il est entierement implemente sur la JVM et ne depend pas de l'ABI.

Limites de securite et de confidentialite:

- La source est ouverte uniquement via un URI `content` exact en lecture seule.
- La sortie est encodee uniquement vers l'URI exact de transaction appartenant a l'hote.
- Le plugin n'ecrit jamais a cote de la source et ne renvoie aucun URI arbitraire.
- La sortie est limitee a JPEG, PNG ou WebP et a 256 MiB.
- Le plugin ne demande aucune autorisation de stockage ou de reseau.
