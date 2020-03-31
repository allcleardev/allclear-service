function TabTemplate() {}
function Tab(id, caption, children, right, onlyAction)
{
	this.id = id;
	this.caption = caption;
	this.children = children;
	this.right = right;	// Sub-menu to the right.
	this.onlyAction = onlyAction;	// Sub-menu option. Indicates that no tab UI changes are required. DLS on 5/14/2013.
}

TabTemplate.prototype = new Template();
TabTemplate.prototype.CSS_MAIN = 'tabber';
TabTemplate.prototype.CSS_TABS = 'tabs';

TabTemplate.prototype.init = function(body)
{
	var me = this;
	var criteria = { anchors: [] };	// Maintains the state.
	var a, aa, b, t, o = document.createElement('div');
	o.className = this.CSS_MAIN;

	// Create the links and tabs.
	o.appendChild(aa = this.createElement('div', undefined, this.CSS_TABS));
	for (var i = 0; i < this.TABS.length; i++)
	{
		a = undefined;
		t = this.TABS[i];

		// Does the tab have an action?
		if (t.id)
		{
			aa.appendChild(a = criteria.anchors[i] = this.createAnchor(t.caption,
				function(ev) { me.handleAction(criteria, this); }));

			criteria[t.id] = a;
		}

		// Does the tab have a sub-menu?
		if (t.children)
		{
			if (!a)
			{
				aa.appendChild(a = criteria.anchors[i] = this.createAnchor(t.caption, function(ev) {
					// If the tab has been initialized, select the tab and close the sub-menu.
					if (this.myInit)
					{
						me.showTab(criteria, this);
						me.closeMenuNow(criteria, this);
					}
				}));
			}
			a.onmouseover = function(ev) { me.openMenu(criteria, this); };
			a.onmouseout = function(ev) { me.closeMenuSoon(criteria, this); };
		}

		a.myIndex = i;
		a.myInit = false;
		a.myTab = t;

		o.appendChild(b = document.createElement('div'));
		(a.myBody = $(b)).hide();
	}

	// Show the first tab.
	t = criteria.currentTab = this.TABS[0];
	a = criteria.currentAnchor = criteria.anchors[0];
	(b = a.myBody).show();
	a.myInit = true;
	a.className = 'selected';
	this[t.id](b);

	body.empty();
	body.append(o);

	if (undefined != this.onPostInit)
		this.onPostInit(criteria);

	return criteria;
}

TabTemplate.prototype.changeTab = function(criteria, index)
{
	var a = criteria.anchors;
	if ((0 > index) || (a.length <= index))
		throw 'The INDEX is not valid.';

	a = a[index];
	this.handleAction(criteria, a);

	return a.myBody;
}

TabTemplate.prototype.changeTabById = function(criteria, id)
{
	var a = criteria[id];
	if (!a)
		throw 'The INDEX is not valid.';

	this.handleAction(criteria, a);

	return a.myBody;
}

TabTemplate.prototype.handleAction = function(criteria, elem)
{
	var a = criteria.currentAnchor;
	a.myBody.hide();
	a.className = '';

	var t = criteria.currentTab = elem.myTab;
	criteria.currentAnchor = elem;

	// Only re-init, if the user has clicked on the selected tab. Otherwise just display.
	if (a.myIndex == elem.myIndex)
		elem.myInit = false;

	// Must show the body first so that focus can be set to generated fields.
	elem.className = 'selected';
	elem.myBody.show();

	if (!elem.myInit)
	{
		elem.myInit = true;
		this[t.id](elem.myBody);
	}
}

/** Switches to the tab without calling "handleAction". */
TabTemplate.prototype.showTab = function(criteria, elem)
{
	var current = criteria.currentAnchor;
	current.myBody.hide();
	current.className = '';

	criteria.currentTab = elem.myTab;
	criteria.currentAnchor = elem;

	elem.className = 'selected';
	elem.myBody.show();
}

TabTemplate.prototype.openMenu = function(criteria, elem)
{
	// Keep open if back over the tab.
	if (elem.mySubMenuCloseId)
	{
		window.clearTimeout(elem.mySubMenuCloseId);
		delete elem.mySubMenuCloseId;
	}

	// Is the menu already open?
	if (elem.mySubMenu)
		return;

	var me = this;
	var tab = elem.myTab;
	var a, b, o = elem.mySubMenu = document.createElement('div');
	o.className = 'tabberDropdown';
	(b = document.body).insertBefore(o, b.firstChild);

	var child, children = tab.children;
	for (var i = 0; i < children.length; i++)
	{
		child = children[i];
		o.appendChild(a = this.createAnchor(child.caption, function(ev) {
			if (this.myTab.onlyAction)
				me[this.myTab.id]();
			else
			{
				me.showTab(criteria, elem);
				me[this.myTab.id](elem.myBody);
				elem.myInit = true;
			}

			me.closeMenuNow(criteria, elem);
		}));

		// Keep open if back over the sub-menu.
		a.onmouseover = function(ev) {
			if (elem.mySubMenuCloseId)
			{
				window.clearTimeout(elem.mySubMenuCloseId);
				delete elem.mySubMenuCloseId;
			}
		};

		a.onmouseout = function(ev) { me.closeMenuSoon(criteria, elem); };
		a.myTab = child;
	}

	var s = o.style;
	var coords = Template.getLocationBelow(elem);
	s.top = coords.top + 'px';
	s.display = 'block';
	if (tab.right)
		coords.left-= (o.clientWidth - elem.clientWidth);
	s.left = coords.left + 'px';
}

/** Close the sub menu now. */
TabTemplate.prototype.closeMenuNow = function(criteria, elem)
{
	// Do NOT need to worry about mySubMenuCloseId because we are over the menu item.
	document.body.removeChild(elem.mySubMenu);
	delete elem.mySubMenu;
}

/** Schedule the close. Give a little time to see if the user mouses over another part of the menu. */
TabTemplate.prototype.closeMenuSoon = function(criteria, elem)
{
	// Is the menu already closed?
	if (!elem.mySubMenu)
		return;

	elem.mySubMenuCloseId = window.setTimeout(function() {
		if (elem.mySubMenu && elem.mySubMenuCloseId)
		{
			document.body.removeChild(elem.mySubMenu);
			delete elem.mySubMenu;
			delete elem.mySubMenuCloseId;
		}
	}, 500);
}