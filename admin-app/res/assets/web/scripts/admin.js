var AdminApp = new TabTemplate();

AdminApp.TABS = [ { id: 'doPeople', caption: 'People', children: [ { id: 'doPatients', caption: 'Patients' },
	                                                               { id: 'doRegistrations', caption: 'Registrations' },
	                                                               { id: 'doTests', caption: 'Tests' } ] },
	{ id: 'doFacilities', caption: 'Facilities', children: [ { id: 'doFacilitate', caption: 'Change Requests' },
	                                                         { id: 'doExperiences', caption: 'Experiences' } ] },
	{ id: 'doLogs', caption: 'Logs', children: [ { id: 'doQueueStats', caption: 'Queue Stats' } ] },
	{ id: 'doSessions', caption: 'Sessions', children: [ { id: 'doAdmins', caption: 'Admins' },
	                                                     { id: 'doCustomers', caption: 'Customers' } ] },
	{ id: 'doConfig', caption: 'Config', children: [ { id: 'doHibernate', caption: 'Hibernate' },
	                                                 { id: 'doHeapDump', caption: 'Heap Dump' } ] } ];

var EditorApp = new TabTemplate();

EditorApp.TABS = [ { id: 'doFacilities', caption: 'Facilities' },
	{ id: 'doFacilitate', caption: 'Change Requests' } ];

AdminApp.doPeople = body => PeopleHandler.filter({ pageSize: 100 }, body);
AdminApp.doPatients = body => PatientsHandler.filter({ pageSize: 100 }, body);
AdminApp.doRegistrations = body => RegistrationsHandler.filter({ pageSize: 100 }, body);
AdminApp.doTests = body => TestsHandler.filter({ pageSize: 100 }, body);
AdminApp.doFacilities = EditorApp.doFacilities = body => FacilitiesHandler.filter({ pageSize: 100 }, body);
AdminApp.doFacilitate = EditorApp.doFacilitate = body => FacilitateHandler.filter({ statusId: 'o', createdAtFrom: Template.weekAgo(), pageSize: 100 }, body);
AdminApp.doExperiences = body => ExperiencesHandler.filter({ pageSize: 100 }, body);
AdminApp.doLogs = body => LogsHandler.filter({ pageSize: 100 }, body);
AdminApp.doSessions = body => SessionsHandler.filter({ pageSize: 100 }, body);
AdminApp.doAdmins = body => AdminsHandler.init(body);
AdminApp.doCustomers = body => CustomersHandler.init(body);
AdminApp.doConfig = body => ConfigurationHandler.init(body);
AdminApp.doHibernate = body => HibernateHandler.init(body);
AdminApp.doHeapDump = body => HeapDumpHandler.init(body);

AdminApp.doQueueStats = body => QueuesHandler.init(body);

AdminApp.onPostInit = EditorApp.onPostInit = function(c) {
	this.loadLists([ 'conditions', 'crowdsourceStatuses', 'experiences', 'exposures', 'facilityTypes', 'healthWorkerStatuses', 'originators', 'peopleStatuses', 'sexes', 'statures', 'symptoms', 'testCriteria', 'testTypes', 'timezones', 'visibilities' ]);
}

var UPLOAD_SOURCES_INSTRUCTIONS = 'Add comma separated text that is split by type, name, and code in that order.<br /><br />Types:<blockquote>';

function toCreator(value, property, me) { return property + ' @ ' + me.toDateTime(value.createdAt); }
function toUpdater(value, property, me) { return property + ' @ ' + me.toDateTime(value.updatedAt); }

// Global function to load People type-ahead list.
function fillFacilitiesDropdownList(c)
{
	Template.get('facilities', { name: c.field.value }, function(data) { c.caller.fill(c, data); });
}

function fillPeopleDropdownList(c)
{
	Template.get('peoples', { name: c.field.value }, function(data) { c.caller.fill(c, data); });
}

var AdminsHandler = new ListTemplate({
	NAME: 'admin',
	SINGULAR: 'Admin',
	PLURAL: 'Admins',
	RESOURCE: 'admins',

	CAN_ADD: true,
	CAN_EDIT: true,
	CAN_REMOVE: true,
	EDIT_METHOD: 'put',

	onEditorPostLoad: function(c) {
		if (c.value.id) c.form.elements['id'].disabled = true;	// CanNOT update the ID. Must remove and re-add with different ID. DLS on 1/2/2019.
	},

	COLUMNS: [ new TextColumn('id', 'ID', undefined, true),
	           new EditColumn('email', 'Email'),
	           new EditColumn('firstName', 'First Name'),
	           new EditColumn('lastName', 'Last Name'),
	           new TextColumn('supers', 'Super?'),
	           new TextColumn('editor', 'Editor?'),
	           new TextColumn('alertable', 'Alert?'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],
	FIELDS: [ new EditField('id', 'ID', true, false, 128, 50),
	          new PassField('password', 'Password', true, 40, 40),
	          new EditField('email', 'Email', true, false, 128, 50),
	          new EditField('firstName', 'First Name', true, false, 32, 50),
	          new EditField('lastName', 'Last Name', true, false, 32, 50),
	          new EditField('phone', 'Phone', false, false, 32, 20),
	          new BoolField('supers', 'Super?', false),
	          new BoolField('editor', 'Editor?', false),
	          new BoolField('alertable', 'Alertable?', false),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],
	SEARCH: {
		NAME: 'admin',
		SINGULAR: 'Admin',
		PLURAL: 'Admins',
		RESOURCE: 'admins',

		FIELDS: [ new EditField('id', 'ID', false, false, 128, 50),
		          new EditField('email', 'Email', false, false, 128, 50),
		          new EditField('firstName', 'First Name', false, false, 32, 50),
		          new EditField('lastName', 'Last Name', false, false, 32, 50),
		          new EditField('phone', 'Phone', false, false, 32, 20),
		          new ListField('supers', 'Super?', false, 'yesNoOptions', undefined, 'No Search'),
		          new ListField('editor', 'Editor?', false, 'yesNoOptions', undefined, 'No Search'),
		          new ListField('alertable', 'Alertable?', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('createdAt', 'Created At'),
		          new DatesField('updatedAt', 'Updated At'),
		          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	}
});

var CustomersHandler = new ListTemplate({
	NAME: 'customer',
	SINGULAR: 'Customer',
	PLURAL: 'Customers',
	RESOURCE: 'customers',

	CAN_ADD: true,
	CAN_EDIT: true,
	CAN_REMOVE: true,
	EDIT_METHOD: 'put',

	onEditorPostLoad: function(c) {
		if (c.value.id) c.form.elements['id'].disabled = true;	// CanNOT update the ID. Must remove and re-add with different ID. DLS on 1/2/2019.
	},

	COLUMNS: [ new TextColumn('id', 'ID', undefined, true),
	           new EditColumn('name', 'Name'),
	           new EditColumn('limit', 'Limit'),
	           new TextColumn('active', 'Active?'),
	           new TextColumn('lastAccessedAt', 'Last Accessed At', 'toDateTime'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],
	FIELDS: [ new TextField('id', 'ID'),
	          new EditField('name', 'Name', true, false, 128, 50),
	          new EditField('limit', 'Limit', true, false, 10, 5, 'Indicates the maximum number of calls per second. Zero indicates no limit.'),
	          new BoolField('active', 'Active?', false),
	          new TextField('lastAccessedAt', 'Last Accessed At', 'toDateTime'),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],
	SEARCH: {
		NAME: 'customer',
		SINGULAR: 'Customer',
		PLURAL: 'Customers',
		RESOURCE: 'customers',

		FIELDS: [ new EditField('id', 'ID', false, false, 40, 40),
		          new EditField('name', 'Name', false, false, 128, 50),
		          new ListField('hasLimit', 'Has Limit?', false, 'yesNoOptions', undefined, 'No Search'),
		          new EditField('limit', 'Limit', false, false, 10, 5),
		          new RangeField('limit', 'Limit', false, 10, 5),
		          new ListField('active', 'Active?', false, 'yesNoOptions', undefined, 'No Search'),
		          new ListField('hasLastAccessedAt', 'Has Accessed?', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('lastAccessedAt', 'Last Accessed At'),
		          new DatesField('createdAt', 'Created At'),
		          new DatesField('updatedAt', 'Updated At'),
		          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	}
});

var ExperiencesHandler = new ListTemplate({
	NAME: 'experience',
	SINGULAR: 'Experience',
	PLURAL: 'Experiences',
	RESOURCE: 'experiences',

	CAN_EDIT: true,
	CAN_REMOVE: true,
	EDIT_METHOD: 'put',

	openPerson: (c, e) => PeopleHandler.EDITOR.doEdit(e.myRecord.personId),
	openFacility: (c, e) => FacilitiesHandler.EDITOR.doEdit(e.myRecord.facilityId),

	COLUMNS: [ new IdColumn('id', 'ID', true),
	           new TextColumn('personName', 'Person', undefined, false, false, 'openPerson'),
	           new TextColumn('facilityName', 'Facility', undefined, false, false, 'openFacility'),
	           new TextColumn('positive', 'Positive?'),
	           new TextColumn('tags', 'Tags', 'toNames'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],

	FIELDS: [ new IdField('id', 'ID'),
	          new DropField('personId', 'Person', true, fillPeopleDropdownList, 'personName'),
	          new TextField('personName', '', undefined, undefined, true),
	          new DropField('facilityId', 'Facility', true, fillFacilitiesDropdownList, 'facilityName'),
	          new TextField('facilityName', '', undefined, undefined, true),
	          new BoolField('positive', 'Positive?', true),
	          new TagField('tags', 'Tags', false, 'types/experiences'),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],

	SEARCH: {
		NAME: 'experience',
		SINGULAR: 'Experience',
		PLURAL: 'Experiences',
		RESOURCE: 'experiences',

		FIELDS: [ new EditField('id', 'ID', false, false, 20, 10),
		          new DropField('personId', 'Person', false, fillPeopleDropdownList, 'personName'),
		          new TextField('personName', '', undefined, undefined, true),
		          new DropField('facilityId', 'Facility', false, fillFacilitiesDropdownList, 'facilityName'),
		          new TextField('facilityName', '', undefined, undefined, true),
		          new ListField('positive', 'Positive?', false, 'yesNoOptions', undefined, 'No Search'),
		          new TagField('includeTags', 'Include Tags', false, 'types/experiences'),
		          new TagField('excludeTags', 'Exclude Tags', false, 'types/experiences'),
		          new DatesField('createdAt', 'Created At', 'toDateTime'),
		          new DatesField('updatedAt', 'Updated At', 'toDateTime') ]
	}
});

var FacilitiesHandler = new ListTemplate({
	NAME: 'facility',
	SINGULAR: 'Facility',
	PLURAL: 'Facilities',
	RESOURCE: 'facilities',

	CAN_ADD: true,
	CAN_EDIT: true,
	CAN_REMOVE: true,
	EDIT_METHOD: 'put',

	ACTIONS: [ new RowAction('geocode', 'Geocode'),
	           new RowAction('review', 'Review') ],

	ROW_ACTIONS: [ new RowAction('openChangeRequests', 'Change Requests'),
	               new RowAction('openExperiences', 'Experiences'),
	               new RowAction('calcRatings', 'Calc Ratings'),
	               new RowAction('openPatients', 'Patients'),
	               new RowAction('release', 'Release', 'lockedBy') ],

	geocode: function(c, e) { this.GEOCODE.open(); },
	review: function(c, e) { FacilitiesReviewer.open(); },
	openChangeRequests: (c, e) => FacilitateHandler.filter({ entityId: e.myRecord.id }, undefined, { entityId: true }),
	openExperiences: (c, e) => ExperiencesHandler.filter({ facilityId: e.myRecord.id }, undefined, { facilityName: true }),
	calcRatings: function(c, e) { this.CALC_RATINGS.open(e.myRecord.id); },
	openPatients: (c, e) => PatientsHandler.filter({ facilityId: e.myRecord.id }, undefined, { facilityName: true }),
	release: function(c, e) {
		this.remove('facilities', e.myRecord.id + '/lock', o => window.alert(o.message ? o.message : 'Released successfully.'));
	},

	onListPostLoad: c => c.defaultValue = { active: FACILITY_ACTIVE_DEFAULT },
	onEditorPostLoad: function(c) {
		var me = this;
		var f = c.form;
		f.address.onchange = function(ev) {
			me.get('maps/geocode', { location: this.value }, function(data) {
				if (data.message)
				{
					window.alert(data.message);
					return;
				}

				if (!f.city.value && data.city) f.city.value = data.city;
				if (!f.state.value && data.state) f.state.value = data.state;
				if (!f.postalCode.value && data.postalCode) f.postalCode.value = data.postalCode;
				if (!f.latitude.value && data.latitude) f.latitude.value = data.latitude;
				if (!f.longitude.value && data.longitude) f.longitude.value = data.longitude;

				me.onCoordChange(f);
			});
		};

		f.latitude.onchange = f.longitude.onchange = function(ev) { me.onCoordChange(f); };
	},

	onCoordChange: function(f) {
		var lat = f.latitude.value;
		var lng = f.longitude.value;

		if (!lat || !lng) return;

		this.get('maps/block', { latitude: lat, longitude: lng }, function(data) {
			if (data.message)
			{
				window.alert(data.message);
				return;
			}

			var o = data.County;
			if (o)
			{
				if (o.FIPS) f.countyId.value = o.FIPS;
				if (o.name) f.countyName.value = o.name;
			}
		});
	},

	COLUMNS: [ new IdColumn('id', 'ID', true),
	           new EditColumn('name', 'Name'),
	           new EditColumn('city', 'City'),
	           new EditColumn('state', 'State'),
	           new EditColumn('phone', 'Phone'),
	           new EditColumn('email', 'Email'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],
	FIELDS: [ new IdField('id', 'ID'),
	          new EditField('name', 'Name', true, false, 128, 50),
	          new EditField('address', 'Address', true, false, 128, 50),
	          new EditField('city', 'City', true, false, 128, 50),
	          new EditField('state', 'State', true, false, 128, 50),
	          new EditField('postalCode', 'Postal Code', true, false, 16, 16),
	          new EditField('latitude', 'Latitude', true, false, 9, 9),
	          new EditField('longitude', 'Longitude', true, false, 10, 10),
	          new EditField('countyId', 'County ID', false, false, 5, 5),
	          new EditField('countyName', 'County Name', false, false, 128, 50),
	          new EditField('phone', 'Phone Number', false, false, 32, 20),
	          new EditField('appointmentPhone', 'Appointment Phone', false, false, 32, 20),
	          new EditField('email', 'Email Address', false, false, 128, 50),
	          new EditField('url', 'URL', false, false, 128, 50),
	          new EditField('appointmentUrl', 'Appointment URL', false, false, 128, 50),
	          new EditField('hours', 'Hours of Operation', false, true, 60, 5),
	          new ListField('typeId', 'Type', false, 'facilityTypes', undefined, 'None'),
	          new TagField('testTypes', 'Test Types', false, 'types/testTypes'),
	          new BoolField('driveThru', 'Has Drive-thru?', true),
	          new ListField('appointmentRequired', 'Appointment Required?', false, 'yesNoOptions', undefined, 'None'),
	          new ListField('acceptsThirdParty', 'Accepts Third Party?', false, 'yesNoOptions', undefined, 'None'),
	          new BoolField('referralRequired', 'Referral Required?', true),
	          new ListField('testCriteriaId', 'Testing Criteria', false, 'testCriteria', undefined, 'None'),
	          new EditField('otherTestCriteria', 'Other Test Criteria', false, true, 60, 5),
	          new EditField('testsPerDay', 'Tests per Day', false, false, 10, 10),
	          new BoolField('governmentIdRequired', 'Government Identification Required', true),
	          new EditField('minimumAge', 'Minimum Age', false, false, 3, 3),
	          new EditField('doctorReferralCriteria', 'Doctor Referral Criteria', false, true, 60, 5),
	          new BoolField('firstResponderFriendly', 'First Responder Friendly?', true),
	          new BoolField('telescreeningAvailable', 'Telescreening Available?', true),
	          new BoolField('acceptsInsurance', 'Accepts Insurance?', true),
	          new EditField('insuranceProvidersAccepted', 'Insurance Providers Accepted', false, true, 60, 5),
	          new BoolField('freeOrLowCost', 'Free or Low-Cost?', true),
	          new BoolField('canDonatePlasma', 'Can Donate Plasma?', true, 'Indicates that the facility accepts plasma donations.'),
	          new BoolField('resultNotificationEnabled', 'Result Notification Enabled?', true, 'Indicates that the facility has contracted to have their results sent directly to their patients via SMS, email, or push notification.'),
	          new EditField('notes', 'Notes', false, true, 60, 5),
	          new EditField('reviewedAt', 'Reviewed At', true, false, 24, 26, 'Example: 2020-11-11T16:45:00-0400', 'yyyy-mm-ddThh:mm:ss-0000'),
	          new EditField('reviewedBy', 'Reviewed By', false, false, 128, 50, 'The user who last reviewed the Facility.'),
	          new EditField('lockedTill', 'Locked Till', false, false, 24, 26, 'Example: 2020-11-11T16:45:30-0400', 'yyyy-mm-ddThh:mm:ss-0000'),
	          new EditField('lockedBy', 'Locked By', false, false, 128, 50, 'The user who currently holds a lock on the Facility.'),
	          new BoolField('active', 'Active?', true),
	          new TextField('activatedAt', 'Activated At', 'toDateTime'),
	          new TagField('people', 'Associates', false, fillPeopleDropdownList, undefined, "Represents the associates of a facility who can act on a facility's behalf."),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],
	SEARCH: {
		NAME: 'facility',
		SINGULAR: 'Facility',
		PLURAL: 'Facilities',
		RESOURCE: 'facilities',

		onEditorPreSubmit: c => {
			var v = c.value;

			if (((v.fromLatitude && v.fromLongitude) || v.fromLocation) && (v.fromMiles || v.fromKm))
				c.value.from = { latitude: v.fromLatitude, longitude: v.fromLongitude, location: v.fromLocation, miles: v.fromMiles, km: v.fromKm };
		},

		FIELDS: [ new EditField('id', 'ID', false, false, 20, 10),
		      new EditField('name', 'Name', false, false, 128, 50),
		      new EditField('fromLatitude', 'FROM Latitude', false, false, 9, 9),
		      new EditField('fromLongitude', 'FROM Longitude', false, false, 10, 10),
		      new EditField('fromLocation', 'FROM Location', false, false, 128, 50),
		      new EditField('fromMiles', 'FROM Miles', false, false, 5, 5),
		      new EditField('fromKm', 'FROM km', false, false, 5, 5),
		      new EditField('address', 'Address', false, false, 128, 50),
		      new EditField('city', 'City', false, false, 128, 50),
		      new EditField('state', 'State', false, false, 128, 50),
		      new EditField('postalCode', 'Postal Code', false, false, 16, 16),
		      new ListField('hasPostalCode', 'Has Postal Code', false, 'yesNoOptions', undefined, 'No Search'),
		      new RangeField('latitude', 'Latitude', false, 9, 9),
		      new RangeField('longitude', 'Longitude', false, 10, 10),
		      new EditField('countyId', 'County ID', false, false, 5, 5),
		      new ListField('hasCountyId', 'Has County ID', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('countyName', 'County Name', false, false, 128, 50),
		      new ListField('hasCountyName', 'Has County Name', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('phone', 'Phone Number', false, false, 32, 20),
		      new ListField('hasPhone', 'Has Phone Number', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('appointmentPhone', 'Appointment Phone', false, false, 32, 20),
		      new ListField('hasAppointmentPhone', 'Has Appointment Phone', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('email', 'Email Address', false, false, 128, 50),
		      new ListField('hasEmail', 'Has Email Address', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('url', 'URL', false, false, 128, 50),
		      new ListField('hasUrl', 'Has URL', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('appointmentUrl', 'Appointment URL', false, false, 128, 50),
		      new ListField('hasAppointmentUrl', 'Has Appointment URL', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('hours', 'Hours of Operation', false, false, 60, 5),
		      new ListField('hasHours', 'Has Hours', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('typeId', 'Type', false, 'facilityTypes', undefined, 'No Search'),
		      new TagField('includeTestTypes', 'Include Test Types', false, 'types/testTypes'),
		      new TagField('excludeTestTypes', 'Exclude Test Types', false, 'types/testTypes'),
		      new ListField('hasTypeId', 'Has Type', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('driveThru', 'Has Drive-thru?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('appointmentRequired', 'Appointment Required?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('hasAppointmentRequired', 'Has Appointment Required', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('acceptsThirdParty', 'Accepts Third Party?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('hasAcceptsThirdParty', 'Has Accepts Third Party', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('referralRequired', 'Referral Required?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('testCriteriaId', 'Testing Criteria', false, 'testCriteria', undefined, 'No Search'),
		      new ListField('notTestCriteriaId', 'Exclude Testing Criteria', false, 'testCriteria', undefined, 'No Search'),
		      new ListField('hasTestCriteriaId', 'Has Test Criteria', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('otherTestCriteria', 'Other Test Criteria', false, false, 60, 5),
		      new ListField('hasOtherTestCriteria', 'Has Other Test Criteria', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('testsPerDay', 'Tests per Day', false, false, 10, 10),
		      new ListField('hasTestsPerDay', 'Has Tests per Day', false, 'yesNoOptions', undefined, 'No Search'),
		      new RangeField('testsPerDay', 'Tests per Day', false, 10, 10),
		      new ListField('governmentIdRequired', 'Government Identification Required', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('minimumAge', 'Minimum Age', false, false, 3, 3),
		      new RangeField('minimumAge', 'Minimum Age', false, 3, 3),
		      new ListField('hasMinimumAge', 'Has Minimum Age', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('doctorReferralCriteria', 'Doctor Referral Criteria', false, false, 60, 5),
		      new ListField('hasDoctorReferralCriteria', 'Has Doctor Referral Criteria', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('firstResponderFriendly', 'First Responder Friendly?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('telescreeningAvailable', 'Telescreening Available?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('acceptsInsurance', 'Accepts Insurance?', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('insuranceProvidersAccepted', 'Insurance Providers Accepted', false, false, 128, 50),
		      new ListField('hasInsuranceProvidersAccepted', 'Has Insurance Providers Accepted', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('freeOrLowCost', 'Free or Low-Cost?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('canDonatePlasma', 'Can Donate Plasma?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('resultNotificationEnabled', 'Result Notification Enabled?', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('notes', 'Notes', false, false, 128, 50),
		      new ListField('hasNotes', 'Has Notes', false, 'yesNoOptions', undefined, 'No Search'),
		      new DatesField('reviewedAt', 'Reviewed At'),
		      new EditField('reviewedBy', 'Reviewed By', false, false, 128, 50),
		      new ListField('hasReviewedBy', 'Has Reviewed By', false, 'yesNoOptions', undefined, 'No Search'),
		      new DatesField('lockedTill', 'Locked Till'),
		      new ListField('hasLockedTill', 'Has Locked Till', false, 'yesNoOptions', undefined, 'No Search'),
		      new EditField('lockedBy', 'Locked By', false, false, 128, 50),
		      new ListField('hasLockedBy', 'Has Locked By', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('active', 'Active?', false, 'yesNoOptions', undefined, 'No Search'),
		      new ListField('hasActivatedAt', 'Has Activated At', false, 'yesNoOptions', undefined, 'No Search'),
		      new DatesField('activatedAt', 'Activated At'),
		      new TagField('people', 'Associates', false, fillPeopleDropdownList),
		      new DatesField('createdAt', 'Created At'),
		      new DatesField('updatedAt', 'Updated At'),
	          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ],
	},

	CALC_RATINGS: new EditTemplate({
		NAME: 'facility',
		SINGULAR: 'Calculated Rating',
		PLURAL: 'Calculated Rating',
		RESOURCE: 'experiences/calc',

		CAPTION_SUBMIT: undefined,
		CAPTION_CANCEL: Template.prototype.CAPTION_CLOSE,

		open: function(facilityId) { this.run({ url: this.RESOURCE + '?facilityId=' + facilityId, filter: { isAdd: false } }, undefined, 'get'); },

		toValueAndPercent: function(v, t, l) {
			var o = this.toNumber(v) + ' (' + this.toPercent(v / t) + '%)';
			if (l) o+= (' / ' + this.toDateTime(l));

			return o;
		},
		onEditorPreGenerate: function(c) {
			var v = c.value;
			v.tags_ = {};
			if (v.total)
			{
				v.positives_ = this.toValueAndPercent(v.positives, v.total);
				v.negatives_ = this.toValueAndPercent(v.negatives, v.total);

				for (k in v.tags)
				{
					var o = v.tags[k];
					v.tags_[o.name] = this.toValueAndPercent(o.count, v.total, o.last);
				}
			}
			else
			{
				v.positives_ = 0;
				v.negatives_ = 0;
			}
		},

		FIELDS: [ new TextField('total', 'Total'),
		          new TextField('positives_', 'Positives'),
		          new TextField('negatives_', 'Negatives'),
		          new MetaField('tags_', 'Tags') ]
	}),

	GEOCODE: new EditTemplate({
		NAME: 'facility',
		SINGULAR: 'Geocode',
		PLURAL: 'Geocode',
		RESOURCE: 'facilities',

		CAPTION_SUBMIT: 'Run',
		CAPTION_CANCEL: Template.prototype.CAPTION_CLOSE,

		getTitle: c => 'Geocode Facilities',

		open: function(facilityId) { this.run({ value: { status: 'Waiting', total: 0, currentId: 0, log: 'Click Run to start.' }, filter: { isAdd: false } }, undefined, 'get'); },

		handleSubmit: function(c, f) {
			var me = this;
			var l = f.log;
			var v = c.value;
			var t = c.texts['total'];
			var s = c.texts['status'];
			var cur = c.texts['currentId'];

			s.innerHTML = 'Running ...';

			this.post('facilities/search', { idFrom: v.currentId + 1, hasPostalCode: false, sortOn: 'id', sortDir: 'ASC', pageSize: 250 }, data => {
				if (!data.records)
				{
					s.innerHTML = 'DONE';
				}
				else
				{
					var count = 0;
					var expected = data.records.length;
					var lastId = data.records[expected - 1].id;
					data.records.forEach(rec => {
						if (rec.address)
						{
							me.get('maps/geocode', { location: rec.address }, b => {
								if (b.postalCode)
								{
									count++;
									rec.postalCode = b.postalCode;

									me.put('facilities', rec, d => {
										if (lastId == rec.id)
										{
											l.value = l.value + '\nGeocoded ' + count + ' / ' + expected + ', IDs: ' + data.records[0].id + ' - ' + v.currentId;
											me.handleSubmit(c, f);
										}
									});	// Run the next one when on the last record.
								}
								else if (lastId == rec.id)	// Run the next one when on the last record.
								{
									l.value = l.value + '\nGeocoded ' + count + ' / ' + expected + ', IDs: ' + data.records[0].id + ' - ' + v.currentId;
									me.handleSubmit(c, f);
								}
							});
						}
						/*
						if (rec.latitude && rec.longitude)
						{
							me.get('maps/block', { latitude: rec.latitude, longitude: rec.longitude }, b => {
								if (b.County)
								{
									count++;
									if (b.County.FIPS) rec.countyId = b.County.FIPS;
									if (b.County.name) rec.countyName = b.County.name;

									me.put('facilities', rec, d => {
										if (lastId == rec.id)
										{
											l.value = l.value + '\nGeocoded ' + count + ' / ' + expected + ', IDs: ' + data.records[0].id + ' - ' + v.currentId;
											me.handleSubmit(c, f);
										}
									});	// Run the next one when on the last record.
								}
								else if (lastId == rec.id)	// Run the next one when on the last record.
								{
									l.value = l.value + '\nGeocoded ' + count + ' / ' + expected + ', IDs: ' + data.records[0].id + ' - ' + v.currentId;
									me.handleSubmit(c, f);
								}
							});
						}
						*/
						else if (lastId == rec.id)	// Run the next one when on the last record.
						{
							l.value = l.value + '\nGeocoded ' + count + ' / ' + expected + ', IDs: ' + data.records[0].id + ' - ' + v.currentId;
							me.handleSubmit(c, f);
						}

						t.innerHTML = me.toText(v.total++);
						cur.innerHTML = me.toText(v.currentId = rec.id);
					});

					l.value = l.value + '\nGeocoding ' + expected + ', IDs: ' + data.records[0].id + ' - ' + v.currentId;
				}
			});
		},

		FIELDS: [ new TextField('status', 'Status'),
		          new TextField('total', 'Total'),
		          new TextField('currentId', 'Current ID'),
		          new EditField('log', 'Log', false, true, 60, 30) ]
	}),

	HISTORY: {
		NAME: 'facility',
		SINGULAR: 'Facility',
		PLURAL: 'Facilities',
		RESOURCE: 'facilities'
	}
});

var FacilitiesReviewer = new EditTemplate({
	NAME: 'facility',
	SINGULAR: 'Review',
	PLURAL: 'Reviews',
	RESOURCE: 'facilities/review',

	EDIT_METHOD: 'put',
	CAPTION_CANCEL: 'Release',

	open: function() { this.run({ url: 'facilities/lock', filter: { isAdd: false } }, undefined, 'get'); },

	// Release lock before closing.
	handleCancel: function(c) {
		this.remove('facilities', c.value.id + '/lock', data => c.body.closeMe());
	},

	onEditorPostLoad: function(c) {
		var v = c.value;
		var f = c.form;

		f.elements['name'].parentElement.appendChild(this.createLink('&rarr;', 'https://www.google.com?query=' + v.name, 'nextToFormElement', '_new'));

		if (v.url)
			f.elements['url'].parentElement.appendChild(this.createLink('&rarr;', v.url, 'nextToFormElement', '_new'));
	},

	FIELDS: FacilitiesHandler.FIELDS.map(v => {
		if (['reviewedBy', 'lockedBy'].includes(v.id))
			return new TextField(v.id, v.caption);
		else if (['reviewedAt', 'lockedTill'].includes(v.id))
			return new TextField(v.id, v.caption, 'toDateTime');

		return v;
	})
});

var FacilitateHandler = new ListTemplate({
	NAME: 'facilitate',
	SINGULAR: 'Change Request',
	PLURAL: 'Change Requests',
	RESOURCE: 'facilitates',

	CAN_ADD: false,
	CAN_EDIT: true,
	CAN_REMOVE: true,

	EDIT_METHOD: 'post',

	CAPTION_SUBMIT: 'Promote',
	CAPTION_CANCEL: 'Close',

	ROW_ACTIONS: [ new RowAction('promote', 'Promote', undefined, 'promotedAt'),
	               new RowAction('reject', 'Reject', undefined, 'rejectedAt') ],

	getTitle: c => 'Promote/Reject ' + (c.value.change ? 'Change' : 'Create') + ' Request',
	onListPostLoad: c => $('input[type=checkbox]', c.body).attr('disabled', true),
	onEditorPostLoad: function(c) {
		var id = c.value.id;
		c.submitUrl = this.RESOURCE + '/' + id + '/promote';

		var me = this;
		var a = c.actions;
		this.addSpace(a);
		a.appendChild(this.genButton('rejecter', 'Reject', function(ev) {
			me.remove(me.RESOURCE, id + '/reject', data => me.processResponse(data, c, c.form));
		}));

		var f = c.form;
		f.address.onchange = function(ev) {	// ALLCLEAR-614: when the address changes, populate the Facility payload with the geocoded data. DLS on 6/11/2020.
			var field = this.value;
			me.get('maps/geocode', { location: field }, function(data) {
				if (data.message)
				{
					window.alert(data.message);
					return;
				}

				var v = c.value.value;
				v.address = field;
				if (data.city) v.city = data.city;
				if (data.state) v.state = data.state;
				if (data.postalCode) v.postalCode = data.postalCode;
				if (data.latitude) v.latitude = data.latitude;
				if (data.longitude) v.longitude = data.longitude;
				if (v.latitude && v.longitude)
				{
					me.get('maps/block', { latitude: v.latitude, longitude: v.longitude }, function(block) {
						if (block.message)
						{
							window.alert(block.message);
							return;
						}

						var o = block.County;
						if (o)
						{
							if (o.FIPS) v.countyId = o.FIPS;
							if (o.name) v.countyName = o.name;
						}

						f.value_.value = Template.toJSON(v);	// Do NOT update 'c.value.value_'. The diff on submittal with f.value_ will trigger sending this payload. DLS on 6/11/2020.
					});
				}
				else
					f.value_.value = Template.toJSON(v);	// Do NOT update 'c.value.value_'. The diff on submittal with f.value_ will trigger sending this payload. DLS on 6/11/2020.
			});
		};
	},
	onEditorPreGenerate: c => {
		var v = c.value;
		v.address = v.value.address;
		v.value_ = Template.toJSON(v.value);
	},
	openEntity: function(c, e) { FacilitiesHandler.EDITOR.doEdit(e.myRecord.entityId); },
	openCreator: function(c, e) { PeopleHandler.EDITOR.doEdit(e.myRecord.creatorId); },
	promote: function(c, e) {
		var id = e.myRecord.id;
		this.post(this.RESOURCE + '/' + id + '/promote', undefined, data => window.alert(data.message ? data.message : 'Promoted ' + id + ' successfully.'));
	},
	reject: function(c, e) {
		var id = e.myRecord.id;
		this.remove(this.RESOURCE, id + '/reject', data => window.alert(data.message ? data.message : 'Rejected ' + id + ' successfully.'));
	},
	handleSubmit: function(c, f) {
		var me = this;
		var v = f.value_.value;
		var value_ = c.value.value_;
		v = (v && (v != value_)) ? this.fromJSON(v) : null;

		this.post(c.submitUrl, v, data => me.processResponse(data, c, f));
	},

	COLUMNS: [ new TextColumn('status', 'Status', 'toName'),
	           new TextColumn('originator', 'Originator', 'toName'),
	           new TextColumn('gotTested', 'Got Tested?'),
	           new TextColumn('change', 'Change?'),
	           new IdColumn('entityId', 'Facility ID', false, false, 'openEntity'),
	           new TextColumn('creatorId', 'Creator', undefined, false, false, 'openCreator'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime', true),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],

	FIELDS: [ new TextField('id', 'ID'),
	          new TextField('location', 'Location'),
	          new TextField('gotTested', 'Got Tested?'),
	          new TextField('originator', 'Originator', (v, p) => p.name),
	          new TextField('status', 'Status', (v, p) => p.name),
	          new TextField('change', 'Change Request?'),
	          new LinkField('entityId', 'Facility ID', function(ev) { FacilitiesHandler.EDITOR.doEdit(this.myValue.entityId); }),
	          new EditField('address', 'Address', false, false, 128, 50, 'Provide value to be geocoded and applied to the Facility JSON.'),
	          new EditField('value_', 'Facility JSON', true, true, 80, 10),
	          new TextField('promoterId', 'Promoter'),
	          new TextField('promotedAt', 'Promoted At', 'toDateTime'),
	          new TextField('rejecterId', 'Rejecter'),
	          new TextField('rejectedAt', 'Rejected At', 'toDateTime'),
	          new LinkField('creatorId', 'Creator', function(ev) { PeopleHandler.EDITOR.doEdit(this.myValue.creatorId); }),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],

	SEARCH: {
		NAME: 'facilitate',
		SINGULAR: 'Change Request',
		PLURAL: 'Change Requests',
		RESOURCE: 'facilitates',

		FIELDS: [ new EditField('location', 'Location', false, false, 128, 50),
		          new ListField('gotTested', 'Got Tested?', false, 'yesNoOptions', undefined, 'No Search'),
		          new ListField('originatorId', 'Originator', false, 'originators', undefined, 'No Search'),
		          new ListField('statusId', 'Status', false, 'crowdsourceStatuses', undefined, 'No Search'),
		          new ListField('change', 'Change Request?', false, 'yesNoOptions', undefined, 'No Search'),
		          new EditField('entityId', 'Facility ID', false, false, 19, 10),
		          new EditField('promoterId', 'Promoter', false, false, 128, 50),
		          new DatesField('promotedAt', 'Promoted At'),
		          new EditField('rejecterId', 'Rejecter', false, false, 128, 50),
		          new DatesField('rejectedAt', 'Rejected At'),
		          new DropField('creatorId', 'Creator', false, fillPeopleDropdownList, 'creatorName'),
		          new TextField('creatorName', '', undefined, undefined, true),
		          new DatesField('createdAt', 'Created At'),
		          new DatesField('updatedAt', 'Updated At'),
		          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	}
});

var PatientsHandler = new ListTemplate({
	NAME: 'patient',
	SINGULAR: 'Patient',
	PLURAL: 'Patients',
	RESOURCE: 'patients',

	CAN_ADD: true,
	CAN_EDIT: true,
	CAN_REMOVE: true,
	EDIT_METHOD: 'put',

	COLUMNS: [ new IdColumn('id', 'ID', true),
	           new TextColumn('facilityName', 'Facility'),
	           new TextColumn('personName', 'Person'),
	           new TextColumn('alertable', 'Alertable?'),
	           new TextColumn('enrolledAt', 'Enrolled At', 'toDateTime'),
	           new TextColumn('rejectedAt', 'Rejected At', 'toDateTime'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],

	FIELDS: [ new IdField('id', 'ID'),
	          new DropField('facilityId', 'Facility', true, 'facilities', 'facilityName'),
	          new TextField('facilityName', '', undefined, undefined, true),
	          new DropField('personId', 'Person', true, fillPeopleDropdownList, 'personName'),
	          new TextField('personName', '', undefined, undefined, true),
	          new BoolField('alertable', 'Alertable?', false),
	          new EditField('enrolledAt', 'Enrolled At', false, false, 24, 26, 'Example: 2020-08-03T21:00:00-0400', 'yyyy-mm-ddThh:mm:ss-0000'),
	          new EditField('rejectedAt', 'Rejected At', false, false, 24, 26, 'Example: 2020-08-03T21:00:00-0400', 'yyyy-mm-ddThh:mm:ss-0000'),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],

	SEARCH: {
		NAME: 'patient',
		SINGULAR: 'Patient',
		PLURAL: 'Patients',
		RESOURCE: 'patients',

		FIELDS: [ new EditField('id', 'ID', false, false, 19, 10),
		          new DropField('facilityId', 'Facility', false, 'facilities', 'facilityName'),
		          new TextField('facilityName', '', undefined, undefined, true),
		          new DropField('personId', 'Person', false, fillPeopleDropdownList, 'personName'),
		          new TextField('personName', '', undefined, undefined, true),
		          new ListField('alertable', 'Alertable?', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('enrolledAt', 'Enrolled At'),
		          new ListField('hasEnrolledAt', 'Has Enrolled At', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('rejectedAt', 'Rejected At'),
		          new ListField('hasRejectedAt', 'Has Rejected At', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('createdAt', 'Created At'),
		          new DatesField('updatedAt', 'Updated At')]
	}
});

var PeopleHandler = new ListTemplate({
	NAME: 'people',
	SINGULAR: 'Person',
	PLURAL: 'People',
	RESOURCE: 'peoples',

	CAN_ADD: true,
	CAN_EDIT: true,
	CAN_REMOVE: true,
	EDIT_METHOD: 'put',

	ROW_ACTIONS: [ new RowAction('authenticate', 'Authenticate'),
	               new RowAction('openSymptomsLogs', 'Symptoms Logs'),
	               new RowAction('alert', 'Alert'),
	               new RowAction('openFriendRequests', 'Friend Requests'),
	               new RowAction('openInvitees', 'Invitees'),
	               new RowAction('openFriendships', 'Friendships'),
	               new RowAction('openFields', 'Fields'),
	               new RowAction('openExperiences', 'Experiences') ],

	alert: function(c, e) {
		var r = e.myRecord;
		this.post(this.RESOURCE + '/' + r.id + '/alert', null, function(data) {
			if (data && data.message) window.alert(data.message);

			window.alert('The New Facility Alert has been sent to ' + r.phone + '.');
		});
	},
	authenticate: function(c, e) {
		this.post(this.RESOURCE + '/' + e.myRecord.id + '/auth', null, function(data) {
			if (data.message) window.alert(data.message);
			else SessionsHandler.EDITOR.doValue(data);
		});
	},
	openFields: function(c, e) { this.FIELDS_.open(e.myRecord.id); },
	openInvitees: function(c, e) { this.FRIENDS.filter({ inviteeId: e.myRecord.id }); },
	openFriendRequests: function(c, e) { this.FRIENDS.filter({ personId: e.myRecord.id }); },
	openFriendships: function(c, e) { PeopleHandler.filter({ friendshipId: e.myRecord.id, pageSize: 100 }); },
	openSymptomsLogs: function(c, e) { this.SYMPTOMS_LOG.filter({ personId: e.myRecord.id, pageSize: 100 }); },
	openExperiences: (c, e) => ExperiencesHandler.filter({ personId: e.myRecord.id }, undefined, { personName: true }),

	COLUMNS: [ new IdColumn('id', 'ID', true),
	           new EditColumn('name', 'Name'),
	           new EditColumn('phone', 'Phone'),
	           new EditColumn('email', 'Email'),
	           new EditColumn('firstName', 'First Name'),
	           new EditColumn('lastName', 'Last Name'),
	           new TextColumn('active', 'Active?'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],

	FIELDS: [ new IdField('id', 'ID'),
	          new EditField('name', 'Name', true, false, 64, 50),
	          new EditField('phone', 'Phone', true, false, 32, 15),
	          new EditField('email', 'Email', false, false, 128, 50),
	          new EditField('firstName', 'First Name', false, false, 32, 15),
	          new EditField('lastName', 'Last Name', false, false, 32, 15),
	          new DateField('dob', 'Date of Birth', false),
	          new ListField('statusId', 'Status', false, 'peopleStatuses', undefined, 'None'),
	          new ListField('statureId', 'Stature', false, 'statures', undefined, 'None'),
	          new ListField('sexId', 'Sex', false, 'sexes', undefined, 'None'),
	          new ListField('healthWorkerStatusId', 'Health Worker Status', true, 'healthWorkerStatuses'),
	          new EditField('latitude', 'Latitude', false, false, 12, 12),
	          new EditField('longitude', 'Longitude', false, false, 12, 12),
	          new EditField('locationName', 'Location Name', false, false, 255, 50),
	          new TagField('conditions', 'Conditions', false, 'types/conditions'),
	          new TagField('exposures', 'Exposures', false, 'types/exposures'),
	          new TagField('symptoms', 'Symptoms', false, 'types/symptoms'),
	          new TagField('facilities', 'Facilities', false, 'facilities', undefined, 'Favorited facilities by the person'),
	          new TagField('associations', 'Associations', false, 'facilities', undefined, 'Represents the facilities with which a person is associated usually as a worker.'),
	          new BoolField('alertable', 'Is Alertable?', true),
	          new BoolField('active', 'Is Active?', true),
	          new TextField('authAt', 'Last Auth At', 'toDateTime'),
	          new TextField('phoneVerifiedAt', 'Phone Verified At', 'toDateTime'),
	          new TextField('emailVerifiedAt', 'Email Verified At', 'toDateTime'),
	          new TextField('alertedOf', '# of Facilities  Alerted'),
	          new TextField('alertedAt', 'Last Alerted At', 'toDateTime'),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],

	SEARCH: {
		NAME: 'people',
		SINGULAR: 'Person',
		PLURAL: 'People',
		RESOURCE: 'peoples',

		FIELDS: [ new TextField('id', 'ID'),
		          new EditField('name', 'Name', false, false, 64, 50, 'Starts-with search on user name'),
		          new EditField('nameX', 'Name - fuzzy', false, false, 64, 50, 'Fuzzy search on user name'),
		          new EditField('phone', 'Phone', false, false, 32, 15),
		          new EditField('email', 'Email', false, false, 128, 50),
		          new EditField('firstName', 'First Name', false, false, 32, 15),
		          new EditField('lastName', 'Last Name', false, false, 32, 15),
		          new DateField('dob', 'Date of Birth', false),
		          new DatesField('dob', 'Date of Birth', false),
		          new ListField('statusId', 'Status', false, 'peopleStatuses', undefined, 'No Search'),
		          new ListField('statureId', 'Stature', false, 'statures', undefined, 'No Search'),
		          new ListField('sexId', 'Sex', false, 'sexes', undefined, 'No Search'),
		          new ListField('healthWorkerStatusId', 'Health Worker Status', false, 'healthWorkerStatuses', undefined, 'No Search'),
		          new ListField('timezoneId', 'Timezone', false, 'timezones', undefined, 'No Search'),
		          new EditField('latitude', 'Latitude', false, false, 12, 12),
		          new EditField('longitude', 'Longitude', false, false, 12, 12),
		          new EditField('locationName', 'Location Name', false, false, 255, 50),
		          new ListField('alertable', 'Is Alertable?', false, 'yesNoOptions', undefined, 'No Search'),
		          new ListField('active', 'Is Active?', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('authAt', 'Last Auth At', false),
		          new DatesField('phoneVerifiedAt', 'Phone Verified At', false),
		          new DatesField('emailVerifiedAt', 'Email Verified At', false),
		          new ListField('hasAlertedAt', 'Has Been Alerted?', false, 'yesNoOptions', undefined, 'No Search'),
		          new EditField('alertedOf', '# of Facilities  Alerted', false, false, 5, 5),
		          new RangeField('alertedOf', '# of Facilities  Alerted', false, 5, 5),
		          new DatesField('alertedAt', 'Last Alerted At', false),
		          new DatesField('createdAt', 'Created At', false),
		          new DatesField('updatedAt', 'Updated At', false),
		          new DropField('friendId', 'Friend Requester', false, fillPeopleDropdownList, 'friendName'),
		          new TextField('friendName', '', undefined, undefined, true),
		          new DropField('inviteeId', 'Invitee', false, fillPeopleDropdownList, 'inviteeName'),
		          new TextField('inviteeName', '', undefined, undefined, true),
		          new DropField('friendshipId', 'Friendship', false, fillPeopleDropdownList, 'friendshipName'),
		          new TextField('friendshipName', '', undefined, undefined, true),
		          new TagField('includeConditions', 'Include Conditions', false, 'types/conditions'),
		          new TagField('excludeConditions', 'Exclude Conditions', false, 'types/conditions'),
		          new TagField('includeExposures', 'Include Exposures', false, 'types/exposures'),
		          new TagField('excludeExposures', 'Exclude Exposures', false, 'types/exposures'),
		          new TagField('includeSymptoms', 'Include Symptoms', false, 'types/symptoms'),
		          new TagField('excludeSymptoms', 'Exclude Symptoms', false, 'types/symptoms'),
		          new ListField('hasTakenTest', 'Has Taken Test?', false, 'yesNoOptions', undefined, 'No Search'),
		          new ListField('hasPositiveTest', 'Has Positive Test?', false, 'yesNoOptions', undefined, 'No Search'),
		          new ListField('hasFacilities', 'Has Facilities?', false, 'yesNoOptions', undefined, 'No Search'),
		          new TagField('includeFacilities', 'Include Facilities', false, 'facilities'),
		          new TagField('excludeFacilities', 'Exclude Facilities', false, 'facilities'),
		          new ListField('hasAssociations', 'Has Associations?', false, 'yesNoOptions', undefined, 'No Search'),
		          new TagField('includeAssociations', 'Include Associations', false, 'facilities'),
		          new TagField('excludeAssociations', 'Exclude Associations', false, 'facilities'),
		          new ListField('visibilityHealthWorkerStatusId', 'Visibility Health Worker Status', false, 'visibilities', undefined, 'No Search'),
		          new ListField('visibilityConditions', 'Visibility Conditions', false, 'visibilities', undefined, 'No Search'),
		          new ListField('visibilityExposures', 'Visibility Exposures', false, 'visibilities', undefined, 'No Search'),
		          new ListField('visibilitySymptoms', 'Visibility Symptoms', false, 'visibilities', undefined, 'No Search'),
		          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	},

	FIELDS_: new EditTemplate({
		NAME: 'people',
		SINGULAR: 'Person Field',
		PLURAL: 'Person Fields',
		RESOURCE: 'peoples/fields',

		EDIT_METHOD: 'put',

		open: function(id) {
			var me = this;
			this.get(`peoples/${id}/fields`, null, data => me.doValue(data));
		},

		FIELDS: [ new TextField('id', 'ID'),
		          new TextField('name', 'Name'),
		          new ListField('visibilityHealthWorkerStatusId', 'Visibility Health Worker Status', true, 'visibilities'),
		          new ListField('visibilityConditions', 'Visibility Conditions', true, 'visibilities'),
		          new ListField('visibilityExposures', 'Visibility Exposures', true, 'visibilities'),
		          new ListField('visibilitySymptoms', 'Visibility Symptoms', true, 'visibilities') ]
	}),

	FRIENDS: new ListTemplate({
		NAME: 'friend',
		SINGULAR: 'Friend Request',
		PLURAL: 'Friend Requests',
		RESOURCE: 'friends',

		CAN_ADD: true,
		CAN_EDIT: true,
		CAN_REMOVE: true,

		openOwner: (c, e) => { PeopleHandler.EDITOR.doEdit(e.myRecord.personId); },
		openInvitee: (c, e) => { PeopleHandler.EDITOR.doEdit(e.myRecord.inviteeId); },

		COLUMNS: [ new TextColumn('id', 'ID', undefined, true),
		           new TextColumn('personName', 'Owner', undefined, false, false, 'openOwner'),
		           new TextColumn('inviteeName', 'Invitee', undefined, false, false, 'openInvitee'),
		           new TextColumn('acceptedAt', 'Accepted At', 'toDateTime'),
		           new TextColumn('rejectedAt', 'Rejected At', 'toDateTime'),
		           new TextColumn('createdAt', 'Created At', 'toDateTime') ],
		FIELDS: [ new DropField('personId', 'Owner', true, fillPeopleDropdownList, 'personName'),
		          new TextField('personName', '', undefined, undefined, true),
		          new DropField('inviteeId', 'Invitee', true, fillPeopleDropdownList, 'inviteeName'),
		          new TextField('inviteeName', '', undefined, undefined, true),
		          new TextField('acceptedAt', 'Accepted At', 'toDateTime'),
		          new TextField('rejectedAt', 'Rejected At', 'toDateTime'),
		          new TextField('createdAt', 'Created At', 'toDateTime') ],
		SEARCH: {
			NAME: 'friend',
			SINGULAR: 'Friend Request',
			PLURAL: 'Friend Requests',
			RESOURCE: 'friends',

			FIELDS: [ new DropField('personId', 'Owner', false, fillPeopleDropdownList, 'personName'),
			          new TextField('personName', '', undefined, undefined, true),
			          new DropField('inviteeId', 'Invitee', true, fillPeopleDropdownList, 'inviteeName'),
			          new TextField('inviteeName', '', undefined, undefined, true),
			          new ListField('hasAcceptedAt', 'Has Been Accepted', false, 'yesNoOptions', undefined, 'No Search'),
			          new DatesField('acceptedAt', 'Accepted At', false),
			          new ListField('hasRejectedAt', 'Has Been Rejected', false, 'yesNoOptions', undefined, 'No Search'),
			          new DatesField('rejectedAt', 'Rejected At', false),
			          new DatesField('createdAt', 'Created At', false) ]
		}
	}),

	SYMPTOMS_LOG: new ListTemplate({
		NAME: 'symptomsLog',
		SINGULAR: 'Symptoms Log',
		PLURAL: 'Symptoms Logs',
		RESOURCE: 'symptomsLogs',

		CAN_ADD: false,
		CAN_EDIT: false,
		CAN_REMOVE: false,

		toName: v => v.name,

		COLUMNS: [ new TextColumn('symptom', 'Symptom', 'toName'),
		           new TextColumn('startedAt', 'Started At', 'toDateTime'),
		           new TextColumn('endedAt', 'Ended At', 'toDateTime') ],

		SEARCH: {
			NAME: 'symptomsLog',
			SINGULAR: 'Symptoms Log',
			PLURAL: 'Symptoms Logs',
			RESOURCE: 'symptomsLogs',

			FIELDS: [ new HideField('personId'),
			          new ListField('symptomId', 'Symptom', false, 'symptoms', undefined, 'No Search'),
			          new DatesField('startedAt', 'Started At'),
			          new ListField('hasEndedAt', 'Has Ended At', undefined, 'yesNoOptions', undefined, 'No Search'),
			          new DatesField('endedAt', 'Ended At') ]
		}
	})
});

var RegistrationsHandler = new ListTemplate({
	NAME: 'registration',
	SINGULAR: 'Registration Request',
	PLURAL: 'Registration Requests',
	RESOURCE: 'registrations',

	CAN_ADD: false,
	CAN_EDIT: false,
	CAN_REMOVE: true,
	IDENTIFIER: 'key',

	COLUMNS: [ new TextColumn('key', 'Key'),
	          new TextColumn('phone', 'Phone'),
	          new TextColumn('beenTested', 'Been Tested'),
	          new TextColumn('haveSymptoms', 'Have Symptoms'),
	          new TextColumn('ttl', 'TTL', 'fromSeconds') ],

	FIELDS: [ new TextField('key', 'Key'),
	          new TextField('phone', 'Phone'),
	          new TextField('beenTested', 'Been Tested'),
	          new TextField('haveSymptoms', 'Have Symptoms'),
	          new TextField('ttl', 'TTL', 'fromSeconds') ],

	SEARCH: {
		NAME: 'registration',
		SINGULAR: 'Registration Request',
		PLURAL: 'Registration Requests',
		RESOURCE: 'registrations',

		FIELDS: [ new EditField('phone', 'Phone', false, false, 32, 15),
		          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	}
});

var SessionsHandler = new ListTemplate({
	NAME: 'session',
	SINGULAR: 'Session',
	PLURAL: 'Sessions',
	RESOURCE: 'sessions',

	CAN_ADD: false,
	CAN_EDIT: false,
	CAN_REMOVE: true,

	openSession: (c, e) => {
		var v = e.myRecord;
		if (v.admin) AdminsHandler.EDITOR.doEdit(v.admin.id);
		else if (v.person) PeopleHandler.EDITOR.doEdit(v.person.id);
		else RegistrationsHandler.EDITOR.doValue(v.registration);
	},
	toSessionName: (p, v) => (v.admin ? v.admin.id : ((v.person) ? v.person.name : v.registration.phone)),

	COLUMNS: [ new TextColumn('id', 'ID'),
	           new TextColumn('rememberMe', 'Remember Me?'),
	           new TextColumn('duration', 'Duration', 'fromMilliseconds'),
	           new TextColumn('type', 'Type'),
	           new TextColumn('name', 'Name', undefined, false, false, 'openSession'),
	           new TextColumn('expiresAt', 'Exipres At', 'toDateTime'),
	           new TextColumn('lastAccessedAt', 'Last Accessed At', 'toDateTime'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime') ],

	FIELDS: [ new TextField('id', 'ID'),
	          new TextField('rememberMe', 'Remember Me?'),
	          new TextField('duration', 'Duration', 'fromMilliseconds'),
	          new TextField('type', 'Type'),
	          new TextField('name', 'Name'),
	          new TextField('expiresAt', 'Exipres At', 'toDateTime'),
	          new TextField('lastAccessedAt', 'Last Accessed At', 'toDateTime'),
	          new TextField('createdAt', 'Created At', 'toDateTime') ],

	SEARCH: {
		NAME: 'session',
		SINGULAR: 'Session',
		PLURAL: 'Sessions',
		RESOURCE: 'sessions',

		FIELDS: [ new EditField('id', 'ID', false, false, 128, 50) ]
	}
});

var TestsHandler = new ListTemplate({
	NAME: 'tests',
	SINGULAR: 'Test',
	PLURAL: 'Tests',
	RESOURCE: 'tests',

	CAN_ADD: true,
	CAN_EDIT: true,
	CAN_REMOVE: true,
	EDIT_METHOD: 'put',

	openPerson: (c, e) => { PeopleHandler.EDITOR.doEdit(e.myRecord.personId); },
	openFacility: (c, e) => { FacilitiesHandler.EDITOR.doEdit(e.myRecord.facilityId); },

	COLUMNS: [ new IdColumn('id', 'ID', true),
	           new TextColumn('personName', 'Person', undefined, false, false, 'openPerson'),
	           new TextColumn('type', 'Type', 'toName'),
	           new TextColumn('takenOn', 'Taken On', 'toDate'),
	           new TextColumn('facilityName', 'Facility', undefined, false, false, 'openFacility'),
	           new TextColumn('positive', 'Positive?'),
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],
	FIELDS: [ new IdField('id', 'ID'),
	          new DropField('personId', 'Person', true, fillPeopleDropdownList, 'personName'),
	          new TextField('personName', '', undefined, undefined, true),
	          new ListField('typeId', 'Type', true, 'testTypes'),
	          new DateField('takenOn', 'Taken on', true),
	          new DropField('facilityId', 'Facility', true, fillFacilitiesDropdownList, 'facilityName'),
	          new TextField('facilityName', '', undefined, undefined, true),
	          new EditField('remoteId', 'Remote ID', false, false, 64, 50, "Represents an identifier in the facility's system of record."),
	          new BoolField('positive', 'Positive?', true),
	          new EditField('notes', 'Notes', false, true, 60, 5),
	          new EditField('receivedAt', 'Received At', false, false, 24, 26, 'Example: 2020-07-14T21:00:00-0400', 'yyyy-mm-ddThh:mm:ss-0000'),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],
	SEARCH: {
		NAME: 'tests',
		SINGULAR: 'Test',
		PLURAL: 'Tests',
		RESOURCE: 'tests',

		FIELDS: [ new EditField('id', 'ID', false, false, 19, 10),
	          new DropField('personId', 'Person', false, fillPeopleDropdownList, 'personName'),
	          new TextField('personName', '', undefined, undefined, true),
	          new ListField('typeId', 'Type', false, 'testTypes', undefined, 'No Search'),
	          new DateField('takenOn', 'Taken on', false),
	          new DatesField('takenOn', 'Taken on', false),
	          new DropField('facilityId', 'Facility', false, fillFacilitiesDropdownList, 'facilityName'),
	          new TextField('facilityName', '', undefined, undefined, true),
	          new ListField('hasRemoteId', 'Has Remote ID?', false, 'yesNoOptions', undefined, 'No Search'),
	          new EditField('remoteId', 'Remote ID', false, false, 64, 50),
	          new ListField('positive', 'Positive?', false, 'yesNoOptions', undefined, 'No Search'),
	          new EditField('notes', 'Notes', false, false, 255, 50),
	          new ListField('hasReceivedAt', 'Has Received At?', false, 'yesNoOptions', undefined, 'No Search'),
	          new DatesField('receivedAt', 'Received At', false),
	          new DatesField('createdAt', 'Created At', false),
	          new DatesField('updatedAt', 'Updated At', false),
	          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	}
});

function parseConnectionString(value)
{
	var o = {};
	value.split(';').map(v => {
		var i = v.indexOf('=');
		if (1 > i) return null;
		return [ v.substring(0, i), v.substring(i + 1) ];
	}).filter(v => (null != v) && ('AccountKey' != v[0]))
		.forEach(v => o[v[0]] = v[1]);

	return o;
}

var ConfigurationHandler = new EditTemplate({
	NAME: 'config',
	SINGULAR: 'Configuration',
	PLURAL: 'Configurations',
	RESOURCE: 'info',

	init: function(body) { this.doEdit('config', undefined, body); },

	onEditorPreGenerate: c => {
		c.value.queue = parseConnectionString(c.value.queue);
		c.value.admins = parseConnectionString(c.value.admins);
	},

	FIELDS: [ new TextField('env', 'Environment'),
	          new TextField('version', 'Version'),
	          new TextField('baseUrl', 'Base URL'),
	          new TextField('adminUrl', 'Admin URL'),
	          new TextField('registrationPhone', 'Registration Sid/Phone', (v, p) => p ? p : v.registrationSid),
	          new TextField('authPhone', 'Authentication Sid/Phone', (v, p) => p ? p : v.authSid),
	          new TextField('alertPhone', 'Alert Sid/Phone', (v, p) => p ? p : v.alertSid),
	          new TextField('registrationSMSMessage', 'Registration SMS Message'),
	          new TextField('authSMSMessage', 'Authentication SMS Message'),
	          new TextField('alertSMSMessage', 'Alert SMS Message'),
	          new TextField('admins', 'Cosmos Table Account', (v, p) => p.AccountName),
	          new TextField('admins', 'Cosmos Table Endpoint', (v, p) => p.TableEndpoint),
	          new TextField('queue', 'Queue Table Account', (v, p) => p.AccountName),
	          new TextField('queue', 'Queue Endpoint Suffix', (v, p) => p.EndpointSuffix),
	          new TextField('geocode', 'Geocode Redis Host', (v, p) => p.host),
	          new TextField('session', 'Session Redis Host', (v, p) => p.host),
	          new TextField('twilio', 'Twilio', (v, p) => p.baseUrl),
	          new TextField('trans', 'mySQL URL', (v, p) => p.url),
	          new TextField('read', 'mySQL-ro URL', (v, p) => p.url) ],

	CAPTION_SUBMIT: undefined,
	CAPTION_CANCEL: undefined,
});

var HeapDumpHandler = new EditTemplate({
	NAME: 'heapDump',
	SINGULAR: 'Heap Dump',
	PLURAL: 'Heap Dumps',
	RESOURCE: 'heap/histogram',

	getTitle: function(c) { return this.SINGULAR; },

	init: function(body) {
		var me = this;
		this.text(this.RESOURCE, null, function(data) {
			me.run({ value: { dump: data }, filter: { isAdd: false }}, body);
		});
	},

	handleSubmit: function(c) {
		this.dump('admin/tasks/gc', null, function(data) { window.alert(data); })
	},

	CAPTION_SUBMIT: 'Run GC',
	CAPTION_CANCEL: undefined,

	FIELDS: [ new EditField('dump', 'Dump', false, true, 100, 40) ]
});

var HibernateHandler = new EditTemplate({
	NAME: 'hibernate',
	SINGULAR: 'Hibernate',
	PLURAL: 'Hibernate',
	RESOURCE: 'info/hibernate',

	init: function(body) { this.doEdit('stats', undefined, body); },
	handleSubmit: function(c) {
		Template.remove(this.RESOURCE, 'cache', function(data) { window.alert('Cleared ' + data.count + ' Hibernate cache elements.'); });
	},

	FIELDS: [ new TextField('startTime', 'Start Time', 'toDateTimeN'),
	          new TextField('sessionOpenCount', 'Session Open Count'),
	          new TextField('sessionCloseCount', 'Session Close Count'),
	          new TextField('flushCount', 'Flush Count'),
	          new TextField('connectCount', 'Connect Count'),
	          new TextField('prepareStatementCount', 'Prepared Statement Count'),
	          new TextField('closeStatementCount', 'Close Statement Count'),
	          new TextField('entityLoadCount', 'Entity Load Count'),
	          new TextField('entityUpdateCount', 'Entity Update Count'),
	          new TextField('entityInsertCount', 'Entity Insert Count'),
	          new TextField('entityDeleteCount', 'Entity Delete Count'),
	          new TextField('entityFetchCount', 'Entity Fetch Count'),
	          new TextField('collectionLoadCount', 'Collection Load Count'),
	          new TextField('collectionUpdateCount', 'Collection Update Count'),
	          new TextField('collectionRemoveCount', 'Collection Remove Count'),
	          new TextField('collectionRecreateCount', 'Collection Recreate Count'),
	          new TextField('collectionFetchCount', 'Collection Fetch Count'),
	          new TextField('secondLevelCacheHitCount', 'Second Level Cache Hit Count'),
	          new TextField('secondLevelCacheMissCount', 'Second Level Cache Miss Count'),
	          new TextField('secondLevelCachePutCount', 'Second Level Cache Put Count'),
	          new TextField('naturalIdCacheHitCount', 'Natural ID Cache Hit Count'),
	          new TextField('naturalIdCacheMissCount', 'Natural ID Cache Miss Count'),
	          new TextField('naturalIdCachePutCount', 'Natural ID Cache Put Count'),
	          new TextField('naturalIdQueryExecutionCount', 'Natural ID Query Execution Count'),
	          new TextField('naturalIdQueryExecutionMaxTime', 'Natural ID Query Execution Max Time'),
	          new TextField('queryExecutionCount', 'Query Execution Count'),
	          new TextField('queryExecutionMaxTime', 'Query Execution Max Time'),
	          new EditField('queryExecutionMaxTimeQueryString', 'Query Execution Max Time Query String', false, true, 100, 5),
	          new TextField('queryCacheHitCount', 'Query Cache Hit Count'),
	          new TextField('queryCacheMissCount', 'Query Cache Miss Count'),
	          new TextField('queryCachePutCount', 'Query Cache Put Count'),
	          new TextField('updateTimestampsCacheHitCount', 'Update Timestamps Cache Hit Count'),
	          new TextField('updateTimestampsCacheMissCount', 'Update Timestamps Cache Miss Count'),
	          new TextField('updateTimestampsCachePutCount', 'Update Timestamps Cache Put Count'),
	          new TextField('transactionCount', 'Transaction Count'),
	          new TextField('optimisticFailureCount', 'Optimistic Failure Count'),
	          new TextField('statisticsEnabled', 'Statistics Enabled'),
	          new TextField('successfulTransactionCount', 'Successful Transaction Count'),
	          new MultiField('entityNames', 'Entity Names', false, 60, 10),
	          new MultiField('queries', 'Queries', false, 100, 10),
	          new MultiField('collectionRoleNames', 'Collection Role Names', false, 60, 10),
	          new MultiField('secondLevelCacheRegionNames', 'Second Level Cache Region Names', false, 60, 10) ],

	CAPTION_SUBMIT: 'Clear Cache',
	CAPTION_CANCEL: undefined
});

var LogsHandler = new ListTemplate({
	NAME: 'log',
	SINGULAR: 'Log',
	PLURAL: 'Logs',
	RESOURCE: 'logs',

	CAN_EDIT: true,

	COLUMNS: [ new TextColumn('name', 'Name', undefined, true), new TextColumn('level', 'Level') ],

	FIELDS: [ new TextField('name', 'Name'),
	          new ListField('level', 'Level', true, 'logLevels', 'Change the log level.') ],

	SEARCH: {
		NAME: 'log',
		SINGULAR: 'Log',
		PLURAL: 'Logs',
		RESOURCE: 'logs',

		FIELDS: [ new EditField('name', 'Name', false, false, 255, 60),
		          new ListField('level', 'Level', false, 'logLevels', undefined, 'No Search'),
		          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	}
});

var QueuesHandler = new ListTemplate({
	NAME: 'queueStats',
	SINGULAR: 'Queue Stats',
	PLURAL: 'Queue Stats',
	RESOURCE: 'queues/stats',

	CAN_ADD: false,
	CAN_EDIT: false,
	CAN_REMOVE: false,

	/*
	ROW_ACTIONS: [ new RowAction('viewQueue', 'View',),
	               new RowAction('viewDLQ', 'View DLQ', 'dlqSize'),
	               new RowAction('reQueue', 'Re-Queue', 'dlqSize') ],

	viewQueue: function(c, e) { queues(e.myRecord.name.substring(9), false).filter({ pageSize: 100 }); },
	viewDLQ: function(c, e) { queues(e.myRecord.name.substring(9), true).filter({ pageSize: 100 }); },

	reQueue: function(c, e) {
		var me = this;
		var n = e.myRecord.name.substring(9);	// Strip out the leading 'platform:'.
		this.post('queues/' + n + '/dlq', null, function(data) {
			if (data.message)
				window.alert(data.message);
			else
				me.init(c.body);
		});
	},
	*/

	COLUMNS: [ new TextColumn('name', 'Name'),
	           new TextColumn('queueSize', 'Queue Size'),
	           // new TextColumn('dlqSize', 'DLQ Size'),
	           new TextColumn('successes', '# Successes'),
	           new TextColumn('skips', '# of Skips'),
	           new TextColumn('errors', '# of Errors') ]
});

var QUEUES = {};
var QUEUE_CAPTIONS = { 'waiting': { singular: 'Wait', plural: 'Wait' } };

function queues(name, dlq)
{
	var n = dlq ? name + '/dlq' : name;
	var o = QUEUES[n];
	if (null != o) return o;

	var c = queueCaptions(name);	// Use non-DLQ name to get captions.
	return o = QUEUES[n] = createQueueTemplate(n, c.singular, c.plural);
}

function queueCaptions(name)
{
	var o = QUEUE_CAPTIONS[name];
	return (null != o) ? o : { singular: name, plural: name };
}

/** Creates a ListTemplate for the specified queue. */
function createQueueTemplate(name, singular, plural)
{
	return new ListTemplate({
		NAME: 'queue_' + name,
		SINGULAR: singular,
		PLURAL: plural,
		RESOURCE: 'queues/' + name,

		CAN_EDIT: true,
		CAN_REMOVE: true,
		EDIT_METHOD: 'put',

		ACTIONS: [ new RowAction('runAll', 'Run All'), new RowAction('doClear', 'Clear'), new RowAction('doDLQ', 'DLQ') ],
		ROW_ACTIONS: [ new RowAction('runOne', 'Run') ],
		doClear: function(c, e) {
			if (!window.confirm('Click OK to confirm and continue with your clearing of all queue items.'))
				return;

			var me = this;
			this.remove(this.RESOURCE, undefined, function(value) {
				if (value.message)
					window.alert(value.message);
				else
					me.run(c);
			});
		},
		runAll: function(c, e) {
			var me = this;
			this.post(this.RESOURCE, undefined, function(value) {
				if (value.message)
					window.alert(value.message);
				else
					me.run(c);
			});
		},
		doDLQ: function(c, e) {
			var me = this;
			this.post(this.RESOURCE + '/dlq', undefined, function(value) {
				if (value.message)
					window.alert(value.message);
				else
					me.run(c);
			});
		},
		runOne: function(c, e) {
			var me = this;
			this.post(this.RESOURCE + '/' + e.myRecord.id, undefined, function(value) {
				if (value.error)
					window.alert(value.error);
				else
					me.run(c);
			});
		},

		COLUMNS: [ new IdColumn('id', 'ID', true),
		           new TextColumn('tries', '# of Tries'),
		           new TextColumn('nextRunAt', 'Next Run At', 'toDateTimeN'),
		           new TextColumn('value', 'Payload', 'toPayload') ],

		toPayload: function(v) {
			var o = this.toJSON(v);
			if (250 >= o.length)
				return o;

			return o.substring(0, 250);
		},

		FIELDS: [ new IdField('id', 'ID'),
		          new TextField('tries', 'Number of Tries'),
		          new TextField('nextRunAt', 'Next Run At', 'toDateTimeN'),
		          new EditField('value', 'Payload', false, true, 60, 20) ],

		onEditorPostLoad: function(c) {
			c.form.elements['value'].value = this.toJSON(c.value.value);
		},

		SEARCH: {
			NAME: 'queue_' + name,
			SINGULAR: singular,
			PLURAL: plural,
			RESOURCE: 'queues/' + name,

			FIELDS: [ new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
		}
	});
}
