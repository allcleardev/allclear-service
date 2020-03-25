package app.allclear.platform.dao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.common.redis.RedisClient;
import app.allclear.common.redis.RedisConfig;
import app.allclear.junit.redis.RedisServerRule;

/** Functional test class that verifies RegistrationDAO component.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/24/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class RegistrationDAOTest
{
	public static final RedisServerRule REDIS = new RedisServerRule();

	private static RedisClient redis;
	private static RegistrationDAO dao;

	@BeforeAll
	public static void up()
	{
		redis = new RedisClient(new RedisConfig("localhost", RedisServerRule.PORT_DEFAULT));
		dao = new RegistrationDAO(redis);
	}

	@AfterAll
	public static void down()
	{
		redis.close();
	}
}
