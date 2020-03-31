var TimestampPicker = {}

TimestampPicker.CSS_MAIN = 'timestampPicker';

TimestampPicker.create = function(name, value, extra, callback, interval)
{
	var h, o = document.createElement('span');
	var criteria = { name: name, value: value, caller: this, extra: extra, callback: callback };
	o.appendChild(h = criteria.hidden = Template.genInput(name, 'hidden'));
	o.className = this.CSS_MAIN;

	if (undefined != value)
		h.value = new Date(value).getTime();

	criteria.dateCallback = { doSelect: function(c, v) {
		var b;
		if (undefined == criteria.value)
			b = new Date(v.getFullYear(), v.getMonth(), v.getDate());
		else
		{
			b = new Date(criteria.value);
			b.setFullYear(v.getFullYear(), v.getMonth(), v.getDate());
		}

		criteria.hidden.value = criteria.value = b.getTime();

		if (callback && callback.doSelect)
			callback.doSelect(criteria);
	}};

	criteria.timeCallback = { doSelect: function(c) {
		var b, a = new Date(c.value);
		if (undefined == criteria.value)
			b = new Date();
		else
			b = new Date(criteria.value);

		b.setHours(a.getUTCHours(), a.getUTCMinutes(), 0, 0);
		criteria.hidden.value = criteria.value = b.getTime();

		if (callback && callback.doSelect)
			callback.doSelect(criteria);
	}};

	o.appendChild(DatePicker.create((new Date()).getTime(), value, extra, criteria.dateCallback));
	o.appendChild(document.createTextNode(' '));
	o.appendChild(TimePicker.create((new Date()).getTime(), value, extra, criteria.timeCallback, interval));

	return o;
}
