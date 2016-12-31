
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

function viewSummary() {
	us.execute('initDoc', {}, function(rslt) {
		us._docId = rslt.output.shared.docId;

		us.execute('summary', {}, function(rslt) {
			if ($.fn.DataTable.isDataTable('#summary')) {
				$('#summary').DataTable().destroy();
			}
			$('#summary tbody').empty();
			summaryObj.clear();

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
						var row8 = row[8] === '' ? row[8] : Math.round(
								parseFloat(row[8]) * 100, -1);
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

			$('thead > tr > th').css('vertical-align', 'middle');
			$('tbody > tr > td').css('vertical-align', 'middle');
		});

		us.execute('transaction', {}, function(rslt) {
			if ($.fn.DataTable.isDataTable('#transaction')) {
				$('#transaction').DataTable().destroy();
			}
			$('#transaction tbody').empty();
			transactionObj.clear();

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

		});
	})
}

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
	},
	'summary' : {
		'input' : {
			"_sheet" : "summary",
			"_event" : "getList",
			'input' : "[2,2,23,'_end']"
		}
	},
	'transaction' : {
		'input' : {
			"_sheet" : "summary",
			"_event" : "getList",
			'input' : "[26,2,'_end','_end']"
		}
	}
}

var us = new GWeb(gconfig);