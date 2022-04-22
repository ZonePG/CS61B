package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    private static class Position {

        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }
    }

    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dx = 0; dx < length; dx++) {
            tiles[p.x + dx][p.y] = tile;
        }
    }

    public static void addHexagonHelper(TETile[][] tiles, Position p, TETile tile, int b,
            int t) {
        Position startOfRow = p.shift(b, 0);
        drawRow(tiles, startOfRow, tile, t);

        if (b > 0) {
            Position nextP = p.shift(0, -1);
            addHexagonHelper(tiles, nextP, tile, b - 1, t + 2);
        }

        Position startOfReflectedRow = startOfRow.shift(0, -(2 * b + 1));
        drawRow(tiles, startOfReflectedRow, tile, t);
    }

    public static void addHexagon(TETile[][] tiles, Position p, TETile t, int size) {
        if (size < 2) {
            return;
        }

        addHexagonHelper(tiles, p, t, size - 1, size);
    }

    public static void addHexColum(TETile[][] tiles, Position p, int size, int num) {
        if (num < 1) {
            return;
        }

        addHexagon(tiles, p, randomTile(), size);

        if (num > 1) {
            Position bottomNeighbor = getBottomNeighbor(p, size);
            addHexColum(tiles, bottomNeighbor, size, num - 1);
        }
    }

    public static void fillBoardWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /**
     * Picks a RANDOM tile a wall chance of being empty space.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0:
                return Tileset.AVATAR;
            case 1:
                return Tileset.WALL;
            case 2:
                return Tileset.FLOOR;
            case 3:
                return Tileset.WATER;
            default:
                return Tileset.GRASS;
        }
    }

    public static Position getBottomNeighbor(Position p, int n) {
        return p.shift(0, -2 * n);
    }

    public static Position getTopRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, n);
    }

    public static Position getBottomRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, -n);
    }

    public static void drawWorld(TETile[][] tiles, Position p, int hexSize, int tessSize) {
        addHexColum(tiles, p, hexSize, tessSize);

        for (int i = 1; i < tessSize; i++) {
            p = getTopRightNeighbor(p, hexSize);
            addHexColum(tiles, p, hexSize, tessSize + i);
        }

        for (int i = tessSize - 2; i >= 0; i--) {
            p = getBottomRightNeighbor(p, hexSize);
            addHexColum(tiles, p, hexSize, tessSize + i);
        }
    }

    public static void main(String[] args) {
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // initialize tiles
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillBoardWithNothing(world);
        Position anchor = new Position(10, 35);
        drawWorld(world, anchor, 3, 4);

        // draws the world to the screen
        ter.renderFrame(world);
    }
}
