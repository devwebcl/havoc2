package com.github.eostermueller.tjp.launcher.agent;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.eostermueller.tjp.launcher.agent.history.EventHistory;
import com.google.common.flogger.FluentLogger;

public class DefaultFactory implements Factory {
	
	/**
	 * This private constructor helps consistently use the getFactory() "-D" system property plugin mechanism.
	 * See FACTORY_DASH_D_PARM for the property name.
	 * 
	 * 
	 * Use 
	 * <PRE>
	 * Factory f = DefaultFactory.getFactory();
	 * </PRE>
	 * instead of:
	 * <PRE>
	 * Factory f = new DefaultFactory();
	 * </PRE>
	 * 
	 * 
	 */
	private DefaultFactory() {
		
	}
	
	static EventHistory eventHistory = new EventHistory();
	
	Configuration config = new DefaultConfiguration();
	public static final String FACTORY_DASH_D_PARM = "com.github.eostermueller.tjp.launcher.agent.FactoryImpl";
	
	private FluentLogger LOG = FluentLogger.forEnclosingClass();
	static AtomicInteger jvmLifetimeUniqueId = new AtomicInteger();
	
	
	//Consider using the following:
	//https://github.com/fluent/fluent-logger-java
	//private static FluentLogger LOG = FluentLogger.getLogger("app", "remotehost", port);
	
	/**
	 * Default to value that JVM initializes, hopefully from operating system's configuration.
	 */
	private  Locale localeForMessages = Locale.getDefault();
	private  Messages messages = null;
	public  Locale getLocaleForMessages() {
		return localeForMessages;
	}
	/* (non-Javadoc)
	 * @see com.github.eostermueller.tjp.launcher.agent.MyFactory#setLocaleForMessages(java.lang.String)
	 */
	@Override
	public void setLocaleForMessages(String languageTag) {
		localeForMessages = Locale.forLanguageTag(languageTag);
	}
	/* (non-Javadoc)
	 * @see com.github.eostermueller.tjp.launcher.agent.MyFactory#getMessages()
	 */
	@Override
	public Messages getMessages() {
		if (messages==null)
			messages=createMessages();
		return messages;
	}
	/**
	 * Abides by the following, but this method replaces any
	 * dashes (-) with with underscores, so that the string
	 * can be appended to java class names.
	 * https://en.wikipedia.org/wiki/IETF_language_tag
	 * @return
	 */
	private String getMangledLanguageTag() {
		String languageTag = getLocaleForMessages().toLanguageTag();
		return languageTag.replace('-', '_');
	}
	private Messages createMessages() {
		String packageAndClassName = "com.github.eostermueller.tjp.launcher.agent.Messages_" + getMangledLanguageTag();
		
		Messages messages;
		try {
			Class messagesClass = Class.forName(packageAndClassName);
			messages = (Messages) messagesClass.newInstance();
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			LOG.atWarning().withCause(e).log("Could not find class [%s] in the classpath, will use %s instead", packageAndClassName, Messages_en_US.class.getName() );
			messages = new Messages_en_US();
		}
		return messages;
	}
	@Override
	public long getJvmLifetimeUniqueId() {
		return jvmLifetimeUniqueId.incrementAndGet();
	}
	/**
	 * Build factory from Java -D system parameter: com.github.eostermueller.tjp.launcher.agent.FactoryImpl
	 * @return
	 * @throws CannotFindTjpFactoryClass 
	 */
	public static Factory getFactory() throws CannotFindTjpFactoryClass {
		String myFactoryClassName = System.getProperty(FACTORY_DASH_D_PARM,DefaultFactory.class.getName());
		try {
			Class<Factory> factoryClass = (Class<Factory>) Class.forName(myFactoryClassName); 
			Constructor[] ctors = factoryClass.getDeclaredConstructors();
			Constructor ctor = null;
			for (int i = 0; i < ctors.length; i++) {
			    ctor = ctors[i];
			    if (ctor.getGenericParameterTypes().length == 0)
				break;
			}
			
			Factory factory;
		
			factory = (Factory)ctor.newInstance();
			return factory;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ClassNotFoundException e) {
			CannotFindTjpFactoryClass cftf = new CannotFindTjpFactoryClass(e,myFactoryClassName);
			throw cftf;
		}
		
		
	}
	@Override
	public Configuration getConfiguration() {
		return this.config;
	}
	@Override
	public EventHistory getEventHistory() {
		return eventHistory;
	}
	

}