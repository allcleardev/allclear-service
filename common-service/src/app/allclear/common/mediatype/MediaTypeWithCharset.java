package app.allclear.common.mediatype;

import javax.ws.rs.core.MediaType;

/** Represents a Jersey media type inclusive of character set.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class MediaTypeWithCharset extends MediaType
{
	private final String charset;

	public MediaTypeWithCharset()
	{
		super();
		this.charset = MEDIA_TYPE_WILDCARD;
	}

	public MediaTypeWithCharset(final String type, final String subType, final String charset)
	{
		super(type, subType);
		this.charset = charset;
	}

	public MediaTypeWithCharset(final MediaType mediaType, final String charset)
	{
		super(mediaType.getType(), mediaType.getSubtype());
		this.charset = charset;
	}

	public String getCharset() { return charset; }

	public boolean isWildcardCharset() { return this.getCharset().equals(MEDIA_TYPE_WILDCARD); }

	public String toString()
	{
		return String.format("%s; charset=%s", super.toString(), charset); 
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charset == null) ? 0 : charset.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object o)
	{
		if (this == o) return true;
		if (!super.equals(o)) return false;
		if (!(o instanceof MediaTypeWithCharset)) return false;

		var other = (MediaTypeWithCharset) o;
		if (charset == null)
			if (other.charset != null) return false;
		else if (!charset.equals(other.charset)) return false;

		return true;
	}
}
