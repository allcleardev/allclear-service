package app.allclear.common;

import app.allclear.common.value.ManifestValue;

/** Utilities class that operates on a JAR's manifest.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ManifestUtils
{
    /** Extract Info from the class's Package, if possible. */
    public static ManifestValue getInfo(Class<?> clazz)
    {
        var p = clazz.getPackage();
        var vendor = p.getImplementationVendor();
        var title = p.getImplementationTitle();
        var version = p.getImplementationVersion();
        return new ManifestValue(vendor, title, version);
    }
}
