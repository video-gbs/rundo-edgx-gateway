package com.runjian.common.utils;


import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BeanUtil extends BeanUtils {
	private BeanUtil() {
	}

	public static <T, R> R convert(T source, Class<R> targetClass, Callback callback, String... ignoreProperties) {
		if (source == null) {
			return null;
		} else {
			Object target = null;

			try {
				target = targetClass.newInstance();
			} catch (InstantiationException | IllegalAccessException var6) {
				throw  new BusinessException(BusinessErrorEnums.UNKNOWN_ERROR);
			}

			copyProperties(source, target, ignoreProperties);
			if (callback != null) {
				callback.doCallback(source, target);
			}

			return (R) target;
		}
	}

	public static <T, R> R convert(T source, Class<R> targetClass, Callback callback) {
		return convert(source, targetClass, callback, (String[])null);
	}

	public static <T, R> R convert(T source, Class<R> targetClass, String... ignoreProperties) {
		return convert(source, targetClass, (Callback)null, ignoreProperties);
	}

	public static <T, R> R convert(T source, Class<R> targetClass) {
		return convert(source, targetClass, (Callback)null, (String[])null);
	}

	public static <T, R> List<R> convertList(Collection<T> list, Class<R> targetType, Callback callback, String... ignoreProperties) {
		if (CollectionUtils.isEmpty(list)) {
			return new ArrayList();
		} else {
			List newList = new ArrayList(list.size());
			list.forEach((item) -> {
				newList.add(convert(item, targetType, callback, ignoreProperties));
			});
			return newList;
		}
	}

	public static <T, R> List<R> convertList(Collection<T> list, Class<R> targetType, String... ignoreProperties) {
		return convertList(list, targetType, (Callback)null, ignoreProperties);
	}

	public static <T, R> List<R> convertList(Collection<T> list, Class<R> targetType, Callback callback) {
		return convertList(list, targetType, callback, (String[])null);
	}

	public static <T, R> List<R> convertList(Collection<T> list, Class<R> targetType) {
		return convertList(list, targetType, (Callback)null, (String[])null);
	}

	public interface Callback<T, R> {
		void doCallback(T var1, R var2);
	}
}
