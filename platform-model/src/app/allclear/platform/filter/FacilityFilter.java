package app.allclear.platform.filter;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import app.allclear.common.dao.QueryFilter;

/********************************************************************************************************************
*
*	Value object class that represents the search criteria for Facility query.
*
*	@author smalleyd
*	@version 1.0.23
*	@since April 2, 2020
*
*******************************************************************************************************************/

public class FacilityFilter extends QueryFilter
{
	private static final long serialVersionUID = 1L;

	// Members
	public Long id = null;
	public String name = null;
	public String address = null;
	public String city = null;
	public String state = null;
	public BigDecimal latitudeFrom = null;
	public BigDecimal latitudeTo = null;
	public BigDecimal longitudeFrom = null;
	public BigDecimal longitudeTo = null;
	public String phone = null;
	public Boolean hasPhone = null;
	public String appointmentPhone = null;
	public Boolean hasAppointmentPhone = null;
	public String email = null;
	public Boolean hasEmail = null;
	public String url = null;
	public Boolean hasUrl = null;
	public String appointmentUrl = null;
	public Boolean hasAppointmentUrl = null;
	public String hours = null;
	public Boolean hasHours = null;
	public String typeId = null;
	public Boolean hasTypeId = null;
	public Boolean driveThru = null;
	public Boolean appointmentRequired = null;
	public Boolean hasAppointmentRequired = null;
	public Boolean acceptsThirdParty = null;
	public Boolean hasAcceptsThirdParty = null;
	public Boolean referralRequired = null;
	public String testCriteriaId = null;
	public String notTestCriteriaId = null;
	public Boolean hasTestCriteriaId = null;
	public String otherTestCriteria = null;
	public Boolean hasOtherTestCriteria = null;
	public Integer testsPerDay = null;
	public Boolean hasTestsPerDay = null;
	public Integer testsPerDayFrom = null;
	public Integer testsPerDayTo = null;
	public Boolean governmentIdRequired = null;
	public Integer minimumAge = null;
	public Boolean hasMinimumAge = null;
	public Integer minimumAgeFrom = null;
	public Integer minimumAgeTo = null;
	public String doctorReferralCriteria = null;
	public Boolean hasDoctorReferralCriteria = null;
	public Boolean firstResponderFriendly = null;
	public Boolean telescreeningAvailable = null;
	public Boolean acceptsInsurance = null;
	public String insuranceProvidersAccepted = null;
	public Boolean hasInsuranceProvidersAccepted = null;
	public Boolean freeOrLowCost = null;
	public String notes = null;
	public Boolean hasNotes = null;
	public Boolean active = null;
	public Date createdAtFrom = null;
	public Date createdAtTo = null;
	public Date updatedAtFrom = null;
	public Date updatedAtTo = null;
	public GeoFilter from = null;
	public boolean restrictive = false;	// Indicate to restrict the facility search based on the current user profile. DLS on 4/6/2020.

	// Mutators
	public FacilityFilter withId(final Long newValue) { id = newValue; return this; }
	public FacilityFilter withName(final String newValue) { name = newValue; return this; }
	public FacilityFilter withAddress(final String newValue) { address = newValue; return this; }
	public FacilityFilter withCity(final String newValue) { city = newValue; return this; }
	public FacilityFilter withState(final String newValue) { state = newValue; return this; }
	public FacilityFilter withLatitudeFrom(final BigDecimal newValue) { latitudeFrom = newValue; return this; }
	public FacilityFilter withLatitudeTo(final BigDecimal newValue) { latitudeTo = newValue; return this; }
	public FacilityFilter withLongitudeFrom(final BigDecimal newValue) { longitudeFrom = newValue; return this; }
	public FacilityFilter withLongitudeTo(final BigDecimal newValue) { longitudeTo = newValue; return this; }
	public FacilityFilter withPhone(final String newValue) { phone = newValue; return this; }
	public FacilityFilter withHasPhone(final Boolean newValue) { hasPhone = newValue; return this; }
	public FacilityFilter withAppointmentPhone(final String newValue) { appointmentPhone = newValue; return this; }
	public FacilityFilter withHasAppointmentPhone(final Boolean newValue) { hasAppointmentPhone = newValue; return this; }
	public FacilityFilter withEmail(final String newValue) { email = newValue; return this; }
	public FacilityFilter withHasEmail(final Boolean newValue) { hasEmail = newValue; return this; }
	public FacilityFilter withUrl(final String newValue) { url = newValue; return this; }
	public FacilityFilter withHasUrl(final Boolean newValue) { hasUrl = newValue; return this; }
	public FacilityFilter withAppointmentUrl(final String newValue) { appointmentUrl = newValue; return this; }
	public FacilityFilter withHasAppointmentUrl(final Boolean newValue) { hasAppointmentUrl = newValue; return this; }
	public FacilityFilter withHours(final String newValue) { hours = newValue; return this; }
	public FacilityFilter withHasHours(final Boolean newValue) { hasHours = newValue; return this; }
	public FacilityFilter withTypeId(final String newValue) { typeId = newValue; return this; }
	public FacilityFilter withHasTypeId(final Boolean newValue) { hasTypeId = newValue; return this; }
	public FacilityFilter withDriveThru(final Boolean newValue) { driveThru = newValue; return this; }
	public FacilityFilter withAppointmentRequired(final Boolean newValue) { appointmentRequired = newValue; return this; }
	public FacilityFilter withHasAppointmentRequired(final Boolean newValue) { hasAppointmentRequired = newValue; return this; }
	public FacilityFilter withAcceptsThirdParty(final Boolean newValue) { acceptsThirdParty = newValue; return this; }
	public FacilityFilter withHasAcceptsThirdParty(final Boolean newValue) { hasAcceptsThirdParty = newValue; return this; }
	public FacilityFilter withReferralRequired(final Boolean newValue) { referralRequired = newValue; return this; }
	public FacilityFilter withTestCriteriaId(final String newValue) { testCriteriaId = newValue; return this; }
	public FacilityFilter withNotTestCriteriaId(final String newValue) { notTestCriteriaId = newValue; return this; }
	public FacilityFilter withHasTestCriteriaId(final Boolean newValue) { hasTestCriteriaId = newValue; return this; }
	public FacilityFilter withOtherTestCriteria(final String newValue) { otherTestCriteria = newValue; return this; }
	public FacilityFilter withHasOtherTestCriteria(final Boolean newValue) { hasOtherTestCriteria = newValue; return this; }
	public FacilityFilter withTestsPerDay(final Integer newValue) { testsPerDay = newValue; return this; }
	public FacilityFilter withHasTestsPerDay(final Boolean newValue) { hasTestsPerDay = newValue; return this; }
	public FacilityFilter withTestsPerDayFrom(final Integer newValue) { testsPerDayFrom = newValue; return this; }
	public FacilityFilter withTestsPerDayTo(final Integer newValue) { testsPerDayTo = newValue; return this; }
	public FacilityFilter withGovernmentIdRequired(final Boolean newValue) { governmentIdRequired = newValue; return this; }
	public FacilityFilter withMinimumAge(final Integer newValue) { minimumAge = newValue; return this; }
	public FacilityFilter withHasMinimumAge(final Boolean newValue) { hasMinimumAge = newValue; return this; }
	public FacilityFilter withMinimumAgeFrom(final Integer newValue) { minimumAgeFrom = newValue; return this; }
	public FacilityFilter withMinimumAgeTo(final Integer newValue) { minimumAgeTo = newValue; return this; }
	public FacilityFilter withDoctorReferralCriteria(final String newValue) { doctorReferralCriteria = newValue; return this; }
	public FacilityFilter withHasDoctorReferralCriteria(final Boolean newValue) { hasDoctorReferralCriteria = newValue; return this; }
	public FacilityFilter withFirstResponderFriendly(final Boolean newValue) { firstResponderFriendly = newValue; return this; }
	public FacilityFilter withTelescreeningAvailable(final Boolean newValue) { telescreeningAvailable = newValue; return this; }
	public FacilityFilter withAcceptsInsurance(final Boolean newValue) { acceptsInsurance = newValue; return this; }
	public FacilityFilter withInsuranceProvidersAccepted(final String newValue) { insuranceProvidersAccepted = newValue; return this; }
	public FacilityFilter withHasInsuranceProvidersAccepted(final Boolean newValue) { hasInsuranceProvidersAccepted = newValue; return this; }
	public FacilityFilter withFreeOrLowCost(final Boolean newValue) { freeOrLowCost = newValue; return this; }
	public FacilityFilter withNotes(final String newValue) { notes = newValue; return this; }
	public FacilityFilter withHasNotes(final Boolean newValue) { hasNotes = newValue; return this; }
	public FacilityFilter withActive(final Boolean newValue) { active = newValue; return this; }
	public FacilityFilter withCreatedAtFrom(final Date newValue) { createdAtFrom = newValue; return this; }
	public FacilityFilter withCreatedAtTo(final Date newValue) { createdAtTo = newValue; return this; }
	public FacilityFilter withUpdatedAtFrom(final Date newValue) { updatedAtFrom = newValue; return this; }
	public FacilityFilter withUpdatedAtTo(final Date newValue) { updatedAtTo = newValue; return this; }
	public FacilityFilter withFrom(final GeoFilter newValue) { from = newValue; return this; }
	public FacilityFilter withRestrictive(final boolean newValue) { restrictive = newValue; return this; }

	/**************************************************************************
	*
	*	Constructors
	*
	**************************************************************************/

	/** Default/empty. */
	public FacilityFilter() {}

	/** Populator.
		@param page
		@param pageSize
	*/
	public FacilityFilter(final int page, final int pageSize) { super(page, pageSize); }

	/** Populator.
		@param sortOn
		@param sortDir
	*/
	public FacilityFilter(final String sortOn, final String sortDir) { super(sortOn, sortDir); }

	/** Populator.
		@param page
		@param pageSize
		@param sortOn
		@param sortDir
	*/
	public FacilityFilter(final int page, final int pageSize, final String sortOn, final String sortDir) { super(page, pageSize, sortOn, sortDir); }

	/** Populator.
		@param id represents the "id" field.
		@param name represents the "name" field.
		@param address represents the "address" field.
		@param city represents the "city" field.
		@param state represents the "state" field.
		@param latitudeFrom represents the "latitude" field - lower boundary.
		@param latitudeTo represents the "latitude" field - upper boundary.
		@param longitudeFrom represents the "longitude" field - lower boundary.
		@param longitudeTo represents the "longitude" field - upper boundary.
		@param phone represents the "phone" field.
		@param appointmentPhone represents the "appointment_phone" field.
		@param email represents the "email" field.
		@param url represents the "url" field.
		@param appointmentUrl represents the "appointment_url" field.
		@param hours represents the "hours" field.
		@param typeId represents the "type_id" field.
		@param driveThru represents the "drive_thru" field.
		@param appointmentRequired represents the "appointment_required" field.
		@param acceptsThirdParty represents the "accepts_third_party" field.
		@param referralRequired represents the "referral_required" field.
		@param testCriteriaId represents the "test_criteria_id" field.
		@param otherTestCriteria represents the "other_test_criteria" field.
		@param testsPerDay represents the "tests_per_day" field.
		@param testsPerDayFrom represents the "tests_per_day" field - lower boundary.
		@param testsPerDayTo represents the "tests_per_day" field - upper boundary.
		@param governmentIdRequired represents the "government_id_required" field.
		@param minimumAge represents the "minimum_age" field.
		@param minimumAgeFrom represents the "minimum_age" field - lower boundary.
		@param minimumAgeTo represents the "minimum_age" field - upper boundary.
		@param doctorReferralCriteria represents the "doctor_referral_criteria" field.
		@param firstResponderFriendly represents the "first_responder_friendly" field.
		@param telescreeningAvailable represents the "telescreening_available" field.
		@param acceptsInsurance represents the "accepts_insurance" field.
		@param insuranceProvidersAccepted represents the "insurance_providers_accepted" field.
		@param freeOrLowCost represents the "free_or_low_cost" field.
		@param notes represents the "notes" field.
		@param active represents the "active" field.
		@param createdAt represents the "created_at" field.
		@param createdAtFrom represents the "created_at" field - lower boundary.
		@param createdAtTo represents the "created_at" field - upper boundary.
		@param updatedAt represents the "updated_at" field.
		@param updatedAtFrom represents the "updated_at" field - lower boundary.
		@param updatedAtTo represents the "updated_at" field - upper boundary.
	*/
	public FacilityFilter(final Long id,
		final String name,
		final String address,
		final String city,
		final String state,
		final BigDecimal latitudeFrom,
		final BigDecimal latitudeTo,
		final BigDecimal longitudeFrom,
		final BigDecimal longitudeTo,
		final String phone,
		final String appointmentPhone,
		final String email,
		final String url,
		final String appointmentUrl,
		final String hours,
		final String typeId,
		final Boolean driveThru,
		final Boolean appointmentRequired,
		final Boolean acceptsThirdParty,
		final Boolean referralRequired,
		final String testCriteriaId,
		final String otherTestCriteria,
		final Integer testsPerDay,
		final Integer testsPerDayFrom,
		final Integer testsPerDayTo,
		final Boolean governmentIdRequired,
		final Integer minimumAge,
		final Integer minimumAgeFrom,
		final Integer minimumAgeTo,
		final String doctorReferralCriteria,
		final Boolean firstResponderFriendly,
		final Boolean telescreeningAvailable,
		final Boolean acceptsInsurance,
		final String insuranceProvidersAccepted,
		final Boolean freeOrLowCost,
		final String notes,
		final Boolean active,
		final Date createdAtFrom,
		final Date createdAtTo,
		final Date updatedAtFrom,
		final Date updatedAtTo)
	{
		this.id = id;
		this.name = name;
		this.address = address;
		this.city = city;
		this.state = state;
		this.latitudeFrom = latitudeFrom;
		this.latitudeTo = latitudeTo;
		this.longitudeFrom = longitudeFrom;
		this.longitudeTo = longitudeTo;
		this.phone = phone;
		this.appointmentPhone = appointmentPhone;
		this.email = email;
		this.url = url;
		this.appointmentUrl = appointmentUrl;
		this.hours = hours;
		this.typeId = typeId;
		this.driveThru = driveThru;
		this.appointmentRequired = appointmentRequired;
		this.acceptsThirdParty = acceptsThirdParty;
		this.referralRequired = referralRequired;
		this.testCriteriaId = testCriteriaId;
		this.otherTestCriteria = otherTestCriteria;
		this.testsPerDay = testsPerDay;
		this.testsPerDayFrom = testsPerDayFrom;
		this.testsPerDayTo = testsPerDayTo;
		this.governmentIdRequired = governmentIdRequired;
		this.minimumAge = minimumAge;
		this.minimumAgeFrom = minimumAgeFrom;
		this.minimumAgeTo = minimumAgeTo;
		this.doctorReferralCriteria = doctorReferralCriteria;
		this.firstResponderFriendly = firstResponderFriendly;
		this.telescreeningAvailable = telescreeningAvailable;
		this.acceptsInsurance = acceptsInsurance;
		this.insuranceProvidersAccepted = insuranceProvidersAccepted;
		this.freeOrLowCost = freeOrLowCost;
		this.notes = notes;
		this.active = active;
		this.createdAtFrom = createdAtFrom;
		this.createdAtTo = createdAtTo;
		this.updatedAtFrom = updatedAtFrom;
		this.updatedAtTo = updatedAtTo;
	}

	/**************************************************************************
	*
	*	Helper methods
	*
	**************************************************************************/

	/** Helper method - trims all string fields and converts empty strings to NULL. */
	public FacilityFilter clean()
	{
		name = StringUtils.trimToNull(name);
		address = StringUtils.trimToNull(address);
		city = StringUtils.trimToNull(city);
		state = StringUtils.trimToNull(state);
		phone = StringUtils.trimToNull(phone);
		appointmentPhone = StringUtils.trimToNull(appointmentPhone);
		email = StringUtils.trimToNull(email);
		url = StringUtils.trimToNull(url);
		appointmentUrl = StringUtils.trimToNull(appointmentUrl);
		hours = StringUtils.trimToNull(hours);
		typeId = StringUtils.trimToNull(typeId);
		testCriteriaId = StringUtils.trimToNull(testCriteriaId);
		notTestCriteriaId = StringUtils.trimToNull(notTestCriteriaId);
		otherTestCriteria = StringUtils.trimToNull(otherTestCriteria);
		doctorReferralCriteria = StringUtils.trimToNull(doctorReferralCriteria);
		insuranceProvidersAccepted = StringUtils.trimToNull(insuranceProvidersAccepted);
		notes = StringUtils.trimToNull(notes);

		return this;
	}

	/**************************************************************************
	*
	*	Object methods
	*
	**************************************************************************/

	@Override
	public String toString()
	{
		return new StringBuilder("{ id: ").append(id)
			.append(", name: ").append(name)
			.append(", address: ").append(address)
			.append(", city: ").append(city)
			.append(", state: ").append(state)
			.append(", latitudeFrom: ").append(latitudeFrom)
			.append(", latitudeTo: ").append(latitudeTo)
			.append(", longitudeFrom: ").append(longitudeFrom)
			.append(", longitudeTo: ").append(longitudeTo)
			.append(", phone: ").append(phone)
			.append(", hasPhone: ").append(hasPhone)
			.append(", appointmentPhone: ").append(appointmentPhone)
			.append(", hasAppointmentPhone: ").append(hasAppointmentPhone)
			.append(", email: ").append(email)
			.append(", hasEmail: ").append(hasEmail)
			.append(", url: ").append(url)
			.append(", hasUrl: ").append(hasUrl)
			.append(", appointmentUrl: ").append(appointmentUrl)
			.append(", hasAppointmentUrl: ").append(hasAppointmentUrl)
			.append(", hours: ").append(hours)
			.append(", hasHours: ").append(hasHours)
			.append(", typeId: ").append(typeId)
			.append(", hasTypeId: ").append(hasTypeId)
			.append(", driveThru: ").append(driveThru)
			.append(", appointmentRequired: ").append(appointmentRequired)
			.append(", hasAppointmentRequired: ").append(hasAppointmentRequired)
			.append(", acceptsThirdParty: ").append(acceptsThirdParty)
			.append(", hasAcceptsThirdParty: ").append(hasAcceptsThirdParty)
			.append(", referralRequired: ").append(referralRequired)
			.append(", testCriteriaId: ").append(testCriteriaId)
			.append(", notTestCriteriaId: ").append(notTestCriteriaId)
			.append(", hasTestCriteriaId: ").append(hasTestCriteriaId)
			.append(", otherTestCriteria: ").append(otherTestCriteria)
			.append(", hasOtherTestCriteria: ").append(hasOtherTestCriteria)
			.append(", testsPerDay: ").append(testsPerDay)
			.append(", hasTestsPerDay: ").append(hasTestsPerDay)
			.append(", testsPerDayFrom: ").append(testsPerDayFrom)
			.append(", testsPerDayTo: ").append(testsPerDayTo)
			.append(", governmentIdRequired: ").append(governmentIdRequired)
			.append(", minimumAge: ").append(minimumAge)
			.append(", hasMinimumAge: ").append(hasMinimumAge)
			.append(", minimumAgeFrom: ").append(minimumAgeFrom)
			.append(", minimumAgeTo: ").append(minimumAgeTo)
			.append(", doctorReferralCriteria: ").append(doctorReferralCriteria)
			.append(", hasDoctorReferralCriteria: ").append(hasDoctorReferralCriteria)
			.append(", firstResponderFriendly: ").append(firstResponderFriendly)
			.append(", telescreeningAvailable: ").append(telescreeningAvailable)
			.append(", acceptsInsurance: ").append(acceptsInsurance)
			.append(", insuranceProvidersAccepted: ").append(insuranceProvidersAccepted)
			.append(", hasInsuranceProvidersAccepted: ").append(hasInsuranceProvidersAccepted)
			.append(", freeOrLowCost: ").append(freeOrLowCost)
			.append(", notes: ").append(notes)
			.append(", hasNotes: ").append(hasNotes)
			.append(", active: ").append(active)
			.append(", createdAtFrom: ").append(createdAtFrom)
			.append(", createdAtTo: ").append(createdAtTo)
			.append(", updatedAtFrom: ").append(updatedAtFrom)
			.append(", updatedAtTo: ").append(updatedAtTo)
			.append(", from: ").append(from)
			.append(", restrictive: ").append(restrictive)
			.append(" }").toString();
	}
}
