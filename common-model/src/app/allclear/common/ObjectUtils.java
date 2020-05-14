package app.allclear.common;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/** Object utility class.
 * 
 * @author smalleyd
 * @version 1.1.57
 * @version 5/13/2020
 *
 */

public class ObjectUtils
{
	public static String toString(final Object o)
	{
		var b = new ReflectionToStringBuilder(o, ToStringStyle.JSON_STYLE);
		b.setExcludeNullValues(true);

		return b.toString();
	}
}
