package hu.szte.rubikscubecamera.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import cs.min2phase.Tools;

public class KociembaImplTest {
    @Test
    public void solveCubeRandomTest() {
        String solution = KociembaImpl.solveCubeRandom();
        assertNotNull(solution);
        assertEquals(String.class, solution.getClass());
    }

    @Test
    public void solveCubeTest() {
        String cube = Tools.randomCube();
        String solution = KociembaImpl.solveCube(cube);
        assertNotNull(solution);
        assertEquals(String.class, solution.getClass());
    }
}