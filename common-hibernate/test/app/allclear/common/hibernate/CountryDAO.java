package app.allclear.common.hibernate;

import org.hibernate.SessionFactory;

import app.allclear.common.entity.Country;

/** Data access object that provides access to the Country test entity.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class CountryDAO extends AbstractDAO<Country>
{
	public CountryDAO(final SessionFactory factory)
	{
		super(factory);
	}

	public Country add(final String id, final String name, final String code, final String numCode, final Runnable afterTrans)
	{
		if (null != afterTrans) afterTrans(afterTrans);

		return persist(new Country(id, name, code, numCode));
	}

	public Country add(final String id, final String name, final String code, final String numCode, final Runnable onCommit, final Runnable onRollback)
	{
		afterTrans(onCommit, onRollback);

		return persist(new Country(id, name, code, numCode));
	}

	public Country find(final String id, final Runnable afterTrans) { afterTrans(afterTrans); return get(id); }

	public Country add(final Country record) { return persist(record); }
	public Country find(final String id) { return get(id); }
}
