var smObj;
var tsObj;

var accountView = function() {
	var gconfig = {
		'summary' : {
			'input' : {
				"_sheet" : "개요",
				"_event" : "getList",
				'input' : "[2,2,23,'_end']"
			}
		},
		'transaction' : {
			'input' : {
				"_sheet" : "개요",
				"_event" : "getList",
				'input' : "[26,2,'_end','_end']"
			}
		}
	}

	us = new GWeb(gconfig);

	var columns = [];
	var cdf = [ {
		targets : [ 0, 1 ],
		className : 'col_center'
	}, {
		targets : [ 2, 3 ],
		className : 'col_month'
	}, {
		targets : [ 4, 5 ],
		className : 'col_year'
	}, {
		targets : [ 6, 7, 8 ],
		className : 'col_total_year'
	} ];

	var summaryObj;
	var transactionObj;
	summaryObj = $('#summary').DataTable({
		columnDefs : cdf,
		paging : false,
		lengthChange : false,
		searching : false,
		bInfo : false,
		autoWidth : true
	});

	var cdf2 = [ {
		targets : [ 0, 1 ],
		className : 'col_center'
	} ];

	transactionObj = $('#transaction').DataTable({
		columnDefs : cdf2,
		paging : false,
		lengthChange : false,
		searching : false,
		bInfo : false,
		autoWidth : true
	});

	executeQuery(function() {
		viewSummary(summaryObj, transactionObj, function() {
		});
	});
}

var statsView = function() {
	var gconfig = {
		'sm' : {
			'input' : {
				"_sheet" : "통계",
				"_event" : "getList",
				'input' : "[2,2,23,'_end']"
			}
		},
		'ts' : {
			'input' : {
				"_sheet" : "통계",
				"_event" : "getList",
				'input' : "[26,2,'_end','_end']"
			}
		}
	}

	us = new GWeb(gconfig);

	var columns = [];

	var cdf = [
			{
				targets : [ 0 ],
				className : 'col_center'
			},
			{
				targets : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
						16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
						30, 31, 32, 33, 34, 35, 36, 37, 38 ],
				className : 'col_total_year'
			} ];

	if (!$.fn.DataTable.isDataTable('#sm')) {
		smObj = $('#sm').DataTable({
			columnDefs : cdf,
			scrollX : true,
			searching : false,
			paging : false,
			sort : false,
			bInfo : false,
			scrollCollapse : true
		});
	}

	var cdf2 = [
			{
				targets : [ 0 ],
				className : 'col_center'
			},
			{
				targets : [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
						16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26 ],
				className : 'col_total_year'
			} ];

	if (!$.fn.DataTable.isDataTable('#ts')) {
		tsObj = $('#ts').DataTable({
			columnDefs : cdf2,
			scrollX : true,
			searching : false,
			paging : false,
			sort : false,
			bInfo : false,
			scrollCollapse : true
		});
	}

	executeQuery(function() {
		viewStats(smObj, tsObj, function() {
			if (!$.fn.DataTable.isDataTable('#sm')) {
				new $.fn.dataTable.FixedColumns(smObj, {
					leftColumns : 3
				});
			}
			if (!$.fn.DataTable.isDataTable('#ts')) {
				new $.fn.dataTable.FixedColumns(tsObj, {
					leftColumns : 3
				});
			}
		});
	});
}

var monthlyView = function() {
	var gconfig = {
		'monthly' : {
			'input' : {
				"_sheet" : "1월",
				"_event" : "getList",
				'input' : "[2,1,'_end','_end']"
			}
		}
	}

	us = new GWeb(gconfig);

	var monthlyObj;
	var columns = [];
	var month;

	var cdf = [ {
		targets : [ 0 ],
		className : 'col_center'
	}, {
		targets : [ 6, 7, 8 ],
		className : 'col_total_year'
	} ];

	monthlyObj = $('#monthly').DataTable({
		columnDefs : cdf,
		searching : false,
		paging : false,
		sort : false,
		bInfo : false,
		scrollCollapse : true
	});

	executeQuery(function() {
		viewMonthly(monthlyObj, function() {
		});
	});

	$("#month").change(function() {
		month = $('#month').val();
		gconfig.monthly.input._sheet = $("#month").val();
		us = new GWeb(gconfig);
		viewMonthly(monthlyObj, function() {
		});
	});

}

function executeQuery(cb) {

	var gconfig = {
		'gauth' : {
			'url' : 'https://script.google.com/macros/s/AKfycbzFe_XXpMdAUnoJDjgsz46LGuZFDT1qBuXfWI5Ljiu2uF4mlFk/exec',
			'client_id' : '1049868489243-16ehvu7nkrfec2kltkikj6q0r79knurj.apps.googleusercontent.com'
		},
		'initDoc' : {
			'input' : {
				"_folderPath" : "account",
				"_doc" : "accountBook",
				"_event" : "initDoc"
			}
		}
	}

	gconfig.initDoc.input._doc = $('#sheetName').val();

	if (!us.config.gauth) {
		us.config.gauth = gconfig.gauth;
		us.config.initDoc = gconfig.initDoc;
	}

	var _docId = sessionStorage.getItem("_docId");
	if (!_docId) {
		us.execute('initDoc', {}, function(rslt) {
			us._docId = rslt.output.shared.docId;
			if (cb) {
				cb.call(null);
			}
		})
	} else {
		if (cb) {
			cb.call(null);
		}
	}
}

function viewSummary(summaryObj, transactionObj, cb) {

	us.execute('summary', {}, function(rslt) {
		if ($.fn.DataTable.isDataTable('#summary')) {
			$('#summary').DataTable().destroy();
		}
		$('#summary tbody').empty();
		summaryObj.clear();

		if (rslt.output) {
			for (var i = 1; i < rslt.output.rows.length; i++) {
				var row = rslt.output.rows[i];
				if (row[0]) {
					try {
						var row2 = formatCurrency(row[2]);
						var row3 = formatCurrency(row[3]);
						var row4 = formatCurrency(row[4]);
						var row5 = formatCurrency(row[5]);
						var row6 = formatCurrency(row[6]);
						var row7 = formatCurrency(row[7]);
						var row8 = formatPercent(row[8]);
						var rowObj = summaryObj.row.add(
								[ row[0], row[1], row2, row3, row4, row5, row6,
										row7, row8 ]).draw(false);
						if (i == 1 || i > 20) {
							$(rowObj.nodes()).addClass('col_total_year');
						}
					} catch (e) {
						debugger;
					}
				}
			}
		}

		us.execute('transaction', {}, function(rslt) {
			if ($.fn.DataTable.isDataTable('#transaction')) {
				$('#transaction').DataTable().destroy();
			}
			$('#transaction tbody').empty();
			transactionObj.clear();

			if (rslt.output) {
				for (var i = 1; i < rslt.output.rows.length; i++) {
					var row = rslt.output.rows[i];
					if (row[0]) {
						var row2 = formatCurrency(row[2]);
						var row3 = formatCurrency(row[3]);
						var rowObj = transactionObj.row.add(
								[ row[0], row[1], row2, row3 ]).draw(false);
						if (i > 5) {
							$(rowObj.nodes()).addClass('col_total_year');
						}
					}
				}
			}

			$('thead > tr > th').css('vertical-align', 'middle');
			$('tbody > tr > td').css('vertical-align', 'middle');
			if (cb) {
				cb.call(null);
			}
		});
	});
}

function viewStats(smObj, tsObj, cb) {

	us.execute('sm', {}, function(rslt) {
		$('#sm tbody').empty();
		smObj.clear();

		if (rslt.output) {
			for (var i = 1; i < rslt.output.rows.length; i++) {
				var row = rslt.output.rows[i];
				if (row[0]) {
					try {
						var row1 = formatCurrency(row[1]);
						var row2 = formatCurrency(row[2]);
						var rowArry = [ row[0], row1, row2 ];
						for (var j = 3; j <= 38; j++) {
							if (j % 3 == 2) {
								rowArry.push(formatPercent(row[j]));
							} else {
								rowArry.push(formatCurrency(row[j]));
							}
						}
						var rowObj = smObj.row.add(rowArry).draw(false);
						if (i > 23) {
							$(rowObj.nodes()).addClass('col_total_year');
						}
					} catch (e) {
						debugger;
					}
				}
			}
		}

		us.execute('ts', {}, function(rslt) {
			if ($.fn.DataTable.isDataTable('#ts')) {
				$('#ts').DataTable().destroy();
			}
			$('#ts tbody').empty();
			tsObj.clear();

			if (rslt.output) {
				for (var i = 1; i < rslt.output.rows.length; i++) {
					var row = rslt.output.rows[i];
					if (row[0]) {
						var rowArry = [ row[0] ];
						for (var j = 1; j <= 27; j++) {
							rowArry.push(formatCurrency(row[j]));
						}
						var rowObj = tsObj.row.add(rowArry).draw(false);
						if (i > 5) {
							$(rowObj.nodes()).addClass('col_total_year');
						}
					}
				}
			}

			$('thead > tr > th').css('vertical-align', 'middle');
			$('tbody > tr > td').css('vertical-align', 'middle');
			if (cb) {
				cb.call(null);
			}
		});
	});
}

function viewMonthly(monthlyObj, cb) {
	us.execute('monthly', {}, function(rslt) {
		if ($.fn.DataTable.isDataTable('#monthly')) {
			$('#monthly').DataTable().destroy();
		}
		$('#monthly tbody').empty();
		monthlyObj.clear();

		if (rslt.output) {
			for (var i = 1; i < rslt.output.rows.length; i++) {
				var row = rslt.output.rows[i];
				if (row[0]) {
					var date = moment(row[0]).format('YYYY-MM-DD');
					var rowArry = [ date ];
					for (var j = 1; j <= 10; j++) {
						if (j >= 4 && j <= 9) {
							rowArry.push(formatCurrency(row[j]));
						} else {
							rowArry.push(row[j]);
						}
					}
					var rowObj = monthlyObj.row.add(rowArry).draw(false);
					if (i < 3) {
						$(rowObj.nodes()).addClass('col_total_year');
					}
				}
			}
		}

		$('thead > tr > th').css('vertical-align', 'middle');
		$('tbody > tr > td').css('vertical-align', 'middle');
		if (cb) {
			cb.call(null);
		}
	});
}

function formatCurrency(input) {
	try {
		if (input === '') {
			return input;
		} else if (typeof input == 'number') {
			return input.toLocaleString();
		} else {
			return input;
		}
	} catch (e) {
		debugger;
	}
}

function formatPercent(input) {
	try {
		return input === '' ? input : Math.round(parseFloat(input) * 100, -1);
	} catch (e) {
		debugger;
	}
}
