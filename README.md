# PirateMap
Procedural Pirate Maps, /r/proceduralgeneration monthly challenge Dec 2015

This project will create a map of an island, complete with a X marking the spot, the route to take to the spot, and written directions for getting there.


Here is an example:

![Small pirate map](https://cloud.githubusercontent.com/assets/15094408/11775749/511219b8-a1f6-11e5-8794-1768a03b8a2d.png)

Start at at the old shack.
Turn left at the tree with a lion painted on.
Turn left at the old post.
Turn right at the old shack.
Dig at the stone marked with a monkey!

A larger example:

![Large pirate map](https://cloud.githubusercontent.com/assets/15094408/11775792/ac1278bc-a1f6-11e5-99d8-0e90984afc08.png)

Start at the old turtle nest.
Turn right at the stone that looks like a tiger.
Turn left at the old shack.
Turn left at the stone that looks like a parrot.
Turn right at the stone that looks like a monkey.
Turn right at the stone with three scratch marks.
Turn left at the stone marked with a monkey.
Turn right through the mossy cave.
Turn right at the broken shack.
Turn right at the broken shack.
Dig at the old post!

Currently, there is no jar to download. You must clone it, compile it with java and run it. This project was developed with Java 1.7 .

How to run:

git clone https://github.com/dattasid/PirateMap.git
cd Piratemap
mkdir bin
find . -iname '*.java' | xargs javac  -d  bin
java -cp  bin piratemap.generate.PirateMap


This program takes the following command line arguments:

--size :  Image size specified as WidthxHeight eg. 800x800. If only one value is provided, it is assumed to be both width and height. Random values are chosen if both are  not provided.
--tileSize : Tile size, used to make the details smaller or larger. Default value is 32.
--seed : Random seed. Map will always be the same for the same seed. NOTE: Map might be rendered slightly differently for the same seed and might look a little different. But the grid generated will be the same, and the directions will be the same.

Example:

java -cp  bin piratemap.generate.PirateMap --seed 8978979 --tileSize 40 --size 800


