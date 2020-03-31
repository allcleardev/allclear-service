function HistoryTemplate(properties)
{
	this.load(properties);
}

HistoryTemplate.prototype = new Template();
HistoryTemplate.prototype.BASE_URI = 'auditLogs';
HistoryTemplate.prototype.CSS_MAIN = 'history'

HistoryTemplate.prototype.open = function(id, body) { this.run({ url: this.BASE_URI + '/' + this.RESOURCE + '/' + id }, body, 'get') }
HistoryTemplate.prototype.getTitle = function(c) { return this.SINGULAR + ' Audit Log'; }

HistoryTemplate.prototype.generate = function(criteria)
{
	var me = this;
	var e, o = this.createDiv(undefined, this.CSS_MAIN);
	var v = criteria.value;
	if ((undefined == v) || (0 == v.length))
		return this.createNoRecordsFoundMessage();

	var val, entries = [];
	for (var i = 0; i < v.length; i++)
		entries[i] = { id: i, name: (val = v[i]).actionType + ' at ' + this.toDateTime(val.actionDate) + ' by ' + val.userId };

	o.appendChild(criteria.entries = e = this.genList('entries', 20, entries));
	e.selectedIndex = 0;
	e.className = this.CSS_MAIN;
	e.onchange = function(ev) { me.handleChange(criteria, this); };

	(criteria.tree = $(this.addSpan(o, undefined, this.CSS_MAIN))).jsontree(v[0].entity);

	// Wrap viewer in form to display buttons at bottom.
	var f = criteria.form = this.createElement('form', undefined, this.CSS_MAIN);
	var s = criteria.fields = this.addDiv(f, undefined, this.CSS_MAIN);
	s.appendChild(o);

	s = criteria.actions = this.addDiv(f, undefined, this.CSS_ACTIONS);
	s.appendChild(this.genButton('canceler', this.CAPTION_CLOSE,
		function(ev) { me.handleCancel(criteria); }));
	this.addSpace(s);
	s.appendChild(this.genButton('copier', this.CAPTION_COPY, function(ev) {
		me.handleCopy(criteria, criteria.entries);
	}));

	return f;
}

HistoryTemplate.prototype.handleCancel = function(criteria)
{
	if (criteria.isModal)
		criteria.body.closeMe();
}

HistoryTemplate.prototype.handleChange = function(criteria, elem)
{
	var i = elem.selectedIndex;
	if (-1 < 0)
		criteria.tree.jsontree(criteria.value[i].entity);
}

HistoryTemplate.prototype.handleCopy = function(criteria, elem)
{
	var i = elem.selectedIndex;
	if (-1 < 0)
		this.VIEWER.open(this.toJSON(criteria.value[i].entity));
}

HistoryTemplate.prototype.createNoRecordsFoundMessage = function()
{
	return this.createDiv('No ' + this.SINGULAR + ' audit log entries found.', 'noRecordsFound');
}

HistoryTemplate.prototype.VIEWER = new EditTemplate({
	NAME: 'historyViewer',

	getTitle: function(c) { return 'Copy Viewer'; },

	open: function(text) { this.run({ value: { data: text }, filter: {}}); },

	FIELDS: [ new EditField('data', 'Payload', false, true, 100, 20) ]
});