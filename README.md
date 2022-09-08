# Escape The Dungeon

![61b-project-3-EFD-movie](https://user-images.githubusercontent.com/25239743/189192976-7229f091-698e-48f2-95c7-4e2222faa38e.gif)

Designed and implemented a 2D tile-based world exploration engine. By “tile-based”, the worlds I generated consist of a 2D grid of tiles. By “world exploration engine”, my software built a world, which the user will be able to explore by walking around and interacting with objects in that world. This world will have an overhead perspective. Used HashMap, LinkedList to store user’s data retrieving necessary player data to play, reload, and replay efficiently.


## Main Feature
- World is pseudorandomly generated, and contains a random number of rooms and hallways
- Generate world include distinct rooms and hallways, though it may also include outdoor spaces.
- The locations of the rooms and hallway are random. The width and height of rooms also are radom. Even, the length of hallways are random.
- Able to move up, left, down, and right using the W, A, S, and D keys, respectively.
- Given a certain seed, the same set of key presses yield the exact same results.
- Enters “:Q”, the program should quit and save.
- Loading a world run again with an input string starting with “L”.
- A health mechanic to the game to make it more interactive.
- Various monster types L, M, S which have different damage power respectively.

## Extra Features
- The ability for the user to “replay” their most recent save, visually displaying all of the actions taken since the last time a new world was created. This result in the same final state as would occur if the user had loaded the most recent save.
- A display of real date and time in the Heads Up Display
- Portals to your world which teleport the avatar.

## Features to be added
- Multiple save slots which also adds a new menu option and a new shortcut to save to slots other than slot 1.
- The menu option to change avatar appearance
- Ability to rotate the world
- NPCs (Non-Player Charcters) which interact with the avatar. For example, adding 4 ghosts which chase the avatar and end the game if they are caught
- An inventory to the game to store items which the avatar can use or equip.
