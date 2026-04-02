package enterprises.iwakura.docs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ListUtils {

    public static <T> List<T> emptyIfNull(List<T> list) {
        return list == null ? new ArrayList<>() : list;
    }

    public static <T> List<T> emptyIfNull(T[] array) {
        return array == null ? new ArrayList<>() : Arrays.asList(array);
    }
}
