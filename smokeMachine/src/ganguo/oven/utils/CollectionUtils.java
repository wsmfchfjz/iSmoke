package ganguo.oven.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author Wilson
 * 
 */
public class CollectionUtils {

	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}

	public static boolean isEmptyMap(Map<?, ?> map) {
		return map == null || isEmpty(map.keySet());
	}

}
