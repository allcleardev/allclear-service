var MetadataEditor = {
	CSS_MAIN: 'metadata'
};

MetadataEditor.create = function(name, value, extra, callback)
{
	var e, o = document.createElement('span');
	o.className = this.CSS_MAIN;
	o.appendChild(e = Template.genHidden(name, Template.toJSON(value)));
	var criteria = { name: name, value: value, field: e, elem: o, extra: extra, callback: callback };

	var i = 0;
	if (value)
		for (var id in value)
			o.appendChild(this.genRow(++i, id, value[id], criteria));

	criteria.count = i;

	// Add meta data section.
	var d = criteria.addSection = Template.addDiv(o);
	d.appendChild(Template.genTextBox(name + '_key_add', 255, 25));
	d.appendChild(Template.genTextBox(name + '_value_add', 255, 25));
	d.appendChild(e = Template.genButton(name + '_add', '+'));
	e.className = this.CSS_MAIN;
	e.onclick = function(ev) { MetadataEditor.onAdd(criteria, this); };

	return o;
}

MetadataEditor.genRow = function(i, key, value, criteria)
{
	var e, name = criteria.name, o = document.createElement('div');
	o.appendChild(e = Template.genTextBox(name + '_key_' + i, 255, 25));
	e.value = key;
	e.onchange = function(ev) { MetadataEditor.onChange(criteria, this); };
	o.appendChild(e = Template.genTextBox(name + '_value_' + i, 255, 25));
	e.value = value;
	e.onchange = function(ev) { MetadataEditor.onChange(criteria, this); };
	o.appendChild(e = Template.genButton(name + '_remove', '-'));
	e.className = this.CSS_MAIN;
	e.onclick = function(ev) { MetadataEditor.onRemove(criteria, this, o, i); };

	return o;
}

MetadataEditor.onAdd = function(c, e)
{
	// Get the key and value.
	var k, v, f = e.form, name = c.name;
	var key = (k = f.elements[name + '_key_add']).value;
	if ('' == key)
	{
		window.alert('Please supply the key.');
		k.focus();
		return;
	}
	var value = (v = f.elements[name + '_value_add']).value;
	if ('' == value)
	{
		window.alert('Please supply the value.');
		v.focus();
		return;
	}

	// Add the new section.
	c.elem.insertBefore(this.genRow(c.count+=1, key, value, c), c.addSection);

	// Update the data.
	this.onChange(c, e);

	// Reset the add section.
	k.value = '';
	v.value = '';
	k.focus();
}

MetadataEditor.onChange = function(c, e)
{
	this.onChange_(c, e.form);
}

MetadataEditor.onChange_ = function(c, f)
{
	// Loop through all the fields and rebuild the data.
	var e, data = {};
	var name = c.name;
	for (var i = 1; i <= c.count; i++)
	{
		// Removed key/value pairs will have their elements removed, too. Count stays the same, though.
		if (null == (e = f.elements[name + '_key_' + i]))
			continue;

		var key = e.value;
		var value = f.elements[name + '_value_' + i].value;

		// Exclude empty keys or values.
		if (('' == key) || ('' == value))
			continue;

		data[key] = value;
	}

	c.value = data;
	c.field.value = Template.toJSON(data);
}

MetadataEditor.onRemove = function(c, e, container, index)
{
	// Get the form before removing the element's container.
	var f = e.form;

	// Remove the key/value pair section.
	container.parentNode.removeChild(container);

	// Update the data.
	this.onChange_(c, f);
}
