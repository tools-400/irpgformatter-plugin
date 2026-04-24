/*******************************************************************************
 * Copyright (c) 2026 Thomas Raddatz
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/

package de.tools400.lpex.irpgformatter;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.tools400.lpex.menuextension.ILpexMenuExtension;
import de.tools400.lpex.menuextension.LpexMenuExtensionPlugin;

/**
 * The activator class controls the plug-in life cycle.
 */
public class IRpgleFormatterPlugin extends AbstractUIPlugin implements LpexMenuExtensionPlugin {

    /** The plug-in ID */
    public static final String PLUGIN_ID = "de.tools400.lpex.irpgformatter.core";

    public static final String IMAGE_RESET = "reset-10-bw.png";

    /** The shared instance */
    private static IRpgleFormatterPlugin plugin;

    /** The install URL of the plugin */
    private static URL installURL;

    /** The Lpex menu extension */
    private ILpexMenuExtension menuExtension;

    /**
     * The constructor.
     */
    public IRpgleFormatterPlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        installURL = context.getBundle().getEntry("/");
    }

    @Override
    public void stop(BundleContext context) throws Exception {

        if (menuExtension != null) {
            menuExtension.uninstall();
        }

        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static IRpgleFormatterPlugin getDefault() {
        return plugin;
    }

    /**
     * Sets the LPEX menu extension, so that it can be removed when the plug-in
     * ends.
     */
    public void setLpexMenuExtension(ILpexMenuExtension menuExtension) {
        this.menuExtension = menuExtension;
    }

    /**
     * Convenience method to log error messages to the application log.
     * 
     * @param message Message
     * @param e The exception that has produced the error
     */
    public static void logError(String message, Throwable e) {
        if (plugin == null) {
            System.err.println(message);
            if (e != null) {
                e.printStackTrace();
            }
            return;
        }
        plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, Status.ERROR, message, e));
    }

    public static ImageDescriptor getImageDescriptor(String name) {
        String iconPath = "icons/";
        try {
            URL url = new URL(installURL, iconPath + name);
            return ImageDescriptor.createFromURL(url);
        } catch (MalformedURLException e) {
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }
}
