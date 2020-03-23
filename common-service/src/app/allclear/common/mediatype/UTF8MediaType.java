package app.allclear.common.mediatype;

import javax.ws.rs.core.MediaType;

/** Constants class that represents UTF8 media types.
 * 
 * @author smalleyd
 * @version 1.0.0
 * @since 3/22/2020
 *
 */

public class UTF8MediaType
{
	private final static String CHARSET = "UTF-8";
	private static MediaTypeWithCharset type(final String type, final String subtype) { return new MediaTypeWithCharset(new MediaType(type, subtype), CHARSET); }
	
    public final static String APPLICATION_XML = MediaType.APPLICATION_XML + "; charset=" + CHARSET;
    public final static String APPLICATION_ATOM_XML = MediaType.APPLICATION_ATOM_XML + "; charset=" + CHARSET;
    public final static String APPLICATION_XHTML_XML = MediaType.APPLICATION_XHTML_XML + "; charset=" + CHARSET;
    public final static String APPLICATION_SVG_XML = MediaType.APPLICATION_SVG_XML + "; charset=" + CHARSET;
    public final static String APPLICATION_JSON = MediaType.APPLICATION_JSON + "; charset=" + CHARSET;
    public final static String APPLICATION_FORM_URLENCODED = MediaType.APPLICATION_FORM_URLENCODED + "; charset=" + CHARSET;
    public final static String APPLICATION_OCTET_STREAM = MediaType.APPLICATION_OCTET_STREAM + "; charset=" + CHARSET;
    public final static String MULTIPART_FORM_DATA = MediaType.MULTIPART_FORM_DATA + "; charset=" + CHARSET;
    public final static String TEXT_CSV = "text/csv" + "; charset=" + CHARSET;
    public final static String TEXT_PLAIN = MediaType.TEXT_PLAIN + "; charset=" + CHARSET;
    public final static String TEXT_XML = MediaType.TEXT_XML + "; charset=" + CHARSET;
    public final static String TEXT_HTML = MediaType.TEXT_HTML + "; charset=" + CHARSET;

    public final static MediaTypeWithCharset APPLICATION_XML_TYPE = type("application","xml");
    public final static MediaTypeWithCharset APPLICATION_ATOM_XML_TYPE = type("application","atom+xml");
    public final static MediaTypeWithCharset APPLICATION_XHTML_XML_TYPE = type("application","xhtml+xml");
    public final static MediaTypeWithCharset APPLICATION_SVG_XML_TYPE = type("application","svg+xml");
    public final static MediaTypeWithCharset APPLICATION_JSON_TYPE = type("application","json");
    public final static MediaTypeWithCharset APPLICATION_FORM_URLENCODED_TYPE = type("application","x-www-form-urlencoded");
    public final static MediaTypeWithCharset APPLICATION_OCTET_STREAM_TYPE = type("application","octet-stream");
    public final static MediaTypeWithCharset MULTIPART_FORM_DATA_TYPE = type("multipart","form-data");
    public final static MediaTypeWithCharset TEXT_CSV_TYPE = type("text", "csv");
    public final static MediaTypeWithCharset TEXT_PLAIN_TYPE = type("text","plain");
    public final static MediaTypeWithCharset TEXT_XML_TYPE = type("text","xml");
    public final static MediaTypeWithCharset TEXT_HTML_TYPE = type("text","html");
}
