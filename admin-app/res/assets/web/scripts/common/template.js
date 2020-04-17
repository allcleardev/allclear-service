function Template() {}

Template.MODAL_ZINDEX = 100;

Template.prototype.KILO = 1024;
Template.prototype.MEGA = 1024 * 1024;
Template.prototype.GIGA = 1024 * 1024 * 1024;

Template.prototype.LISTS = { dateRanges: [ { id: 'today', name: 'Today'}, { id: 'yesterday', name: 'Yesterday'}, { id: 'thisWeek', name: 'This Week'}, { id: 'lastWeek', name: 'Last Week'}, { id: 'thisMonth', name: 'This Month'}, { id: 'lastMonth', name: 'Last Month'}, { id: 'thisQuarter', name: 'This Quarter'}, { id: 'lastQuarter', name: 'Last Quarter'}, { id: 'thisYear', name: 'This Year'}, { id: 'lastYear', name: 'Last Year'}, { id: 'custom', name: 'Custom' } ],
                             logLevels: [ { id: 'ALL', name: 'All' }, { id: 'DEBUG', name: 'Debug' }, { id: 'ERROR', name: 'Error' }, { id: 'INFO', name: 'Info' }, { id: 'OFF', name: 'Off' }, { id: 'TRACE', name: 'Trace' }, { id: 'WARN', name: 'Warn' } ],
                             radiusUnits: [ { id: 'km', name: 'Kilometers' }, { id: 'mi', name: 'Miles' } ],
                             yesNoOptions: [ { id: 'true', name: 'Yes' }, { id: 'false', name: 'No' } ],
                             pageSizes: [ { id: 10, name: '10' }, { id: 20, name: '20' }, { id: 50, name: '50' }, { id: 100, name: '100' }, { id: 1000, name: '1000' } ] };

Template.prototype.REGEX_UTC = /\+0000/;
Template.prototype.REGEX_GMT_TIME = / 00:00:00 GMT/;
Template.prototype.REGEX_NEW_LINE = /[\n]+/;
Template.prototype.REGEX_PATH_PREFIX = /\/$/;   // Determine if a URL ends with the path separator.
Template.prototype.REGEX_WHITESPACE = /[\s]+/g;

Template.prototype.CSS_ACTIONS = 'actions';
Template.prototype.CSS_FIELDS = 'fields';
Template.prototype.CSS_MODAL = 'modal';

Template.prototype.CAPTION_ADD = 'Add';
Template.prototype.CAPTION_BACK = 'Back';
Template.prototype.CAPTION_CANCEL = 'Cancel';
Template.prototype.CAPTION_CLOSE = 'Close';
Template.prototype.CAPTION_COPY = 'Copy';
Template.prototype.CAPTION_IMPORT = 'Import';
Template.prototype.CAPTION_PREVIEW = 'Preview';
Template.prototype.CAPTION_REMOVE = 'Remove';
Template.prototype.CAPTION_SUBMIT = 'Submit';

Template.prototype.HEADER_SESSION = Template.HEADER_SESSION = 'X-AllClear-SessionID';
Template.prototype.SESSION_DURATION_LONG = 30 * 24 * 3600000;
Template.prototype.SESSION_DURATION_SHORT = 30 * 60000;

Template.prototype.PUBLIC_GET_RESOURCES = Template.PUBLIC_GET_RESOURCES = { 'sessions': true, 'agentFamilies': true, 'agentTypes': true, 'applicationStatuses': true, 'deviceTypes': true, 'documentRepos': true, 'documentSources': true, 'jobStatuses': true, 'labels': true, 'linkTypes': true, 'loginTypes': true, 'osFamilies': true, 'phoneTypes': true, 'roles': true, 'sourceMappingTypes': true, 'sourceTypes': true, 'timeZones': true, 'tlds': true, 'userTypes': true };

Template.prototype.load = function(properties)
{
	if (undefined == properties)
		return;

	for (id in properties)
		this[id] = properties[id];
};

Template.prototype.run = function(criteria, body, method)
{
	var me = this;
	if (body)
		criteria.body = body;

	// Set the default HTTP method.
	if (undefined == method)
		method = 'post';

	// If no BODY exists at all, create a modal dialog.
	// Do NOT include an ELSE section to set "criteria.isModal = false" because the modal body could already exist.
	// Only set the isModal if undefined. For table/list modals the "run" method can be called multiple times as sorts & pages change.
	if (undefined == criteria.isModal)
		criteria.isModal = (undefined == criteria.body);

	// When modal ListTemplate re-sorts, do NOT need to re-add "center" and "closeMe" methods. 
	if (criteria.isModal && (undefined == criteria.body))
	{
		var b = document.body;
		b.insertBefore(body = this.createDiv('Loading ...', 'modalDialog'), b.firstChild);
		body = criteria.body = $(body);
		body.CENTERED = 0;      // Keeps track of how many times the dialog is centered. Same instance could be centered multiple times if re-run.
		body.center = function() {
			var w = $(window);
			
			this.css('top', (this.top = ((w.height() - this.height()) / 2) + w.scrollTop()) + 'px');
			this.css('left', (this.left = ((w.width() - this.width()) / 2) + w.scrollLeft()) + 'px');
			
			body.css('zIndex', Template.MODAL_ZINDEX++);   // Make that follow-up modal dialogs are layered on top.
			this.CENTERED++;
		};
		
		// SHOULD use closeMe to close the modal dialog to ensure that the MODAL_INDEX is decremented.
		body.closeMe = function() {
			Template.MODAL_ZINDEX-= this.CENTERED;
			this.remove();
		};
	}

	ProgressBar.start(criteria);

	// Do NOT check on criteria.value because ListTemplate will have a value while trying to page or sort. DLS on 10/27/2014.
	if (undefined != criteria.url)
	{
		this[method](criteria.url, criteria.filter, function(value) {
			ProgressBar.stop(criteria);
			me.display(criteria, value);
		});
	}
	else
	{
		ProgressBar.stop(criteria);
		me.display(criteria, criteria.value);
	}

	return criteria;
}

Template.prototype.display = function(criteria, value)
{
	criteria.body.empty();
	criteria.value = value;
	var me = this;
	var isModal = criteria.isModal;
	if (isModal)
	{
		var e = criteria.header = this.createHeader();
		this.addSpan(e, this.getTitle(criteria));       // Put within SPAN within H1 so that it doesn't overlap the close anchor.
		criteria.body.append($(e));
		e.appendChild(this.createAnchor('X',
			function(ev) { me.handleCancel(criteria); }));

		this.makeElementDraggable(criteria, e);
	}
	criteria.body.append($(this.generate(criteria)));

	if (this.onPostLoad)
		this.onPostLoad(criteria);

	if (isModal)
		criteria.body.center();
}

Template.prototype.getRestPath = Template.getRestPath = function() { return REST_PATH; }

Template.prototype.get = Template.get = function(url, params, handler, headers, restPath)
{
	// Wrap all GET calls to check for authentication exceptions and force a login.
	// Plus make sure that for IE it's a unique call.
	// Do NOT use convenience method of .get so that can specify to NOT cache.
	var me = this;
	// $.get(REST_PATH + url, params,
	if (!restPath) restPath = this.getRestPath();
	$.ajax(restPath + url, { type: 'GET', data: params, cache: false, dataType: 'json',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'get', url, params, headers); }});
}

Template.prototype.auth = Template.auth = function(url, params, handler, headers)
{
	// Wrap all GET calls to check for authentication exceptions.
	// If authentication exception is found, just returns NULL.
	var me = this;
	$.ajax(this.getRestPath() + url, { type: 'GET', data: params, cache: false, dataType: 'json',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) {
			if (403 == xhr.status) handler(null);
			else me.handleError(xhr, handler, 'get', url, params, headers);
		}});
}

/** Run a GET operation with TEXT/HTML as the expected output. */
Template.prototype.html = Template.html = function(url, params, handler, headers)
{
	if (!headers)
		headers = {};
	headers['Accept'] = 'text/html';

	var me = this;

	$.ajax(this.getRestPath() + url, { type: 'GET', data: params, cache: false, dataType: 'text',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'html', url, params, headers); }});
}

/** Run a GET operation with TEXT/PLAIN as the expected output. */
Template.prototype.text = Template.text = function(url, params, handler, headers)
{
	var me = this;

	$.ajax(this.getRestPath() + url, { type: 'GET', data: params, cache: false, dataType: 'text',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'text', url, params, headers); }});
}

Template.prototype.post = Template.post = function(url, params, handler, headers)
{
	if (!params)
		params = {};

	// Wrap all POST calls to check for authentication exceptions and force a login.
	var me = this;
	$.ajax(this.getRestPath() + url, { type: 'POST', data: JSON.stringify(params), dataType: 'json',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'post', url, params, headers); },
		contentType: 'application/json; charset=UTF-8' });
}

/** Run a POST operation with plain/text (XML) as the payload. */
Template.prototype.xml = Template.xml = function(url, params, handler, headers)
{
	if (!params)
		params = {};

	// Wrap all POST calls to check for authentication exceptions and force a login.
	var me = this;
	$.ajax(this.getRestPath() + url, { type: 'POST', data: params.payload, dataType: 'json',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'xml', url, params, headers); },
		contentType: 'text/plain; charset=UTF-8' });
}

/** Run a POST operation with plain/text (XML) as the payload. */
Template.prototype.xmlPut = Template.xmlPut = function(url, params, handler, headers)
{
	if (!params)
		params = {};

	// Wrap all PUT calls to check for authentication exceptions and force a login.
	var me = this;
	$.ajax(this.getRestPath() + url, { type: 'PUT', data: params.payload, dataType: 'json',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'xml', url, params, headers); },
		contentType: 'text/plain; charset=UTF-8' });
}

/** Run a POST operation with TEXT as the expected output. */
Template.prototype.dump = Template.dump = function(url, params, handler, headers)
{
	if (!params)
		params = {};

	// Wrap all POST calls to check for authentication exceptions and force a login.
	var me = this;
	$.ajax(this.getRestPath() + url, { type: 'POST', data: JSON.stringify(params), dataType: 'text',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'dump', url, params, headers); },
		contentType: 'application/json; charset=UTF-8' });
}

Template.prototype.put = Template.put = function(url, params, handler, headers)
{
	if (!params)
		params = {};

	// Wrap all PUT calls to check for authentication exceptions and force a login.
	var me = this;
	$.ajax(this.getRestPath() + url, { type: 'PUT', data: JSON.stringify(params), dataType: 'json',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'put', url, params, headers); },
		contentType: 'application/json; charset=UTF-8' });
}

Template.prototype.remove = Template.remove = function(url, id, handler, headers)
{
	// Wrap all GET calls to check for authentication exceptions and force a login.
	// Plus make sure that for IE it's a unique call.
	// Do NOT use convenience method of .get so that can specify to NOT cache.
	var me = this;

	$.ajax(this.getRestPath() + url + ((undefined != id) ? ('/' + id) : ''), { type: 'DELETE', dataType: 'json',
		headers: this.appendCurrentSessionId(headers),
		success: function(value) { handler(value); },
		error: function(xhr) { me.handleError(xhr, handler, 'remove', url, id, headers); }});
}

Template.prototype.multiPart = Template.multiPart = function(url, params, handler, headers)
{
	if (!params)
		params = new FormData();

	// https://abandon.ie/notebook/simple-file-uploads-using-jquery-ajax
	var me = this;
	$.ajax(this.getRestPath() + url,
			{ type: 'POST',
			  data: params,
			  cache: false,
			  dataType: 'json',
			  processData: false,	// Don't process the files
			  contentType: false,	// Set content type to false as jQuery will tell the server its a query string request
			  headers: this.appendCurrentSessionId(headers),
			  success: handler,
			  error: function(xhr) { me.handleError(xhr, handler, 'multiPart', url, params, headers); }
			});
}

Template.prototype.handleError = Template.handleError = function(xhr, handler, method, url, params, headers)
{
	if (200 == xhr.status)
		handler({});	// jQuery treats empty response as an error. DLS on 1/2/2019.

	else if (403 == xhr.status)
	{
		var me = this;
		LoginHandler.doLogin(function() { me[method](url, params, handler, headers); })
	}
	else if (401 == xhr.status)
	{
		handler({ isError: true, message: 'The specified user is not authorized to perform this action.' });
	}
	else
	{
		var value = xhr.responseText;
		if (!value)
			value = { message: xhr.statusText };
		else
		{
			value = JSON.parse(value);
			if ((undefined == value.message) && (undefined != value.error))
				value.message = value.error;
		}
		value.isError = true;
		delete value.stackTrace;	// Don't need the stack trace cluttering the error.
		handler(value);
	}
}

/** Gets a list of data from local cache or the specified resource. */
Template.prototype.getList = function(resource)
{
	return this.LISTS[resource];
}

/** Loads lists maintained by the platform. */
Template.prototype.loadLists = function(resources)
{
	for (var i = 0; i < resources.length; i++)
		this.loadList(resources[i]);
}

/** Loads a single list maintained by the platform.
 * 
 *  MUST be kept separate from loadLists because the 'resource' var must be accessible in its current state
 *  within the closure. DLS on 12/3/2015.
 */
Template.prototype.loadList = function(resource)
{
	var me = this;
	this.get('types/' + resource, undefined, function(data) { me.LISTS[resource] = data; });
}

/** Remove empty strings from the object. */
Template.prototype.clean = function(value)
{
	var v;
	for (id in value)
		if ((typeof(v = value[id]) == 'string') && ('' == v))
			delete value[id];
}

/** Appends the current session ID to the headers.
 * @param headers existing headers appended to.
 * @param noRequiresAuth if TRUE exclude the RequiresAuth header. Exclude that header for login.
 */
Template.prototype.appendCurrentSessionId = Template.appendCurrentSessionId = function(headers)
{
	var value = this.getCurrentSession();
	if (undefined == value) return headers;

	if (undefined == headers) headers = {};
	headers[this.HEADER_SESSION] = value.id;
	this.setCurrentSessionId(value.id, value.userName, value.rememberMe);	// Upon using the session, always extend out the expiration period.

	return headers;
}

/** Gets the current session ID. */
Template.prototype.getCurrentSession = Template.getCurrentSession = function()
{
	return Cookies.getJSON(this.HEADER_SESSION);
}

/** Sets the current session ID. */
Template.prototype.setCurrentSessionId = Template.setCurrentSessionId = function(id, userName, rememberMe)
{
	if (undefined == id)
		return;

	var duration = rememberMe ? this.SESSION_DURATION_LONG : this.SESSION_DURATION_SHORT;
	var expiresAt = new Date();
	expiresAt.setTime(expiresAt.getTime() + duration);
	Cookies.set(this.HEADER_SESSION, { id: id, userName: userName, rememberMe: rememberMe }, { expires: expiresAt });
}

/** Deletes the current session ID. */
Template.prototype.deleteCurrentSessionId = Template.deleteCurrentSessionId = function()
{
	Cookies.remove(this.HEADER_SESSION);
}

/** Used for images external to the current site. */
Template.prototype.createImg = function(url, title)
{
	var o = document.createElement('img');
	o.src = url;

	if (title)
		o.title = title;

	return o;
}

Template.prototype.createImage = function(src, title)
{
	var o = document.createElement('img');
	o.src = IMAGE_PATH + src;

	if (title)
		o.title = title;

	return o;
}

Template.prototype.createAnchor = Template.createAnchor = function(caption, action, css)
{
	var o = document.createElement('a');
	o.href = 'javascript:void(null)';
	o.onclick = action;

	var t = typeof(caption);
	if (('string' == t) || ('number' == t))
		o.innerHTML = caption;
	else if (undefined != caption)
		o.appendChild(caption);

	if (css)
		o.className = css;

	return o;
}

Template.prototype.createLink = Template.createLink = function(caption, href, css)
{
	var o = document.createElement('a');
	o.href = href;

	var t = typeof(caption);
	if (('string' == t) || ('number' == t))
		o.innerHTML = caption;
	else
		o.appendChild(caption);

	if (css)
		o.className = css;

	return o;
}

Template.prototype.createDiv = function(value, css) { return this.createElement('div', value, css); }
Template.prototype.createHeader = function(value, css) { return this.createElement('h1', value, css); }
Template.prototype.createSpan = function(value, css) { return this.createElement('span', value, css); }
Template.prototype.createElement = Template.createElement = function(name, value, css)
{
	var o = document.createElement(name);
	if (value)
		o.innerHTML = value;
	if (css)
		o.className = css;

	return o;
}

Template.prototype.addBreak = Template.addBreak = function(elem)
{
	var o = document.createElement('br');
	elem.appendChild(o);

	return o;
}

Template.prototype.addSpace = function(elem)
{
	var o = document.createTextNode(' ');
	elem.appendChild(o);

	return o;
}

Template.prototype.addText = Template.addText = function(elem, value)
{
	var o = document.createTextNode(value);
	elem.appendChild(o);

	return o;
}

Template.prototype.addErrorText = Template.addErrorText = function(elem, value)
{
	return this.addElem(elem, 'i', value, 'error');
}

Template.prototype.addSuccessText = Template.addSuccessText = function(elem, value)
{
	return this.addElem(elem, 'i', value, 'success');
}

Template.prototype.addElem = Template.addElem = function(elem, type, value, css)
{
	var o = this.createElement(type, value, css);
	elem.appendChild(o);

	return o;
}

Template.prototype.addDiv = Template.addDiv = function(elem, value, css) { return this.addElem(elem, 'div', value, css); }
Template.prototype.addSpan = function(elem, value, css) { return this.addElem(elem, 'span', value, css); }
Template.prototype.addValue = function(elem, value, css) { return this.addElem(elem, 'em', value, css); }
Template.prototype.addCaption = function(elem, value, css) { return this.addElem(elem, 'b', value, css); }
Template.prototype.addFootnote = function(elem, value, css) { return this.addElem(elem, 'em', value, css); }
Template.prototype.addHeader = function(elem, value, css) { return this.addElem(elem, 'h1', value, css); }

/** Makes an element the draggable component of a widgets body. Used by modal dialogs to move around
 * by dragging the title bar
 */
Template.prototype.makeElementDraggable = function(criteria, e)
{
	// Add mousedown and mouseup events to facilitate dragging.
	$(e).mousedown(criteria, function(ev) {
		// Only perform dragging for left mouse clicks.
		if (1 != ev.which)
			return;

		ev.preventDefault();

		// Original coordinates of the mousedown for reference point.
		ev.data.pageX = ev.pageX;
		ev.data.pageY = ev.pageY;
		$(document.body).mousemove(ev.data, function(ev) {
			var c = ev.data;
			var b = c.body;
			b.css('top', (c.bodyTop = (b.top + (ev.pageY - c.pageY))) + 'px');
			b.css('left', (c.bodyLeft = (b.left + (ev.pageX - c.pageX))) + 'px');
		});
	});
	$(document.body).mouseup(criteria, function(ev) {
		$(document.body).unbind('mousemove');
		var c = ev.data;
		var b = c.body;
		b.top = c.bodyTop;
		b.left = c.bodyLeft;
	});
}

/** Set the maximum height for the fields section of the form. */
Template.prototype.setFieldsMaxHeight = function(criteria)
{
	var w = $(window);
	var b = criteria.body;
	var f = criteria.fields;
	var os = (b.height() - f.offsetHeight) + 15;    // The offset height (all other sections minus the fields section).
	var maxHeight = w.height() - os;
	if (w.height() < b.height())
	{
		f.style.height = maxHeight + 'px';
		
		// Also make it wider so it doesn't scroll horizontally on account of the vertical scroll bars.
		f.style.width = (f.offsetWidth + 25) + 'px';
	}

	// Just in case the form grows set a body max-height.
	else
		f.style.maxHeight = maxHeight + 'px';
}

/** Formatters. */
Template.prototype.toText = Template.toText = function(value)
{
	if (undefined == value)
		return '';

	var type = typeof(value);
	if (type == 'boolean')
		return value ? 'Yes' : 'No';
	if (type == 'number')
		return this.toNumber(value);

	return value;
}

/** Does number perform number formating. Easier to copy and paste into SQL without the formating (commas). DLS on 11/11/2016. */
Template.prototype.toIdentifier = function(value)
{
	if (undefined == value)
		return '';

	if ('boolean' == typeof(value))
		return value ? 'Yes' : 'No';

	return value;
}

/** Shrinks text for a table column to only 1000 characters if longer. */
Template.prototype.toColumnText = function(value)
{
	return this.shrink(value, 1000);
}

Template.prototype.toColumnJSON = function(value)
{
	return this.shrink(this.toJSON(value), 200);	// Make small since generated JSON will have no whitespace for natural line breaks. DLS on 10/4/2018.
}

/** Helper method - shrinks text if necessary. */
Template.prototype.shrink = Template.shrink = function(value, maxSize)
{
	if (undefined == value)
		return '';

	if (maxSize >= value.length)
		return value;

	return value.substring(0, maxSize) + '&hellip;';
}

/** Converts an array into a string with each item separated by a new-lines and/or carriage returns.
 *  
 *  @return empty string if no values found.
 */
Template.prototype.fromArray = function(values)
{
	if ((undefined == values) || (0 == values.length))
		return '';

	var value = values[0];

	// Loop through the values and throw out any empty items.
	for (var i = 1; i < values.length; i++)
		value+= '\n' + values[i];

	return value;
}

/** Converts an array into a string with each item separated by a new-lines and/or carriage returns.
 *  
 *  @return empty string if no values found.
 */
Template.prototype.fromArrayWithComma = Template.fromArrayWithComma = function(values)
{
	if ((undefined == values) || (0 == values.length))
		return '';

	var value = values[0];

	// Loop through the values and throw out any empty items.
	for (var i = 1; i < values.length; i++)
		value+= ', ' + values[i];

	return value;
}

/** Converts a string separated by new-lines and/or carriage returns to an array.
 *
 *  @return empty array if no items found.
 */
Template.prototype.toArray = function(value)
{
	if ((undefined == value) || (0 == value.length))
		return [];

	// Split the values.
	var values = value.split(this.REGEX_NEW_LINE);

	// Loop through the values and throw out any empty items.
	for (var i = 0; i < values.length; i++)
		if ('' == (values[i] = $.trim(values[i])))
			values.splice(i--, 1);

	return values;
}

/** Takes a single comma separated line and splits into an array. */
Template.prototype.parseCSV = function(value)
{
	if ((undefined == value) || (0 == value.length))
		return [];

	var s = ',', q = '"', l = value.length, r = [];
	var i = 0, f = 0;
	for (; i < l; i++)
	{
		var c = value[i];
		if (s == c)
		{
			r[r.length] = (i > f) ? value.substring(f, i) : '';
			f = i + 1;
		}
		else if (q == c)
		{
			var n = value.indexOf(q, i + 1);
			if (-1 == n)
			{
				r[r.length] = value.substring(i + 1);
				i = l;
			}
			else
			{
				r[r.length] = value.substring(i + 1, n);
				i = n + 1;	// Skips proceeding comma (includes the i++ at the head of the loop).
			}
			f = i + 1;
		}
	}

	// Pick up part between last comma and the end of the line.
	if (i > f)
		r[r.length] = value.substring(f, i);

	return r;
}

/** Rounds number to the specified digit. */
Template.prototype.round = function(value, places)
{
	var div = Math.pow(10, places);
	
	return (Math.round(value * div) / div);
}

/** Converts a float to a currency. */
Template.prototype.toCurrency = function(value)
{
	if (undefined == value)
		return '';

	return this.toNumber(value, 2);
}

/** Converts a float to a percentage. */
Template.prototype.toPercent = function(value)
{
	if (undefined == value)
		return '';

	return this.toNumber(this.round(value * 100, 2), 2);
}

Template.prototype.fromMilliseconds = function(value)
{
	return (undefined != value) ? this.fromSeconds(value / 1000) : '';
}

Template.prototype.fromSeconds = function(value)
{
	if (undefined == value) return '';

	var o = '';
	var v = Math.floor(value / 3600);
	if (0 < v)
	{
		o = (v + ' hours');
		value%= 3600;
	}

	v = Math.floor(value / 60);
	if (0 < v)
	{
		if (o) o+= ', ';
		o+= (v + ' minutes');
		value%= 60;
	}

	if (0 < value)
	{
		if (o) o+= ', ';
		o+= (value + ' seconds');
	}

	return o;
}

/** Converts milliseconds to seconds with three decimal places. */
Template.prototype.toSeconds = function(value)
{
	// MUST use full UNDEFINED comparison because the number could be zero (false).
	if (undefined == value)
		return '';

	return this.toNumber(this.round(value / 1000, 3), 3);
}

/** Converts to whole number. */
Template.prototype.toWhole = function(value)
{
	// MUST use full UNDEFINED comparison because the number could be zero (false).
	if (undefined == value)
		return '';

	return this.toNumber(Math.round(value), 0);
}

/** Converts to whole number. */
Template.prototype.toWholeN = Template.toWholeN = function(value)
{
	// MUST use full UNDEFINED comparison because the number could be zero (false).
	if (undefined == value)
		return 'N/A';

	return this.toNumber(Math.round(value), 0);
}

/** Formats a number with commas. */
Template.prototype.toNumber = Template.toNumber = function(value, places)
{
	value+= '';
	x = value.split('.');
	x1 = x[0];
	x2 = x.length > 1 ? x[1] : '';

	// If places is defined, make sure pad the decimal with necessary zeros.
	if (undefined != places)
	{
		if (x2.length < places)
		{
	    	while (x2.length < places)
    			x2+= '0';
    	}
    	else if (x2.length > places)
    		x2 = x2.substr(0, places);
	}

	if (0 < x2.length)
    	x2 = '.' + x2;

	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1))
    	x1 = x1.replace(rgx, '$1' + ',' + '$2');

	return x1 + x2;
}

Template.prototype.toBool = function(value)
{
	return ('T' == value) ? 'Yes' : 'No';
}

Template.prototype.today = Template.today = function()
{
	var v = new Date();
	v.setUTCHours(0, 0, 0, 0);

	return this.toDbDate(v);
}

Template.prototype.weekAgo = Template.weekAgo = function()
{
	var v = new Date();
	v.setUTCHours(0, 0, 0, 0);
	v.setUTCDate(v.getUTCDate() - 7);

	return this.toDbDate(v);
}

Template.prototype.toDateTime = function(value)
{
	if (undefined == value)
		return '';

	return (new Date(value.replace(this.REGEX_UTC, 'Z'))).toLocaleString();
}

Template.prototype.toDateTimeN = function(value)
{
	if (undefined == value)
		return '';

	return (new Date(value)).toLocaleString();
}

Template.prototype.toDate = function(value)
{
	if (undefined == value)
		return '';

	return (new Date(value.replace(this.REGEX_UTC, 'Z'))).toDateString();
}

Template.prototype.toDateUTC = function(value)
{
	if (undefined == value)
		return '';

	return (new Date(value.replace(this.REGEX_UTC, 'Z'))).toUTCString().replace(this.REGEX_GMT_TIME, '');
}

Template.prototype.toTime = function(value)
{
	if (undefined == value)
		return '';

	return (new Date(value.replace(this.REGEX_UTC, 'Z'))).toTimeString();
}

Template.prototype.toDbDate = Template.toDbDate = function(value)
{
	return value.getUTCFullYear() + '-' + this.padInt(value.getUTCMonth() + 1) + '-' + this.padInt(value.getUTCDate()) + "T00:00:00Z";
}

Template.prototype.padInt = Template.padInt = function(value)
{
	if (10 > value)
		return '0' + value;

	return value;
}

Template.prototype.getTimezoneOffset = Template.getTimezoneOffset = function()
{
	var m = new Date().getTimezoneOffset();
	if (0 == m)
		return '+0000';

	var sign = '-';
	if (0 > m)	// Negative is positive in offsets.
	{
		m = -m;
		sign = '+';
	}

	return sign + this.padInt(parseInt(m / 60)) + this.padInt(m % 60)
}

Template.prototype.toDuration = function(seconds)
{
	if (undefined == seconds)
		return '';

	var value = '';
	if (604800 < seconds)
	{
		value = this.appendUnit(value, this.round(seconds / 604800, 0), 'week');
		seconds%= 604800;
	}

	if (86400 < seconds)
	{
		value = this.appendUnit(value, this.round(seconds / 86400, 0), 'day');
		seconds%= 86400;
	}

	if (3600 < seconds)
	{
		value = this.appendUnit(value, this.round(seconds / 3600, 0), 'hour');
		seconds%= 3600;
	}

	if (60 < seconds)
	{
		value = this.appendUnit(value, this.round(seconds / 60, 0), 'minute');
		seconds%= 60;
	}

	if (0 < seconds)
		return this.appendUnit(value, seconds, 'second');

	return value;
}

Template.prototype.toDurationN = function(seconds)
{
	if (undefined == seconds)
		return '';

	if (604800 < seconds)
		return this.appendUnit('', this.round(seconds / 604800, 2), 'week');

	if (86400 < seconds)
		return this.appendUnit('', this.round(seconds / 86400, 2), 'day');

	if (3600 < seconds)
		return this.appendUnit('', this.round(seconds / 3600, 2), 'hour');

	if (60 < seconds)
		return this.appendUnit('', this.round(seconds / 60, 2), 'minute');

	return this.appendUnit('', seconds, 'second');
}

Template.prototype.appendUnit = Template.appendUnit = function(value, count, unit)
{
	if (0 < value.length)
		value+= ', ';

	return value + count + ' ' + unit + ((1 < count) ? 's' : '');
}

Template.prototype.toSize = function(value)
{
	if (!value)
		return 'N/A';

	if (this.KILO > value)
		return this.toWhole(value) + ' bytes';
	if (this.MEGA > value)
		return this.toNumber(value / this.KILO, 3) + ' KB';
	if (this.GIGA > value)
		return this.toNumber(value / this.MEGA, 3) + ' MB';

	return this.toNumber(value / this.GIGA, 3) + ' GB';
}

/** Trims a string a returns the value or NULL if empty. */
Template.prototype.trimToNull = Template.trimToNull = function(value)
{
	if (undefined == value) return null;
	if ('' == (value = value.trim())) return null;

	return value;
}

/** Converts an object to a JSON string. */
Template.prototype.toJSON = Template.toJSON = function(value)
{
	if (undefined == value)
		return '';

	return JSON.stringify(value);
}

/** Converts a JSON string to an object. */
Template.prototype.fromJSON = Template.fromJSON = function(value)
{
	if ('' == value)
		return undefined;

	return JSON.parse(value);
}

/** Clones the supplied object by converting to JSON and then back to an object. */
Template.prototype.cloneObject = Template.cloneObject = function(value)
{
	if (null == value) return null;

	return JSON.parse(JSON.stringify(value));
}

Template.prototype.removeProperties = Template.removeProperties = function(value)
{
	if ((undefined == value) || ('object' != typeof(value)) || (1 >= arguments.length))
		return value;

	// Start after first argument (value).
	for (var i = 1; i < arguments.length; i++)
		value = this.removeProperty(value, arguments[i]);

	return value;
}

Template.prototype.removeProperty = Template.removeProperty = function(value, name)
{
	if ((undefined == value) || ('object' != typeof(value)))
		return value;

	// Loop through each property. If the property equals 'name', the property is removed.
	// If the property is not the same as 'name', traverse into the property/object
	// deep remove the property.
	for (id in value)
	{
		if (id == name)
			delete value[id];
		else
			this.removeProperty(value[id], name);
	}

	return value;
}

Template.prototype.genInput = Template.genInput = function(name, type, checked)
{
	if (document.all)
	{
		var c = checked ? ' checked' : '';
		return document.createElement('<input name="' + name + '" type="' + type + '"' + c + ' />');
	}

	var o = document.createElement('input');
	o.name = name;
	o.type = type;
	o.checked = checked;

	// For enabling & disabling the field plus it's caption.
	o.setFieldStatus = function(b) {
		this.disabled = !b;
		this.previousSibling.className = (b ? '' : 'disabled');
	};

	return o;
}

Template.prototype.genButton = Template.genButton = function(name, caption, onclick)
{
	var o = this.genInput(name, 'button');
	if (caption) o.value = caption;
	if (onclick) o.onclick = onclick;

	return o;
}

Template.prototype.genCheckBox = Template.genCheckBox = function(name, checked)
{
	return this.genInput(name, 'checkbox', checked);
}

Template.prototype.genCheckBoxes = function(name, options, values, cols)
{
	if (!cols)
		cols = 1;

	var opt, checked, e, s, o = this.createDiv(undefined, 'multiselect');
	var c = 'column_' + cols;
	for (var i = 0; i < options.length; i++)
	{
		opt = options[i];
		checked = false;
		if (values)
			for (var j = 0; j < values.length; j++)
				if (checked = (opt.id == values[j]))
					break;

		if ((0 < i) && (0 == (i % cols)))
			this.addBreak(o);

		(s = this.addSpan(o, opt.name, c)).insertBefore(e = this.genCheckBox(name, checked), s.firstChild);
		e.value = opt.id;
	}

	return o;
}

Template.prototype.genFile = function(name, maxLength, size)
{
	var o = this.genInput(name, 'file');
	if (maxLength)
		o.maxLength = maxLength;
	if (size)
		o.size = size;

	return o;
}

Template.prototype.genHidden = Template.genHidden = function(name, value)
{
	var o = this.genInput(name, 'hidden');
	if (undefined != value)
		o.value = value;

	return o;
}

Template.prototype.genList = Template.genList = function(name, size, options, value)
{
	var o = document.createElement('select');
	o.name = name;
	o.multiple = true;
	o.size = (undefined != size) ? size : 10;

	if (options)
	{
		var opt = o.options;
		for (var i = 0; i < options.length; i++)
		{
			var item = options[i];
			opt[opt.length] = new Option(item.name, item.id);
		}
	}

	if (value)
		o.value = value;

	// For enabling & disabling the field plus it's caption.
	o.setFieldStatus = function(b) {
		this.disabled = !b;
		this.previousSibling.className = (b ? '' : 'disabled');
	};

	return o;
}

Template.prototype.genPassword = function(name, maxLength, size)
{
	var o = this.genInput(name, 'password');
	if (maxLength)
		o.maxLength = maxLength;
	if (size)
		o.size = size;

	return o;
}

Template.prototype.genRadio = function(name, checked, value)
{
	var o = this.genInput(name, 'radio', checked);
	if (value)
		o.value = value;

	return o;
}

Template.prototype.genRadios = function(name, options, value, cols)
{
	if (!cols)
		cols = 1;

	var opt, checked, e, s, o = this.createDiv(undefined, 'multiselect');
	var c = 'column_' + cols;
	for (var i = 0; i < options.length; i++)
	{
		opt = options[i];
		if ((0 < i) && (0 == (i % cols)))
			this.addBreak(o);

		(s = this.addSpan(o, opt.name, c)).insertBefore(e = this.genRadio(name, value[opt.id]), s.firstChild);
		e.value = opt.id;
	}

	return o;
}

Template.prototype.genSelect = Template.genSelect = function(name, options, value, header)
{
	var o;
	if (document.all)
		o = document.createElement('<select name="' + name + '" />');
	else
	{
		o = document.createElement('select');
		o.name = name;
	}

	var opt = o.options;
	if (header)
		opt[0] = new Option(header, '');

	if (options)
	{
		for (var i = 0; i < options.length; i++)
		{
			var item = options[i];
			opt[opt.length] = new Option(item.name, item.id);
		}
	}

	if (undefined != value) o.value = value;

	// For enabling & disabling the field plus it's caption.
	o.setFieldStatus = function(b) {
		this.disabled = !b;
		this.previousSibling.className = (b ? '' : 'disabled');
	};

	return o;
}

/** Repopulates a select list with new options from a remote call. */
Template.prototype.repopulateList = Template.repopulateList = function(elem, url, filter, header)
{
	this.post(url, filter, function(data) {
		var d, o = elem.options;
		o.length = 0;

		if (header)
			o[o.length] = new Option(header, '');

		if (data && (0 < data.length))
			for (var i = 0; i < data.length; i++)
				o[o.length] = new Option((d = data[i]).name, d.id);
	});
}

Template.prototype.genSubmit = function(name, caption)
{
	var o = this.genInput(name, 'submit');
	if (caption) o.value = caption;

	return o;
}

Template.prototype.genTextBox = Template.genTextBox = function(name, maxLength, size, placeholder)
{
	var o = this.genInput(name, 'text');
	if (maxLength)
		o.maxLength = maxLength;
	if (size)
		o.size = size;
	if (placeholder)
		o.placeholder = placeholder;

	// For enabling & disabling the field plus it's caption.
	o.setFieldStatus = function(b) {
		this.disabled = !b;
		this.previousSibling.className = (b ? '' : 'disabled');
	};

	return o;
}

Template.prototype.genTextArea = function(name, cols, rows, value)
{
	var o;
	if (document.all)
		o = document.createElement('<textarea name="' + name + '" />');
	else
	{
		o = document.createElement('textarea');
		o.name = name;
	}

	if (cols) o.cols = cols;
	if (rows) o.rows = rows;
    if (undefined != value) o.value = value;

	// For enabling & disabling the field plus it's caption.
	o.setFieldStatus = function(b) {
		this.disabled = !b;
		this.previousSibling.className = (b ? '' : 'disabled');
	};

	return o;
}

/** Converts non-ASCII characters to HTML entities. */
Template.prototype.encodeHTML = Template.encodeHTML = function(value)
{
	return value;	// Disable conversion for now.
/*
	if (!value)
		return value;

	var c, o = '';
	var len = value.length;
	for (var i = 0; i < len; i++)
	{
		c = value.charCodeAt(i);
		if (255 < c)
			o+= '&#' + c + ';';
		else
			o+= value.charAt(i);
	}

	return o;
*/
}

Template.prototype.escapeHTML = Template.escapeHTML = function(value)
{
	return value
		.replace(/&/g, "&amp;")
		.replace(/</g, "&lt;")
		.replace(/>/g, "&gt;")
		.replace(/"/g, "&quot;")
		.replace(/'/g, "&#039;");
}

Template.prototype.getLocation = Template.getLocation = function(elem)
{
	return $(elem).offset();
}

Template.prototype.getLocationBelow = Template.getLocationBelow = function(elem)
{
	var o = (elem = $(elem)).offset();
	o.top+= elem.outerHeight();

	return o;
}

Template.prototype.getLocationRightBottom = Template.getLocationRightBottom = function(elem)
{
	var o = (elem = $(elem)).offset();
	o.top+= elem.outerHeight();
	o.right = o.left + elem.outerWidth();

	return o;
}

Template.prototype.getLocationRightTop = Template.getLocationRightTop = function(elem)
{
	var o = (elem = $(elem)).offset();
	o.right = o.left + elem.outerWidth();
	o.bottom = o.top + elem.outerHeight();

	return o;
}
