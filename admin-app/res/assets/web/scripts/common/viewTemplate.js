function ViewTemplate(properties) { this.load(properties); }

ViewTemplate.prototype = new Template();

ViewTemplate.prototype.getTitle = function(criteria) { return this.SINGULAR; }

ViewTemplate.prototype.init = function()
{
	this.run({ value: {}, filter: { isAdd: false }});
}

ViewTemplate.prototype.open = function(params, body, url)
{
	if (undefined == url)
		url = this.GET_URL;

	this.run({ filter: params, url: url }, body, 'get');
}

ViewTemplate.prototype.openById = function(id, params, body)
{
	this.open(params, body, this.GET_URL + id);
}

ViewTemplate.prototype.onPostLoad = function(criteria)
{
	if (criteria.isModal)
	{
		criteria.body.center();
		criteria.header.style.width = criteria.form.offsetWidth + 'px';	// On IE7 the H1 doesn't do a 100% width. DLS on 3/28/2011.
	}

	if (this.onViewerPostLoad)
		this.onViewerPostLoad(criteria);
}

ViewTemplate.prototype.handleCancel = function(criteria)
{
	if (criteria.isModal)
		criteria.body.closeMe();
}

ViewTemplate.prototype.generate = function(criteria)
{
	var me = this;
	var isModal = criteria.isModal;
	var e, s, o = criteria.form = this.createElement('form');
	var value = criteria.value;

	o.className = this.NAME;

	this.append(criteria, o);

	s = this.addDiv(o, undefined, 'close');
	s.appendChild(this.genButton('closer', 'Close',
		function(ev) { me.handleCancel(criteria); }));

	return o;
}

ViewTemplate.prototype.append = function(criteria, o)
{
	var e, s, value = criteria.value;

	s = this.addDiv(o);
	s.appendChild(this.createImg('images/ashley.png', 'Ashley&nbsp;da\'Branch'));

	this.addElem(s, 'h3', 'Ashley&nbsp;da\'Branch', 'center');
}
