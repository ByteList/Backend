package de.gamechest.backend.util;

import java.lang.reflect.Array;

/**
 * Created by ByteList on 10.06.2017.
 * <p>
 * Copyright by ByteList - https://bytelist.de/
 */

public class Java15Compat {
    @SuppressWarnings("unchecked")
    public static <T> T[] Arrays_copyOfRange(T[] original, int start, int end) {
        if (original.length >= start && 0 <= start) {
            if (start <= end) {
                int length = end - start;
                int copyLength = Math.min(length, original.length - start);
                T[] copy = (T[]) Array.newInstance(original.getClass().getComponentType(), length);

                System.arraycopy(original, start, copy, 0, copyLength);
                return copy;
            }
            throw new IllegalArgumentException();
        }
        throw new ArrayIndexOutOfBoundsException();
    }
}
