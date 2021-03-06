package com.github.eostermueller.snail4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.eostermueller.snail4j.launcher.CannotFindTjpFactoryClass;
import com.github.eostermueller.snail4j.launcher.CommandLine;
import com.github.eostermueller.snail4j.launcher.ConfigLookup;
import com.github.eostermueller.snail4j.launcher.ConfigReaderWriter;
import com.github.eostermueller.snail4j.launcher.Configuration;
import com.github.eostermueller.snail4j.launcher.DefaultCommandLine;
import com.github.eostermueller.snail4j.launcher.DefaultConfigLookup;
import com.github.eostermueller.snail4j.launcher.DefaultConfigReaderWriter;
import com.github.eostermueller.snail4j.launcher.DefaultConfiguration;
import com.github.eostermueller.snail4j.launcher.EventHistory;
import com.github.eostermueller.snail4j.launcher.Factory;
import com.github.eostermueller.snail4j.launcher.Messages;
import com.github.eostermueller.snail4j.launcher.Messages_en_US;
import com.github.eostermueller.snail4j.processmodel.DefaultLoadGenerator;
import com.github.eostermueller.snail4j.processmodel.DefaultProcessModelBuilder;
import com.github.eostermueller.snail4j.processmodel.DefaultSystemUnderTest;
import com.github.eostermueller.snail4j.processmodel.LoadGenerator;
import com.github.eostermueller.snail4j.processmodel.ProcessModelBuilder;
import com.github.eostermueller.snail4j.processmodel.SystemUnderTest;
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
	static Factory FACTORY_INSTANCE = null;
	
	Configuration config = new DefaultConfiguration();
	public static final String FACTORY_DASH_D_PARM = "com.github.eostermueller.snail4j.FactoryImpl";
    public static final String DEFAULT_FACTORY =     "com.github.eostermueller.snail4j.DefaultFactory";
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
	@Override
	public Messages createMessages() {
		String packageAndClassName = "com.github.eostermueller.snail4j.launcher.Messages_" + getMangledLanguageTag();
		
		Messages messages;
		try {
			Class messagesClass = Class.forName(packageAndClassName);
			messages = (Messages) messagesClass.getDeclaredConstructor(null).newInstance(null);
			
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			LOG.atWarning().withCause(e).log("Could not find class [%s] in the classpath, will use %s instead", packageAndClassName, Messages_en_US.class.getName() );
			messages = new Messages_en_US();
		}
		return messages;
	}
	
	/**
	 *   todo:  figure out why I got this intermittent failure one time:
	 * 
	 * 
[INFO] Results:
[INFO]
[ERROR] Failures:
[ERROR]   UniqueIdTest.test08:60->test:100 expected:<[4, 5, 6, 7, 8, 9, 10, 11]> but was:<[3, 4, 5, 6, 7, 8, 9, 10]>                                 
[INFO]
[ERROR] Tests run: 24, Failures: 1, Errors: 0, Skipped: 1
[INFO]
	 *
	 *
	 */
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
		if (FACTORY_INSTANCE==null) {
			String myFactoryClassName = System.getProperty(FACTORY_DASH_D_PARM,DEFAULT_FACTORY);
			try {
				Class<Factory> factoryClass = (Class<Factory>) Class.forName(myFactoryClassName); 
				Constructor[] ctors = factoryClass.getDeclaredConstructors();
				Constructor ctor = null;
				for (int i = 0; i < ctors.length; i++) {
				    ctor = ctors[i];
				    if (ctor.getGenericParameterTypes().length == 0)
					break;
				}
				FACTORY_INSTANCE = (Factory)ctor.newInstance();
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | ClassNotFoundException e) {
				CannotFindTjpFactoryClass cftf = new CannotFindTjpFactoryClass(e,myFactoryClassName);
				throw cftf;
			}
		}
		
		return FACTORY_INSTANCE;
	}
	@Override
	public Configuration getConfiguration() {
		return this.config;
	}
	@Override
	public EventHistory getEventHistory() {
		return eventHistory;
	}
	@Override
	public Configuration getConfiguration(File folder) {
		throw new UnsupportedOperationException();
		//return null;
	}
	@Override
	public ConfigReaderWriter getConfigReaderWriter(Configuration cfg, File tmpFolder) {
		ConfigReaderWriter configReaderWriter = new DefaultConfigReaderWriter(cfg,tmpFolder);
		return configReaderWriter;
	}
	@Override
	public ProcessModelBuilder createProcessModelBuilder() {
		return new DefaultProcessModelBuilder( this.getConfiguration() );
	}
	@Override
	public
	Snail4jInstaller createNewInstaller() {
		return new Snail4jInstaller();
	}
	@Override
	public ConfigLookup createConfigLookup() {
		ConfigLookup cfgLookup = new DefaultConfigLookup();
		cfgLookup.setConfiguration(this.getConfiguration());
		return cfgLookup;
	}
	@Override
	public CommandLine createNewCommandLine(String val)  {
		CommandLine cmdLine = new DefaultCommandLine(val);
		cmdLine.setConfigLookup( createConfigLookup() );
		return cmdLine;
//broken		return new CommandLineWrapper(val);
	}
	@Override
	public void setConfiguration(Configuration val) {
		this.config = val;
	}
	
	@Override
	public SystemUnderTest createSystemUnderTest() throws Snail4jException {
		SystemUnderTest sut = new DefaultSystemUnderTest((Configuration)this.getConfiguration()); 
		return sut;
	}
	@Override
	public LoadGenerator createLoadGenerator() throws Snail4jException {
		return new DefaultLoadGenerator((Configuration)this.getConfiguration() );
	}
	
	

}
