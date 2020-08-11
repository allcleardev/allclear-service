package app.allclear.platform.dao;

import app.allclear.common.redis.FakeRedisClient;
import app.allclear.platform.ConfigTest;

/** Test version of the SessionDAO.
 * 
 * @author smalleyd
 * @version 1.1.126
 * @since 8/10/2020
 *
 */

public class FakeSessionDAO extends SessionDAO
{
	public FakeSessionDAO()
	{
		super(new FakeRedisClient(), ConfigTest.loadTest());
	}
}
