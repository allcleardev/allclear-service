package app.allclear.common.log;

import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/** Unit test class that verifies the LogFilter POJO.
 * 
 * @author smalleyd
 * @version 1.0.45
 * @since 4/5/2020
 *
 */

public class LogFilterTest
{
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(LogFilterTest.class);

	@BeforeAll
	public static void up()
	{
		log.info("Initialized");
	}

	public static Stream<Arguments> match()
	{
		return Stream.of(
			arguments(new LogFilter(), true),
			arguments(new LogFilter().withName(""), true),
			arguments(new LogFilter().withName("common"), true),
			arguments(new LogFilter().withName("COMMON"), true),
			arguments(new LogFilter().withName("common").withLevel(""), true),
			arguments(new LogFilter().withName("platform"), false),
			arguments(new LogFilter().withName("PLATFORM"), false),
			arguments(new LogFilter().withLevel(""), true),
			arguments(new LogFilter().withLevel("debug"), true),
			arguments(new LogFilter().withName("").withLevel("debug"), true),
			arguments(new LogFilter().withLevel("DEBUG"), true),
			arguments(new LogFilter().withLevel("info"), false),
			arguments(new LogFilter().withLevel("INFO"), false),
			arguments(new LogFilter().withName("common").withLevel("DEBUG"), true),
			arguments(new LogFilter().withName("COMMON").withLevel("debug"), true),
			arguments(new LogFilter().withName("platform").withLevel("DEBUG"), false),
			arguments(new LogFilter().withName("COMMON").withLevel("INFO"), false),
			arguments(new LogFilter().withName("common").withLevel("info"), false),
			arguments(new LogFilter().withName("PLATFORM").withLevel("debug"), false));
	}

	@ParameterizedTest
	@MethodSource
	public void match(final LogFilter filter, final boolean expected)
	{
		Assertions.assertEquals(expected, filter.clean().match((Logger) log));
	}
}
