package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import byow.TileEngine.TERenderer;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.File;
import java.text.SimpleDateFormat;

import java.util.Random;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Calendar;

public class World {
    private int WIDTH;
    private int HEIGHT;
    private long SEED;
    private Random RANDOM;
    private String save;
    private Avatar avatar;
    private Point door;
    private Monster sMonster;
    private Monster mMonster;
    private Monster lMonster;
    private Portal portal;
    private String beforeLoad = "";
    private String afterLoad = "";
    private int floor;
    private TERenderer ter = new TERenderer();
    private static TETile[][] TABLE;
    private HashMap<Integer, Room> roomContainer = new HashMap<>();
    private LinkedList<Room> treeList = new LinkedList<>();

    public World(TETile[][] table, int width, int height, String seed) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.TABLE = table;
        makeSeed(seed.toLowerCase());
        floor = 1;
        this.RANDOM = new Random(SEED);
        fillNothing();
        avatar = new Avatar(0, 0);
        sMonster = new Monster(0 + 1, 0, 20);
        mMonster = new Monster(0 + 2, 0, 50);
        lMonster = new Monster(0 + 3, 0 ,100);
        portal = new Portal(0 + 4, 0);
        generatWorld();
        generateAvatar();
        generateLockdoor();
        generateSmonster();
        generateMmonster();
        generateLmonster();
        generatePortal();
    }

    public void secondWorld(long seed) {
        this.TABLE = new TETile[WIDTH][HEIGHT];
        roomContainer = new HashMap<>();
        treeList = new LinkedList<>();
        ter.initialize(WIDTH, HEIGHT, 0, 0);
        this.RANDOM = new Random(seed);
        fillNothing();
        generatWorld();
        generateAvatar();
        generateLockdoor();
        generateSmonster();
        generateMmonster();
        generateLmonster();
        if (portal.getCount() != 0) {
            generatePortal();
        }
        /*if (floor == 2) {
            generateMmonster();
        } else if (floor == 3) {
            generatLSmonster();
        }*/
        ///ter.renderFrame(TABLE);
    }

    public void replay() {
        if (!beforeLoad.equals("")) {
            moveReplay(beforeLoad);
        }
        if (!afterLoad.equals("")) {
            moveReplay(afterLoad);
        }
    }

    public void load() {
        if (!beforeLoad.equals("")) {
            moveLoad(beforeLoad);
        }
        if (!afterLoad.equals("")) {
            moveLoad(afterLoad);
        }
    }

    private void makeSeed(String input) {
        String seed = "";
        String load = "";
        int index = 0;
        if (input.charAt(0) == 'n') {
            while (input.charAt(index) != 's') {
                index += 1;
            }
            save = input.substring(0, index) + 's';
            seed = input.substring(1, index);
            SEED = Long.parseLong(seed);
            seed = "";
            index += 1;
            for (int i = index; i < input.length(); i++) {
                seed = seed + input.charAt(i);
            }
            beforeLoad = seed;
        } else {
            load = loadFile();
            while (load.charAt(index) != 's') {
                index += 1;
            }
            save = load.substring(0, index) + 's';
            seed = load.substring(1, index);
            SEED = Long.parseLong(seed);
            seed = "";
            index += 1;
            for (int i = index; i < load.length(); i++) {
                seed = seed + load.charAt(i);
            }
            beforeLoad = seed;
            afterLoad = input.substring(1, input.length());
        }
    }

    public void fillNothing() {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                TABLE[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void generatWorld() {
        int count = 0;
        for (int i = 3; i < WIDTH - 9; i += 1) {
            int numberOfrectangle = RANDOM.nextInt(2);
            for (int j = 0; j < numberOfrectangle; j++) {
                int y = RANDOM.nextInt(HEIGHT - 12) + 2;
                int yEnd = y + 3 + RANDOM.nextInt(6);
                int xEnd = i + 3 + RANDOM.nextInt(6);
                if (boundary(TABLE, i, y, xEnd, yEnd)) {
                    count += 1;
                    Point newPoint =  new Point(i, y);
                    Room newRoom = new Room(newPoint, xEnd - i, yEnd - y);
                    roomContainer.put(count, newRoom);
                    treeList.addLast(newRoom);
                    generateRoom(i, y, xEnd, yEnd);
                    if (treeList.size() > 1) {
                        hallwayMaker(treeList.removeFirst(), treeList.get(0));
                    }
                }
            }
        }
    }

    private void generateRoom(int xStart, int yStart, int xEnd, int yEnd) {
        for (int i = yStart; i <= yEnd; i++) {
            for (int j = xStart; j <= xEnd; j++) {
                if (i == yStart || i == yEnd || j == xStart || j == xEnd) {
                    TABLE[j][i] = Tileset.WALL;
                } else {
                    TABLE[j][i] = Tileset.FLOOR;
                }
            }
        }
    }

    private boolean boundary(TETile[][] tile, int xStart, int yStart, int xEnd, int yEnd) {
        if (!(xEnd < WIDTH && yEnd < HEIGHT)) {
            return false;
        }
        return (tile[xStart][yStart].equals(Tileset.NOTHING)
                && tile[xEnd][yEnd].equals(Tileset.NOTHING)
                && tile[xEnd][yStart].equals(Tileset.NOTHING)
                && tile[xStart][yEnd].equals(Tileset.NOTHING)
                && tile[(xStart + xEnd) / 2][yEnd].equals(Tileset.NOTHING)
                && tile[(xStart + xEnd) / 2][yStart].equals(Tileset.NOTHING)
                && tile[xEnd][(yStart + yEnd) / 2].equals(Tileset.NOTHING)
                && tile[xStart][(yStart + yEnd) / 2].equals(Tileset.NOTHING));
    }

    private void hallwayMaker(Room r1, Room r2) {
        int x = r1.center().x;
        int y = r1.center().y;
        if (y > r2.center().y) {
            while (y >= r2.center().y) {
                TABLE[r1.center().x][y] = Tileset.FLOOR;
                if (TABLE[r1.center().x + 1][y].equals(Tileset.NOTHING)) {
                    TABLE[r1.center().x + 1][y] = Tileset.WALL;
                }
                if (TABLE[r1.center().x - 1][y].equals(Tileset.NOTHING)) {
                    TABLE[r1.center().x - 1][y] = Tileset.WALL;
                }
                y -= 1;
            }
            if (TABLE[r1.center().x - 1][y].equals(Tileset.NOTHING)) {
                TABLE[r1.center().x - 1][y] = Tileset.WALL;
            }
            if (y - 1 >= 0 && TABLE[r1.center().x - 1][y - 1].equals(Tileset.NOTHING)) {
                TABLE[r1.center().x - 1][y - 1] = Tileset.WALL;
            }
            if (TABLE[r1.center().x][y].equals(Tileset.NOTHING)) {
                TABLE[r1.center().x][y] = Tileset.WALL;
                TABLE[r1.center().x + 1][y] = Tileset.WALL;
            }
            while (x <= r2.center().x) {
                TABLE[x][y] = Tileset.FLOOR;
                if (y + 1 <= HEIGHT && TABLE[x][y + 1].equals(Tileset.NOTHING)) {
                    TABLE[x][y + 1] = Tileset.WALL;
                }
                if (y - 1 >= 0 && TABLE[x][y - 1].equals(Tileset.NOTHING)) {
                    TABLE[x][y - 1] = Tileset.WALL;
                }
                x += 1;
            }
        } else {
            while (y <= r2.center().y) {
                TABLE[r1.center().x][y] = Tileset.FLOOR;
                if (x + 1 <= WIDTH && TABLE[r1.center().x + 1][y].equals(Tileset.NOTHING)) {
                    TABLE[r1.center().x + 1][y] = Tileset.WALL;
                }
                if (x - 1 >= 0 && TABLE[r1.center().x - 1][y].equals(Tileset.NOTHING)) {
                    TABLE[r1.center().x - 1][y] = Tileset.WALL;
                }
                y += 1;
            }
            if (TABLE[r1.center().x - 1][y].equals(Tileset.NOTHING)) {
                TABLE[r1.center().x - 1][y] = Tileset.WALL;
            }
            if (y + 1 <= HEIGHT && TABLE[r1.center().x - 1][y + 1].equals(Tileset.NOTHING)) {
                TABLE[r1.center().x - 1][y + 1] = Tileset.WALL;
            }
            if (TABLE[r1.center().x][y].equals(Tileset.NOTHING)) {
                TABLE[r1.center().x][y] = Tileset.WALL;
                TABLE[r1.center().x + 1][y] = Tileset.WALL;
            }
            while (x <= r2.center().x) {
                TABLE[x][y] = Tileset.FLOOR;
                if (TABLE[x][y + 1].equals(Tileset.NOTHING)) {
                    TABLE[x][y + 1] = Tileset.WALL;
                }
                if (TABLE[x][y - 1].equals(Tileset.NOTHING)) {
                    TABLE[x][y - 1] = Tileset.WALL;
                }
                x += 1;
            }
        }
    }

    public void generateAvatar() {
        int roomChoice = RANDOM.nextInt(WIDTH);
        while (!roomContainer.containsKey(roomChoice)) {
            roomChoice = RANDOM.nextInt(WIDTH);
        }
        Room newRoom = roomContainer.get(roomChoice);
        TABLE[newRoom.center().x][newRoom.center().y] = Tileset.AVATAR;
        avatar.position.x = newRoom.center().x;
        avatar.position.y = newRoom.center().y;
    }

    public Point avatarPoint() {
        return avatar.position();
    }

    public TETile[][] table() {
        return TABLE;
    }

    private void generateLockdoor() {
        int x = RANDOM.nextInt(WIDTH - 2) + 1;
        int y = RANDOM.nextInt(HEIGHT - 2) + 1;
        while (!(TABLE[x][y].equals(Tileset.WALL) && lockDoorchekcer(x, y))) {
            x = RANDOM.nextInt(WIDTH);
            y = RANDOM.nextInt(HEIGHT);
        }
        TABLE[x][y] = Tileset.LOCKED_DOOR;
        door = new Point(x, y);
    }

    private void generateSmonster() {
        int x = RANDOM.nextInt(WIDTH - 2) + 1;
        int y = RANDOM.nextInt(HEIGHT - 2) + 1;
        while (!(TABLE[x][y].equals(Tileset.FLOOR))) {
            x = RANDOM.nextInt(WIDTH);
            y = RANDOM.nextInt(HEIGHT);
        }
        TABLE[x][y] = Tileset.SMONSTER;
        sMonster.position.x = x;
        sMonster.position.y = y;

    }

    private void generateMmonster() {
        int x = RANDOM.nextInt(WIDTH - 2) + 1;
        int y = RANDOM.nextInt(HEIGHT - 2) + 1;
        while (!(TABLE[x][y].equals(Tileset.FLOOR))) {
            x = RANDOM.nextInt(WIDTH);
            y = RANDOM.nextInt(HEIGHT);
        }
        TABLE[x][y] = Tileset.MSMONSTER;
        mMonster.position.x = x;
        mMonster.position.y = y;
    }

    private void generatePortal() {
        int x = RANDOM.nextInt(WIDTH - 2) + 1;
        int y = RANDOM.nextInt(HEIGHT - 2) + 1;
        while (!(TABLE[x][y].equals(Tileset.FLOOR))) {
            x = RANDOM.nextInt(WIDTH);
            y = RANDOM.nextInt(HEIGHT);
        }
        TABLE[x][y] = Tileset.SAND;
        portal.position.x = x;
        portal.position.y = y;
    }

    private void generateLmonster() {
        /*int x = RANDOM.nextInt(WIDTH - 2) + 1;
        int y = RANDOM.nextInt(HEIGHT - 2) + 1;
        while (!(TABLE[x][y].equals(Tileset.FLOOR))) {
            x = RANDOM.nextInt(WIDTH);
            y = RANDOM.nextInt(HEIGHT);
        }*/
        int x = door.x;
        int y = door.y;
        /*while (TABLE[x][y].equals(Tileset.FLOOR)) {
            int r = RANDOM.nextInt(4);
            switch (r) {
                case 0:
                    x = x + 1;
                    break;
                case 1:
                    x = x - 1;
                    break;
                case 2:
                    y = y + 1;
                    break;
                case 3:
                    y = y - 1;
                    break;
                default:
                    r = RANDOM.nextInt(4);
            }
        }*/
        if (TABLE[x + 1][y].equals(Tileset.FLOOR)) {
            TABLE[x + 1][y] = Tileset.LMONSTER;
            lMonster.position.x = x + 1;
            lMonster.position.y = y;
        } else if (TABLE[x][y + 1].equals(Tileset.FLOOR)) {
            TABLE[x][y + 1] = Tileset.LMONSTER;
            lMonster.position.x = x;
            lMonster.position.y = y + 1;
        } else if (TABLE[x - 1][y].equals(Tileset.FLOOR)) {
            TABLE[x - 1][y] = Tileset.LMONSTER;
            lMonster.position.x = x - 1;
            lMonster.position.y = y;
        } else if (TABLE[x][y - 1].equals(Tileset.FLOOR)) {
            TABLE[x][y - 1] = Tileset.LMONSTER;
            lMonster.position.x = x;
            lMonster.position.y = y - 1;
        }
    }

    private boolean lockDoorchekcer(int x, int y) {
        return TABLE[x + 1][y].equals(Tileset.FLOOR) || TABLE[x - 1][y].equals(Tileset.FLOOR)
                || TABLE[x][y + 1].equals(Tileset.FLOOR) || TABLE[x][y - 1].equals(Tileset.FLOOR);
    }

    public TETile[][] move() {
        Character input;
        boolean playerTurn = true;
        while (playerTurn) {
            int x = (int) StdDraw.mouseX();
            int y = (int) StdDraw.mouseY();
            ter.renderFrame(TABLE);
            hud(x, y);
            clock();
            healthHud();
            floorHud();
            StdDraw.enableDoubleBuffering();
            StdDraw.show();
            if (StdDraw.hasNextKeyTyped()) {
                input = Character.toUpperCase(StdDraw.nextKeyTyped());
                switch (input) {
                    case 'W':
                        Point up = new Point(avatar.position().x, avatar.position().y + 1);
                        movingAvatar(up);
                        movingSmonster();
                        movingMmonster();
                        movingLmonster();
                        save += "W";
                        monsterChecker(avatar.position().x, avatar.position().y);
                        if (TABLE[avatar.position().x][avatar.position().y + 1].equals(Tileset.LOCKED_DOOR)) {
                            TABLE[avatar.position().x][avatar.position().y + 1] = Tileset.UNLOCKED_DOOR;
                            ter.renderFrame(TABLE);
                            StdDraw.pause(1000);
                            floor = floor + 1;
                            secondWorld(randomSeed());
                            return TABLE;
                        } else if (TABLE[avatar.position().x][avatar.position().y + 1].equals(Tileset.SAND)) {
                            TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                            portal.count -= 1;
                            if (portal.getCount() == 0) {
                                TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                            }
                            setPortal();
                        }
                        return TABLE;
                    case 'S':
                        Point down = new Point(avatar.position().x, avatar.position().y - 1);
                        movingAvatar(down);
                        movingSmonster();
                        movingMmonster();
                        movingLmonster();
                        save += "S";
                        monsterChecker(avatar.position().x, avatar.position().y);
                        if (TABLE[avatar.position().x][avatar.position().y - 1].equals(Tileset.LOCKED_DOOR)) {
                            TABLE[avatar.position().x][avatar.position().y - 1] = Tileset.UNLOCKED_DOOR;
                            ter.renderFrame(TABLE);
                            StdDraw.pause(1000);
                            floor = floor + 1;
                            secondWorld(randomSeed());
                            return TABLE;
                        } else if (TABLE[avatar.position().x][avatar.position().y - 1].equals(Tileset.SAND)) {
                            TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                            portal.count -= 1;
                            if (portal.getCount() == 0) {
                                TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                            }
                            setPortal();
                        }
                        return TABLE;
                    case 'A':
                        Point left = new Point(avatar.position().x - 1, avatar.position().y);
                        movingAvatar(left);
                        movingSmonster();
                        movingMmonster();
                        movingLmonster();
                        save += "A";
                        monsterChecker(avatar.position().x, avatar.position().y);
                        if (TABLE[avatar.position().x - 1][avatar.position().y].equals(Tileset.LOCKED_DOOR)) {
                            TABLE[avatar.position().x - 1][avatar.position().y] = Tileset.UNLOCKED_DOOR;
                            ter.renderFrame(TABLE);
                            StdDraw.pause(1000);
                            floor = floor + 1;
                            secondWorld(randomSeed());
                            return TABLE;
                        } else if (TABLE[avatar.position().x - 1][avatar.position().y].equals(Tileset.SAND)) {
                            TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                            portal.count -= 1;
                            if (portal.getCount() == 0) {
                                TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                            }
                            setPortal();
                        }
                        return TABLE;
                    case 'D':
                        Point right = new Point(avatar.position().x + 1, avatar.position().y);
                        movingAvatar(right);
                        movingSmonster();
                        movingMmonster();
                        movingLmonster();
                        save += "D";
                        monsterChecker(avatar.position().x, avatar.position().y);
                        if (TABLE[avatar.position().x + 1][avatar.position().y].equals(Tileset.LOCKED_DOOR)) {
                            TABLE[avatar.position().x + 1][avatar.position().y] = Tileset.UNLOCKED_DOOR;
                            ter.renderFrame(TABLE);
                            StdDraw.pause(1000);
                            floor = floor + 1;
                            secondWorld(randomSeed());
                            return TABLE;
                        } else if (TABLE[avatar.position().x + 1][avatar.position().y].equals(Tileset.SAND)) {
                            TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                            portal.count -= 1;
                            if (portal.getCount() == 0) {
                                TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                            }
                            setPortal();
                        }
                        return TABLE;
                    case ':':
                        while (true) {
                            if (StdDraw.hasNextKeyTyped()) {
                                if (Character.toUpperCase(StdDraw.nextKeyTyped()) == 'Q') {
                                    saveFile(save);
                                    System.exit(0);
                                    return TABLE;
                                }
                            }
                        }
                    default:
                        return TABLE;
                }
            }
        }
        TABLE[avatar.position().x][avatar.position().y] = Tileset.TREE;
        return TABLE;
    }

    public boolean userTurn() {
        if (avatar.health > 0 && floor < 4) {
            return true;
        } else if (avatar.health <= 0) {
            save = save.substring(0, save.length() - 1);
            saveFile(save);
            TABLE[avatar.position().x][avatar.position().y] = Tileset.TREE;
            ter.renderFrame(TABLE);
            StdDraw.pause(1000);
            return false;
        } else {
            fillNothing();
            ter.renderFrame(TABLE);
            return false;
        }
    }

    private long randomSeed() {

        return RANDOM.nextLong();
    }

    public int getFloor() {
        return floor;
    }

    private void monsterChecker(int x, int y) {
        if (TABLE[x][y + 1].equals(Tileset.SMONSTER) || TABLE[x][y - 1].equals(Tileset.SMONSTER) ||
                TABLE[x + 1][y].equals(Tileset.SMONSTER) || TABLE[x - 1][y].equals(Tileset.SMONSTER)) {
            avatar.health = avatar.health - sMonster.attack;
        } else if (TABLE[x][y + 1].equals(Tileset.MSMONSTER) || TABLE[x][y - 1].equals(Tileset.MSMONSTER) ||
                TABLE[x + 1][y].equals(Tileset.MSMONSTER) || TABLE[x - 1][y].equals(Tileset.MSMONSTER)) {
            avatar.health = avatar.health - mMonster.attack;
        } else if (TABLE[x][y + 1].equals(Tileset.LMONSTER) || TABLE[x][y - 1].equals(Tileset.LMONSTER) ||
                TABLE[x + 1][y].equals(Tileset.LMONSTER) || TABLE[x - 1][y].equals(Tileset.LMONSTER)) {
            avatar.health = avatar.health - lMonster.attack;
        }
    }

    private void hud(int x, int y) {
        if (x > 0 && x < WIDTH && y > 0 && y < HEIGHT) {
            if (TABLE[x][y].equals(Tileset.NOTHING)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Nothing");
            } else if (TABLE[x][y].equals(Tileset.WALL)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Wall");
            } else if (TABLE[x][y].equals(Tileset.FLOOR)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Floor");
            } else if (TABLE[x][y].equals(Tileset.AVATAR)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Avatar");
            } else if (TABLE[x][y].equals(Tileset.LOCKED_DOOR)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Locked Door");
            } else if (TABLE[x][y].equals(Tileset.UNLOCKED_DOOR)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Unlocked Door");
            } else if (TABLE[x][y].equals(Tileset.MOUNTAIN)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Unlocked Door");
            } else if (TABLE[x][y].equals(Tileset.SMONSTER)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "sMonster");
            } else if (TABLE[x][y].equals(Tileset.MSMONSTER)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "mMonster");
            } else if (TABLE[x][y].equals(Tileset.LMONSTER)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "lMonster");
            } else if (TABLE[x][y].equals(Tileset.SAND)) {
                //ter.renderFrame(TABLE);
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(5, HEIGHT - 2, "Portal");
            }
        }
    }

    private void healthHud() {
        String health = "h:" + avatar.health;
        if (avatar.health > 20) {
            StdDraw.setPenColor(Color.GREEN);
            StdDraw.text(avatar.position().x, avatar.position().y +  2, health);
            StdDraw.enableDoubleBuffering();
        } else if (avatar.health <= 20) {
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(avatar.position().x, avatar.position().y +  2, health);
        }
    }

    private void portalCount() {
        String count = "  " + portal.getCount();
        StdDraw.setPenColor(Color.GREEN);
        StdDraw.text(portal.getPosition().x, portal.getPosition().y + 1, count);
        StdDraw.enableDoubleBuffering();
    }

    public void clock() {
        String timeStamp
                = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                .format(Calendar.getInstance().getTime());
        StdDraw.setPenColor(Color.CYAN);
        StdDraw.text(5, HEIGHT - 1, timeStamp);
        StdDraw.enableDoubleBuffering();
    }

    private void floorHud() {
        if (floor == 1) {
            StdDraw.setPenColor(Color.RED);
            StdDraw.text(WIDTH / 2, HEIGHT - 1, "3rd Floor");
            StdDraw.enableDoubleBuffering();
        } else if (floor == 2) {
            StdDraw.setPenColor(Color.CYAN);
            StdDraw.text(WIDTH / 2, HEIGHT - 1, "2nd Floor");
            StdDraw.enableDoubleBuffering();
        } else {
            StdDraw.setPenColor(Color.CYAN);
            StdDraw.text(WIDTH / 2, HEIGHT - 1, "1st Floor");
            StdDraw.enableDoubleBuffering();
        }
    }

    public void moveReplay(String user) {
        ter.initialize(WIDTH, HEIGHT, 0, 0);
        for (int i = 0; i < user.length(); i++) {
            healthHud();
            floorHud();
            StdDraw.enableDoubleBuffering();
            StdDraw.show();
            StdDraw.pause(50);
            switch (user.charAt(i)) {
                case 'w':
                    Point up = new Point(avatar.position().x, avatar.position().y + 1);
                    movingAvatar(up);
                    movingSmonster();
                    movingMmonster();
                    movingLmonster();
                    if (TABLE[avatar.position().x][avatar.position().y + 1].equals(Tileset.LOCKED_DOOR)) {
                        TABLE[avatar.position().x][avatar.position().y + 1] = Tileset.UNLOCKED_DOOR;
                        ter.renderFrame(TABLE);
                        StdDraw.pause(1000);
                        //TETile[][] newtable = new TETile[WIDTH][HEIGHT];
                        //secondWorld(newtable, WIDTH, HEIGHT, randomSeed());
                        floor = floor + 1;
                        secondWorld(randomSeed());
                    } else if (TABLE[avatar.position().x][avatar.position().y + 1].equals(Tileset.SAND)) {
                        TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                        portal.count -= 1;
                        if (portal.getCount() == 0) {
                            TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                        }
                        setPortal();
                    }
                    monsterChecker(avatar.position().x, avatar.position().y);
                    ter.renderFrame(TABLE);
                    save += "w";
                    break;
                case 's':
                    Point down = new Point(avatar.position().x, avatar.position().y - 1);
                    movingAvatar(down);
                    movingSmonster();
                    movingMmonster();
                    movingLmonster();
                    if (TABLE[avatar.position().x][avatar.position().y - 1].equals(Tileset.LOCKED_DOOR)) {
                        TABLE[avatar.position().x][avatar.position().y - 1] = Tileset.UNLOCKED_DOOR;
                        ter.renderFrame(TABLE);
                        StdDraw.pause(1000);
                        floor = floor + 1;
                        secondWorld(randomSeed());
                        //TETile[][] newtable = new TETile[WIDTH][HEIGHT];
                        //secondWorld(newtable, WIDTH, HEIGHT, randomSeed());
                    } else if (TABLE[avatar.position().x][avatar.position().y - 1].equals(Tileset.SAND)) {
                        TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                        portal.count -= 1;
                        if (portal.getCount() == 0) {
                            TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                        }
                        setPortal();
                    }
                    monsterChecker(avatar.position().x, avatar.position().y);
                    ter.renderFrame(TABLE);
                    save += "s";
                    break;
                case 'a':
                    Point left = new Point(avatar.position().x - 1, avatar.position().y);
                    movingAvatar(left);
                    movingSmonster();
                    movingMmonster();
                    movingLmonster();
                    if (TABLE[avatar.position().x - 1][avatar.position().y].equals(Tileset.LOCKED_DOOR)) {
                        TABLE[avatar.position().x - 1][avatar.position().y] = Tileset.UNLOCKED_DOOR;
                        ter.renderFrame(TABLE);
                        StdDraw.pause(1000);
                        floor = floor + 1;
                        secondWorld(randomSeed());
                        //TETile[][] newtable = new TETile[WIDTH][HEIGHT];
                        //secondWorld(newtable, WIDTH, HEIGHT, randomSeed());
                    } else if (TABLE[avatar.position().x - 1][avatar.position().y].equals(Tileset.SAND)) {
                        TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                        portal.count -= 1;
                        if (portal.getCount() == 0) {
                            TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                        }
                        setPortal();
                    }
                    monsterChecker(avatar.position().x, avatar.position().y);
                    ter.renderFrame(TABLE);
                    save += "a";
                    break;
                case 'd':
                    Point right = new Point(avatar.position().x + 1, avatar.position().y);
                    movingAvatar(right);
                    movingSmonster();
                    movingMmonster();
                    movingLmonster();
                    if (TABLE[avatar.position().x + 1][avatar.position().y].equals(Tileset.LOCKED_DOOR)) {
                        TABLE[avatar.position().x + 1][avatar.position().y] = Tileset.UNLOCKED_DOOR;
                        ter.renderFrame(TABLE);
                        StdDraw.pause(1000);
                        floor = floor + 1;
                        secondWorld(randomSeed());
                        //TETile[][] newtable = new TETile[WIDTH][HEIGHT];
                        //secondWorld(newtable, WIDTH, HEIGHT, randomSeed());
                    } else if (TABLE[avatar.position().x + 1][avatar.position().y].equals(Tileset.SAND)) {
                        TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
                        portal.count -= 1;
                        if (portal.getCount() == 0) {
                            TABLE[portal.getPosition().x][portal.getPosition().y] = Tileset.FLOOR;
                        }
                        setPortal();
                    }
                    monsterChecker(avatar.position().x, avatar.position().y);
                    ter.renderFrame(TABLE);
                    save += "d";
                    break;
                case ':':
                    saveFile(save);
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }

    public void moveLoad(String user) {
        for (int i = 0; i < user.length(); i++) {
            switch (user.charAt(i)) {
                case 'w':
                    Point up = new Point(avatar.position().x, avatar.position().y + 1);
                    movingAvatar(up);
                    save += "w";
                    break;
                case 's':
                    Point down = new Point(avatar.position().x, avatar.position().y - 1);
                    movingAvatar(down);
                    save += "s";
                    break;
                case 'a':
                    Point left = new Point(avatar.position().x - 1, avatar.position().y);
                    movingAvatar(left);
                    save += "a";
                    break;
                case 'd':
                    Point right = new Point(avatar.position().x + 1, avatar.position().y);
                    movingAvatar(right);
                    save += "d";
                    break;
                case ':':
                    saveFile(save);
                    break;
                default:
            }
        }
    }

    private void movingAvatar(Point p) {
        if (TABLE[p.x][p.y].equals(Tileset.FLOOR)) {
                //|| TABLE[p.x][p.y].equals(Tileset.UNLOCKED_DOOR)) {
            TABLE[avatar.position().x][avatar.position().y] = Tileset.FLOOR;
            TABLE[p.x][p.y] = Tileset.AVATAR;
            avatar.position().x = p.x;
            avatar.position().y = p.y;
        }
    }

    private void setPortal() {
        int x = RANDOM.nextInt(WIDTH - 2) + 1;
        int y = RANDOM.nextInt(HEIGHT - 2) + 1;
        while (!(TABLE[x][y].equals(Tileset.FLOOR))) {
            x = RANDOM.nextInt(WIDTH);
            y = RANDOM.nextInt(HEIGHT);
        }
        TABLE[x][y] = Tileset.AVATAR;
        avatar.position.x = x;
        avatar.position.y = y;
    }

    private void movingSmonster() {
        int monsterX;
        int monsterY;
        if (!sMonster.equals(null)) {
            int x = RANDOM.nextInt(4);
            System.out.println("sMonster Moving");
            monsterX = sMonster.position().x;
            monsterY = sMonster.position().y;
            while (!TABLE[monsterX][monsterY].equals(Tileset.FLOOR)) {
                switch (x) {
                    case 0:
                        if (TABLE[sMonster.position().x + 1][sMonster.position().y].equals(Tileset.FLOOR)) {
                            TABLE[sMonster.position().x][sMonster.position().y] = Tileset.FLOOR;
                            TABLE[sMonster.position().x + 1][sMonster.position().y] = Tileset.SMONSTER;
                            sMonster.position.x += 1;
                            break;
                        }
                    case 1:
                        if (TABLE[sMonster.position().x - 1][sMonster.position().y].equals(Tileset.FLOOR)) {
                            TABLE[sMonster.position().x][sMonster.position().y] = Tileset.FLOOR;
                            TABLE[sMonster.position().x - 1][sMonster.position().y] = Tileset.SMONSTER;
                            sMonster.position.x -= 1;
                            break;
                        }
                    case 2:
                        if (TABLE[sMonster.position().x][sMonster.position().y + 1].equals(Tileset.FLOOR)) {
                            TABLE[sMonster.position().x][sMonster.position().y] = Tileset.FLOOR;
                            TABLE[sMonster.position().x][sMonster.position().y + 1] = Tileset.SMONSTER;
                            sMonster.position.y += 1;
                            break;
                        }
                    case 3:
                        if (TABLE[sMonster.position().x][sMonster.position().y - 1].equals(Tileset.FLOOR)) {
                            TABLE[sMonster.position().x][sMonster.position().y] = Tileset.FLOOR;
                            TABLE[sMonster.position().x][sMonster.position().y - 1] = Tileset.SMONSTER;
                            sMonster.position.y -= 1;
                            break;
                        }
                    default:
                        x = RANDOM.nextInt(4);
                }
            }
        }

    }

    private void movingMmonster() {
        int monsterX;
        int monsterY;
        if (!mMonster.equals(null)) {
            System.out.println("mMonster Moving");
            monsterX = mMonster.position().x;
            monsterY = mMonster.position().y;
            while (!TABLE[monsterX][monsterY].equals(Tileset.FLOOR)) {
                int x = RANDOM.nextInt(4);
                switch (x) {
                    case 0:
                        if (TABLE[mMonster.position().x + 1][mMonster.position().y].equals(Tileset.FLOOR)) {
                            TABLE[mMonster.position().x][mMonster.position().y] = Tileset.FLOOR;
                            TABLE[mMonster.position().x + 1][mMonster.position().y] = Tileset.MSMONSTER;
                            mMonster.position.x += 1;
                            break;
                        }
                    case 1:
                        if (TABLE[mMonster.position().x - 1][mMonster.position().y].equals(Tileset.FLOOR)) {
                            TABLE[mMonster.position().x][mMonster.position().y] = Tileset.FLOOR;
                            TABLE[mMonster.position().x - 1][mMonster.position().y] = Tileset.MSMONSTER;
                            mMonster.position.x -= 1;
                            break;
                        }
                    case 2:
                        if (TABLE[mMonster.position().x][mMonster.position().y + 1].equals(Tileset.FLOOR)) {
                            TABLE[mMonster.position().x][mMonster.position().y] = Tileset.FLOOR;
                            TABLE[mMonster.position().x][mMonster.position().y + 1] = Tileset.MSMONSTER;
                            mMonster.position.y += 1;
                            break;
                        }
                    case 3:
                        if (TABLE[mMonster.position().x][mMonster.position().y - 1].equals(Tileset.FLOOR)) {
                            TABLE[mMonster.position().x][mMonster.position().y] = Tileset.FLOOR;
                            TABLE[mMonster.position().x][mMonster.position().y - 1] = Tileset.MSMONSTER;
                            mMonster.position.y -= 1;
                            break;
                        }
                    default:
                        x = RANDOM.nextInt(4);
                }
            }
        }
    }

    private void movingLmonster() {
        int monsterX;
        int monsterY;
        if (!lMonster.equals(null)) {
            System.out.println("lMonster Moving");
            monsterX = lMonster.position().x;
            monsterY = lMonster.position().y;
            while (!TABLE[monsterX][monsterY].equals(Tileset.FLOOR)) {
                int x = RANDOM.nextInt(4);
                switch (x) {
                    case 0:
                        if (TABLE[lMonster.position().x + 1][lMonster.position().y].equals(Tileset.FLOOR)) {
                            TABLE[lMonster.position().x][lMonster.position().y] = Tileset.FLOOR;
                            TABLE[lMonster.position().x + 1][lMonster.position().y] = Tileset.LMONSTER;
                            lMonster.position.x += 1;
                            break;
                        }
                    case 1:
                        if (TABLE[lMonster.position().x - 1][lMonster.position().y].equals(Tileset.FLOOR)) {
                            TABLE[lMonster.position().x][lMonster.position().y] = Tileset.FLOOR;
                            TABLE[lMonster.position().x - 1][lMonster.position().y] = Tileset.LMONSTER;
                            lMonster.position.x -= 1;
                            break;
                        }
                    case 2:
                        if (TABLE[lMonster.position().x][lMonster.position().y + 1].equals(Tileset.FLOOR)) {
                            TABLE[lMonster.position().x][lMonster.position().y] = Tileset.FLOOR;
                            TABLE[lMonster.position().x][lMonster.position().y + 1] = Tileset.LMONSTER;
                            lMonster.position.y += 1;
                            break;
                        }
                    case 3:
                        if (TABLE[lMonster.position().x][lMonster.position().y - 1].equals(Tileset.FLOOR)) {
                            TABLE[lMonster.position().x][lMonster.position().y] = Tileset.FLOOR;
                            TABLE[lMonster.position().x][lMonster.position().y - 1] = Tileset.LMONSTER;
                            lMonster.position.y -= 1;
                            break;
                        }
                    default:
                        x = RANDOM.nextInt(4);
                }
            }
        }
    }

    //@Source from saveDemo
    private static void saveFile(String s) {
        File file = new File("./save_data.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(s);
        }  catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    //@Source from saveDemo
    private static String loadFile() {
        File f = new File("./save_data.txt");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                return (String) os.readObject();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }
        return null;
    }

    private class Room {
        private Point p;
        private int w;
        private int h;

        private Room(Point p, int w, int h) {
            this.w = w;
            this.h = h;
            this.p = p;
        }
        private Point center() {
            Point centerPoint = new Point(p.x + w / 2, p.y + h / 2);
            return centerPoint;
        }
    }

    private class Avatar {
        private Point position;
        private int health;

        private Avatar(int x, int y) {
            position = new Point(x,y);
            health = 100;
        }

        private int getHealth() {
            return health;
        }

        private Point position() {
            return position;
        }
    }

    private class Monster {
        private Point position;
        private int attack;

        private Monster(int x, int y, int attack) {
            position = new Point (x,y);
            this.attack = attack;
        }

        private int getAttack() {
            return attack;
        }

        private Point position() {
            return position;
        }
    }

    private class Portal {
        private Point position;
        private int count;

        private Portal(int x, int y) {
            position = new Point(x, y);
            count = 2;
        }

        private int getCount() {
            return count;
        }

        private Point getPosition() {
            return position;
        }
    }

    private class Point {
        private int x;
        private int y;
        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

    }
}

