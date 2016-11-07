var GWeb = function() {
	var SCRIPT_PROP = PropertiesService.getScriptProperties();

	var input;
	var doc;
	var lock;
	var mainSheetCount = 1;
	var sheetPool = [];
	var shared = {};
	var start0 = new Date();
	var logstr = '';
	var clear = 'false';
	var expire_s = 60 * 60 * 24 * 30;

	var initDoc = function(input) {
		var msg = '';
		var fileList = getCache(input._doc);
		if (Object.keys(fileList).length === 0) {
			fileList = [];
		}
		try {
			var fileName = input._doc;
			var docCount = parseInt(input._docCount);
			var folderPath = input._folderPath;
			var oriId;
			var folders = DriveApp.getFoldersByName(folderPath);
			if (folders.hasNext()) {
				var folder = folders.next();
				var files = folder.getFiles();
				while (files.hasNext()) {
					var file = files.next();
					if (file.getName() === fileName) {
						oriId = file.getId();
						break;
					}
				}
				if (oriId) {
					var oriFile = DriveApp.getFileById(oriId);
					for (var i = 1; i <= docCount; i++) {
						var newFileName = fileName + i;
						var newFiles = folder.getFilesByName(newFileName);
						var docId = null;
						while (newFiles.hasNext()) {
							var newFile = newFiles.next();
							docId = newFile.getId();
						}
						var newFile = {
							name : newFileName
						};
						if (!docId) {
							var copied = oriFile.makeCopy(newFileName);
							docId = copied.getId();
							newFile.docId = docId;
							for (var j = fileList.length - 1; j >= 0; j--) {
								if (fileList[j].name === newFileName) {
									fileList.splice(j, 1);
								}
							}
							fileList.push(newFile);
						} else {
							var exist = false;
							for (var j = 0; j < fileList.length; j++) {
								if (fileList[j].name === newFileName) {
									exist = true;
									break;
								}
							}
							if (!exist) {
								newFile.docId = docId;
								fileList.push(newFile);
							}
						}
					}
				} else {
					msg = 'No file: ' + fileName;
				}
			} else {
				msg = 'No folder: ' + fileName;
			}
		} catch (e) {
			msg = 'No file2: ' + e;
		}
		setCache(input._doc, fileList);

		lock.releaseLock();

		docId = getDoc(input);
		if (docId) {
			doc = SpreadsheetApp.openById(docId);
		} else {
			msg = msg + ',' + docId;
		}

		var rslt = {
			msg : msg,
			shared : {
				fileList : fileList,
				docId : docId
			}
		}
		return rslt;
	}

	var getDoc = function(data) {
		var docId;
		if (Array.isArray(data)) {
			data = data[0];
		}
		if (data._doc) {
			var docs = getCache(data._doc);
			for (var i = 0; i < docs.length; i++) {
				if (!docs[i].occupied) {
					docId = docs[i].docId;
					docs[i].occupied = true;
					break;
				}
			}
			setCache(data._doc, docs);
			if (!docId && docs.length > 0) {
				docId = docs[0].docId;
			}
		}
		return docId;
	}

	var releaseDoc = function(data, docId) {
		if (Array.isArray(data)) {
			data = data[0];
		}
		if (data._doc) {
			var docs = getCache(data._doc);
			for (var i = 0; i < docs.length; i++) {
				if (docs[i].docId == docId) {
					docs[i].occupied = false;
					break;
				}
			}
			setCache(data._doc, docs);
		}
	}

	this.mainExec = function(input) {
		var start = new Date();
		lock = LockService.getPublicLock();
		// lock.waitLock(30000); // 30000, wait 30 seconds before conceding
		// defeat.
		var result = input;
		var docId;
		try {
			// docId = getDoc(input);
			if (!docId) {
				docId = SCRIPT_PROP.getProperty("docId");
			}
			if (docId) {
				doc = SpreadsheetApp.openById(docId);
			}

			var meta = {};

			// var result = {};
			if (input._event) {
				var start1 = new Date();
				if (input._version) {
					meta.version = input._version;
				}
				if (input._cache && input._cache === 'true') {
					var uri = hashCode(JSON.stringify(input));
					result = getCache(uri);
					if (Object.keys(result).length === 0) {
						result = dispatch(input);
						setCache(uri, result);
					}
				} else {
					result = dispatch(input);
				}
				var end1 = new Date();
				var diff1 = end1.getTime() - start1.getTime();
				Logger.log(input._event + ' diff:' + diff1);
				meta[input._event] = diff1;
			} else {
				var cached = false;
				var main_event;
				var main_uri;
				var main_seq;
				for (var i = 0; i < input.length; i++) {
					var param = input[i];
					if (param._main) {
						if (mainSheetCount && mainSheetCount > 1) {
							main_seq = Math
									.floor((Math.random() * mainSheetCount) + 1);
							param._sheet += '_' + main_seq;
						}
						main_event = param._event;
						if (param._cache && param._cache === 'true') {
							var uri = hashCode(JSON.stringify(param));
							main_uri = uri;
							result = getCache(uri);
							if (Object.keys(result).length != 0) {
								cached = true;
								if (param._version) {
									meta.version = param._version;
								}
								break;
							}
						} else {
							clear = 'true';
						}
					}
				}
				getLog(JSON.stringify(result));
				if (!cached) {
					for (var i = 0; i < input.length; i++) {
						var start1 = new Date();

						var param = input[i];
						if (param._version) {
							meta.version = param._version;
						}
						// Logger.log('param:' + JSON.stringify(param));
						// setCache(uri, {});
						if (param._cache && param._cache === 'true') {
							var uri = hashCode(JSON.stringify(param));
							if (param._result) {
								if (main_seq) {
									param._sheet += '_' + main_seq;
								} else {
									param._sheet;
								}
								uri += main_uri;
							}
							result = getCache(uri);
							if (Object.keys(result).length === 0) {
								result = dispatch(param);
								setCache(uri, result);
								if (param._result) {
									setCache(main_uri, result);
								}
							}
						} else {
							result = dispatch(param);
						}
						if (result.shared) {
							for ( var key in result.shared) {
								shared[key] = result.shared[key];
							}
						}
						var end1 = new Date();
						var diff1 = end1.getTime() - start1.getTime();
						Logger.log(param._event + ' diff:' + diff1);
						meta[param._event] = diff1;
						// Logger.log('result:' + JSON.stringify(result));
					}
				} else {
					result = getCache(main_uri);
				}
			}

			var end = new Date();
			var diff = end.getTime() - start.getTime();
			Logger.log('total diff:' + diff);
			meta.total = diff;
			if (main_seq) {
				meta.main_seq = main_seq;
			}
			meta.logstr = logstr;
			Logger.log('result:' + JSON.stringify(result));

			result = {
				"output" : result,
				"meta" : meta,
				"input" : input
			};
			return result;
		} catch (err) {
			result = {
				"error" : err
			};
			return result;
		} finally {
			lock.releaseLock();
			releaseDoc(input, docId);
		}
	}

	this.handleResponse = function(result) {
		try {
			return ContentService.createTextOutput(JSON.stringify(result))
					.setMimeType(ContentService.MimeType.JSON);
		} catch (err) {
			result.err = err;
			return ContentService.createTextOutput(JSON.stringify(result))
					.setMimeType(ContentService.MimeType.JSON);
		}
	}

	var getSheet = function(params) {
		var sheet_name = params._sheet;
		var sheet = sheetPool[sheet_name];
		if (!sheet) {
			sheet = doc.getSheetByName(sheet_name);
			sheetPool[sheet_name] = sheet;
		}
		return sheet;
	}

	this.getParams = function(input) {
		// var input = {
		// 'params[0][_event]' : "getRowNum",
		// 'params[0][_sheet]' : "차명DB",
		// 'params[0][col]' : "9",
		// 'params[0][input]' : "5G 그랜저 HG300",
		// 'params[1][_event]' : "getALine",
		// 'params[1][_sheet]' : "차명DB",
		// 'params[1][output]' : "aaa"
		// }

		// var input2 = {
		// 'input[B10]' : "85000",
		// 'input[C4]' : "5G 그랜저 HG300",
		// 'input[C10]' : "198000",
		// 'input[G9]' : "0"
		// }

		var input2 = {};
		Object.keys(input).sort().forEach(function(key) {
			input2[key] = input[key];
		});

		var firstKey = Object.keys(input2)[0].toString();
		if (firstKey.indexOf('params[') == 0) {
			var rowtmp = 0;
			var newParam = {};
			var newParams = [];
			for ( var key in input2) {
				var row = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
				var col = key.substring(key.indexOf('][') + 2, key
						.lastIndexOf(']'));
				var val = input2[key];
				if (row != rowtmp) {
					newParams.push(newParam);
					newParam = {};
					rowtmp = row;
				}
				newParam[col] = val;
			}
			newParams.push(newParam);
			return newParams;
		} else if (firstKey.indexOf('input[') == 0
				|| firstKey.indexOf('output[') == 0) {
			var rowtmp = 0;
			var newParam = {};
			for ( var key in input2) {
				var col = key.substring(key.indexOf('[') + 1, key.indexOf(']'));
				var val = input2[key];
				newParam[col] = val;
			}
			return newParam;
		}
		return {};
	}

	var getValue = function(params) {
		sheet = getSheet(params);
		var result = {};
		for ( var key in params) {
			if (key.indexOf('_') != 0) {
				var range = params[key];
				var val = sheet.getRange(range).getValue();
				result[key] = val;
			}
		}
		return result;
	}

	var getList = function(params) {
		sheet = getSheet(params);

		var input = params.input;
		if (input) {
			if (typeof input === 'string') {
				input = input.substring(1, input.length - 1);
				input = input.replace(/ /g, "")
				input = input.replace(/'/g, "")
				input = input.replace(/"/g, "")
				input = input.split(',');
			}
		}

		var result = {};
		var output = params.output;
		if (output) {
			if (typeof output === 'string') {
				output = output.substring(1, output.length - 1);
				output = output.replace(/ /g, "")
				output = output.replace(/'/g, "")
				output = output.replace(/"/g, "")
				output = output.split(',');
			}
			var rows = [];
			for (var j = 0; j < sheet.getLastRow(); j++) {
				var row = {};
				for ( var i in output) {
					var col = output[i] + (parseInt(j) + 1);
					var val = sheet.getRange(col).getValue();
					row[col] = val;
				}
				rows.push(row);
			}
			result.rows = rows;
		} else {
			if (input) {
				var startRow = parseInt(input[0]);
				var startColumn = parseInt(input[1]);
				var numRows = input[2];
				if (numRows == '_end') {
					numRows = sheet.getLastRow();
				} else {
					numRows = parseInt(numRows);
				}
				var numColumns = input[3];
				if (numColumns == '_end') {
					numColumns = sheet.getLastColumn();
				} else {
					numColumns = parseInt(numColumns);
				}
				result.rows = sheet.getSheetValues(startRow, startColumn,
						numRows, numColumns);
			} else {
				result.rows = sheet.getSheetValues(1, 1, sheet.getLastRow(),
						sheet.getLastColumn());
			}
		}
		return result;
	}

	var appendRow = function(params) {
		sheet = getSheet(params);
		var result = {
			msg : 'success'
		};
		if (!sheet) {
			result.msg = 'can not find1: ' + params._sheet;
		} else {
			var nextRow = sheet.getLastRow() + 1;
			try {
				for ( var key in params) {
					if (key.indexOf('_') != 0) {
						var val = params[key];
						var rg = key + nextRow;
						sheet.getRange(rg).setValue(val);
					}
				}
			} catch (e) {
				result.msg = 'can not find3: ' + key + ', '
						+ JSON.stringify(params);
			}
		}
		return result;
	}

	var deleteRow = function(params) {
		sheet = getSheet(params);
		var result = {
			msg : 'success'
		};
		if (!sheet) {
			result.msg = 'can not find1: ' + params._sheet;
		} else {
			try {
				var rowPosition = params.row;
				var howMany = params.count;
				if (!howMany) {
					howMany = 1;
				}
				sheet.deleteRows(rowPosition, howMany);
			} catch (e) {
				result.msg = 'can not delete :' + JSON.stringify(params);
			}
		}
		return result;
	}

	var update = function(params) {
		sheet = getSheet(params);
		var result = {
			msg : 'success'
		};
		if (!sheet) {
			result.msg = 'can not find1: ' + params._sheet;
		} else {
			for ( var key in params) {
				if (key.indexOf('_') != 0) {
					var val = params[key];
					try {
						sheet.getRange(key).setValue(val);
					} catch (e) {
						result.msg = 'can not find3: ' + key + ', '
								+ JSON.stringify(params);
					}
				}
			}
		}
		return result;
	}

	var getValueFromUpdate = function(params) {
		sheet = getSheet(params);
		var result = {};
		var input = params._input;
		if (typeof params._input === 'string') {
			input = JSON.parse(params._input);
		}
		for ( var key in input) {
			if (key.indexOf('_') != 0) {
				var val = input[key];
				sheet.getRange(key).setValue(val);
			}
		}
		var output = params._output;
		if (typeof params._output === 'string') {
			output = JSON.parse(params._output);
		}
		for ( var key in output) {
			if (key.indexOf('_') != 0) {
				var range = output[key];
				var val = sheet.getRange(range).getValue();
				result[key] = val;
			}
		}
		return result;
	}

	var getRowNum = function(params) {
		sheet = getSheet(params);
		var qeuryStr = params.input;
		var column = params.col; // column Index
		var columnValues = sheet.getRange(2, column, sheet.getLastRow())
				.getValues();
		var srow = 0;
		for (var i = 0; i < columnValues.length; i++) {
			if (columnValues[i][0] === qeuryStr) {
				srow = i + 2;
				break;
			}
		}
		var rslt = {
			shared : {
				srow : srow
			}
		}
		return rslt;
	}

	var getALine = function(params) {
		sheet = getSheet(params);
		var result = {};
		var output = params.output;
		var srow = 0;
		if (shared.srow) {
			srow = shared.srow;
		}
		if (typeof output === 'string') {
			output = output.substring(1, output.length - 1);
			output = output.replace(/'/g, "")
			output = output.replace(/"/g, "")
			output = output.split(',');
		}
		for ( var i in output) {
			var col = output[i] + srow.toString();
			var val = sheet.getRange(col).getValue();
			result[col] = val;
		}
		var rslt = {
			shared : {
				line : result
			}
		}
		return rslt;
	}

	var getLineFromRowNum = function(params) {
		var result = {};
		sheet = getSheet(params);
		var qeuryStr = params.input;
		var column = params.col; // column Index
		var columnValues = sheet.getRange(2, column, sheet.getLastRow())
				.getValues();
		var srow = 0;
		for (var i = 0; i < columnValues.length; i++) {
			if (columnValues[i][0] === qeuryStr) {
				srow = i + 2;
				break;
			}
		}
		var output = params.output;
		if (typeof output === 'string') {
			output = output.substring(1, output.length - 1);
			output = output.replace(/'/g, "")
			output = output.replace(/"/g, "")
			output = output.split(',');
		}
		for ( var i in output) {
			var col = output[i] + srow.toString();
			var val = sheet.getRange(col).getValue();
			result[col] = val;
		}
		var rslt = {
			shared : {
				srow : srow,
				line : result
			}
		}
		return rslt;
	}

	var attachedScript = function(params) {
		time_log('-----------------3-1');
		sheet = getSheet(params);
		var result = {};
		for ( var key in params) {
			var col = key.substring(key.indexOf('[') + 1, key.length - 1);
			var col2 = params[key];
			var val2 = shared.line[col2 + shared.srow];
			sheet.getRange(col).setValue(val2);
		}
		result.msg = "success";
		time_log('-----------------3-2');
		return result;
	}

	var updateFromShared = function(params) {
		time_log('-----------------4-1');
		sheet = getSheet(params);
		var result = {};
		for ( var key in params) {
			if (key.indexOf('_') != 0) {
				var col = params[key];
				var val = shared.line[col + shared.srow];
				sheet.getRange(key).setValue(val);
			}
		}
		result.msg = "success";
		time_log('-----------------4-2');
		return result;
	}

	function _func1() {
		if (sheet.getRange('W7').getValue() == 0) {
			sheet.getRange('W7').setValue(1.1);
		} else {
			sheet.getRange('W7').setValue(1.17150);
		}
	}

	var script = function(params) {
		sheet = getSheet(params);
		var result = {};
		eval(params._script);
		_func();
		result.msg = "success";
		return result;
	}

	var dispatch = function(params) {
		var result = {};
		if (params._event === 'initDoc') {
			result = initDoc(params);
		} else if (params._event === 'getValue') {
			result = getValue(params);
		} else if (params._event === 'getList') {
			result = getList(params);
		} else if (params._event === 'appendRow') {
			result = appendRow(params);
		} else if (params._event === 'deleteRow') {
			result = deleteRow(params);
		} else if (params._event === 'update') {
			result = update(params);
		} else if (params._event === 'getValueFromUpdate') {
			result = getValueFromUpdate(params);
		} else if (params._event === 'getRowNum') {
			result = getRowNum(params);
		} else if (params._event === 'getLineFromRowNum') {
			result = getLineFromRowNum(params);
		} else if (params._event === 'getALine') {
			result = getALine(params);
		} else if (params._event === 'updateFromShared') {
			result = updateFromShared(params);
		} else if (params._event === 'script') {
			result = script(params);
		}
		return result;
	}

	var time_log = function(title) {
		return;
		var end0 = new Date();
		var diff0 = end0.getTime() - start0.getTime();
		Logger.log(title + ':' + diff0);
	}

	var getLog = function(str) {
		return logstr += str + '\n';
	}

	var setCache = function(cacheKey, cacheData, expires) {
		if (clear != 'true') {
			var cache = CacheService.getScriptCache();
			if (expires == undefined || expires == 'null') {
				expires = expire_s;
			}
			try {
				cache.put(cacheKey, JSON.stringify(cacheData), expires);
			} catch (e) {
			}
		}
	}

	var getCache = function(cacheKey) {
		var cache = CacheService.getScriptCache();
		if (clear == 'true') {
			cache.put(cacheKey, '{}', expire_s);
			return {};
		} else {
			var cached = cache.get(cacheKey);
			if (cached != null) {
				return JSON.parse(cached);
			} else {
				return {};
			}
		}
	}

	var hashCode = function(s) {
		return s.split("").reduce(function(a, b) {
			a = ((a << 5) - a) + b.charCodeAt(0);
			return a & a
		}, 0);
	}

	var initSheet = function(e) {
		var input = JSON.parse(e);
		// input = {
		// 'sheet' : '렌터카견적',
		// 'count' : 3
		// }
		mainSheetCount = input.count;
		try {
			// var ss = SpreadsheetApp.getActiveSpreadsheet();
			doc = SpreadsheetApp.openById(SCRIPT_PROP.getProperty("docId"));
			var sheet_nm = input.sheet;

			var sheets = doc.getSheets();
			if (sheets.length > 1) {
				for (var i = 1; i <= input.count; i++) {
					var exist = false;
					var sheet_t = sheet_nm + '_' + i;
					for (var j = 0; j < sheets.length; j++) {
						var tmp = sheets[j].getName();
						if (tmp === sheet_t) {
							exist = true;
							break;
						}
					}
					if (!exist) {
						doc.duplicateActiveSheet();
						doc.renameActiveSheet(sheet_t);
					}
				}
			}
		} catch (e) {
		}
		return JSON.stringify(input);
	}
}

GWeb.prototype.request = function(e) {
	var input = e;
	if (e.parameter) {
		input = e.parameter;
		input = this.getParams(input);
	}
	return this.handleResponse(this.mainExec(input));
}

GWeb.prototype.cmd = function(e) {
	var input = JSON.parse(e);
	return JSON.stringify(this.mainExec(input));
}

GWeb.prototype.setup = function(e) {
	var SCRIPT_PROP = PropertiesService.getScriptProperties();
	var doc = SpreadsheetApp.getActiveSpreadsheet();
	SCRIPT_PROP.setProperty("docId", doc.getId());
}

function test() {
	var initDoc_p = {
		'_folderPath' : '외부견적기',
		'_doc' : 's_gweb',
		'_event' : 'initDoc',
		'_docCount' : 2
	}

	var appendRow_p = {
		'_version' : 1,
		'_sheet' : 'access_logs',
		'_event' : 'appendRow',
		'A' : 'doohee323@gmail.com',
		'B' : '10/30/20016'
	}

	var deleteRow_p = {
		'_sheet' : 'running_logs',
		'_event' : 'deleteRow',
		'row' : 2,
		'count' : 1
	}

	var update_p = {
		'_version' : 1,
		'_sheet' : '렌터카견적',
		'_event' : 'update',
		"_main" : 'true',
		'_cache' : 'false',
		'C4' : "5G 그랜저 HG300", // 견적차종
		'V5' : true, // 인수옵션
		'T5' : 4, // 약정거리
		'W5' : 4, // 계약기간
		'P26' : 2, // 대물/자손
		'G9' : 0, // 보증금
		'K9' : 0.01, // 수수료
		'U5' : true, // 정비상태
	}

	var update2_p = {
		'_version' : 1,
		'_sheet' : '2차탁송료',
		'_event' : 'update',
		'_cache' : 'false',
		'G2' : '과천', // 1차탁송료
		'G3' : '전라도', // 2차탁송료
	}

	var getValue_p = {
		'_sheet' : '렌터카견적',
		'_event' : 'getValue',
		'_cache' : 'false',
		"_result" : 'true',
		'col_1' : 'I13', // 월납부금
		'col_2' : 'N4', // 금리
		'col_3' : 'U19', // 잔존가치
		'col_4' : 'B10', // 1차탁송료
		'col_5' : 'C10', // 2차탁송료
		'col_6' : 'D10', // 부대비용
		'col_7' : 'O27', // 보험료
		'col_8' : 'O28', // 자차보험료
	}

	var getList_p = {
		"_sheet" : "_차명매핑",
		"_event" : "getList",
		'input' : "[1,1,'_end',4]"
	}

	var getValueFromUpdate_p = {
		'_sheet' : '렌터카견적',
		'_event' : 'getValueFromUpdate',
		'_main' : 'true',
		'_result' : 'true',
		'_cache' : 'false',
		'_input' : {
			'C4' : '5G 그랜저 HG300',
			'V5' : true,
			'T5' : 4,
			'W5' : 4,
			'P26' : 2,
			'G9' : 0,
			'K9' : 0.01,
			'U5' : true,
			'B10' : 84000,
			'C10' : 198000
		},
		'_output' : {
			'col_1' : 'I13',
			'col_2' : 'N4',
			'col_3' : 'U19',
			'col_4' : 'B10',
			'col_5' : 'C10',
			'col_6' : 'D10',
			'col_7' : 'O27',
			'col_8' : 'O28'
		}
	}

	var getRowNum_p = {
		'_version' : 1,
		'_sheet' : '차명DB',
		'_event' : 'getRowNum',
		'input' : "5G 그랜저 HG300", // 견적차종
		'col' : 9
	// I(9) 컬럼에서 검색
	}

	var getALine_p = {
		'_sheet' : '차명DB',
		'_event' : 'getALine',
		'output' : "['I','O','J','P','N','X','Q','Z','L','M','W']"
	}

	var getLineFromRowNum_p = {
		'_sheet' : '차명DB',
		'_event' : 'getLineFromRowNum',
		'input' : '올뉴카니발 11인승 디젤',
		'col' : 9,
		'output' : "['I','O','J','P','N','X','Q','Z','L','M','W']"
	}

	var updateFromShared_p = {
		'_sheet' : '렌터카견적',
		'_event' : 'updateFromShared',
		'N7' : 'O', // 견적차종
		'O7' : 'J', // 배기량
		'P8' : 'P', // 잔가
		// 'P7' : 38, // 잔가율
		'S7' : 'N', // 인승/적재량
		'T7' : 'X', // 특수구분
		'U7' : 'Q', // 정비차종
		'V7' : 'Z', // 특판할인율
		'W7' : 'L', // 면세지수
		'X7' : 'M', // 납세율
	}

	var s = "function _func(){ ";
	s += "if (sheet.getRange('W7').getValue() == 0) { ";
	s += "  sheet.getRange('W7').setValue(1.1);";
	s += "} else { ";
	s += "  sheet.getRange('W7').setValue(1.17150); ";
	s += "} ";
	s += "}";

	var script_p = {
		'_sheet' : '렌터카견적',
		'_event' : 'script',
		'_script' : s
	}

	// attachedScript, 잔가율, 면세지수
	var multi_p = [ deleteRow_p ];
	// var multi_p = [ getLineFromRowNum_p, updateFromShared_p, script_p ]; //
	// var multi_p = [ getRowNum_p, getALine_p, updateFromShared_p,
	// update_p, getValue_p ];
	// var multi_p = [ getLineFromRowNum_p, updateFromShared_p, update2_p,
	// getValueFromUpdate_p ]

	// var aaa = JSON.stringify(multi_p);
	// Logger.log(aaa);

	var e = {
		parameter : multi_p
	};
	input = e.parameter;
	// input = getParams(input);

	var up = new GWeb();
	return up.request(input);
}

function run(e) {
	var up = new GWeb();
	return up.cmd(e);
}

// function doGet() {
// return HtmlService.createHtmlOutputFromFile('index')
// .setSandboxMode(HtmlService.SandboxMode.IFRAME);
// }

function doPost(e) {
	var up = new GWeb();
	return up.request(e);
}

function setup(e) {
	var up = new GWeb();
	return up.setup(e);
}

// code.gs
function doGet() {
	return HtmlService.createHtmlOutputFromFile('public');
}

function getResult() {
	var getValue_p = {
		'_sheet' : 'config',
		'_event' : 'getValue',
		'col_1' : 'A1',
		'col_2' : 'A2',
		'col_3' : 'B1',
		'col_4' : 'B2'
	}

	var multi_p = [ getValue_p ];
	var e = {
		parameter : multi_p
	};
	input = e.parameter;

	var up = new GWeb();
	var result = up.mainExec(input);
	return JSON.stringify(result.output);
}

function include(filename) {
	return HtmlService.createHtmlOutputFromFile(filename).setSandboxMode(
			HtmlService.SandboxMode.IFRAME).getContent();
}
