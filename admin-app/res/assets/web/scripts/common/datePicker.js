var DatePicker = {};

DatePicker.CSS_MAIN = 'datePicker';
DatePicker.SUFFIX = '_date';
DatePicker.DAYS = ['Su', 'M', 'Tu', 'W', 'Th', 'F', 'Sa'];
DatePicker.MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

DatePicker.create = function(name, value, extra, callback)
{
	var h, f, e, o = document.createElement('span');
	var criteria = { name: name, value: value, closed: true, caller: this, extra: extra, callback: callback };
	o.appendChild(h = criteria.hidden = this.genInput(name, 'hidden'));

	o.appendChild(f = criteria.field = this.genInput(name + this.SUFFIX, 'text'));
	o.appendChild(e = criteria.div = document.createElement('div'));	// MUST be after the text box.
	o.className = f.className = e.className = this.CSS_MAIN;
	f.setAttribute('autocomplete', 'off');

	// DO full UNDEFINED test since it could be ZERO.
	if (undefined != value)
	{
		h.value = value;
		f.value = this.toInputDate(value);
	}

	var me = this;
	f.onchange = function(ev) {
		var v = this.value;
		if ('' == v)
		{
			h.value = '';
			criteria.value = undefined;
			return true;
		}

		try
		{
			me.select(criteria, me.parseInputDate(this.value));
		}
		catch (ex) { window.alert(ex); return false; }

		return true;
	};

	f.onfocus = function(ev) { me.open(criteria); };
	f.onblur = function(ev) { me.scheduleClose(criteria); };

	return o;
}

/** Opens the calendar.
 * @param c the current session criteria.
 * @param d a supplied date. Overrides the current value.
 */
DatePicker.open = function(c, d)
{
	// Cancel any closes that may be going on. For example flipping through months
	// causes the text box to lose focus which schedules a close.
	this.cancelClose(c);

	var me = this;
	var e, l, u, o = c.div;

	if (!d)
	{
		if (undefined != c.value)
			d = this.parseDbDate(c.value);
		else
			d = new Date();
	}

	// Create date from POSIX integer.
	var day, date = 1;
	var m = d.getMonth();	// Get the month. Stop building the calendar once the month is different.
	d.setDate(date);	// Set to first day of the month.
	d.clone = function() { return new Date(this.getTime()); };

	// Clear out before starting.
	o.innerHTML = '';

	// Add the month/year header.
	o.appendChild(u = document.createElement('ul'));
	u.className = 'header';
	this.addFlippers(u, this.MONTHS[m], c, d, 'Month');
	this.addFlippers(u, d.getFullYear(), c, d, 'FullYear');

	o.appendChild(u = document.createElement('ul'));
	u.className = 'caption';
	for (var i = 0; i < this.DAYS.length; i++)
	{
		u.appendChild(l = document.createElement('li'));
		l.innerHTML = this.DAYS[i];
	}

	// Start first line of date options. Need to buffer for days before the first day of the month.
	o.appendChild(u = document.createElement('ul'));
	u.className = 'detail';
	u.appendChild(l = document.createElement('li'));
	l.className = 'buffer' + d.getDay();
	l.innerHTML = '&nbsp;';
	u.appendChild(l = document.createElement('li'));
	this.addAnchor(l, date, function(ev) {
		me.select(c, this.myValue);
		return false;
	}, d.clone());
	d.setDate(++date);

	do
	{
		// If the day is zero, it's a start of a new week.
		day = d.getDay();
		if (0 == day)
		{
			o.appendChild(u = document.createElement('ul'));
			u.className = 'detail';
		}

		u.appendChild(l = document.createElement('li'));
		this.addAnchor(l, date, function(ev) {
			me.select(c, this.myValue);
			return false;
		}, d.clone());

		d.setDate(++date);
	} while (m == d.getMonth());

	// Put a buffer at the end for the remaining days.
	u.appendChild(l = document.createElement('li'));
	l.className = 'buffer' + (6 - day);
	l.innerHTML = '&nbsp;';

	// Added footer to close it.
	o.appendChild(u = document.createElement('ul'));
	u.className = 'footer';
	u.appendChild(l = document.createElement('li'));
	this.addAnchor(l, 'Close', function(ev) { me.close(c); return false; });

	c.closed = false;
	o.style.display = 'block';
}

DatePicker.select = function(c, d)
{
	c.hidden.value = c.value = Template.toDbDate(d);
	c.field.value = this.toInputDate_(d);

	var cb = c.callback;
	if (cb && cb.doSelect)
		cb.doSelect(c, d);

	this.close(c);
}

DatePicker.close = function(c)
{
	delete c.scheduleCloseId;
	c.closed = true;
	c.div.style.display = 'none';
}

DatePicker.scheduleClose = function(c)
{
	// Is a close already scheduled?
	if (c.scheduleCloseId)
		return;

	var me = this;
	c.scheduleCloseId = setTimeout(function() { me.close(c); }, 1000);
}

DatePicker.cancelClose = function(c)
{
	// If a close is NOT scheduled, there is nothing to cancel.
	if (!c.scheduleCloseId)
		return;

	clearTimeout(c.scheduleCloseId);
	delete c.scheduleCloseId;
}

DatePicker.genInput = Template.prototype.genInput;
DatePicker.createAnchor = Template.prototype.createAnchor;
DatePicker.addAnchor = function(elem, caption, action, value)
{
	var a = this.createAnchor(caption, action);
	a.myValue = value;

	elem.appendChild(a);

	return a;
}

DatePicker.addFlipper = function(elem, caption, criteria, date, part, dir)
{
	var me = this;
	this.addAnchor(elem, caption, function(ev) {
		var d = this.myValue;
		d['set' + part](d['get' + part]() + dir);
		me.open(criteria, d);
		return false;
	}, date.clone());
}

DatePicker.addFlippers = function(elem, caption, criteria, date, part)
{
	var l = document.createElement('li');
	elem.appendChild(l);
	this.addFlipper(l, '&lt;', criteria, date, part, -1);
	l.appendChild(document.createTextNode(caption));
	this.addFlipper(l, '&gt;', criteria, date, part, 1);
}

DatePicker.toInputDate = function(v)
{
	return this.toInputDate_(this.parseDbDate(v));
}

DatePicker.toInputDate_ = function(value)
{
	return (value.getMonth() + 1) + '/' + value.getDate() + '/' + value.getFullYear();
}

DatePicker.parseInputDate = function(v)
{
	var m, d, y, f = v.split('/', 3);
	if (2 > f.length)
		throw 'Invalid input date: must contain at least 2 parts separated by a backslash.';

	if (isNaN(m = parseInt(f[0])))
		throw 'Invalid input date: the month (' + f[0] + ') is not a number';

	if ((1 > m) || (12 < m))
		throw 'Invalid input date: the month (' + f[0] + ') is not between 1 and 12.';

	if (isNaN(d = parseInt(f[1])))
		throw 'Invalid input date: the date (' + f[1] + ') is not a number';

	if ((1 > d) || (31 < d))
		throw 'Invalid input date: the date (' + f[1] + ') is not between 1 and 31.';

	var value = new Date();
	value.setHours(0, 0, 0, 0);	// Remove the time portion.
	value.setMonth(m - 1, d);

	// If only two parts to the date string, assume the current year.
	if (2 < f.length)
	{
		if (isNaN(y = parseInt(f[2])))
			throw 'Invalid input date: the year (' + f[2] + ') is not a number';

		if (0 > y)
			throw 'Invalid input date: the year (' + f[2] + ') has to be a positive number.';

		// If the number is between 0 and 49, put in the current century.
		if (50 > y)
			y+= 2000;

		// If the number is between 50 and 100, put in the last century.
		else if (100 > y)
			y+= 1900;

		value.setFullYear(y);
	}

	return value;
}

/** Need to convert the format YYYY-MM-DD to a JavaScript Date object. */
DatePicker.parseDbDate = function(v)
{
console.log(v, v.replace('+0000', 'Z'), new Date(v.replace('+0000', 'Z')));
	var v = new Date(v.replace('+0000', 'Z'));
	v.setHours(0, 0, 0, 0);
console.log(v);

	return v;
	/*
	var m, d, y, f = v.split('-', 3);
	if (3 > f.length)
		throw 'Invalid DB date: must contain 3 parts separated by a hyphen.';

	if (isNaN(y = parseInt(f[0])))
		throw 'Invalid DB date: the year (' + f[0] + ') is not a number.';

	if (isNaN(m = parseInt(this.removeZeroPadding(f[1]))))
		throw 'Invalid DB date: the month (' + f[1] + ') is not a number.';

	if (isNaN(d = parseInt(this.removeZeroPadding(f[2]))))
		throw 'Invalid DB date: the date (' + f[2] + ') is not a number.';

	return new Date(y, m - 1, d, 0, 0, 0, 0);
	*/
}

/** Remove preceding zero's used for date and month padding from the DB. Need to remove
    before converting to int. For some reason - parseInt('08') equals zero and not eight. */
DatePicker.removeZeroPadding = function(v)
{
	if ('' == v)
		return v;

	// Break after the first hit to a NON-zero digit.
	var i = 0;
	for (; i < v.length; i++)
		if ('0' != v.charAt(i))
			break;

	if ((0 < i) && (v.length > i))
		v = v.substr(i);

	return v;
}