var TimePicker = {}

TimePicker.CSS_MAIN = 'timePicker';
TimePicker.SUFFIX = '_time';
TimePicker.DEFAULT_INTERVAL = 15;	// In minutes.
TimePicker.MINUTES_IN_DAY = 60 * 24;
TimePicker.VALUES = {};

TimePicker.create = function(name, value, extra, callback, interval)
{
	// Also include zero - interval should never be zero.
	if (!interval)
		interval = this.DEFAULT_INTERVAL;

	if (undefined == value)
		value = 0;

	var h, f, o = document.createElement('span');
	var criteria = { name: name, value: value, caller: this, extra: extra, callback: callback, interval: interval };
	o.appendChild(h = criteria.hidden = Template.genInput(name, 'hidden'));
	o.appendChild(f = criteria.field = Template.genSelect(name + this.SUFFIX, this.getValues(interval), this.toMinutes(value)));

	o.className = f.className = this.CSS_MAIN;

	if (undefined != value)
		h.value = new Date(value).getTime();

	f.onchange = function(ev) {
		criteria.hidden.value = criteria.value = parseInt(this.value) * 60000;
		if (callback && callback.doSelect)
			callback.doSelect(criteria);
	};

	return o;
}

/** Converts a LONG date-time value to just minutes. */ 
TimePicker.toMinutes = function(value)
{
	var d = new Date(value);

	return (d.getHours() * 60) + d.getMinutes();
}

TimePicker.getValues = function(interval)
{
	var values = this.VALUES[interval];
	if (values)
		return values;

	var i = 0;
	var min = 0;
	values = [];
	do
	{
		values[i++] = { id: min, name: this.toCaption(min) };
	} while ((min+= interval) < this.MINUTES_IN_DAY);

	return this.VALUES[interval] = values;
}

/** Converts minutes to a traditional time format. */
TimePicker.toCaption = function(minutes)
{
	var hour = Math.floor(minutes / 60);
	var min = minutes % 60;
	var am = (12 > hour) ? 'AM' : 'PM';

	if (0 == hour)
		hour = 12;
	else if (12 < hour)
		hour-= 12;

	if (10 > min)
		min = '0' + min;

	return hour + ':' + min + ' ' + am;
}