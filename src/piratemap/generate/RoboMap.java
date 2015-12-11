package piratemap.generate;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import piratemap.utils.TerrainMap;
import piratemap.utils.Util;
import static piratemap.generate.RoboMap.Tile.*;

public class RoboMap
{

    static enum Tile {
        WATER(4), SAND(8), TREES(2), HILL(0)
        ;
        
        public int terrainCode;

        private Tile(int terrainCode)
        {
            this.terrainCode = terrainCode;
        }
        
    }
    
    static class Coord
    {
        public int x, y;

        public Coord(int x, int y)
        {
            super();
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString()
        {
            return "[" + x + ", " + y + "]";
        }

        /**
         * Taxicab distance
         * @param other
         * @return
         */
        public int gridDist(Coord other)
        {
            int d = Math.abs(other.x - x)
                    + Math.abs(other.y - y);
            return d; 
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Coord other = (Coord) obj;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            return true;
        }

        public Coord add(Coord dir)
        {
            return new Coord(x+dir.x, y+dir.y);
        }
        
    }

    
    static class Map 
    {
        int W, H;
        Tile[][] grid;
        
        Random rand = new Random();
        public Coord mark;
        public ArrayList<Coord> route;
        public ArrayList<Coord> routeDirs;
        
        private static final int[] d = new int[] {
                -1, 0, 0, -1, 1, 0, 0, 1
        };
        
        public Map(Tile[][] grid, int w, int h, Random rand)
        {
            super();
            W = w;
            H = h;
            
            this.grid = grid;
            
            this.rand = rand;
        }
        
        public ArrayList<Coord> getNeighbours(Coord c)
        {
            ArrayList<Coord> n = new ArrayList<>();
            
            for (int i = 0; i < d.length-1; i+=2)
            {
                int x1 = c.x+d[i];
                int y1 = c.y+d[i+1];
                if (x1 >= 0 && y1 >=0 && x1 < W && y1 < H)
                    n.add(new Coord(x1, y1));
            }
            
            return n;
        }

        public ArrayList<Tile> getNeighbourTiles(int x, int y)
        {
            ArrayList<Coord> n = getNeighbours(new Coord(x, y));
            ArrayList<Tile> t = new ArrayList<>(n.size());
            
            for (Coord nn : n)
            {
                t.add(getTile(nn));
            }
            
            return t;
        }
        
        public Tile getTile(int x, int y)
        {
            return grid[x][y];
        }
        
        public void setTile(int x, int y, Tile tile)
        {
            grid[x][y] = tile;
        }

        public Tile getTile(Coord c)
        {
            return getTile(c.x, c.y);
        }

        public void setTile(Coord c, Tile type)
        {
            setTile(c.x, c.y, type);
        }

        public Coord randomSpot()
        {
            return new Coord(rand.nextInt(W), rand.nextInt(H));
        }
        
        HashSet<Coord> getConnected(Coord start)
        {
            
            ArrayList<Coord> list = new ArrayList<>();
            HashSet<Coord> conn = new HashSet<>();
            
            if (getTile(start) == WATER)
            {
                return conn;
            }
            
            
            list.add(start);
            conn.add(start);
            
            while(list.size() > 0)
            {
                Coord c = list.remove(0);
                
                ArrayList<Coord> nn = getNeighbours(c);
                
                for (Coord n : nn)
                {
                    if (grid[n.x][n.y] != WATER
                        && !conn.contains(n))
                    {
                        conn.add(n);
                        list.add(n);
                    }
                }
            }
            
            return conn;
        }
        

        void makeRandomHillTrees(Random rand)
        {
            ArrayList<Coord> seeds = new ArrayList<>();
            int numTries = (int) (Math.sqrt(W*H)/1);

            // Find a few random seed places
            for (int i = 0; i < numTries; i++)
            {
                int x = rand.nextInt(W);
                int y = rand.nextInt(H);
                
                if (getTile(x, y) == SAND)
                {
                    setTile(x, y,
                            rand.nextBoolean()?TREES:HILL);
                    seeds.add(new Coord(x, y));
                }
            }
            
            ArrayList<Coord> region = new ArrayList<>();
            for (Coord c : seeds)
            {
                region.clear();
                region.add(c);
                Tile type = getTile(c);

                for (int i = 0; i < 10 && region.size() > 0; i++)
                {
                    Coord c1 = region.remove(rand.nextInt(region.size()));
                    ArrayList<Coord> n = getNeighbours(c1);
                    for (Coord c2 : n)
                    {
                        if (rand.nextInt(10) < 2 && getTile(c2) == SAND)
                        {
                            setTile(c2, type);
                            region.add(c2);
                        }
                    }
                }
            }
        }

        
        void print()
        {

            Random rand = new Random();
            Coord loc = new Coord(0, 0);
            for (int y = 0; y < H; y++)
            {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < W; x++)
                {
                    char c;
                    loc.x = x;
                    loc.y = y;
                    if (mark != null &&
                            x==mark.x && y == mark.y)
                        c = 'X';
                    else if (route != null && route.contains(loc))
                    {
                        c = '-';
                    }
                    else
                    {
                        switch (getTile(x, y))
                        {
                        case WATER:
                            c = ' ';
                            if (rand.nextInt(50) < 1)
                                c = '~';
                            break;
                        case SAND:
                            c = '.';
                            break;
                        case HILL:
                            c = '^';
                            break;
                        case TREES:
                            c = 'T';
                            break;
                        default:
                            c='?';
                            break;
                        }
                    }
                    sb.append(c);
                }
                System.out.println(y + " "+ sb);
            }
            
        }
        
        BufferedImage render(int tileSizeX, int tileSizeY)
        {
            
            BufferedImage hill = null, palm = null;
            try
            {
                hill = ImageIO.read(new File("tiles/hill.png"));
                palm = ImageIO.read(new File("tiles/palm_small.png"));
            } catch (IOException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
            
            
            BufferedImage im = new BufferedImage(W*tileSizeX, H * tileSizeY, 
                    BufferedImage.TYPE_INT_ARGB);
            
            Graphics2D g2 = im.createGraphics();
            
            int[] codes = new int[4];
//            for (int x = 0; x < W; x++)
//                for (int y = 0; y < H; y++)
//                {
//                    Tile t = grid[x][y];
//                    if (t == WATER)
//                    {
//                        getCornersWaterSand(x, y, codes);
//                        TerrainMap.printTile(g2, x*32, y*32,
//                                codes[0], codes[1], codes[2], codes[3]);
//                    }
//                    else
//                        TerrainMap.printTile(g2, x*32, y*32, 8, 8, 8, 8);
//                }

            
            // "Correct" algorithm for deciding tiles
            // Sandly, it makes islands too thin, route goes outside land,
            // And sand does not have interfaces with other types
            for (int x = 0; x < W; x++)
                for (int y = 0; y < H; y++)
                {
                    getCornersWaterSand1(x, y, codes);
                    TerrainMap.printTile(g2, x*tileSizeX, y*tileSizeY,
                            tileSizeX, tileSizeY,
                            codes[0], codes[1], codes[2], codes[3]);
                }
            
            g2.translate(-tileSizeX/2, -tileSizeY/2);
            // hills
            for (int x = 0; x < W; x++)
                for (int y = 0; y < H; y++)
                {
                    if (grid[x][y] == HILL)
                    {
                        g2.drawImage(hill,
                                x*tileSizeX + rand.nextInt(tileSizeX/4),
                                y*tileSizeY + rand.nextInt(tileSizeY/4),
                                tileSizeX, tileSizeY,
                                null);
                        if (x == W-1 || y == H-1)
                            continue;
                        if (grid[x+1][y] != WATER
                            && grid[x][y+1] != WATER
                            && grid[x+1][y+1] != WATER)
                                g2.drawImage(hill,
                                        x*tileSizeX +tileSizeX/2 + rand.nextInt(tileSizeX/2),
                                        y*tileSizeY +tileSizeY/2 + rand.nextInt(tileSizeY/2),
                                        tileSizeX, tileSizeY,
                                        null);
                    }
                }
            
            // trees
            for (int x = 0; x < W; x++)
                for (int y = 0; y < H; y++)
                {
                    if (grid[x][y] == TREES)
                    {
                        g2.drawImage(palm,
                                x*tileSizeX + rand.nextInt(tileSizeX/4),
                                y*tileSizeY -tileSizeY/2+ rand.nextInt(tileSizeY/4),
                                tileSizeX, tileSizeY,
                                null);
                        if (x == W-1 || y == H-1)
                            continue;
                        if (grid[x+1][y] != WATER
                            && grid[x][y+1] != WATER
                            && grid[x+1][y+1] != WATER)
                        {
                                g2.drawImage(palm,
                                        x*tileSizeX + rand.nextInt(tileSizeX/2),
                                        y*tileSizeY -tileSizeY/2+ rand.nextInt(tileSizeY/2),
                                        tileSizeX, tileSizeY,
                                        null);
                                g2.drawImage(palm,
                                        x*tileSizeX + rand.nextInt(tileSizeX/2),
                                        y*tileSizeY -tileSizeY/2+ rand.nextInt(tileSizeY/2),
                                        tileSizeX, tileSizeY,
                                        null);
                        }
                    }
                }
            
            // Grid
//            g2.setColor(Color.red);
//            for (int x = 1; x < W; x++)
//                for (int y = 1; y < H; y++)
//                {
//                    if (grid[x][y] != grid[x-1][y])
//                        g2.drawLine(x*tileSizeX, y*tileSizeY, x*tileSizeX, (y+1)*tileSizeY);
//                    
//                    if (grid[x][y] != grid[x][y-1])
//                        g2.drawLine(x*tileSizeX, y*tileSizeY, (x+1)*tileSizeX, y*tileSizeY);
//                }
//            g2.translate(16, 16);
            
            
            g2.setColor(Color.red);
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 100f, new float[]{10f}, 0));
            Coord last = null;
            int w2 = tileSizeX/2, h2 = tileSizeY/2;
            if (route != null && mark != null)
            {
                for (Coord cc : route)
                {
                    if (last != null)
                    {
                        g2.drawLine(
                                last.x*tileSizeX+w2, last.y*tileSizeY+h2,
                                cc.x*tileSizeX+w2, cc.y*tileSizeY+h2
                                );
                    }
                    last = cc;
                }
                
                // Connect to coast
    //            for (Coord nn : getNeighbours(last))
    //            {
    //                if (grid[nn.x][nn.y] == WATER)
    //                {
    //                    int dx = nn.x - last.x;
    //                    int dy = nn.y - last.y;
    //                    
    //                    g2.drawLine(
    //                            last.x*tileSizeX+w2, last.y*tileSizeY+h2,
    //                            last.x*tileSizeX+w2+dx*w2, last.y*tileSizeY+h2+dy*h2
    //                            );
    //                    
    //                    break;
    //                }
    //            }
                
                g2.setColor(Color.red);
                g2.setStroke(new BasicStroke(7));
                g2.drawLine(mark.x*tileSizeX,mark.y*tileSizeY,
                        (mark.x+1)*tileSizeX,(mark.y+1)*tileSizeY);
                g2.drawLine(mark.x*tileSizeX,(mark.y+1)*tileSizeY,
                        (mark.x+1)*tileSizeX,mark.y*tileSizeY);
            }
            
            
            return im;
        }
        
        void makeXAndRoute()
        {
            mark = null;
            
            // make sure the mark is on the main landmass
            HashSet<Coord> main = getConnected(new Coord(W/2, H/2));
            // It is possible for W/2, H/2 not to be on the main body, though usually it is.
            if (main.size() == 0)
                return;
            
            for (int i = 0; i < 1000; i++)
            {
                Coord c = randomSpot();
                Tile tile = getTile(c);
                if (main.contains(c) 
                    && tile != WATER)
                {
                    mark = c;
                    break;
                }
            }
            
            if (mark == null)
            {
                System.err.println("Mark spot not found");
                return;
            }

            route = new ArrayList<Coord>();
            routeDirs = new ArrayList<Coord>();
            
            final int MAX_PATH_RETRY = 1000;
            
            boolean badPath = true;
            int minDistStartEnd = (int) (Math.sqrt(W*H)/3);
            int fullPathRetries = 0;
            while(badPath && fullPathRetries < MAX_PATH_RETRY)
            {
                route.clear();
                route.add(mark);
                routeDirs.clear();
                
                Coord cur = mark;

                Coord dir = randomDir();
                int retryCount = 0;
                for (int i = 0; i < 1000; i++)
                {
                    Coord next = cur.add(dir);
                    
                    if (grid[next.x][next.y] == WATER
                            || route.contains(next)
                            || rand.nextInt(10) < 5
                            )
                    {
                        // bad location, change direction and retry
                        dir = randomDir();
                        retryCount++;
                        
                        if (retryCount > 20)
                            break;
                        continue;
                    }
                    else
                    {
                        
                        cur = next;
                        route.add(next);
                        routeDirs.add(dir);
                        
                        
                        
                        retryCount = 0;
                    }
                }
//                System.out.println("MIN "+minDistStartEnd);
                if (cur.gridDist(mark) >= minDistStartEnd
                        && hasWater(getNeighbours(cur)))
                {
                    badPath = false;
                }
                
                fullPathRetries++;
                
            }
            
            if (fullPathRetries >= MAX_PATH_RETRY)
            {
                route.clear();
                routeDirs.clear();
                System.out.println("route fail ");
            }
            
            Collections.reverse(route);
            Collections.reverse(routeDirs);

        }
        
        private boolean hasWater(ArrayList<Coord> neighbours)
        {
            for (Coord n : neighbours)
            {
                if (grid[n.x][n.y] == WATER)
                    return true;
            }
            return false;
        }

        private Coord randomDir()
        {
            int di = rand.nextInt(4);
            return new Coord(d[2*di], d[2*di+1]);
        }
        
        private static final int[] corners = new int[] 
                {-1, -1, 1, -1, 1, 1, -1, 1};
        void getCornersWaterSand(int x, int y, int[] out)
        {
            if (x < 0 || y < 0 || x >= W || y >=W)
                return;
            
            Tile t = grid[x][y];
            
            for (int i = 0; i < 4; i++)
            {
                int x1 = x + corners[i*2];
                int y1 = y + corners[i*2+1];
                
                if (x1 < 0 || y1 < 0 || x1 >= W || y1 >=W)
                    out[i] = t.terrainCode;
                else
                {
//                    out[i] = grid[x1][y1].terrainCode;
                    out[i] = grid[x1][y1] == WATER ?
                            WATER.terrainCode : SAND.terrainCode;
                }
            }

            
            for (int i = 0; i < 4; i++)
            {
                int x1 = x + d[i*2];
                int y1 = y + d[i*2+1];
                
                if (x1 < 0 || y1 < 0 || x1 >= W || y1 >=W)
                    ;
                else if (grid[x1][y1] != WATER)
                {
//                    out[i] = grid[x1][y1].terrainCode;
//                    out[(i+3) %4] = grid[x1][y1].terrainCode;
                    out[i] = SAND.terrainCode;
                    out[(i+3) %4] = SAND.terrainCode;
                }
            }
            
            // atlas indexes corners differently
            int tmp = out[2]; out[2] = out[3]; out[3] = tmp;
        }

        private static final int[] offs = new int[] 
                {0, 0, 1, 0, 0, 1, 1, 1};
        void getCornersWaterSand1(int x, int y, int[] out)
        {
            for (int i = 0; i < 4; i++)
            {
                int x1 = x + offs[i*2];
                int y1 = y + offs[i*2+1];
                
                if (x1 < 0 || y1 < 0 || x1 >= W || y1 >= H)
                    out[i] = WATER.terrainCode;
                else
                {
//                    out[i] = grid[x1][y1].terrainCode;
                    out[i] = grid[x1][y1] == WATER ?
                            WATER.terrainCode : SAND.terrainCode;
                }
            }
        }
        
        private int getDir(Coord d1, Coord d2)
        {
            for (int i = 0; i < 4; i++)
            {
                if (d[i*2] == d1.x && d[i*2+1] == d1.y)
                {
                    if (d1.equals(d2))
                        return 0;
                    else {
                        if (d2.x == d[(i*2 + 6) % 8]
                            && d2.y == d[(i*2 + 7) % 8])
                        {
                            return -1;
                        }
                        else if (d2.x == d[(i*2 + 2) % 8]
                                && d2.y == d[(i*2 + 3) % 8])
                            {
                                return 1;
                            } 
                    }
                }
            }
            
            return -100;
        }
        
        String getWordDirections()
        {
            if (routeDirs == null)
                    return "";
            TemplateRules rules = new TemplateRules();
            StringBuilder sb = new StringBuilder("Start ").append(rules.getStr("start")).append(".\n");
            Coord last = null;
            
            for (int i = 0; i < routeDirs.size(); i++)
            {
                Coord c = routeDirs.get(i);
                Coord pos = route.get(i);
                if (last != null)
                {
                    int d = getDir(last, c);
                    
                    Tile t = getTile(pos);
                    switch (d)
                    {
                    case 0:
//                        sb.append("F");
                        break;
                    case 1:
                        sb.append("Turn right ").append(rules.getStrFor(t)).append(".\n");
                        break;
                    case -1:
                        sb.append("Turn left ").append(rules.getStrFor(t)).append(".\n");
                        break;
                    default:
                        sb.append("[").append(d).append("]")
                        .append(last).append(c);
                    }
                }
                
                last = c;
            }
            
            return sb.toString();
        }
    }
 
    static class Options
    {
        public int imageX, imageY, tileSize;
    }
    
    static void getOptions(String[] args, Options options)
    {
        for (int i = 0; i < args.length; i++)
        {
            String a = args[i];
            
            if ("--size".equals(a))
            {
                if (i >= args.length - 1)
                {
                    printArgError("--size needs an arguement.");
                }
                
                i++;
                String a2 = args[i];
                String words[] = a2.split("x");
                
                try {
                    options.imageX = Integer.parseInt(words[0]);
                    if (words.length > 1)
                        options.imageY = Integer.parseInt(words[1]);
                    else
                        options.imageY = options.imageX;
                } catch (NumberFormatException e)
                {
                    printArgError("Expecting a number for --size "+e.getMessage());
                }
            }
            else if ("--tileSize".equals(a))
            {
                if (i >= args.length - 1)
                {
                    printArgError("--tileSize needs an arguement.");
                }
                
                i++;
                String a2 = args[i];
                
                try {
                    options.tileSize = Integer.parseInt(a2);
                } catch (NumberFormatException e)
                {
                    printArgError("Expecting a number for --tileSize "+e.getMessage());
                }
            }
            else
            {
                printArgError("Invalid arguement "+a);
            }
        }
    }

    private static void printArgError(String msg)
    {
        System.out.println(msg);
        usage();
        System.exit(0);
    }
    
    static void usage()
    {
        System.out.println("PirateMap [--size <sizeX>x<sizeY>] [--tileSize <tileSize>]");
    }
    
    public static void main(String[] args)
    {
        Random rand  = new Random();
        int W = 10+rand.nextInt(20),
                H = 10+rand.nextInt(20);

        Options options = new Options();
        options.tileSize = 32;
        options.imageX = options.tileSize * W;
        options.imageY = options.tileSize * H;
        getOptions(args, options);
        
        W = options.imageX / options.tileSize;
        H = options.imageY / options.tileSize;
        
        options.imageX = W * options.tileSize;
        options.imageY = H * options.tileSize;
        
        Tile grid[][] = new Tile[W][H];
        Map map = new Map(grid, W, H, rand);
        
        for (Tile[] gg : grid)
        {
            Arrays.fill(gg, WATER);
        }
        
        // random rect
//        for (int i = 0; i < 10; i++)
//        {
//            int x = rand.nextInt(W);
//            int y = rand.nextInt(H);
//            
//            int w = rand.nextInt(W/2);
//            int h = rand.nextInt(H/2);
//            
//            fill(grid, x, y, w, h, SAND);
//            
////            if (count(grid, WATER) < W*H/4)
////                break;
//        }
        
        drawSubDivRect(grid, 0, 1, W, H, 0, rand);
        fillCirc(grid, W/2-W/8, H/2-H/8, W/4, H/4, SAND); // make sure some island in exact mid.
        fillRect(grid, 0, 0, W, 1, WATER);
        fillRect(grid, 0, H-1, W, 1, WATER);
        fillRect(grid, 0, 0, 1, H, WATER);
        fillRect(grid, W-1, 0, 1, H, WATER);
        
        drawRoughen(map, rand);
        
        deleteInlandWater(map);
        
        map.makeRandomHillTrees(rand);
        
        map.makeXAndRoute();
        
//        map.print();
        
        BufferedImage im = map.render(options.tileSize, options.tileSize);

        System.out.println(map.getWordDirections());
        

        
        try
        {
            ImageIO.write(im, "png", new File("PirateMap.png"));
        } catch (IOException e)
        {
            System.err.println("Could not save image, "+ 
                        e.getMessage());
        }
        
        Util.showImage(im);
        Util.exitAfter(20);
    }

    private static void drawRoughen(Map map, Random rand)
    {
        for (int x = 0; x < map.W; x++)
        {
            for (int y = 0; y < map.H; y++)
            {
                if (map.getTile(x, y) != WATER)
                {
                    int c = count(map.getNeighbourTiles(x, y), WATER);
                    
                    if (c > 0 && c < 3 && rand.nextInt(10)<3)
                        map.setTile(x, y, WATER);
                }
            }
        }
    }

    private static int count(ArrayList<Tile> neighbours, Tile tile)
    {
        int c = 0;
        for (Tile nn : neighbours)
        {
            if (nn == tile)
                c++;
        }
        return c;
    }

    private static void drawSubDivRect(Tile[][] grid, int x, int y, int w, int h,
            int depth, Random rand)
    {
        if (grid.length == 0 || grid[0].length == 0)
            return;
        
        
        Tile fillType = SAND;
        if (depth > 0)
        {
            // More the recursion depth, exponentially less chance of being land
//            for (int i = 0; i <= depth; i++)
//                if (/*rand.nextBoolean()*/rand.nextInt(10)<9)
//                    fillType = WATER;
            if (rand.nextInt(4)<3)
                fillType = WATER;
        }
        
        if (rand.nextBoolean())
            fillRect(grid, x+w/4, y+h/4, w/2, h/2, fillType);
        else
            fillCirc(grid, x+w/4, y+h/4, w/2, h/2, fillType);
        
        if (w > 3 && h > 3)
        {
//            int ww = w/2, hh = h/2;
            int ww = w/3+rand.nextInt(w/3),
                    hh = h/3+rand.nextInt(h/3);
            drawSubDivRect(grid, x, y, ww, hh, depth+1, rand);
            drawSubDivRect(grid, x+ww, y, w-ww, hh, depth+1, rand);
            drawSubDivRect(grid, x, y+hh, ww, h-hh, depth+1, rand);
            drawSubDivRect(grid, x+ww, y+hh, w-ww, h-hh, depth+1, rand);
        }
        
    }
    
    private static void deleteInlandWater(Map a)
    {
        ArrayList<Coord> waters = new ArrayList<>();
        boolean marked[][] = new boolean[a.W][a.H];
        for (boolean[] mm : marked)
            Arrays.fill(mm, false);
        
        
        waters.add(new Coord(0, 0));
        marked[0][0] = true;
        
        while(waters.size() > 0)
        {
            Coord loc = waters.remove(0);
            
            
            ArrayList<Coord> n = a.getNeighbours(loc);
            
            for (Coord newloc : n)
            {
                if (!marked[newloc.x][newloc.y] && a.getTile(newloc.x, newloc.y) == WATER)
                {
                    waters.add(newloc);
                    marked[newloc.x][newloc.y] = true;

                }
            }
        }
        
        for (int x = 0; x < a.W; x++)
            for (int y = 0; y < a.H; y++)
            {
                if (a.getTile(x, y)== WATER && !marked[x][y])
                {
                    a.setTile(x, y, SAND);
                }
            }
    }

    private static void fillCirc(Tile[][] grid, int x, int y, int w, int h, Tile type)
    {
        int rx = w/2;
        for (int x1 = -rx; x1 < rx; x1++)
        {
            int ry = (int) (Math.sqrt(rx*rx-x1*x1)+.5)*h/w;
            int x2 = x + rx + x1;
            for (int y1 = -ry; y1 < ry; y1++)
            {
                int y2 = y + h/2 + y1;
                if (x2 > 0 && y2 > 0 &&
                        x2 < grid.length && y2 < grid[x2].length)
                    grid[x2][y2] = type;
            }
        }
    }


    private static void fillRect(Tile[][] grid, int x, int y, int w, int h, Tile type)
    {
        for (int x1 = x; x1 < x+w; x1++)
        {
            for (int y1 = y; y1 < y+h; y1++)
            {
                if (x1 < grid.length && y1 < grid[x1].length)
                    grid[x1][y1] = type;
            }
        }
    }
    
}
