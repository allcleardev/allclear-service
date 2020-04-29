package app.allclear.platform.model;

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

import app.allclear.twilio.model.TwilioUtils;

/** Value object that represents the request to find People by names or phone numbers.
 * 
 * @author smalleyd
 * @version 1.1.9
 * @since 4/29/2020
 *
 */

public class PeopleFindRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	public final List<String> names;
	public final List<String> phones;
	public final long pageSize;

	public PeopleFindRequest(@JsonProperty("names") final List<String> names,
		@JsonProperty("phones") final List<String> phones,
		@JsonProperty("pageSize") final Long pageSize)
	{
		this.pageSize = ((null == pageSize) || (1L > pageSize) || (100L < pageSize)) ? 20L : pageSize;	// Max page size is 100. Default page size is 20.
		this.names = CollectionUtils.isEmpty(names) ? null :
			names.stream()
				.map(v -> StringUtils.trimToNull(v)).filter(v -> null != v).limit(this.pageSize).collect(toList());
		this.phones = CollectionUtils.isEmpty(phones) ? null :
			phones.stream().map(v -> StringUtils.trimToNull(v)).filter(v -> null != v)
				.map(v -> TwilioUtils.normalize(v)).filter(v -> null != v).limit(this.pageSize).collect(toList());
	}

	public boolean valid()
	{
		return CollectionUtils.isNotEmpty(names) || CollectionUtils.isNotEmpty(phones);
	}

	@Override
	public String toString()
	{
		return new StringBuilder("{ names: ").append(names)
			.append(", phones: ").append(phones)
			.append(", pageSize: ").append(pageSize)
			.append(" }").toString();
	}
}
