var AdminApp = new TabTemplate();

AdminApp.TABS = [ { id: 'doPeople', caption: 'People' },
	{ id: 'doLogs', caption: 'Logs', children: [ { id: 'doQueueStats', caption: 'Queue Stats' } ] },
	{ id: 'doSessions', caption: 'Sessions', children: [ { id: 'doAdmins', caption: 'Admins' } ] },
	{ id: 'doConfig', caption: 'Config', children: [ { id: 'doHibernate', caption: 'Hibernate' },
	                                                 { id: 'doHeapDump', caption: 'Heap Dump' } ] } ];

AdminApp.doPeople = function(body) { PeopleHandler.filter({ pageSize: 100 }, body); }
AdminApp.doLogs = function(body) { LogsHandler.filter({ pageSize: 100 }, body); }
AdminApp.doSessions = function(body) { SessionsHandler.filter({ pageSize: 100 }, body); }
AdminApp.doAdmins = function(body) { AdminsHandler.init(body); }
AdminApp.doConfig = function(body) { ConfigurationHandler.init(body); }
AdminApp.doHibernate = function(body) { HibernateHandler.init(body); }
AdminApp.doHeapDump = function(body) { HeapDumpHandler.init(body); }

AdminApp.doQueueStats = function(body) { QueuesHandler.init(body); }

AdminApp.onPostInit = function(c) {
	this.loadLists([ 'conditions', 'exposures', 'peopleStatuses', 'peopleStatures', 'symptoms' ]);
}

var UPLOAD_SOURCES_INSTRUCTIONS = 'Add comma separated text that is split by type, name, and code in that order.<br /><br />Types:<blockquote>';

function toCreator(value, property, me) { return property + ' @ ' + me.toDateTime(value.createdAt); }
function toUpdater(value, property, me) { return property + ' @ ' + me.toDateTime(value.updatedAt); }

// Global function to load People type-ahead list.
function fillPeopleDropdownList(c)
{
	Template.get('people', { name: c.field.value }, function(data) { c.caller.fill(c, data); });
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
	           new TextColumn('createdAt', 'Created At', 'toDateTime'),
	           new TextColumn('updatedAt', 'Updated At', 'toDateTime') ],
	FIELDS: [ new EditField('id', 'ID', true, false, 128, 50),
	          new PassField('password', 'Password', true, 32, 10),
	          new EditField('email', 'Email', true, false, 128, 50),
	          new EditField('firstName', 'First Name', true, false, 32, 50),
	          new EditField('lastName', 'Last Name', true, false, 32, 50),
	          new BoolField('supers', 'Super?', false),
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
		          new ListField('supers', 'Super?', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('createdAt', 'Created At'),
		          new DatesField('updatedAt', 'Updated At') ]
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
	          new ListField('statureId', 'Stature', false, 'peopleStatures', undefined, 'None'),
	          new BoolField('active', 'Is Active?', true),
	          new TextField('authAt', 'Last Auth At', 'toDateTime'),
	          new TextField('phoneVerifiedAt', 'Phone Verified At', 'toDateTime'),
	          new TextField('emailVerifiedAt', 'Email Verified At', 'toDateTime'),
	          new TextField('createdAt', 'Created At', 'toDateTime'),
	          new TextField('updatedAt', 'Updated At', 'toDateTime') ],

	SEARCH: {
		NAME: 'people',
		SINGULAR: 'Person',
		PLURAL: 'People',
		RESOURCE: 'people',

		FIELDS: [ new TextField('id', 'ID'),
		          new EditField('name', 'Name', false, false, 64, 50),
		          new EditField('phone', 'Phone', false, false, 32, 15),
		          new EditField('email', 'Email', false, false, 128, 50),
		          new EditField('firstName', 'First Name', false, false, 32, 15),
		          new EditField('lastName', 'Last Name', false, false, 32, 15),
		          new DateField('dob', 'Date of Birth', false),
		          new DatesField('dob', 'Date of Birth', false),
		          new ListField('statusId', 'Status', false, 'peopleStatuses', undefined, 'No Search'),
		          new ListField('statureId', 'Stature', false, 'peopleStatures', undefined, 'None'),
		          new ListField('active', 'Is Active?', false, 'yesNoOptions', undefined, 'No Search'),
		          new DatesField('authAt', 'Last Auth At', false),
		          new DatesField('phoneVerifiedAt', 'Phone Verified At', false),
		          new DatesField('emailVerifiedAt', 'Email Verified At', false),
		          new DatesField('createdAt', 'Created At', false),
		          new DatesField('updatedAt', 'Updated At', false),
		          new ListField('pageSize', 'Page Size', false, 'pageSizes', 'Number of records on the page') ]
	}
});

var ConfigurationHandler = new EditTemplate({
	NAME: 'config',
	SINGULAR: 'Configuration',
	PLURAL: 'Configurations',
	RESOURCE: 'info',

	init: function(body) { this.doEdit('config', undefined, body); },

	FIELDS: [ new TextField('env', 'Environment'),
	          new TextField('version', 'Version'),
	          new TextField('baseUrl', 'Base URL'),
	          new TextField('registrationPhone', 'Registration Phone'),
	          new TextField('authenticationPhone', 'Authentication Phone'),
	          new TextField('session', 'Session Redis Host', (v, p) => p.host),
	          new TextField('twilio', 'Twilio', (v, p) => p.baseUrl),
	          new TextField('trans', 'mySQL URL', (v, p) => p.url) /* ,
	          new TextField('appES', 'App Elasticsearch User', (v, p) => p.userName),
	          new TextField('jobES', 'Job Elasticsearch Host', (v, p) => p.host),
	          new TextField('jobES', 'Job Elasticsearch Port', (v, p) => p.port),
	          new TextField('jobES', 'Job Elasticsearch SSL', (v, p) => p.ssl),
	          new TextField('task', 'Task Threads', (v, p) => p.threads) */ ],

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

	COLUMNS: [ new TextColumn('name', 'Name'),
	           new TextColumn('queueSize', 'Queue Size'),
	           new TextColumn('dlqSize', 'DLQ Size'),
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
