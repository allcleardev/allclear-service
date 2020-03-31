var PermissionsViewer = {
	CSS_MAIN: 'bulkAnswers',
	COLUMNS: [ 'ID', 'Post?', 'Get?', 'Put?', 'Del?' ],

	create: function(name, records, extra, callback) {
		var o = document.createElement('span');
		o.className = this.CSS_MAIN;

		if (!records || (0 == records.length))
		{
			o.innerHTML = 'No permissions to view.';
			return o;
		}

		var t = this.appendHeader(document.createElement('table'));
		t.className = this.CSS_MAIN;
		var cb, b = document.createElement('tbody');
		var row = 0;
		for (var i in records)
		{
			var rec = records[i];
			var r = b.insertRow(row++);

			r.insertCell(0).innerHTML = i;
			r.insertCell(1).innerHTML = Template.toText(rec.post);
			r.insertCell(2).innerHTML = Template.toText(rec.get);
			r.insertCell(3).innerHTML = Template.toText(rec.put);
			r.insertCell(4).innerHTML = Template.toText(rec['delete']);
		}

		t.appendChild(b);
		o.appendChild(t);

		return o;
	},

	appendHeader: function(t) {
		var o = t.createTHead();
		var r = o.insertRow(0);
		for (var i in this.COLUMNS)
			r.insertCell(i).innerHTML = this.COLUMNS[i];

		return t;
	}
};