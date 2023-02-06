package com.kevin.fish.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.core.io.support.ResourcePropertiesPersister;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 系统配置文件装载
 */
public class PropertyUtils {

	private final static Logger logger = LoggerFactory.getLogger(PropertyUtils.class);

	private static final String CONFIG_FILE = "application.properties";

	private static Map<String, Properties> pMap = new HashMap<String, Properties>();

	private synchronized static Properties get(String propertyFileName) {

		propertyFileName = StringUtils.isNotBlank(propertyFileName) ? propertyFileName : CONFIG_FILE;

		Properties p = pMap.get(propertyFileName);

		if (p == null) {

			try {

				Resource resource = new ClassPathResource(propertyFileName);
				if(propertyFileName.endsWith(".properties")){
					String confPath = System.getProperty("user.dir");
					confPath = confPath + File.separator + propertyFileName;
					File file = new File(confPath);

					if (file.exists()) {
						p = new Properties();
						InputStream in = null;
						try{
							in = new FileInputStream(file);
							ResourcePropertiesPersister.INSTANCE.load(p, in);
						}finally {
							if(in != null){
								in.close();
							}
						}
					}else{
						p = PropertiesLoaderUtils.loadProperties(resource);
					}
				}else if(propertyFileName.endsWith(".yml")){
					YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
					yamlFactory.setResources(resource);
					p = yamlFactory.getObject();
				}

				if (p != null) {
					pMap.put(propertyFileName, p);
					logger.info("配置文件[{}],装载成功~", new Object[] { propertyFileName });
				} else {
					logger.info("配置文件[{}],装载失败!", new Object[] { propertyFileName });
				}
			} catch (IOException e) {
				logger.error("配置文件 [" + propertyFileName + "] ,装载异常:" + e.getMessage(), e);
			}
		}

		return p;
	}

	public static String getProperty(String propertyFileName, String key, String defaultValue) {
		return StringUtils.defaultIfEmpty(get(propertyFileName).getProperty(key), defaultValue);
	}

	public static String getProperty(String key, String defaultValue) {
		return getProperty(null, key, defaultValue);
	}

	public static String getProperty(String key) {
		return getProperty(null, key, null);
	}

	public static void reload() {
		reload(CONFIG_FILE);
	}

	public static void reload(String filename) {
		if (pMap.get(filename) != null) {
			pMap.get(filename).clear();
		}
		get(filename);
	}

	public static void main(String[] args) {
		System.out.println(getProperty("server.port"));
	}
}
