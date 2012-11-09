package org.domain.utils;

import java.util.Enumeration;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ReadPropertiesFile { 
	private static Properties props = null;

	public static Properties getPropertyObject(String configFileName) {
		if (props  == null) {
			props  = new Properties();
			Enumeration<String> enumKeys;
			ResourceBundle bundle = PropertyResourceBundle
					.getBundle(configFileName);
			enumKeys = bundle.getKeys();
			while (enumKeys.hasMoreElements()) {
				String key = (String) enumKeys.nextElement();
				props.setProperty(key, (String) bundle.getString(key));
			}
		}

		return props;
	}

	public static String getProperty(String fileProperty, String key) {
		if (key == null)
			return null;

		return getPropertyObject(fileProperty).getProperty(key);
	}
}