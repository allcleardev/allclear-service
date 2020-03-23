package app.allclear.common.errors;

import java.math.BigDecimal;

import org.junit.*;

/** Unit test class that verifies the Validator component.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class ValidatorTest
{
	@Test
	public void ensureExists()
	{
		checkSingle(new Validator().ensureExists("field", "Caption", null), "field", "Caption is not set.");
	}

	@Test
	public void ensureExists_success()
	{
		checkSuccess(new Validator().ensureExists("field", "Caption", "No Error"));
	}

	@Test
	public void ensureLength()
	{
		checkSingle(new Validator().ensureLength("field", "Caption", "Testing the size of this text", 5), "field", "Caption 'Testing the size of this text' is longer than the expected size of 5.");
	}

	@Test
	public void ensureLength_missing()
	{
		checkSuccess(new Validator().ensureLength("field", "Caption", null, 20));
	}

	@Test
	public void ensureLength_success()
	{
		checkSuccess(new Validator().ensureLength("field", "Caption", "No Error", 20));
	}

	@Test
	public void ensureStringLength_longer()
	{
		checkSingle(new Validator().ensureLength("field", "Caption", "Testing the size of this text", 1, 5), "field", "Caption cannot be longer than 5 characters.");
	}

	@Test
	public void ensureStringLength_shorter()
	{
		checkSingle(new Validator().ensureLength("field", "Caption", "Testing the size of this text", 100, 500), "field", "Caption cannot be shorter than 100 characters.");
	}

	@Test
	public void ensureStringLength_missing()
	{
		checkSuccess(new Validator().ensureLength("field", "Caption", null, 20, 100));
	}

	@Test
	public void ensureStringLength_success()
	{
		checkSuccess(new Validator().ensureLength("field", "Caption", "No Error", 5, 20));
	}

	@Test
	public void ensureExistsAndLength()
	{
		checkSingle(new Validator().ensureExistsAndLength("field", "Caption", "Testing the size of this text", 5), "field", "Caption 'Testing the size of this text' is longer than the expected size of 5.");
	}

	@Test
	public void ensureExistsAndLength_missing()
	{
		checkSingle(new Validator().ensureExistsAndLength("field", "Caption", null, 5), "field", "Caption is not set.");
	}

	@Test
	public void ensureExistsAndLength_success()
	{
		checkSuccess(new Validator().ensureExistsAndLength("field", "Caption", "No Error", 20));
	}

	@Test
	public void ensureExistsAndStringLength_longer()
	{
		checkSingle(new Validator().ensureExistsAndLength("field", "Caption", "Testing the size of this text", 1, 5), "field", "Caption cannot be longer than 5 characters.");
	}

	@Test
	public void ensureExistsAndStringLength_shorter()
	{
		checkSingle(new Validator().ensureExistsAndLength("field", "Caption", "Testing the size of this text", 100, 500), "field", "Caption cannot be shorter than 100 characters.");
	}

	@Test
	public void ensureExistsAndStringLength_missing()
	{
		checkSingle(new Validator().ensureExistsAndLength("field", "Caption", null, 5, 100), "field", "Caption is not set.");
	}

	@Test
	public void ensureExistsAndStringLength_success()
	{
		checkSuccess(new Validator().ensureExistsAndLength("field", "Caption", "No Error", 2, 200));
	}

	@Test
	public void ensureCombo()
	{
		var validator = new Validator();
		validator.ensureLength("field1", "Caption 1", "This text is too long.", 5);
		validator.ensureLength("field2", "Caption 2", "This text is too long for this field.", 10);
		Assert.assertNotNull("Exists", validator.getErrors());
		Assert.assertTrue("Check hasErrors", validator.hasErrors());
		Assert.assertEquals("Check size", 2, validator.getErrors().size());
		Assert.assertEquals("Check name (1)", "field1", validator.getErrors().get(0).name);
		Assert.assertEquals("Check message (1)", "Caption 1 'This text is too long.' is longer than the expected size of 5.", validator.getErrors().get(0).message);
		Assert.assertEquals("Check name (2)", "field2", validator.getErrors().get(1).name);
		Assert.assertEquals("Check message (2)", "Caption 2 'This text is too long for this field.' is longer than the expected size of 10.", validator.getErrors().get(1).message);
	}

	@Test
	public void ensurePattern_domainName()
	{
		checkSingle(new Validator().ensurePattern("domainName", "Domain Name", "swellby", PatternUtils.PATTERN_DOMAIN_NAME), "domainName", "Domain Name 'swellby' does not match a valid pattern.");
	}

	@Test
	public void ensurePattern_domainName_success()
	{
		checkSuccess(new Validator().ensurePattern("domainName", "Domain Name", "swellby.com", PatternUtils.PATTERN_DOMAIN_NAME));
	}

	@Test
	public void ensurePattern_email()
	{
		checkSingle(new Validator().ensurePattern("email", "Email", "david@swellby", PatternUtils.PATTERN_EMAIL), "email", "Email 'david@swellby' does not match a valid pattern.");
	}

	@Test
	public void ensurePattern_email_success()
	{
		checkSuccess(new Validator().ensurePattern("email", "Email", "david+test@swellby.com", PatternUtils.PATTERN_EMAIL));
	}

	@Test
	public void ensurePattern_password()
	{
		checkSingle(new Validator().ensurePattern("password", "Password", "abc#$!-@%^&*+=invalid123", PatternUtils.PATTERN_PASSWORD), "password", "Password 'abc#$!-@%^&*+=invalid123' does not match a valid pattern.");
	}

	@Test
	public void ensurePattern_password_success()
	{
		checkSuccess(new Validator().ensurePattern("password", "Password", "abc#$!-@%&*invalid123", PatternUtils.PATTERN_PASSWORD));
	}

	@Test
	public void ensurePattern_url()
	{
		checkSingle(new Validator().ensurePattern("webSite", "Web Site", "www.swellby.com", PatternUtils.PATTERN_URL), "webSite", "Web Site 'www.swellby.com' does not match a valid pattern.");
	}

	@Test
	public void ensurePattern_url_success()
	{
		checkSuccess(new Validator().ensurePattern("webSite", "Web Site", "https://www.swellby.com", PatternUtils.PATTERN_URL));
	}

	@Test
	public void ensureMinimum_fail()
	{
		checkSingle(new Validator().ensureMinimum("hours", "Hours", 10, 20), "hours", "The %s cannot be less than %d.", "Hours", 20);
	}

	@Test
	public void ensureMinimum_success()
	{
		checkSuccess(new Validator().ensureMinimum("hours", "Hours", 21, 20));
	}

	@Test
	public void ensureMinimum_withNull_fail()
	{
		checkSingle(new Validator().ensureMinimum("hours", "Hours", Integer.valueOf(10), 20), "hours", "The %s cannot be less than %d.", "Hours", 20);
	}

	@Test
	public void ensureMinimum_withNull_success()
	{
		checkSuccess(new Validator().ensureMinimum("hours", "Hours", Integer.valueOf(21), 20));
		checkSuccess(new Validator().ensureMinimum("hours", "Hours", null, 20));
	}

	@Test
	public void ensureExistsAndPrice_missing()
	{
		checkSingle(new Validator().ensureExistsAndPrice("price", "Price", null), "price", "Price is not set.");
	}

	@Test
	public void ensureExistsAndPrice_negative()
	{
		checkSingle(new Validator().ensureExistsAndPrice("price", "Price", new BigDecimal("-5.76")), "price", "The Price, -5.76, cannot be a negative number.");
	}

	@Test
	public void ensureExistsAndPrice_success()
	{
		checkSuccess(new Validator().ensureExistsAndPrice("price", "Price", new BigDecimal("0.00")));
		checkSuccess(new Validator().ensureExistsAndPrice("price", "Price", new BigDecimal("5.76")));
	}

	@Test
	public void ensureRating_missing()
	{
		checkSuccess(new Validator().ensureRating("rating", "Rating", null));
	}

	@Test
	public void ensureRating_toHigh()
	{
		checkSingle(new Validator().ensureRating("rating", "Rating", 6), "rating", "The Rating, 6, cannot be greater than 5.");
	}

	@Test
	public void ensureRating_toLow()
	{
		checkSingle(new Validator().ensureRating("rating", "Rating", 0), "rating", "The Rating, 0, cannot be less than 1.");
	}

	@Test
	public void ensureRating_sucess()
	{
		checkSuccess(new Validator().ensureRating("rating", "Rating", 1));
		checkSuccess(new Validator().ensureRating("rating", "Rating", 2));
		checkSuccess(new Validator().ensureRating("rating", "Rating", 3));
		checkSuccess(new Validator().ensureRating("rating", "Rating", 4));
		checkSuccess(new Validator().ensureRating("rating", "Rating", 5));
	}

	@Test
	public void ensureExistsAndGeoCoord_missing()
	{
		checkSingle(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", null), "latitude", "Latitude is not set.");
	}

	@Test
	public void ensureExistsAndGeoCoord_tooHigh()
	{
		checkSingle(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal("180.5")), "latitude", "Latitude '180.5' is greater than the accepted value of 180.");
	}

	@Test
	public void ensureExistsAndGeoCoord_tooLow()
	{
		checkSingle(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal("-180.5")), "latitude", "Latitude '-180.5' is less than the accepted value of -180.");
	}

	@Test
	public void ensureExistsAndGeoCoord_success()
	{
		checkSuccess(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal(0)));
		checkSuccess(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal(4.58484f)));
		checkSuccess(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal(4.58484d)));
		checkSuccess(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal(-45)));
		checkSuccess(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal(45)));
		checkSuccess(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal(90)));
		checkSuccess(new Validator().ensureExistsAndGeoCoord("latitude", "Latitude", new BigDecimal(-90)));
	}

	@Test
	public void ensureGeoCoord_tooHigh()
	{
		checkSingle(new Validator().ensureGeoCoord("latitude", "Latitude", new BigDecimal("180.5")), "latitude", "Latitude '180.5' is greater than the accepted value of 180.");
	}

	@Test
	public void ensureGeoCoord_tooLow()
	{
		checkSingle(new Validator().ensureGeoCoord("latitude", "Latitude", new BigDecimal("-180.5")), "latitude", "Latitude '-180.5' is less than the accepted value of -180.");
	}

	@Test
	public void ensureGeoCoord_success()
	{
		checkSuccess(new Validator().ensureGeoCoord("latitude", "Latitude", null));
		checkSuccess(new Validator().ensureGeoCoord("latitude", "Latitude", new BigDecimal(4.58484f)));
		checkSuccess(new Validator().ensureGeoCoord("latitude", "Latitude", new BigDecimal(4.58484d)));
		checkSuccess(new Validator().ensureGeoCoord("latitutde", "Latitude", new BigDecimal(-45)));
		checkSuccess(new Validator().ensureGeoCoord("latitude", "Latitude", new BigDecimal(45)));
		checkSuccess(new Validator().ensureGeoCoord("latitude", "Latitude", new BigDecimal(90)));
		checkSuccess(new Validator().ensureGeoCoord("latitude", "Latitude", new BigDecimal(-90)));
	}

	@Test
	public void ensureExistsAndRange_double_missing()
	{
		checkSingle(new Validator().ensureExistsAndRange("rating", "Rating", null, 0d, 5d), "rating", "Rating is not set.");
	}

	@Test
	public void ensureExistsAndRange_double_tooHigh()
	{
		checkSingle(new Validator().ensureExistsAndRange("rating", "Rating", 5.5d, 0d, 5d), "rating", "Rating '5.5' is greater than the accepted value of 5.0.");
	}

	@Test
	public void ensureExistsAndRange_double_tooLow()
	{
		checkSingle(new Validator().ensureExistsAndRange("rating", "Rating", -0.5, 0d, 5d), "rating", "Rating '-0.5' is less than the accepted value of 0.0.");
	}

	@Test
	public void ensureExistsAndRange_double_success()
	{
		checkSuccess(new Validator().ensureExistsAndRange("rating", "Rating", 0d, 0d, 5d));
		checkSuccess(new Validator().ensureExistsAndRange("rating", "Rating", 4.58484d, 0d, 5d));
		checkSuccess(new Validator().ensureExistsAndRange("rating", "Rating", 3.58484d, 0d, 5d));
		checkSuccess(new Validator().ensureExistsAndRange("rating", "Rating", 5d, 0d, 5d));
		checkSuccess(new Validator().ensureExistsAndRange("rating", "Rating", 2d, 0d, 5d));
		checkSuccess(new Validator().ensureExistsAndRange("rating", "Rating", 1.5d, 0d, 5d));
		checkSuccess(new Validator().ensureExistsAndRange("rating", "Rating", 0.7d, 0d, 5d));
	}

	@Test
	public void ensureRange_double_tooHigh()
	{
		checkSingle(new Validator().ensureRange("rating", "Rating", 5.5d, 0d, 5d), "rating", "Rating '5.5' is greater than the accepted value of 5.0.");
	}

	@Test
	public void ensureRange_double_tooLow()
	{
		checkSingle(new Validator().ensureRange("rating", "Rating", -0.5, 0d, 5d), "rating", "Rating '-0.5' is less than the accepted value of 0.0.");
	}

	@Test
	public void ensureRange_double_success()
	{
		checkSuccess(new Validator().ensureRange("rating", "Rating", null, 0d, 5d));
		checkSuccess(new Validator().ensureRange("rating", "Rating", 0d, 0d, 5d));
		checkSuccess(new Validator().ensureRange("rating", "Rating", 4.58484d, 0d, 5d));
		checkSuccess(new Validator().ensureRange("rating", "Rating", 3.58484d, 0d, 5d));
		checkSuccess(new Validator().ensureRange("rating", "Rating", 5d, 0d, 5d));
		checkSuccess(new Validator().ensureRange("rating", "Rating", 2d, 0d, 5d));
		checkSuccess(new Validator().ensureRange("rating", "Rating", 1.5d, 0d, 5d));
		checkSuccess(new Validator().ensureRange("rating", "Rating", 0.7d, 0d, 5d));
	}

	@Test
	public void ensurePassword()
	{
		checkSingle(new Validator().ensurePassword("password", "Password", null), "password", "%s is not set.", "Password");
		Assert.assertEquals("Check empty string", 5, new Validator().ensurePassword("password", "Password", "").getErrors().size());
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa$$w0r"), "password", "%s cannot be shorter than %d characters.", "Password", 8);
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa$$w0rdddddddddddddd"), "password", "%s cannot be longer than %d characters.", "Password", 20);
		checkSingle(new Validator().ensurePassword("password", "Password", "PA$$W0RD"), "password", "%s must contain at least one lower case character (a-z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa$$w0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa$$word"), "password", "%s must contain at least one numeric character (0-9).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Passw0rd"), "password", "%s must contain at least one special character (~ ! @ # $ %% ^ & * ( ) - _ + =).", "Password");

		// Test invalid characters.
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa<sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa>sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa,sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa.sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa/sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa?sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa'sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa\"sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa;sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa:sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa\\sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa|sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa]sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa}sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa[sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Pa{sw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Paàsw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Paësw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "Paîsw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "PaŎsw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "PaƯsw0r&"), "password", "%s can contain only the following characters (a-Z, A-Z, 0-9) and these symbols: ~ ! @ # $ %% ^ & * ( ) - _ + =", "Password");

		// Test all the symbols: ~ ! @ # $ % ^ & * ( ) - _ + =
		// Should only trigger the single error.
		checkSingle(new Validator().ensurePassword("password", "Password", "pa~sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa!sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa@sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa#sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa$sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa%sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa^sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa&sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa*sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa(sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa)sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa-sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa_sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa+sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
		checkSingle(new Validator().ensurePassword("password", "Password", "pa=sw0rd"), "password", "%s must contain at least one upper case character (A-Z).", "Password");
	}

	@Test
	public void ensurePassword_success()
	{
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa~sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa!sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa@sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa#sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa$sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa%sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa^sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa&sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa*sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa(sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa)sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa-sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa_sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa+sw0rd"));
		checkSuccess(new Validator().ensurePassword("password", "Password", "Pa=sw0rd"));
	}

	@Test
	public void add()
	{
		checkSingle(new Validator().add("countryId", "The Country, US, is not valid."), "countryId", "The Country, US, is not valid.");
	}

	@Test
	public void addWithParameters()
	{
		checkSingle(new Validator().add("field", "%s cannot be longer than %d characters.", "Caption", 5), "field", "Caption cannot be longer than 5 characters.");
	}

	@Test(expected=ValidationException.class)
	public void check()
	{
		new Validator().ensureExists("field", "Caption", null).check();
	}

	@Test
	public void check_success()
	{
		new Validator().ensureExists("field", "Caption", "No Error").check();
	}

	@Test
	public void clear()
	{
		var v = new Validator().ensureExists("name", "Name", null);
		Assert.assertNotNull("Exists", v.getErrors());
		Assert.assertEquals("Check toString", "Name is not set.", v.toString());
		Assert.assertEquals("Check size", 1, v.getErrors().size());

		v.clear();
		Assert.assertNull("Cleared", v.getErrors());
		Assert.assertNull("Check toString cleared", v.toString());
	}

	/** Helper method - check the contents of a single error message. */
	private void checkSingle(final Validator validator, final String name, final String message, final Object... args)
	{
		checkSingle(validator, name, String.format(message, args));
	}

	/** Helper method - check the contents of a single error message. */
	private void checkSingle(final Validator validator, final String name, final String message)
	{
		Assert.assertNotNull("Exists", validator.getErrors());
		Assert.assertTrue("Check hasErrors", validator.hasErrors());
		Assert.assertEquals("Check size", 1, validator.getErrors().size());
		Assert.assertEquals("Check name", name, validator.getErrors().get(0).name);
		Assert.assertEquals("Check message", message, validator.getErrors().get(0).message);
	}

	private void checkSuccess(final Validator validator)
	{
		Assert.assertNull("Exists", validator.getErrors());
		Assert.assertFalse("Check hasErrors", validator.hasErrors());
	}
}
