package hu.szte.rubikscubecamera.utils;

import java.util.Random;

import cs.min2phase.Search;
import cs.min2phase.Tools;

/**
 * Interface between the kociemba algorithm and other parts of the code.
 */
public class KociembaImpl {
    private static final long PROBE_MAX = 100000000L;
    private static final long PROBE_MIN = 0L;
    private static final int MAX_DEPTH = 21;
    private static final int VERBOSE = 0;

    /**
     * Solves a random cube.
     *
     * @return Solution as a string.
     */
    public static String solveCubeRandom() {
        Search.init();
        String cube = randomCube();
        return new Search().solution(cube, MAX_DEPTH, PROBE_MAX, PROBE_MIN, VERBOSE);
    }

    /**
     * Solves a cube.
     *
     * @param cube String representation of a cube.
     * @return Solution as a string.
     */
    public static String solveCube(String cube) {
        Search.init();
        return new Search().solution(cube, MAX_DEPTH, PROBE_MAX, PROBE_MIN, VERBOSE);
    }

    /**
     * Return a random cube.
     *
     * @return String representation of a cube.
     */
    public static String randomCube() {
        return Tools.randomCube(new Random());
    }

    /**
     * Verifies if a String representation of a wannabe cube is actually a cube.
     *
     * @param cube String representation of a wannabe cube.
     * @return True if the cube is actually a cube.
     */
    public static boolean verifyCube(String cube) {
        return Tools.verify(cube) == 0;
    }
}
