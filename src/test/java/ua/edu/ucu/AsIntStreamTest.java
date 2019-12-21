package ua.edu.ucu;

import ua.edu.ucu.stream.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;


public class AsIntStreamTest {
    private IntStream stream;

    @Before
    public void setUp() {
        int[] arr = {4, 8, 7, -10, 15, 32, -26, 60, 0, 3};
        stream = AsIntStream.of(arr);
    }

    @Test
    public void testOf() {
        int[] testArr = {0, 1, 2, 3, 4};
        IntStream intStream = AsIntStream.of(testArr);
        StringBuilder result = new StringBuilder();
        intStream.forEach(result::append);
        assertEquals("01234", result.toString());
    }

    @Test
    public void testAverage() {
        assertEquals(Double.valueOf(9.3), stream.average());
    }

    @Test
    public void testMin() {
        assertEquals(Integer.valueOf(-26), stream.min());
    }

    @Test
    public void testMax() {
        assertEquals(Integer.valueOf(60), stream.max());
    }

    @Test
    public void testCount() {
        assertEquals(10, stream.count());
    }

    @Test
    public void testSum() {
        assertEquals(Integer.valueOf(93), stream.sum());
    }

    @Test
    public void testReduce() {
        assertEquals(94, stream.reduce(1, (sum, x) -> sum += x));
    }

    @Test
    public void testForEach() {
        StringBuilder builder = new StringBuilder();
        stream.forEach(x -> builder.append(x).append(", "));
        String result = builder.toString();
        assertEquals("4, 8, 7, -10, 15, 32, -26, 60, 0, 3",
                result.substring(0, result.length() - 2));
    }

    @Test
    public void testToArray() {
        int[] expected = {4, 8, 7, -10, 15, 32, -26, 60, 0, 3};
        assertArrayEquals(expected, stream.toArray());
    }

    @Test
    public void testFilter() {
        int[] expected = {-10, -26, 0};
        assertArrayEquals(expected, stream.filter(x -> x <= 0).toArray());
    }

    @Test
    public void testMap() {
        int[] expected = {12, 24, 21, -30, 45, 96, -78, 180, 0, 9};
        assertArrayEquals(expected, stream.map(x -> x * 3).toArray());
    }

    @Test
    public void testFlatMap() {
        int[] expected = {4, 5, 8, 9, 7, 8, -10, -9, 15, 16, 32, 33, -26, -25,
                60, 61, 0, 1, 3, 4};
        assertArrayEquals(expected, stream.flatMap(x -> AsIntStream.
                of(x, x + 1)).toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidationInFilter() {
        stream.filter(x -> x > 100).sum();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidationInFlatMap() {
        stream.flatMap(x -> AsIntStream.of()).sum();
    }
}
