var ProgressBar = {}

ProgressBar.INTERVAL = 2000;
ProgressBar.ITERATIONS = 20;
ProgressBar.CAPTION = 'Loading ...';

ProgressBar.start = function(criteria)
{
	var me = this;
	var b = criteria.progressBarBody = $('<blockquote>');

	criteria.body.empty();
	criteria.body.append(b);
	b.html(criteria.progressBarLastCaption = this.CAPTION);
	criteria.progressBarInterval = 1;
	criteria.progressBarIntervalId = window.setInterval(function() {
		// Only run so many iterations.
		if (me.ITERATIONS < criteria.progressBarInterval++)
			me.stop(criteria);
		else
			b.html((criteria.progressBarLastCaption+= '.'));
	}, this.INTERVAL);
}

ProgressBar.stop = function(criteria)
{
	if (criteria.progressBarIntervalId)
	{
		window.clearInterval(criteria.progressBarIntervalId);
		criteria.progressBarIntervalId = undefined;
	}
}