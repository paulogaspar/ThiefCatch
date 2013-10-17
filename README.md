ThiefCatch
==========

This is a simulator of bank assaults and police persuits.

Robbers are generated in their hideout, and drive to banks. Once the alarm goes on, the police station is warned, and police cars start the persuit.

The city map is read from a file, and the number of entities is customizable (number of patrol cars, and simultaneous thiefs).

This is essentially a concurrency test, where hundreds of threads run simultaneously to find the quickest paths from where an entity is to its destiny.

To run: ``java -jar ThiefCatch.jar map.txt``
