package hu.szte.rubikscubecamera.utils;

import java.util.Random;

import cs.min2phase.Search;
import cs.min2phase.Tools;

public class KociembaImpl {
    private static final long PROBE_MAX = 100000000L;
    private static final long PROBE_MIN = 0L;
    private static final int MAX_DEPTH = 21;
    private static final int VERBOSE = 0;

    public static String solveCubeRandom() {
        Search.init();
        String cube = randomCube();
        return new Search().solution(cube, MAX_DEPTH, PROBE_MAX, PROBE_MIN, VERBOSE);
    }

    public static String solveCube(String cube) {
        Search.init();
        return new Search().solution(cube, MAX_DEPTH, PROBE_MAX, PROBE_MIN, VERBOSE);
    }

    public static String randomCube() {
        return Tools.randomCube(new Random());
    }

    public static boolean verifyCube(String cube) {
        return Tools.verify(cube) == 0;
    }
}
