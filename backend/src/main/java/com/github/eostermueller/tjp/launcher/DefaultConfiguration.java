package com.github.eostermueller.tjp.launcher;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *  Defines structure of some dependent folders, as detailed here:
 *   http://erikostermueller.com/index.php/tjp-sandbox/

C:\Users\JaneDoe\Documents\tjp ( on mac /Users/JaneDoe/.perfGoat )
 ├── bin
 ├── littleMock-master
 ├── javaPerformanceTroubleshooting-master
 └── maven
   ├── settings.xml  <<<<<===== configured to point local repository to sibling folder named 'repo'
   ├── apache-maven-3.5.4
   └── repo 
   
 * @author erikostermueller
 *
 */
public class DefaultConfiguration implements Configuration {
	 	
	private Path javaHome = null;
	private Path sutHomePath;
	private Path mavenHome;
	private Path userHomeDir;
	private Path perfGoatHomeDir;
	private int maxExceptionCountPerEvent = 100;
	private String mavenZipFileNameWithoutExtension = "apache-maven-3.6.0";
	private Path mavenRepositoryHome;
	private String wiremockZipFileName;
	private Path wiremockHome;
	

	@Override
	public Path getWiremockHome() {
		return wiremockHome;
	}

	@Override
	public void setWiremockHome(Path wiremockHome) {
		this.wiremockHome = wiremockHome;
	}

	private String h2DataFileName;
	private Path h2DataFileHome;
	@Override
	public Path getH2DataFileHome() {
		return h2DataFileHome;
	}

	@Override
	public void setH2DataFileHome(Path h2DataFileHome) {
		this.h2DataFileHome = h2DataFileHome;
	}

	@Override
	public String getH2DataFileName() {
		return h2DataFileName;
	}

	@Override
	public void setH2DataFileName(String h2DataFileName) {
		this.h2DataFileName = h2DataFileName;
	}
	public static final String unix_ABS_PATH_TO_TJP = "/Users/erikostermueller/Documents/src/jdist/tjpUnzipped/tjp";
	public static final String unix_JAVA_HOME = "/Library/Java/JavaVirtualMachines/openjdk-11.0.2.jdk/Contents/Home";
	
	/**
	 * WOW this needs to go away, hard coding paths from my machine.
	 */
	public DefaultConfiguration() {
    		this.setUserHomeDir(		Paths.get( getUserHomeDirString() )	);
		
			this.setJavaHome( 			Paths.get( System.getProperty("java.home")  ) );
			this.setPerfGoatHome(		Paths.get( this.getUserHomeDirString(), ".perfGoat" )			);
			this.setMavenHome(			Paths.get( this.getPerfGoatHome().toString() , this.getMavenZipFileNameWithoutExtension() )		);
			this.setMavenRepositoryHome(Paths.get( this.getPerfGoatHome().toString() , "repository" )		);
			this.setSutHome(			Paths.get( this.getPerfGoatHome().toString() , "tjp2") );
			this.setWiremockHome(		Paths.get( this.getPerfGoatHome().toString() , "wiremock") );
			this.setWiremockZipFileName ("wiremock-2.24.1.jar");

			this.setH2DataFileHome(		Paths.get( this.getPerfGoatHome().toString() , "data") );
			this.setH2DataFileName		("perfSandboxDb.mv.db");
	}
	
	public DefaultConfiguration(Path pgHome, Path javaHome) {
		this.setPerfGoatHome(pgHome);
		this.setJavaHome(javaHome);
	}

	@Override
	public Path getSutHome() {
		return this.sutHomePath;
	}

	@Override
	public Path getMavenRepositoryHome() {
		return this.mavenRepositoryHome;
	}
	@Override
	public void setMavenRepositoryHome(Path val) {
		this.mavenRepositoryHome = val;
	}
	@Override
	public Path getMavenHome() {
		return this.mavenHome;
	}
	@Override
	public void setMavenHome(Path val) {
		this.mavenHome = val;
	}

	@Override
	public Path getUserHomeDir() {
		return this.userHomeDir;
	}
	@Override
	public void setUserHomeDir(Path val) {
		this.userHomeDir = val;
	}
	
	@JsonIgnore
	public String getUserHomeDirString() {
		return System.getProperty("user.home");
	}
	@Override
	public String getMavenZipFileNameWithoutExtension() {
		return mavenZipFileNameWithoutExtension;
	}
	@Override
	public void setMavenZipFileNameWithoutExtension(String val) {
		this.mavenZipFileNameWithoutExtension = val;
	}
	@JsonIgnore
	@Override
	public String getMavenZipFileName() {
		return this.getMavenZipFileNameWithoutExtension() + "-bin.zip";
	}
	@Override
	public Path getPerfGoatHome() {
		return this.perfGoatHomeDir;
	}
	@Override
	public void setPerfGoatHome(Path  val) {
		this.perfGoatHomeDir = val;
	}
	
	@Override
	public Path getJavaHome() {
		return this.javaHome;
	}

	@Override
	public void setJavaHome(Path p) {
		this.javaHome = p;
	}

	@Override
	public int getMaxExceptionCountPerEvent() {
		return this.maxExceptionCountPerEvent;
	}
	@Override
	public void setMaxExceptionCountPerEvent(int val) {
		this.maxExceptionCountPerEvent = val;
	}
	@Override
	public void setSutHome(Path val) {
		this.sutHomePath = val;
		
	}

	@Override
	public String getWiremockZipFileName() {
		return this.wiremockZipFileName;
	}
	@Override
	public void setWiremockZipFileName(String val) {
		this.wiremockZipFileName = val;
	}
	

}