var SHEET_NAME = "Sheet1";

var SCRIPT_PROP = PropertiesService.getScriptProperties();

function doGet(e) {
  return handleResponse(e);
}

function doPost(e) {
  return handleResponse(e);
}

var input;
function handleResponse(e) {
  var lock = LockService.getPublicLock();
  // lock.waitLock(30000); // wait 30 seconds before conceding defeat.

  try {
    var doc = SpreadsheetApp.openById(SCRIPT_PROP.getProperty("key"));
    var sheet = doc.getSheetByName(SHEET_NAME);

    // for debugging with google doc script debugger
    if (!e) {
      e = {
        parameter : {
          'input[A]' : 100,
          'input[B]' : 200,
          last_col : 2
        }
      };
      // e = {parameter: {event: 'total'}
      // };
    } else {
      input = e.parameter;
    }

    var values;
    if (e.parameter.event === 'total') {
      values = sheet.getRange(1, 5, 1, 1).getValue();
    } else {
      var headRow = e.parameter.header_row || 1;
      var lastCol = e.parameter.last_col || sheet.getLastColumn();
      var headers = sheet.getRange(1, 1, 1, lastCol).getValues()[0];
      var nextRow = sheet.getLastRow() + 1;
      var row = [];

      for ( var i in headers) {
        var key = 'input[' + headers[i] + ']';
        var val = e.parameter[key];
        row.push(val);
      }
      sheet.getRange(nextRow, 1, 1, row.length).setValues([ row ]);

      // getRange(row, column, numRows, numColumns)
      values = sheet.getRange(1, 1, sheet.getLastRow(),
          sheet.getLastColumn() + 1).getValues();
      // Logger.log(values);
    }

    return ContentService.createTextOutput(JSON.stringify({
      "result" : values,
      "row" : nextRow
    })).setMimeType(ContentService.MimeType.JSON);
  } catch (e) {
    e.values = values;
    return ContentService.createTextOutput(JSON.stringify({
      "result" : input,
      "error" : e
    })).setMimeType(ContentService.MimeType.JSON);
  } finally {
    lock.releaseLock();
  }
}

function setup() {
  var doc = SpreadsheetApp.getActiveSpreadsheet();
  SCRIPT_PROP.setProperty("key", doc.getId());
}