package org.apache.commons.logging.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.jpos.util.Logger;

/**
 * <p>
 * Implementation of the <code>org.apache.commons.logging.Log</code> interface
 * that wraps the jPos logger system
 * 
 * @author dgrandemange
 * 
 */
@SuppressWarnings("serial")
public class JPosLogger implements Log, Serializable {

	private static final String DEFAULT_ASSOCIATIONS_FILEPATH = "cfg/commons-logging-jPosLogger.properties";

	private static final String JPOS_Q2_LOGGER_BY_DEFAULT = "Q2";

	protected static final String J_POS_DEFAULT_LOGGER_ATTR = "jPosDefaultLogger";

	protected static final String J_POS_LOGGER_ATTR_REGEXP = "^jPosLogger\\.([0-9]*)\\.(.*)";

	protected static Pattern pattern = Pattern
			.compile(J_POS_LOGGER_ATTR_REGEXP);

	protected static Map<String, LoggerNameAssoc> orderedLoggerNameAssoc = null;

	protected static String defaultjPosLoggerName = null;

	static class LoggerNameAssoc {
		private String jPosLoggerName;

		private Pattern regexpPattern;

		public LoggerNameAssoc(String jPosLoggerName, String regexp) {
			this.jPosLoggerName = jPosLoggerName;
			this.regexpPattern = Pattern.compile(regexp);
		}

		/**
		 * @return the jPosLoggerName
		 */
		public String getjPosLoggerName() {
			return jPosLoggerName;
		}

		/**
		 * @return the regexpPatern
		 */
		public Pattern getRegexpPattern() {
			return regexpPattern;
		}

	}

	/**
	 * The underlying JPos Log implementation we are using.
	 */
	protected transient org.jpos.util.Log log = null;

	protected static void init() {
		defaultjPosLoggerName = (String) LogFactory.getFactory().getAttribute(
				J_POS_DEFAULT_LOGGER_ATTR);

		if (null == defaultjPosLoggerName) {
			defaultjPosLoggerName = JPOS_Q2_LOGGER_BY_DEFAULT;
		}

		orderedLoggerNameAssoc = new TreeMap<String, LoggerNameAssoc>();

		String associationsResourcePath = (String) LogFactory.getFactory()
				.getAttribute("jPosLogger.associationsResourcePath");
		if (null == associationsResourcePath) {
			associationsResourcePath = DEFAULT_ASSOCIATIONS_FILEPATH;
		}

		ResourceBundle bundle = null;
		try {
			bundle = new PropertyResourceBundle(
					JPosLogger.class
							.getResourceAsStream(associationsResourcePath));
		} catch (Exception e) {
			System.err
					.println(String
							.format("[WARN] %s %s.init() : Unable to load resource bundle '%s' (Reason : %s)",
									Calendar.getInstance().getTime(),
									JPosLogger.class.getName(),
									associationsResourcePath, e.getMessage()));
		}

		if (null != bundle) {
			Enumeration<String> keysEnum = bundle.getKeys();
			while (keysEnum.hasMoreElements()) {
				String key = keysEnum.nextElement();
				Matcher matcher = pattern.matcher(key);
				if (matcher.matches()) {
					String rank = matcher.group(1);
					String loggerName = matcher.group(2);
					String regexpr = bundle.getString(key);
					LoggerNameAssoc loggerNameAssoc = new LoggerNameAssoc(
							loggerName, regexpr);
					orderedLoggerNameAssoc.put(rank, loggerNameAssoc);
				} else {
					System.err
							.println(String
									.format("[WARN] %s.init() : key '%s' does not match regexp '%s' (see classpath resource '%s')",
											JPosLogger.class.getName(), key,
											J_POS_LOGGER_ATTR_REGEXP,
											associationsResourcePath));
				}
			}
		}
	}

	public JPosLogger(String name) {
		if (null == defaultjPosLoggerName) {
			init();
		}

		this.log = getLog(name);
		if (null == this.log) {
			throw new LogConfigurationException(String.format(
					"No jPos logger available ('%s')", name));
		}
	}

	protected org.jpos.util.Log getLog(String name) {
		org.jpos.util.Log res = null;

		String jPosLoggerName = null;

		for (Entry<String, LoggerNameAssoc> entry : orderedLoggerNameAssoc
				.entrySet()) {
			LoggerNameAssoc loggerNameAssoc = entry.getValue();
			Matcher matcher = loggerNameAssoc.getRegexpPattern().matcher(name);
			if (matcher.matches()) {
				jPosLoggerName = loggerNameAssoc.getjPosLoggerName();
				break;
			}
		}

		if (jPosLoggerName == null) {
			jPosLoggerName = defaultjPosLoggerName;
		}

		if (jPosLoggerName != null) {
			Logger logger = Logger.getLogger(jPosLoggerName);
			res = new org.jpos.util.Log(logger, name);
		}

		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object)
	 */
	public void debug(Object obj) {
		log.debug(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#debug(java.lang.Object,
	 * java.lang.Throwable)
	 */
	public void debug(Object obj, Throwable t) {
		log.debug(obj, t);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#error(java.lang.Object)
	 */
	public void error(Object obj) {
		log.error(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#error(java.lang.Object,
	 * java.lang.Throwable)
	 */
	public void error(Object obj, Throwable t) {
		log.error(obj, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
	 */
	public void fatal(Object obj) {
		log.fatal(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#fatal(java.lang.Object,
	 * java.lang.Throwable)
	 */
	public void fatal(Object obj, Throwable t) {
		log.fatal(obj, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#info(java.lang.Object)
	 */
	public void info(Object obj) {
		log.info(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#info(java.lang.Object,
	 * java.lang.Throwable)
	 */
	public void info(Object obj, Throwable t) {
		log.info(obj, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object)
	 */
	public void trace(Object obj) {
		log.trace(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#trace(java.lang.Object,
	 * java.lang.Throwable)
	 */
	public void trace(Object obj, Throwable t) {
		log.trace(obj, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object)
	 */
	public void warn(Object obj) {
		log.warn(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#warn(java.lang.Object,
	 * java.lang.Throwable)
	 */
	public void warn(Object obj, Throwable t) {
		log.warn(obj, t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#isDebugEnabled()
	 */
	public boolean isDebugEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#isErrorEnabled()
	 */
	public boolean isErrorEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#isFatalEnabled()
	 */
	public boolean isFatalEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#isInfoEnabled()
	 */
	public boolean isInfoEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#isTraceEnabled()
	 */
	public boolean isTraceEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.commons.logging.Log#isWarnEnabled()
	 */
	public boolean isWarnEnabled() {
		return true;
	}

}
