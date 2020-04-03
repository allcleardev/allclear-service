var LoginHandler = new EditTemplate({
	NAME: 'login',
	SINGULAR: 'Login',
	PLURAL: 'Login',
	RESOURCE: 'admins/self',

	getTitle: function(c) { return this.SINGULAR; },

	FIELDS: [ new EditField('userName', 'User Name', true, false, 50, 30),
	          new PassField('password', 'Password', true, 50, 30),
	          new BoolField('rememberMe', 'Remember Me?', false) ],
	/*onEditorPostLoad: function(criteria)
	{
		var h = criteria.header;
		var a = h.lastChild;
		h.innerHTML = 'Login';
		h.appendChild(a);	// Close (X) anchor.
	}, */
	doLogin: function(cb, body) {
		var me = this;
		var callback = function(v) {
			me.setCurrentSessionId(v.id, v.userName, v.rememberMe);
			me.anchorUp();
			if (cb)
				cb();
		};
		this.run({ filter: { isAdd: true }, value: { value: {} }, callback: callback, submitUrl: 'admins/auth' }, body);
	},
	doLogout: function() {
		var me = this;
		this.remove(this.RESOURCE, undefined, function(v) { me.deleteCurrentSessionId(); me.anchorUp(); });
	},
	doStart: function() {
		this.ANCHOR = document.getElementById('loginLink');
		this.LOGIN_INFO = document.getElementById('loginInfo');
		this.anchorUp();
	},
	anchorUp: function()
	{
		var me = this;
		this.auth(this.RESOURCE, {}, function(v) {
			var e = me.ANCHOR;
			var a = me.LOGIN_INFO;
			if (null == v)
			{
				a.innerHTML = '';
				a.onclick = a.myRecord = undefined;
				e.innerHTML = 'Login';
				e.onclick = function(ev) { me.doLogin(); return false; };
			}
			else
			{
				a.innerHTML = v.id;
				a.myRecord = v;
				a.onclick = function(ev) { var t = this; AdminsHandler.EDITOR.doEdit(this.myRecord.id); };
				e.innerHTML = 'Logout';
				e.onclick = function(ev) { me.doLogout(); return false; };
			}
		});
	}
});
