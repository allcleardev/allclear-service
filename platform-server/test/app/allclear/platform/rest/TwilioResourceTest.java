package app.allclear.platform.rest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import app.allclear.platform.ConfigTest;

/** Functional test class that verifies the TwilioResource RESTful component.
 * 
 * @author smalleyd
 * @version 1.1.125
 * @since 8/6/2020
 *
 */

public class TwilioResourceTest
{
	private static TwilioResource rest;

	@BeforeAll
	public static void beforeAll()
	{
		rest = new TwilioResource("key", null, null, ConfigTest.loadTest());
	}

	@ParameterizedTest
	@CsvSource({",",
	            "'',",
	            "'   \t   ',",
	            "y,true",
	            "yes,true",
	            "Y,true",
	            "YES,true",
	            "Yes,true",
	            "yEs,true",
	            "yeS,true",
	            " yeS,true",
	            "yeS ,true",
	            "  yeS  \t \t,true",
	            "n,false",
	            "no,false",
	            "N,false",
	            "NO,false",
	            "No,false",
	            "nO,false",
	            " nO,false",
	            "nO ,false",
	            " \t \t nO  \t \t ,false"})
	public void isYesOrNo(final String value, final Boolean expected)
	{
		Assertions.assertEquals(expected, rest.isYesOrNo(value));
	}

	@ParameterizedTest
	@CsvSource({"abc,'<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response><Message><Body>abc</Body></Message></Response>'",
	            "You have been successfully unsubscribed from further alerts. Have a nice day.,'<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response><Message><Body>You have been successfully unsubscribed from further alerts. Have a nice day.</Body></Message></Response>'",
	            "You have been successfully subscribed to our new facility alerts. Welcome aboard!,'<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<Response><Message><Body>You have been successfully subscribed to our new facility alerts. Welcome aboard!</Body></Message></Response>'"})
	public void response(final String value, final String expected)
	{
		Assertions.assertEquals(expected, rest.response(value));
	}
}
