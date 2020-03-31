var DropdownList = {};

DropdownList.CSS_MAIN = 'dropdownList';
DropdownList.CSS_HOVER = 'hover';
DropdownList.DELAY = 500;
DropdownList.SUFFIX = '_ddl';

DropdownList.create = function(name, callback, value, extra)
{
	var me = this;
	var f, e, o = document.createElement('span');
	var criteria = o.myCriteria = { name: name, callback: callback, value: value, closed: true, caller: this, extra: extra };
	o.appendChild(criteria.hidden = this.genInput(name, 'hidden'));

	// DO full UNDEFINED test since it could be ZERO.
	if (undefined != value)
		criteria.hidden.value = value;

	o.appendChild(f = criteria.field = this.genInput(name + this.SUFFIX, 'text'));
	o.appendChild(this.createAnchor('X', function(ev) { me.reset(criteria); }, this.CSS_MAIN));
	o.appendChild(e = criteria.div = document.createElement('div'));	// MUST be after the text box.
	o.className = f.className = e.className = this.CSS_MAIN;
	f.setAttribute('autocomplete', 'off');

	// DO on key up so that the value already exists of the elements VALUE property.
	// MUST do ENTER check on the keydown otherwise the FORM submit gets triggered before the keyup.
	$(f).keyup(criteria, function(ev) {
		// Delay briefly to avoid issuing too many requests. If the user keeps typing, only
		// submit a single request when finished.
		var c = ev.data;
		if (c.lastTimeoutId)
		{
			window.clearTimeout(c.lastTimeoutId);
			delete c.lastTimeoutId;
		}

		// Certain events should trigger a close.
		var k = ev.which;
		if (('' == ev.target.value) || (KEY_ESCAPE == k) || (KEY_TAB == k))
			me.close(c);
		else if (KEY_DOWN == k)
			me.moveDown(c, 1);
		else if (KEY_UP == k)
			me.moveUp(c, 1);
		else if (KEY_PAGE_DOWN == k)
			me.moveDown(c, 10);
		else if (KEY_PAGE_UP == k)
			me.moveUp(c, 10);
		else if (KEY_HOME == k)
			me.moveTo(c, 0);
		else if (KEY_END == k)
			me.moveTo(c, c.lastIndex);
		else if (KEY_ENTER != k)
		{
			c.lastTimeoutId = window.setTimeout(function() {
				delete c.lastTimeoutId;
				c.callback.doFill(c);
			}, me.DELAY);
		}
	}).keydown(criteria, function(ev) {
		var c = ev.data;

		// If opened, do selection and preventDefault so that the form submit button is not triggered.
		if ((KEY_ENTER == ev.which) && !c.closed)
		{
			me.select(c);
			ev.preventDefault();
		}
	});

	return o;
}

DropdownList.fill = function(criteria, list)
{
	var me = this;
	var a, o = criteria.div;
	o.innerHTML = '';

	criteria.anchors = [];

	for (var i = 0; i < list.length; i++)
	{
		var l = list[i];
		o.appendChild(a = criteria.anchors[i] = document.createElement('a'));
		a.href = 'javascript:void(null)';
		a.innerHTML = this.toCaption(l);
		a.myIndex = i;
		a.myItem = l;
		a.onclick = function(ev) {
			me.handleSelect(criteria, this);
		};
	}

	this.open(criteria);
	o.scrollTop = 0;

	criteria.lastIndex = list.length - 1;
	if (0 < list.length)
		criteria.anchors[criteria.index = 0].className = this.CSS_HOVER;
	else
		criteria.index = undefined;
}

DropdownList.handleSelect = function(criteria, elem)
{
	var item = criteria.selection = elem.myItem;

	criteria.index = elem.myIndex;
	criteria.hidden.value = item.id;
	criteria.field.value = item.name;

	this.close(criteria);

	var cb = criteria.callback;
	if (cb.doSelect)
		cb.doSelect(criteria);
}

DropdownList.reset = function(criteria)
{
	delete criteria.selection;
	criteria.hidden.value = '';
	criteria.field.value = '';

	var cb = criteria.callback;
	if (cb.doSelect)
		cb.doSelect(criteria);
}

DropdownList.open = function(c)
{
	if (!c.closed)
		return;

	c.closed = false;
	c.div.style.display = 'block';
}

DropdownList.close = function(c)
{
	if (c.closed)
		return;

	c.closed = true;
	c.div.style.display = 'none';
}

DropdownList.select = function(c)
{
	if (undefined == c.index)
		return;

	this.handleSelect(c, c.anchors[c.index]);
}

DropdownList.moveUp = function(c, dir)
{
	if (undefined == c.index)
		return;

	var i = c.index - dir;
	if (0 > i)
		i = 0;

	this.moveTo(c, i);
}

DropdownList.moveDown = function(c, dir)
{
	if (undefined == c.index)
		return;

	var i = c.index + dir;
	if (c.lastIndex < i)
		i = c.lastIndex;

	this.moveTo(c, i);
}

DropdownList.moveTo = function(c, index)
{
	if (undefined == c.index)
		return;

	c.anchors[c.index].className = '';
	var a, d = c.div;
	(a = c.anchors[c.index = index]).className = this.CSS_HOVER;

	// Make sure that the newly selected item appears.
	if ((a.offsetTop < d.scrollTop) ||
		((a.offsetTop + a.offsetHeight) > (d.scrollTop + d.clientHeight)))
    		d.scrollTop = a.offsetTop;

	// Make sure that when we move to, that it's open.
	this.open(c);
}

/** Set the field to empty or deselected. */
DropdownList.clear = function(form, name)
{
	var e;
	if (e = form.elements[name])
		e.value = '';
	if (e = form.elements[name + this.SUFFIX])
		e.value = '';
}

DropdownList.enable = function(form, name)
{
	var e;
	if (e = form.elements[name])
		e.setFieldStatus(true);
	if (e = form.elements[name + this.SUFFIX])
		e.disabled = false;
}

DropdownList.disable = function(form, name)
{
	var e;
	if (e = form.elements[name])
	{
		e.value = '';
		e.setFieldStatus(false);
	}
	if (e = form.elements[name + this.SUFFIX])
	{
		e.value = '';
		e.disabled = true;
	}
}

DropdownList.focus = function(form, name)
{
	var e;
	if (e = form.elements[name + this.SUFFIX])
		e.focus();
}

DropdownList.genInput = Template.prototype.genInput;
DropdownList.createAnchor = Template.prototype.createAnchor;
DropdownList.toCaption = function(v) { return v.name + ' (' + v.id + ')'; }