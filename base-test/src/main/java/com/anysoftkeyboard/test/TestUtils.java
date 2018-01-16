package com.anysoftkeyboard.test;

import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static <T> List<T> convertToList(Iterable<T> iterable) {
        ArrayList<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }

        return list;
    }
}
