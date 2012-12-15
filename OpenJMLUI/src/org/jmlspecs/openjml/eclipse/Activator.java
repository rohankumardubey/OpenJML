/*
 * This file is part of the OpenJML project.
 * Copyright (c) 2006-2013 David R. Cok
 * @author David R. Cok
 */
package org.jmlspecs.openjml.eclipse;

import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle. The plug-in is a
 * singleton- there is just one OpenJML Eclipse plug-in in a process.
 */
public class Activator extends AbstractUIPlugin {

	/** The plug-in ID, which must match the content of plugin.xml in several places */
	public static final String PLUGIN_ID = "org.jmlspecs.OpenJMLUI";

	/** The plug-in ID of the Specs project plugin (containing specifications
	 * of Java library classes).  This must match the ID specified in the 
	 * plugin.xml file of the Specs plugin.  The Specs plugin is the
	 * source of all the Java library specifications.
	 */
	public static final String SPECS_PLUGIN_ID = "org.jmlspecs.Specs";

	/** The single shared instance */
	private static Activator plugin;

	/** A general utility instance used by the plugin */
	protected Utils utils;

	/** The instance of the tool's options for the one plugin instance in the UI 
	 * version of the OpenJML tool. 
	 * This is THE, COMMON, GLOBAL instance of the options structure shared
	 * by all projects using this instance of the plug-in. Don't instantiate
	 * another one, since this one gets shared by reference in places.  This is
	 * initialized by Activator.start().
     */
	public static Options options;

	/**
	 * The constructor, called by Eclipse, not by user code
	 */
	public Activator() {
		//Log.log("UI Plugin constructor executed");
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		Log.log.setListener(new ConsoleLogger("JML Console"));
		//Log.log("JML UI plugin started");
		
		// Various initialization: instances of options and utils; 
		// read all preferences
		utils = new Utils();
		utils.initializeProperties();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		utils = null;
		options = null;
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance. 'Default' is an odd name, but it is the
	 * typical name used in Eclipse for this purpose.
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	static ResourceBundle rb;

}
