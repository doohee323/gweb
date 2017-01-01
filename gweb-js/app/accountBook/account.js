function viewSummary(cb) {
	us.execute('initDoc', {}, function(rslt) {
		us._docId = rslt.output.shared.docId;

		us.execute('summary', {}, function(rslt) {
			// if ($.fn.DataTable.isDataTable('#summary')) {
			// $('#summary').DataTable().destroy();
			// }
			// $('#summary tbody').empty();
			// summaryObj.clear();

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

			us.execute('transaction', {}, function(rslt) {
				// if ($.fn.DataTable.isDataTable('#transaction')) {
				// $('#transaction').DataTable().destroy();
				// }
				// $('#transaction tbody').empty();
				// transactionObj.clear();

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

				$('thead > tr > th').css('vertical-align', 'middle');
				$('tbody > tr > td').css('vertical-align', 'middle');
				if (cb) {
					cb.call(null);
				}
			});
		});
	})
}

function viewStats(cb) {
	us.execute('initDoc', {}, function(rslt) {
		us._docId = rslt.output.shared.docId;

		us.execute('summary', {}, function(rslt) {
			// if ($.fn.DataTable.isDataTable('#summary')) {
			// $('#summary').DataTable().destroy();
			// }
			// $('#summary tbody').empty();
			// summaryObj.clear();

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
						var rowObj = summaryObj.row.add(rowArry).draw(false);
						if (i > 23) {
							$(rowObj.nodes()).addClass('col_total_year');
						}
					} catch (e) {
						debugger;
					}
				}
			}

			us.execute('transaction', {}, function(rslt) {
				// if ($.fn.DataTable.isDataTable('#transaction')) {
				// $('#transaction').DataTable().destroy();
				// }
				// $('#transaction tbody').empty();
				// transactionObj.clear();

				for (var i = 1; i < rslt.output.rows.length; i++) {
					var row = rslt.output.rows[i];
					if (row[0]) {
						var rowArry = [ row[0] ];
						for (var j = 1; j <= 27; j++) {
							rowArry.push(formatCurrency(row[j]));
						}
						var rowObj = transactionObj.row.add(rowArry)
								.draw(false);
						if (i > 5) {
							$(rowObj.nodes()).addClass('col_total_year');
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
	})
}

function viewMonth(cb) {
	us.execute('initDoc', {}, function(rslt) {
		us._docId = rslt.output.shared.docId;

		us.execute('transaction', {}, function(rslt) {
			// if ($.fn.DataTable.isDataTable('#transaction')) {
			// $('#transaction').DataTable().destroy();
			// }
			// $('#transaction tbody').empty();
			// transactionObj.clear();

			for (var i = 1; i < rslt.output.rows.length; i++) {
				var row = rslt.output.rows[i];
				if (row[0]) {
					var date = moment(row[0]).format('YYYY-MM-DD');
					var rowArry = [ date ];
					for (var j = 1; j <= 10; j++) {
						rowArry.push(row[j]);
					}
					var rowObj = transactionObj.row.add(rowArry).draw(false);
					if (i > 5) {
						$(rowObj.nodes()).addClass('col_total_year');
					}
				}
			}

			$('thead > tr > th').css('vertical-align', 'middle');
			$('tbody > tr > td').css('vertical-align', 'middle');
			if (cb) {
				cb.call(null);
			}
		});
	})
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
