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
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

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
     * Returns the version of the plug-in, as assigned to "Bundle-Version" in
     * "MANIFEST.MF".
     * 
     * @return Version of the plug-in.
     */
    public String getVersion() {
        String version = getBundle().getHeaders().get(Constants.BUNDLE_VERSION);
        if (version == null) {
            version = "0.0.0"; //$NON-NLS-1$
        }

        return version;
    }

    /**
     * Convenience method to log error messages to the application log.
     * 
     * @param message Message
     * @param The exception that has produced the error
     */
    public static void logError(String message, Throwable e) {
        if (plugin == null) {
            System.err.println(message);
            if (e != null) {
                e.printStackTrace();
            }
            return;
        }
        plugin.getLog().log(new Status(Status.ERROR, PLUGIN_ID, message, e));
    }

    /**
     * Convenience method to log warning messages to the application log.
     * 
     * @param message Message
     * @param The warning that has produced the error
     */
    public static void logWarning(String message) {
        if (plugin == null) {
            System.out.println(String.format("WARN: %s", message));
            return;
        }
        plugin.getLog().log(new Status(Status.WARNING, PLUGIN_ID, message));
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        super.initializeImageRegistry(reg);
        reg.put(IMAGE_RESET, getImageDescriptor(IMAGE_RESET));
    }

    public Image getImage(String name) {
        return getImageRegistry().get(name);
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
