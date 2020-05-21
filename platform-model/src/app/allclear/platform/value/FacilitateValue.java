package app.allclear.platform.value;

import java.io.Serializable;

/** Value object that represents a facility change request.
 * 
 * @author smalleyd
 * @version 1.1.58
 * @since 5/20/2020
 *
 */

public class FacilitateValue implements Serializable
{
	private static final long serialVersionUID = 1L;

	public FacilityValue value = null;
	public boolean gotTested = false;
}
