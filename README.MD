===================================================================================================
===================================== Distributed File System =====================================
===================================================================================================

===================================================================================================
======================================== Global information =======================================
===================================================================================================
Project name : Tiny-Dfs
Version : 1.0.0
Release date : 10/12/2015
Author : Thomas Benhamou, Rémi Kaeffer, Michel Mvouma
Purpose : Project Distributed System M1 MIAGE, supervise by Justine Rochas
Render date : 10/12/2015
===================================================================================================


===================================================================================================
============================================== Usage ==============================================
===================================================================================================
Utiliser le script python fourni :	start-tiny-dfs.py pour linux et mac
									start-tiny-dfs-win.py pour windows
Params : masterServiceName dfsRootFolder nbSlave
===================================================================================================

===================================================================================================
============================================ Known bugs ===========================================
===================================================================================================
/!\ Lors du premier run du script python, maven a besoin de récréer son pom.xml pour être de
nouveau fonctionnel. Le premier run échoue donc forcément. PLus de problème à partir du second run
--> Linux/Ubuntu/Mac : l'accès à un File.lastModified() retourne une précision à la seconde.
Le test a donc une grande probavilité d'échouer
--> Mac : Un fichier .DS_STORE utilisés par Apple pour indexer les fichiers est automatiquement
présent dans n'importe quelle répertoire. Ainsi, le test du nombre de fichier présent dans le 
dfsRootFOlder échoue.
--> Tout OS : Pour le bon fonctionnement des tests d'écrasement et de nombre de fichier, on ne 
supprime pas les fichiers présent dans le dfsRootFolder lors de la création de l'arbre. 
Ainsi, si un DFS avec un nombre de slave inférieur à un DFS présent précedemment fait un test de 
nombre de fichier ou d'écrasement , ce dernier échoue.
===================================================================================================
