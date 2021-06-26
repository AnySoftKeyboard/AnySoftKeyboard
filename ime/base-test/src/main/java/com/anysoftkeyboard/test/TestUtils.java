package com.anysoftkeyboard.test;

import androidx.core.util.Pair;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestUtils {
    public static <T> List<T> convertToList(Iterable<T> iterable) {
        ArrayList<T> list = new ArrayList<>();
        for (T t : iterable) {
            list.add(t);
        }

        return list;
    }

    public static <K, V, O> Map<K, V> convertToMap(
            Iterable<O> iterable, Function<O, Pair<K, V>> parser) {
        Map<K, V> map = new HashMap<>();
        Observable.fromIterable(iterable)
                .map(parser)
                .blockingSubscribe(pair -> map.put(pair.first, pair.second));

        return map;
    }
}
