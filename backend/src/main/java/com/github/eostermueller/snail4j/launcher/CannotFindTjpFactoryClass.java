package com.github.eostermueller.snail4j.launcher;

import com.github.eostermueller.snail4j.DefaultFactory;
import com.github.eostermueller.snail4j.Snail4jException;

public class CannotFindTjpFactoryClass extends Snail4jException {

	String className = null;
	Exception cause = null;
	
	@Override
	public Exception getCause() {
		return cause;
	}

	public void setCause(Exception cause) {
		this.cause = cause;
	}

	public CannotFindTjpFactoryClass(Exception causedBy, String myFactoryClassName) {
		super(causedBy);
		this.setClassName(myFactoryClassName);
		this.setCause(causedBy);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	@Override
	public String getMessage() {
		return "Could not find/instantiate [" + this.getClassName() + "].\n  Specify class name of Factory implementation using -D" + DefaultFactory.FACTORY_DASH_D_PARM + "=my.package.MyFactory, where MyFactory implements com.github.eostermueller.tjp.launcher.agent.Factory";
	}
}
