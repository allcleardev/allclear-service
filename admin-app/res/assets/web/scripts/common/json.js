function JSONTemplate(properties)
{
	this.load(properties);
}

JSONTemplate.prototype = new Template();

JSONTemplate.prototype.openParsed = function(id, body, excludeFields) { this.run({ url: this.RESOURCE + '/' + id + '/parsed', excludeFields: excludeFields }, body, 'get') }
JSONTemplate.prototype.getTitle = function(c) { return this.SINGULAR; }

JSONTemplate.prototype.onPostLoad = function(criteria)
{
	if (criteria.isModal)
		this.setFieldsMaxHeight(criteria);

	if (this.onEditorPostLoad)
		this.onEditorPostLoad(criteria);
}

JSONTemplate.prototype.generate = function(criteria)
{
	var v = criteria.value;
	if (undefined == v)
		return this.createNoRecordsFoundMessage();

	// Exclude fields if specified to do so.
	if (criteria.excludeFields)
		for (var id in criteria.excludeFields)
			delete v[id];

	var o = this.createElement('form');
	$(criteria.fields = this.addDiv(o, undefined, this.CSS_FIELDS)).jsontree(v);

	return o;
}

JSONTemplate.prototype.handleCancel = function(criteria)
{
	if (criteria.isModal)
		criteria.body.closeMe();
}

JSONTemplate.prototype.createNoRecordsFoundMessage = function()
{
	return this.createDiv('The ' + this.SINGULAR + ' was empty.', 'noRecordsFound');
}
