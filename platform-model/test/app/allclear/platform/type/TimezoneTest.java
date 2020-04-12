package app.allclear.platform.type;

import static org.fest.assertions.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.*;

/** Unit test class that verifies the Timezone type.
 * 
 * @author smalleyd
 * @version 1.0.108
 * @since 4/12/2020
 *
 */

public class TimezoneTest
{
	@Test
	public void check()
	{
		Timezone.LIST.forEach(v -> System.out.println(v));

		assertThat(Timezone.LIST).containsExactly(new Timezone("EST", "Eastern Standard Time", new BigDecimal("-85"), new BigDecimal("-70")),
			new Timezone("CST", "Central Standard Time", new BigDecimal("-100"), new BigDecimal("-85")),
			new Timezone("MST", "Mountain Standard Time", new BigDecimal("-115"), new BigDecimal("-100")),
			new Timezone("PST", "Pacific Standard Time", new BigDecimal("-135"), new BigDecimal("-115")));
	}
}
