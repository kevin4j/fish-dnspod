package com.kevin.fish.utils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.kevin.fish.core.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONUtilsEx {

	private static ObjectMapper defaultMapper = new ObjectMapper();

	static {

		// 设定JSON转化日期的格式
		defaultMapper.setDateFormat(new SimpleDateFormat(DateUtilsEx.DATE_FORMAT_SEC));

		// deserialization时遇到没有的属性值,不报错
		defaultMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		defaultMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

		// TODO 看是否能转话字典表数据,不行的话只能在前台转化
		// mapper.setFilters(filterProvider);
	}

	/**
	 * 对象序列化成JSON字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String serialize(Object obj) {
		return serialize(obj, defaultMapper);
	}

	public static String serialize(Object obj, ObjectMapper mapper) {

		StringWriter writer = null;

		try {
			writer = new StringWriter();
			mapper.writeValue(writer, obj);
			writer.close();
		} catch (Exception e) {
			throw new ServiceException("JSON序列化结果异常:" + e.getMessage());
		}

		return writer.toString();
	}

	/**
	 * JSON字符串反序列化成对象
	 * 
	 * @param jsonStr
	 * @param clazz
	 * @return
	 */
	public static <T> T deserialize(String jsonStr, Class<T> clazz) {

		if (StringUtils.isBlank(jsonStr)) {
			return null;
		}

		try {
			return defaultMapper.readValue(jsonStr.replace("\n", ""), clazz);
		} catch (Exception e) {
			throw new ServiceException("JSON反序列化结果异常:" + e.getMessage());
		}
	}

}
