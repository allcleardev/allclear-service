package app.allclear.common.log;

import org.apache.commons.lang3.StringUtils;

import ch.qos.logback.classic.Logger;

import app.allclear.common.dao.QueryFilter;

/** Value object that represents the search criteria for the application logs.
 * 
 * @author smalleyd
 * @version 1.0.45
 * @since 4/5/2020
 *
 */

public class LogFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	public String name = null;
	public String level = null;

	public LogFilter withName(String newValue) { name = newValue; return this; }
	public LogFilter withLevel(String newValue) { level = newValue; return this; }

	public LogFilter clean()
	{
		name = StringUtils.trimToNull(name);
		level = StringUtils.trimToNull(level);

		if (null != name) name = name.toLowerCase();

		return this;
	}

	public boolean match(final Logger log)
	{
		if ((null != name) && (0 > log.getName().toLowerCase().indexOf(name))) return false;
		if ((null != level) && !log.getEffectiveLevel().toString().equalsIgnoreCase(level)) return false;

		return true;
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ name: '").append(name)
			.append("', level: '").append(level)
			.append("', filter: ").append(super.toString())
			.append(" }").toString();
	}
}
