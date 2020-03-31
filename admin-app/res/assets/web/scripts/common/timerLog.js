var META_TIMER_LOG = {
	NAME: 'timerLog',
	SEARCH_URL: '/member/searchTimerLog.json',
	SINGULAR: 'Timer Log',
	PLURAL: 'Timer Log',
	COLUMNS: [ new TextColumn('targetName', 'Service Name'), new TextColumn('signature', 'Method Name'), new TextColumn('count', 'Count'),
	           new TextColumn('totalDuration', 'Total Duration<br />(seconds)', 'toSeconds'),
	           new TextColumn('lowDuration', 'Low Duration<br />(seconds)', 'toSeconds'),
	           new TextColumn('highDuration', 'High Duration<br />(seconds)', 'toSeconds'),
	           new TextColumn('avgDuration', 'Avg. Duration<br />(seconds)', 'toSeconds', false, true),
	           new TextColumn('standardDeviation', 'Std. Dev.<br />(seconds)', 'toSeconds'),
	           new TextColumn('standardDeviationPct', 'Std. Dev.<br />(%)', 'toPercent') ],
	ACTIONS: [ { id: 'resetBizCache', caption: 'Reset Biz Cache' }, { id: 'resetDaoCache', caption: 'Reset DAO Cache' },
	           { id: 'resetStats', caption: 'Reset Stats' } ],
	resetBizCache: function(criteria) { this.resetCache(criteria, 'biz'); },
	resetDaoCache: function(criteria) { this.resetCache(criteria, 'dao'); },
	resetCache: function(criteria, type) {
		this.post('/member/resetCache.json', { id: type }, function(value) {
			window.alert(value.isError ? value.message : 'The cache was cleared.');
		});
	},
	resetStats: function(criteria) {
		if (!window.confirm('Click OK to continue with the statistics reset.'))
			return;

		var me = this;
		this.post('/member/resetTimerLog.json', {}, function(value) {
			if (value.isError)
				window.alert(value.message);
			else
				me.run(criteria);
		});
	}
};

var TimerLogHandler = new ListTemplate(META_TIMER_LOG);
