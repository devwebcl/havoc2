package com.github.eostermueller.snail4j.processmodel;


import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import com.github.eostermueller.snail4j.DefaultFactory;
import com.github.eostermueller.snail4j.Snail4jException;
import com.github.eostermueller.snail4j.launcher.CannotFindTjpFactoryClass;
import com.github.eostermueller.snail4j.launcher.CommandLine;
import com.github.eostermueller.snail4j.launcher.ConfigVariableNotFoundException;
import com.github.eostermueller.snail4j.launcher.Configuration;
import com.github.eostermueller.snail4j.launcher.Event;
import com.github.eostermueller.snail4j.launcher.GroupNameThreadFactory;
import com.github.eostermueller.snail4j.launcher.Level;
import com.github.eostermueller.snail4j.launcher.Messages;
import com.github.eostermueller.snail4j.launcher.ProcessKey;
import com.github.eostermueller.snail4j.launcher.SimpleStdoutProcessRunner;
import com.github.eostermueller.snail4j.launcher.SimpleStdoutProcessRunnerJdk8;
import com.github.eostermueller.snail4j.launcher.StdoutProcessRunner;
import com.github.eostermueller.snail4j.launcher.StdoutProcessRunnerJdk8;

import ch.qos.logback.classic.Logger;

public class DefaultSystemUnderTest implements SystemUnderTest {
	private final Logger LOGGER = (Logger) LoggerFactory.getLogger(this.getClass());
	private String THREAD_NAME = "snail4j-sut";
	private Configuration cfg;
	private SimpleStdoutProcessRunner runner = null;
	private static String OS = System.getProperty("os.name").toLowerCase();
	private SimpleStdoutProcessRunner runnerStop = null;

	public DefaultSystemUnderTest(Configuration val) throws Snail4jException {
		this.cfg = val;

		ProcessKey key = ProcessKey.create(this.getClass().getCanonicalName(), Level.CHILD, "sut");
		runner = new SimpleStdoutProcessRunnerJdk8(key);

		runner.setProcessBuilder( getProcessBuilder() );
		runner.setWorkingDirectory(val.getProcessManagerHome().toFile());


		//TODO: refactor to be more elegant than create another Process (runner), maybe not leaving it here, but call directly to DefaultProcessModelBuilder:
		runnerStop = new SimpleStdoutProcessRunnerJdk8(key);
		runnerStop.setProcessBuilder( getProcessBuilderStop() );
		runnerStop.setWorkingDirectory(val.getProcessManagerHome().toFile());

	}


	private Configuration getConfiguration() {
		return this.cfg;
	}


	//TODO: refactor this method to be more elegant.
	ProcessBuilder getProcessBuilderStop() throws ConfigVariableNotFoundException, Snail4jException {

		CommandLine cmdLine = DefaultFactory.getFactory().createNewCommandLine(
				this.getConfiguration().getWindowsKillerProcess()  //new, comand maven
				);

		ProcessBuilder pb = new ProcessBuilder( cmdLine.getProcessedCommandLine() ); // maven parsing

		pb.directory( this.getConfiguration().getProcessManagerHome().toFile());
		File stdoutLogFile = new File(
				this.getConfiguration().getLogDir().toFile(),
				this.getConfiguration().getSystemUnderTestStdoutLogFileName()
			);
		pb.redirectOutput(stdoutLogFile);

		return pb;
	}


	ProcessBuilder getProcessBuilder() throws ConfigVariableNotFoundException, Snail4jException {

		CommandLine cmdLine = DefaultFactory.getFactory().createNewCommandLine(
				this.getConfiguration().getProcessManagerLaunchCmd()
				);

		ProcessBuilder pb = new ProcessBuilder( cmdLine.getProcessedCommandLine() );

		pb.directory( this.getConfiguration().getProcessManagerHome().toFile());
		File stdoutLogFile = new File(
				this.getConfiguration().getLogDir().toFile(),
				this.getConfiguration().getSystemUnderTestStdoutLogFileName()
			);
		pb.redirectOutput(stdoutLogFile);

		return pb;
	}


	@Override
	public void start() throws ConfigVariableNotFoundException, IOException, Snail4jException {

		runner.start();
		Messages m = DefaultFactory.getFactory().createMessages();
		String d = m.getSutStartMessage( runner.toHumanReadableString() );

		System.out.println("1mensaje = " + d);

		DefaultFactory.getFactory().getEventHistory().getEvents().add( Event.create(d) );

	}


	@Override
	public void stop() throws Snail4jException {
		boolean ynKillFileExistsBefore = false;
		boolean ynKillFileExistsAfter = true;

		File killFile = this.getConfiguration().getSutKillFile().toFile();
		ynKillFileExistsBefore = killFile.exists();
		killFile.delete();
		ynKillFileExistsAfter = killFile.exists();

		Messages m = DefaultFactory.getFactory().createMessages();

		String d = m.getSutStopMessage( runner.toHumanReadableString(), killFile.getAbsolutePath(), ynKillFileExistsBefore, ynKillFileExistsAfter);
		DefaultFactory.getFactory().getEventHistory().getEvents().add( Event.create(d) );


		//TODO: remove!
		System.out.println("*************** EJEMPLO MIO *******************************************");
		System.out.println("2mensaje = " + d);

		//TODO: if it's windows then execute ant-taskkill must be executed
		if(isWindows()) {

			System.out.println("*************** es Windows !");

			runnerStop.start();
			System.out.println("**** " + runnerStop.toHumanReadableString() );


		} else if (isMac()) {
			System.out.println("**** This is Mac!");

			runnerStop.start();
			System.out.println("**** " + runnerStop.toHumanReadableString() );


			/*
			//TODO: cleanup this hardcoded, and use another "SimpleStdoutProcessRunner runner"
			// maybe something similar from DefaultProcessModelBuilder : protected StdoutProcessRunner getJMeterProcess()
			ProcessBuilder processBuilder = new ProcessBuilder();

			//TODO: this is backend and need to be ran on processManager
			//this is generated at DefaultConfiguration - this.setWindowsKillerProcess("#{mavenExePath} antrun:run@tmp-echo");
			processBuilder.command("mvn antrun:run@tmp-echo");

			try {
				Process process = processBuilder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}*/

		}

	}


	public static boolean isWindows() {

		return (OS.indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (OS.indexOf("mac") >= 0);

	}

}

//Runnable r = new Runnable() {
//
//	@Override
//	public void run() {
//	    Process process;
//		try {
//
//			process = DefaultSystemUnderTest.this.getProcessBuilder().start();
//			if (status !=0) {
//				throw new Exception("The following source code failed to compile [" + this.getSourceFileText() + "]");
//			} else {
//				//System.out.println(".class was created after compile?[" + getAbsoluteClassFileName().exists()  + "] javac output: [" + getAbsoluteClassFileName() + "]");
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//	}
//};
//Thread t = new GroupNameThreadFactory(THREAD_NAME).newThread(r);
//t.start();
