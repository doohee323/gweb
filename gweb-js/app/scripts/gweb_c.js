// ///////////////////////////////////////////////////////////////////////////////
// [ GWeb constructor ]
// ///////////////////////////////////////////////////////////////////////////////
var GWeb = function(config) {
	var _super = this;
	_super.config = config;

	_super.init = function(sheet, option) {
		var surl = window.location.href;
		var client_id = _super.getGAuth(sheet).client_id;
		var access_token = sessionStorage.getItem(client_id);
		if (!option.sessionCache) {
			if (option.nosession) {
				access_token = null;
			} else {
				if (location.href.indexOf('#access_token') == -1) {
					alert('Need to sign in!');
					return;
				}
			}
		}
		if (!access_token || access_token === 'undefined') {
			access_token = null;
		}
		if (surl.indexOf('access_token') == -1 && !access_token) {
			var SCOPES = [ 'https://www.googleapis.com/auth/drive.scripts',
					'https://www.googleapis.com/auth/drive' ];

			var scope = SCOPES.join(' ');
			var redirect_uri = surl;
			var response_type = "token";
			var url = "https://accounts.google.com/o/oauth2/auth?scope="
					+ scope + "&client_id=" + client_id + "&redirect_uri="
					+ redirect_uri + "&response_type=" + response_type;
			window.location.replace(url);
			return false;
		} else if (surl.indexOf('access_token') > -1) {
			var params = window.location.hash.split('&');
			access_token = params[0].split('=')[1];
			sessionStorage.setItem(client_id, access_token);
		}
		return true;
	}

	_super.getGAuth = function(sheet) {
		if (_super.config[sheet].gauth) {
			return _super.config[sheet].gauth;
		} else {
			return _super.config.gauth;
		}
	}

	_super.setInputParams = function(sheet, index, postfix) {
		if (_super.config[sheet].params) {
			for (var i = 0; i < _super.config[sheet].params.length; i++) {
				if (index == i) {
					var col;
					if (postfix) {
						col = key + postfix;
					} else {
						col = key;
					}
					for ( var key in _super.config[sheet].params[i]) {
						if (key.indexOf('_') != 0) {
							if ($('#' + col).val()) {
								_super.config[sheet].params[i][key] = $(
										'#' + col).val();
							}
						}
					}
				}
			}
		} else if (_super.config[sheet].input) {
			for ( var key in _super.config[sheet].input) {
				var col;
				if (postfix) {
					col = key + postfix;
				} else {
					col = key;
				}
				if (key.indexOf('_') != 0) {
					if ($('#' + col).val()) {
						_super.config[sheet].input[key] = $('#' + col).val();
					}
				}
			}
		}
	}

	_super.getResults = function(rslt) {
		if (rslt.output.rows) {
		} else {
			for ( var key in rslt.output) {
				$('#' + key).val(rslt.output[key]);
			}
		}
	}

	_super.execute = function(sheet, option, cb) {
		if (!_super.init(sheet, option)) {
			return;
		}
		var rslt = {
			input : _super.config[sheet].input
		}
		var url = _super.config[sheet].url;
		if (!url && _super.config[sheet].gauth) {
			url = _super.config[sheet].gauth.url;
		}
		if (!url) {
			url = _super.config.gauth.url;
		}
		var params = {};
		if (_super.config[sheet].input) {
			var _version;
			var _cache;
			if (option) {
				_version = option.version;
				_cache = option.cache;
			}
			if (_version) {
				_super.config[sheet].input._version = _version;
			}
			if (_cache) {
				if (_super.config[sheet].input._cache) {
					_super.config[sheet].input._cache = _cache;
				}
			}
			params = {
				'input' : _super.config[sheet].input
			}
		} else {
			for (var i = 0; i < _super.config[sheet].params.length; i++) {
				var param = _super.config[sheet].params[i];
				if (param._input) {
					if (typeof param._input != 'string') {
						_super.config[sheet].params[i]._input = JSON
								.stringify(param._input);
					}
				}
				if (param._output) {
					if (typeof param._output != 'string') {
						_super.config[sheet].params[i]._output = JSON
								.stringify(param._output);
					}
				}
			}
			var _version;
			var _cache;
			if (option) {
				_version = option.version;
				_cache = option.cache;
			}
			if (_version) {
				for (var i = 0; i < _super.config[sheet].params.length; i++) {
					_super.config[sheet].params[i]._version = _version;
				}
			}
			if (_cache) {
				for (var i = 0; i < _super.config[sheet].params.length; i++) {
					if (_super.config[sheet].params[i]._cache) {
						_super.config[sheet].params[i]._cache = _cache;
					}
				}
			}
			params = {
				'params' : _super.config[sheet].params
			}
		}
		if (option.owner) {
			if (params.input) {
				if (params.input._sheet.indexOf('$') > -1) {
					params.input._sheet = params.input._sheet.substring(
							params.input._sheet.indexOf('$') + 1,
							params.input._sheet.length);
				}
				params.input._sheet = owner + '$' + params.input._sheet;
			} else if (params.params) {
				for (var i = 0; i < params.params.length; i++) {
					if (params.params[i]._sheet.indexOf('$') > -1) {
						params.params[i]._sheet = params.params[i]._sheet
								.substring(
										params.params[i]._sheet.indexOf('$') + 1,
										params.params[i]._sheet.length);
					}
					params.params[i]._sheet = owner + '$'
							+ params.params[i]._sheet;
				}
			}
		}
		console.time("ajax response time");
		$.ajax({
			method : 'POST',
			url : url,
			data : params,
			dataType : 'json'
		}).done(function(data) {
			console.timeEnd("ajax response time");
			// console.log(JSON.stringify(data));
			cb.call(null, data);
		}).fail(function(msg) {
			console.log(JSON.stringify(msg));
			alert(JSON.stringify(msg));
		})
	}

	// if (W7 == 0) { W7 = 1.1 } else { W7 = 1.17150 }

	function test1() {
		var s = "function _func(){ ";
		s += "if (sheet.getRange('W7').getValue() == 0) { ";
		s += "  sheet.getRange('W7').setValue(1.1);";
		s += "} else { ";
		s += "  sheet.getRange('W7').setValue(1.17150); ";
		s += "} ";
		s += "}";
		eval(s);
		_func();
	}

	function _func() {
		if (sheet.getRange('W7').getValue() == 0) {
			sheet.getRange('W7').setValue(1.1);
		} else {
			sheet.getRange('W7').setValue(1.17150);
		}
	}
}

// //////////////////////////////////////////////////////////////////////////////
// / [configuration]
// ////////////////////////////////////////////////////////////////////////////////

var gconfig = {
	'gauth' : {
		'client_id' : '1024321258351-shqbk1590unuumf9pqofvr3ghlvmn1g6.apps.googleusercontent.com'
	},
	'test' : {
		'url' : 'https://script.google.com/macros/s/AKfycbymUpOWxXQPftcd3Sr2or6F5RmatVmOpyvwHpOUh_CnNytq14M/exec',
		'gauth' : {
			'client_id' : '1037692431504-lr34s2fatms5gchs7aofavtq736lt0o6.apps.googleusercontent.com'
		},
		'input' : {},
		'output' : {}
	}
}
