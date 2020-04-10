function ListTemplate(properties)
{
	this.load(properties);
	if (this.CAN_EDIT || this.CAN_ADD || this.FIELDS)	// Load EDITOR even if CAN_ADD & CAN_EDIT are both FALSE in case some of the Handler wants to use the editor. DLS on 4/10/2020.
		this.EDITOR = new EditTemplate(properties);
	this.OPEN_CHILD = (undefined != this.openChild);
	this.HAS_ROW_ACTIONS = (this.CAN_REMOVE || this.OPEN_CHILD || this.ROW_ACTIONS || this.HISTORY);

	// Field to use off the record for the GET calls. Defaults to 'id'.
	if (!this.IDENTIFIER)
		this.IDENTIFIER = 'id';

	// If DESC_COLS (or NAV_COLS) is undefined, just split the number of cols in half.
	if (!this.DESC_COLS && this.COLUMNS)
	{
		var cols = this.COLUMNS.length;
		if (this.HAS_ROW_ACTIONS)
			cols++;

		var size = cols / 2;
		this.DESC_COLS = Math.ceil(size);
		this.NAV_COLS = Math.floor(size);
	}

	if (this.SEARCH)
	{
		if (!this.SEARCH_METHOD) this.SEARCH_METHOD = 'post';
		if (!this.SEARCH_PATH) this.SEARCH_PATH = this.RESOURCE + '/search';
		if (!this.SEARCH_BIG) this.SEARCH_BIG = this.RESOURCE + '/big';
		this.search = new EditTemplate(this.SEARCH);
		this.search.handleSubmit = function(criteria, form) {
			this.populate(criteria, form);

			// Allow child classes to preprocess data before submission. DLS on 4/3/2020.
			if (this.onEditorPreSubmit) this.onEditorPreSubmit(criteria);

			// Do NOT call handleCancel since on non-modals it will also call the callback,
			// which duplicates the call below.
			// DLS - 1/21/2011.
			if (criteria.isModal)
				criteria.body.closeMe();

			criteria.callback(criteria.value);
		};
	}

	// No search form, but needs a POST for the SEARCH_METHOD.
	else if (this.SEARCH_POST)
	{
		this.SEARCH_METHOD = 'post';
		this.SEARCH_PATH = this.RESOURCE + '/search';
	}

	// If no search form, just all the list/getAll command of the resource. DLS on 4/16/2015.
	else
	{
		this.SEARCH_METHOD = 'get';
		this.SEARCH_PATH = this.RESOURCE;
	}

	// Has audit log (history).
	if (this.HISTORY)
	{
		this.HISTORY_VIEWER = new HistoryTemplate(this.HISTORY);
		if (undefined == this.ROW_ACTIONS)
			this.ROW_ACTIONS = [];
		this.ROW_ACTIONS.push(new RowAction('openHistory', 'History'));
	}

	this.exporter = new EditTemplate(this.EXPORT);
}

function TextColumn(id, caption, formatter, selectable, highlight, clickHandler, css)
{
	this.id = id;
	this.caption = caption;
	this.formatter = (formatter ? formatter : 'toText');
	this.selectable = selectable;
	this.highlight = highlight;
	this.clickHandler = clickHandler;
	this.css = (highlight ? 'highlight' : css);
}

function IdColumn(id, caption, selectable, highlight, clickHandler, css)
{
	this.id = id;
	this.caption = caption;
	this.formatter = 'toIdentifier';
	this.selectable = selectable;
	this.highlight = highlight;
	this.clickHandler = clickHandler;
	this.css = (highlight ? 'highlight' : css);
}

function EditColumn(id, caption, formatter)
{
	this.id = id;
	this.caption = caption;
	this.formatter = (formatter ? formatter : 'toText');
	this.editable = true;
}

function RowAction(id, caption, condition, uncondition)
{
	this.id = id;
	this.caption = caption;
	this.condition = condition;
	this.uncondition = uncondition;
}

ListTemplate.prototype = new Template();

ListTemplate.prototype.MAX_PAGES = 500;
ListTemplate.prototype.HALF_MAX_PAGES = 250;

ListTemplate.prototype.EDIT_METHOD = 'post';

ListTemplate.prototype.init = function(body) { return this.filter({}, body); }

/** Opens the list limited by the filter.
 *   @param filter
 *   @param body
 *   @param exclusions a map of fields/columns to be excluded from this instance of the display. Used for popup child displays.
 */
ListTemplate.prototype.filter = function(filter, body, exclusions) { return this.run({ filter: filter, baseFilter: filter, url: this.SEARCH_PATH, exclusions: exclusions }, body, this.SEARCH_METHOD); }
ListTemplate.prototype.filterX = function(filter, extra) { return this.run({ filter: filter, baseFilter: filter, url: this.SEARCH_PATH, extra: extra }, undefined, this.SEARCH_METHOD); }

ListTemplate.prototype.getTitle = function(criteria)
{
	if (criteria.parent)
		return this.PARENT + ' ' + this.PLURAL + ': ' + criteria.parent.name;
	if (criteria.extra && criteria.extra.name)
		return this.PLURAL + ': ' + criteria.extra.name;

	return this.PLURAL;
}

ListTemplate.prototype.onPostLoad = function(criteria)
{
	if (criteria.isModal)
	{
		var w = $(window);
		var b = criteria.body;
		if (b.height() > w.height())
		{
			var tb = criteria.tbody;
			
			// Before resizing the tbody, get the widths of their cells.
			var rows = tb.rows;
			var bCells = rows[0].cells;
			var cells = [];
			for (var i = 0; i < bCells.length; i++)
				bCells[i].style.width = (cells[i] = bCells[i].clientWidth) + 'px';
			
			for (var i = 1; i < rows.length; i++)
			{
				bCells = rows[i].cells;
				for (var j = 0; j < cells.length; j++)
					bCells[j].style.width = cells[j] + 'px';
			}
			
			// Resize the tbody.
			var offset = (b.height() - tb.offsetHeight) + 25;
			tb.style.height = (w.height() - offset) + 'px';
			tb.className = this.CSS_MODAL;
			criteria.table.style.display = 'inline';
			
			// Now have to fix the widths of the head columns.
			var hCells = criteria.thead.rows[1].cells;
			for (var i = 0; i < cells.length; i++)
				hCells[i].style.width = cells[i] + 'px';
		}
	}
	
	if (this.onListPostLoad)
	        this.onListPostLoad(criteria);
}

ListTemplate.prototype.handleCancel = function(criteria)
{
	if (criteria.isModal)
		criteria.body.closeMe();
}

ListTemplate.prototype.doPaging = function(criteria, elem)
{
	var f = criteria.filter;
	f.page = elem.nextPage;
	if (elem.myPageToken)	// For Google Profile search.
		f.pageToken = elem.myPageToken;

	this.run(criteria);
}

ListTemplate.prototype.doSort = function(criteria, elem)
{
	var filter = criteria.filter;
	var value = criteria.value;

	filter.sortOn = elem.sortOn;

	// If current sort, just reverse the direction.
	if (value.sortOn == filter.sortOn)
		filter.sortDir = ('ASC' == value.sortDir) ? 'DESC' : 'ASC';
	else
		delete filter.sortDir;	// Will force the usage of the default on the backend.

	this.run(criteria);
}

ListTemplate.prototype.generate = function(criteria)
{
	var value = criteria.value;

	// For resources without a search call, an array will be returned instead.
	if ($.isArray(value))
		criteria.value = value = { total: value.length, pageSize: value.length, page: 1, pages: 1, records: value };

	var records = value.records;
	if (!records || (0 == records.length))
		return this.createNoRecordsFoundMessage(criteria);

	var e, r, c, o = criteria.table = document.createElement('table');
	this.appendHeader(criteria, o);
	this.appendBody(criteria, o);

	return o;
}

ListTemplate.prototype.createNoRecordsFoundMessage = function(criteria)
{
	var v = criteria.value;
	var msg = (v.message ? v.message : ('No ' + this.PLURAL + ' found.'));	// In case validation error on the search.
	var o = this.createDiv(msg, 'noRecordsFound');
	var me = this;

	// Append an anchor to add records if capable.
	if (this.CAN_ADD)
		o.appendChild(this.createAnchor('Click here to add a new ' + this.SINGULAR + '.',
			function(ev) { me.handleAdd(criteria, this); }));

	if (this.search)
		o.appendChild(this.createAnchor('Click here to apply a different search.',
			function(ev) { me.handleSearch(criteria, this); }));

	return o;
}

ListTemplate.prototype.appendHeader = function(criteria, table)
{
	var r, o = criteria.thead = table.createTHead();
	var c, cols = this.COLUMNS;

	this.insertPaging(table, o, criteria);
	r = this.insertRow(o);

	// Empty first column for actions like DELETE.
	if (this.HAS_ROW_ACTIONS)
		r.insertCell(0);

	var exclusions = $.extend({}, criteria.exclusions);
	for (var i = 0; i < cols.length; i++)
	{
		c = cols[i];

		// Exclude column?
		if (exclusions[c.id])
			continue;

		this.insertHeader(r, c.id, c.caption, criteria);
	}

	return o;
}

ListTemplate.prototype.appendBody = function(criteria, table)
{
	var a, text, cell, c, cols = this.COLUMNS;
	var record, records = criteria.value.records;
	var e, v, r, o = criteria.tbody = document.createElement('tbody');

	table.appendChild(o);

	var me = this;
	var exclusions = $.extend({}, criteria.exclusions);	// Column exclusions.

	for (var i = 0; i < records.length; i++)
	{
		record = records[i];
		r = this.insertRow(o);

		if (this.HAS_ROW_ACTIONS)
		{
			// Need this div to ensure whitespace doesn't wrap.
			e = this.addDiv(this.insertCell(r), undefined, 'rowActions');
			if (this.CAN_REMOVE)
			{
				e.appendChild(a = this.createAnchor('&chi;', function(ev) {
					me.removeRecord(criteria, this); }, 'delete'));
				a.myRecord = record;
			}

			this.addSpace(e);

			if (this.OPEN_CHILD)
			{
				e.appendChild(a = this.createAnchor('&crarr;',
					function(ev) { me.openChild(criteria, this); }, 'drilldown'));
				a.myRecord = record;

				this.decorateOpenChildAnchor(a, record);
			}

            this.addSpace(e);

            // Custom row actions.
            if (this.ROW_ACTIONS)
            {
				e.appendChild(a = this.createAnchor(undefined, function(ev) {
					return false; }, 'config'));
				a.myRecord = record;
				a.onmouseover = function(ev) { me.openActionsMenu(criteria, this); };
				a.onmouseout = function(ev) { me.closeActionsMenuSoon(criteria, this); };
            }
		}

		for (var j = 0; j < cols.length; j++)
		{
			c = cols[j];

			// Exclude column?
			if (exclusions[c.id])
				continue;

			text = this[c.formatter](v = record[c.id], record);

			// Should an anchor be created as the cell element.
			var onSelect = undefined;
			if (c.selectable)
				onSelect = 'handleSelect';
			else if (c.clickHandler)
				onSelect = c.clickHandler;

			if (onSelect)
			{
				cell = this.insertCell(r, c.css, a = this.createAnchor(text,
					function(ev) { me[this.myOnSelect](criteria, this); }));
				a.myOnSelect = onSelect;
				a.myRecord = record;
			}
			else if (this.CAN_EDIT && (undefined != v) && (typeof(v) == 'boolean'))
			{
				cell = this.insertCell(r, c.css, e = this.genCheckBox(c.id, v));
				cell.className+= ' center';
				e.onclick = function(ev) { me.toggleProperty(criteria, this); };
				e.myRecord = record;
			}
			else if (c.editable)
			{
				$(this.insertCell(r, c.css, text)).dblclick({ c: criteria, r: record, f: c, t: text }, function(ev) {
					// Only perform operation for left mouse clicks.
					// KA-19357 - on IE8 the ev.which property comes through as zero (0) for left mouse double clicks. No idea why.
					if (1 < ev.which)
						return;

					ev.preventDefault();
					var data = ev.data;
					var tgt = ev.target;
					var element = me.genTextBox(data.f.id);
					var d = data.r[data.f.id];
					element.style.width = (tgt.clientWidth - 16) + 'px';
					element.value = (d ? d : '');
					tgt.innerHTML = '';
					tgt.appendChild(element);

					element.focus();
					data.cell = tgt;
					$(element).blur(data, function(ev) { if (ev.target.cancelBlur) return; ev.data.cell.innerHTML = ev.data.t; }).change(data, function(ev) {
						ev.target.cancelBlur = true;
						me.updateField(ev.data, ev.target);
					});
				});
			}
			else
				this.insertCell(r, c.css, text);
		}
	}

	return o;
}

ListTemplate.prototype.openHistory = function(criteria, elem)
{
	this.HISTORY_VIEWER.open(elem.myRecord[this.IDENTIFIER]);
}

ListTemplate.prototype.openActionsMenu = function(criteria, elem)
{
	// Keep open if back over the option.
	this.clearMenuClose(elem);
	
	// Already open?
	if (elem.myMenu)
		return;
	
	var me = this;
	var record = elem.myRecord;
	var a, b, act, o = elem.myMenu = document.createElement('div');
	o.className = 'tabberDropdown';	// SERVICES-2659: added scrolling to the popup menu (CSS: tabberDropdown) with overflow:auto and maxHeight. DLS on 10/29/2018.
	(b = document.body).insertBefore(o, b.firstChild);
	
	for (var i = 0; i < this.ROW_ACTIONS.length; i++)
	{
		act = this.ROW_ACTIONS[i];
		
		// If a record condition is specified, exit if NOT met.
		if ((act.condition && !record[act.condition]) ||
		    (act.uncondition && record[act.uncondition]))
			continue;
		
		o.appendChild(a = this.createAnchor(act.caption, function(ev) {
			me[this.myId](criteria, this);
			me.closeActionsMenu(criteria, elem);
		}));
		a.myRecord = record;
		a.myId = act.id;
		a.onmouseover = function(ev) { me.clearMenuClose(elem); };
		a.onmouseout = function(ev) { me.closeActionsMenuSoon(criteria, elem); };
	}

	var s = o.style;
	var coords = this.getLocationRightTop(elem);
	s.display = 'block';

	// If past halfway on the screen, then display from the bottom up.
	var w = $(window);
	s.maxHeight = w.height() + 'px';	// SERVICES-2659: added scrolling to the popup menu (CSS: tabberDropdown). DLS on 10/29/2018.
	if ((w.height() / 2) < (coords.top - w.scrollTop()))
	{
		var top = (coords.bottom - $(o).outerHeight());
		if (top < w.scrollTop())        // Make sure not past top.
			top = w.scrollTop();
		s.top = top + 'px';
	}
	else
	{
		// Make sure not past bottom.
		var bottom = (coords.top + $(o).outerHeight());
		if (bottom > (w.height() + w.scrollTop()))
			coords.top-= (bottom - (w.height() + w.scrollTop()));
		s.top = coords.top + 'px';
	}
	s.left = coords.right + 'px';
}

ListTemplate.prototype.clearMenuClose = function(elem)
{
	if (elem.myMenuCloseId)
	{
		window.clearTimeout(elem.myMenuCloseId);
		delete elem.myMenuCloseId;
	}
}

ListTemplate.prototype.closeActionsMenu = function(criteria, elem)
{
	// Do NOT need to worry about myMenuCloseId because we are over the menu item.
	document.body.removeChild(elem.myMenu);
	delete elem.myMenu;
}

ListTemplate.prototype.closeActionsMenuSoon = function(criteria, elem)
{
	// Already closed?
	if (!elem.myMenu)
		return;
	
	// Close it in a half a second.
	elem.myMenuCloseId = window.setTimeout(function() {
		if (elem.myMenu && elem.myMenuCloseId)
		{
			document.body.removeChild(elem.myMenu);
			delete elem.myMenu;
			delete elem.myMenuCloseId;
		}
	}, 500);
}

ListTemplate.prototype.insertRow = function(table)
{
	var o = table.insertRow(table.rows.length);
	o.className = (0 == (o.rowIndex % 2)) ? 'even' : 'odd';

	return o;
}

ListTemplate.prototype.insertPaging = function(table, header, criteria)
{
	this.insertPaging_(header, criteria);
	this.insertPaging_(criteria.tfoot = table.createTFoot(), criteria);
}

ListTemplate.prototype.insertPaging_ = function(section, criteria)
{
	var me = this;
	var v = criteria.value;
	var o = this.insertRow(section);
	var e, c = o.insertCell(0);
	c.colSpan = this.DESC_COLS;

	if (1 == v.total)
		c.innerHTML = 'Showing only ' + this.SINGULAR;
	else
	{
		var size = v.records.length;
		if (1 == size)
			c.innerHTML = 'Showing one ' + this.SINGULAR;
		else
			c.innerHTML = 'Showing ' + size + ' ' + this.PLURAL;

		c.innerHTML+= ' out of ' + v.total;
	}

	// Place a ADD link in the header if available.
	if (this.CAN_ADD)
		c.appendChild(this.createAnchor('Add Item', function(ev) { me.handleAdd(criteria, this); }, 'action'));

	// Place a Search link in the header if available.
	if (this.search)
	{
		c.appendChild(this.createAnchor('Search', function(ev) { me.handleSearch(criteria, this); }, 'action'));

		// Creates search form seeded with the existing search criteria.
		if (criteria.hasSearchFilter)
			c.appendChild(this.createAnchor('Searched', function(ev) { me.handleSearched(criteria, this); }, 'action'));
	}

	// Place an Export link in the header.
	c.appendChild(this.createAnchor('Refresh', function(ev) { me.handleRefresh(criteria, this); }, 'action'));

	// Place an Export link in the header.
	c.appendChild(this.createAnchor('Export', function(ev) { me.generateCSV(criteria, this); }, 'action'));

	// Add custom actions.
	if (this.ACTIONS)
	{
		for (var i = 0; i < this.ACTIONS.length; i++)
		{
			var a, action = this.ACTIONS[i];
			c.appendChild(a = this.createAnchor(action.caption, function(ev) { me[this.myId](criteria, this); }, 'action'));
			a.myId = action.id;
		}
	}

	c = o.insertCell(1);
	c.colSpan = this.NAV_COLS;
	c.className = 'right';

	if (2 > v.pages)
		return;

	if (1 < v.page)
	{
		c.appendChild(e = this.createAnchor('&lArr; prev', function(ev) { me.doPaging(criteria, this); }));
		e.nextPage = v.page - 1;
		c.appendChild(document.createTextNode(' '));
	}

	// Incase there are more than 500 pages.
	var start = 1, end = v.pages;
	if (this.MAX_PAGES < v.pages)
	{
		if (this.HALF_MAX_PAGES < v.page)
			start = v.page - this.HALF_MAX_PAGES;
		end = start + this.MAX_PAGES;
		if (end > v.pages)
		{
			end = v.pages;
			start = end - this.MAX_PAGES;
		}
	}

	c.appendChild(e = document.createElement('select'));
	var j = 0;
	if (1 < start)	// Always include the first page as a quick way to all the way home.
		e.options[j++] = new Option(1, 1, false, 1 == v.page);
		
	for (var i = start; i <= end; i++, j++)
		e.options[j] = new Option(i, i, false, i == v.page);

	// Always include the last page for a quick way to the end.
	if (v.pages > end)
		e.options[j++] = new Option(v.pages, v.pages, false, v.pages == v.page);
		
	e.onchange = function(ev) { this.nextPage = this.value; me.doPaging(criteria, this); };

	if (v.pages > v.page)
	{
		c.appendChild(document.createTextNode(' '));
		c.appendChild(e = this.createAnchor('next &rArr;', function(ev) { me.doPaging(criteria, this); }));
		e.nextPage = v.page + 1;
		if (v.filter.pageToken)
			e.myPageToken = v.filter.pageToken;
	}
}

ListTemplate.prototype.insertHeader = function(row, field, caption, criteria)
{
	var a, me = this, o = row.insertCell(row.cells.length);
	o.appendChild(a = this.createAnchor(caption, function(ev) { me.doSort(criteria, this); }));
	a.sortOn = field;

	// Place a moniker on the currently selected sort field to see which way it is sorted.
	var v = criteria.value;
	if (field == v.sortOn)
		o.appendChild(this.createSpan(' ' + (('ASC' == v.sortDir) ? '&uarr;' : '&darr;')));

	return o;
}

ListTemplate.prototype.insertCell = function(row, css, value)
{
	var o = row.insertCell(row.cells.length);
	o.className = (css ? css : row.className);

	if (undefined != value)
	{
		var t = typeof(value);
		if (('string' == t) || ('number' == t))
			o.innerHTML = value;
		else
			o.appendChild(value);
	}

	return o;
}

// Can decorate the open child anchor with the number of children. If the record,
// has a property called numberOfChildren then that is used. Otherwise, look to
// see if a COUNT_CHILDREN_URL exists and make the AJAX call to get the count.
ListTemplate.prototype.decorateOpenChildAnchor = function(elem, record)
{
	if (undefined != record.numberOfChildren)
	{
		this.addSpan(elem, ' (' + this.toWhole(record.numberOfChildren) + ')');
		return;
	}

	var me = this;

	// Should we also place the number of children in the anchor?
	if (this.COUNT_CHILDREN_URL)
		Template.post(this.COUNT_CHILDREN_URL, { id: record.id }, function(count) {
			me.addSpan(elem, ' (' + me.toWhole(count) + ')');
		});
}

ListTemplate.prototype.handleAdd = function(criteria, elem)
{
	var me = this;
	var body = this.NO_EDITOR_MODAL ? criteria.body : undefined;
	this.EDITOR.doAdd(function(value) { me.run(criteria, undefined, me.SEARCH_METHOD); }, body, criteria.defaultValue, criteria.submitUrl);
}

ListTemplate.prototype.handleCopy = function(criteria, elem)
{
	var r = this.cloneObject(elem.myRecord);
	delete r[this.IDENTIFIER];

	var me = this;
	var body = this.NO_EDITOR_MODAL ? criteria.body : undefined;
	this.EDITOR.doAdd(function(value) { me.run(criteria, undefined, me.SEARCH_METHOD); }, body, r, criteria.submitUrl);
}

ListTemplate.prototype.handleSelect = function(criteria, elem)
{
	var me = this;
	var record = elem.myRecord;
	var body = this.NO_EDITOR_MODAL ? criteria.body : undefined;
	this.EDITOR.doEdit(record[this.IDENTIFIER], function(value) { me.run(criteria, undefined, me.SEARCH_METHOD); }, body, criteria.submitUrl);
}

ListTemplate.prototype.handleSearch = function(criteria, elem)
{
	var me = this;
	var body = this.NO_SEARCH_MODAL ? criteria.body : undefined;
	this.search.doSearch(function(filter) {
		if (filter)
			criteria.filter = filter;
		criteria.url = me.SEARCH_PATH;
		criteria.hasSearchFilter = true;
		me.run(criteria, undefined, me.SEARCH_METHOD);
	}, body, criteria.baseFilter);
}

ListTemplate.prototype.handleSearched = function(criteria, elem)
{
	var me = this;
	var body = this.NO_SEARCH_MODAL ? criteria.body : undefined;
	this.search.doSearch(function(filter) {
		if (filter)
			criteria.filter = filter;
		criteria.url = me.SEARCH_PATH;
		criteria.hasSearchFilter = true;
		me.run(criteria, undefined, me.SEARCH_METHOD);
	}, body, criteria.value.filter);
}

ListTemplate.prototype.handleBig = function(criteria, elem)
{
	var me = this;
	var body = this.NO_SEARCH_MODAL ? criteria.body : undefined;
	this.search.doSearch(function(filter) {
		if (filter)
			criteria.filter = filter;
		criteria.url = me.SEARCH_BIG;
		criteria.hasSearchFilter = true;
		me.run(criteria, undefined, me.SEARCH_METHOD);
	}, body, criteria.baseFilter);
}

ListTemplate.prototype.handleRefresh = function(criteria, elem)
{
	if (this.search)
		this.run(criteria, criteria.body, this.SEARCH_METHOD);
	else
		this.init(criteria.body);
}

ListTemplate.prototype.removeRecord = function(criteria, elem)
{
	if (!window.confirm('Click OK to confirm and continue with your delete.'))
		return;

	var me = this;
	this.remove(this.RESOURCE, elem.myRecord[this.IDENTIFIER], function(value) {
		if (value.isError)
			window.alert(value.message);
		else
			me.run(criteria, undefined, me.SEARCH_METHOD);
	});
}

ListTemplate.prototype.updateField = function(data, elem)
{
	var me = this;
	var record = data.r;
	var criteria = data.c;
	var f = data.f;

	record[f.id] = elem.value;
	this[this.EDIT_METHOD](this.RESOURCE, record, function(value) {
		if (value.isError)
		{
			if (value.message)
				window.alert(value.message);
			else
				window.alert('An unexpected error occurred. Please contact the administrator.');
			elem.focus();
			elem.cancelBlur = false;
		}
		else
		{
			var parent = elem.parentNode;
			parent.innerHTML = data.t = elem.value;
			var bg = parent.style.backgroundColor;
			parent.style.backgroundColor = 'red';
			setTimeout(function() { parent.style.backgroundColor = bg; }, 500);
		}
	});
}

ListTemplate.prototype.toggleProperty = function(criteria, elem)
{
	var me = this;
	var record = elem.myRecord;

	record[elem.name] = elem.checked;
	this[this.EDIT_METHOD](this.RESOURCE, record, function(value) {
		if (value.isError)
		{
			if (value.message)
				window.alert(value.message);
			else
				window.alert('An unexpected error occurred. Please contact the administrator.');
			record[elem.name] = elem.checked = !elem.checked;	// Reverse the toggle if there is an error.
		}
		else
		{
			var parent = elem.parentNode;
			var bg = parent.style.backgroundColor;
			parent.style.backgroundColor = 'red';
			setTimeout(function() { parent.style.backgroundColor = bg; }, 500);
		}
	});
}

/** Populate a viewer with the data returned as CSV file. */
ListTemplate.prototype.generateCSV = function(c, e)
{
	var records = c.value.records;
	var o = '';
	var d = ',';
	var l = '\n';
	var columns = this.EDITOR ? this.EDITOR.FIELDS : this.COLUMNS;
	var numOfColumns = columns.length;

	// Output the column names first.
	for (var i = 0; i < numOfColumns; i++)
	{
		if (0 < i)
			o+= d;
		o+= columns[i].caption;
	}
	o+= l;

	// Output the data.
	for (var i = 0; i < records.length; i++)
	{
		var r = records[i];
		for (var c = 0; c < numOfColumns; c++)
		{
			if (0 < c)
				o+= d;

			var v = r[columns[c].id];
			if (undefined != v)
			{
				var t = typeof(v);
				if (('boolean' == t) || ('number' == t))
					o+= v;
				else
					o+= ('"' + v + '"');
			}
		}
		o+= l;
	}

	this.exporter.open(o);
}

ListTemplate.prototype.EXPORT = {
	NAME: 'noCaption',	// Uses CSS to hide field.

	getTitle: function(c) { return 'Generated Output'; },
	open: function(payload) { this.run({ value: { payload: payload }, filter: { isAdd: true }}); },

	FIELDS: [ new EditField('payload', '', false, true, 100, 30) ],

	CAPTION_SUBMIT: undefined,
	CAPTION_CANCEL: Template.prototype.CAPTION_CLOSE
};
