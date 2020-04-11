package app.allclear.platform.dao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import app.allclear.junit.jdbi.JDBiRule;

/** Functional test that verifies the PeopleDAO class.
 * 
 * @author smalleyd
 * @version 1.0.104
 * @since 4/11/2020
 *
 */

@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@ExtendWith(DropwizardExtensionsSupport.class)
public class PeopleDAOTest
{
	public static final JDBiRule RULE = new JDBiRule();
}
