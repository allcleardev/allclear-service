var DropdownTagger = $.extend(true, {}, DropdownList);

DropdownTagger.CSS_MAIN = DropdownTagger.CSS_MAIN + ' tagger';
DropdownTagger.create_ = DropdownTagger.create;

DropdownTagger.create = function(name, callback, values, extra)
{
	var o = this.create_(name, callback, undefined, extra);
	var c = o.myCriteria;
	c.value = { selectedIds: [], selectedNames: [], removedIds: [], removedNames: [] };

	// Add the section to hold the list of values.
	c.list = Template.addElem(o, 'ul', undefined, this.CSS_MAIN);
	if (values)
		this.populate(c, values);

	// Replace with tagger selection handler. Can replace after initial create as the doSelect
	// won't call until accessed.
	var me = this;
	callback.doSelect = function(criteria) {
		me.addItem(criteria, criteria.selection);

		// Empty the text field and reset focus there.
		criteria.field.value = '';
		criteria.field.focus();

		var cb = criteria.callback;
		if (cb.doPostSelect)
			cb.doPostSelect(criteria);
	};

	return o;
}

DropdownTagger.retrieve = function(criteria)
{
	return criteria.value;
}

DropdownTagger.populate = function(criteria, values)
{
	for (var i = 0; i < values.length; i++)
		this.addItem(criteria, values[i]);
}

DropdownTagger.addItem = function(criteria, v)
{
	var rec = criteria.value;
	var a, ids = rec.selectedIds;
	if (-1 < ids.indexOf(v.id))
		return;

	Template.addElem(criteria.list, 'li', this.toCaption(v)).appendChild(a = Template.createAnchor('x', function(ev) {
		var r = this.myRecord;
		var id = r.id;
		var index = ids.indexOf(id);
		if (-1 < index)
		{
			ids.splice(index, 1);
			rec.selectedNames.splice(index, 1);
			if (-1 == (index = rec.removedIds.indexOf(id)))
			{
				rec.removedIds.push(id);
				rec.removedNames.push(r.name);
			}
			$(this.parentNode).remove();
		}
	}));
	a.myRecord = v;
	ids.push(v.id);
	rec.selectedNames.push(v.name);
}

DropdownTagger.reset = function(criteria)
{
	criteria.value = { selectedIds: [], selectedNames: [], removedIds: [], removedNames: [] };
	criteria.list.innerHTML = '';
}

/** Create drop down for media selection. */
var MediaDropdownTagger = $.extend(true, {}, DropdownTagger);
MediaDropdownTagger.toCaption_super = MediaDropdownTagger.toCaption;
MediaDropdownTagger.toCaption = function(v)
{
	return '<img src="' + thumbsPath + v.name + '" width="16" height="16" />&nbsp;' + v.name + ' (' + v.id + ')';
}
