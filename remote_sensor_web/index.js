const express = require('express');
const logger = require('morgan');
const app = express();
const port = 3000;

var sensorsValues = []

app.use(express.json());

var i = 0;

app.use(logger('dev'));

app.disable('etag');

app.post('/SetSensorsValues', (req,res) => {
    var body = req.body;

    if(body['Sensors'] === null || body['Sensors'] === undefined) {
        console.log('INVALID SetSensorsValues, no sensors in body');
    }

    var appSensors = body['Sensors'];

    if(appSensors.length == 0) {
        console.log('EMPTY LIST SENDED');
    }

    for(var s=0;s<appSensors.length;s++) {
        var appSensor = appSensors[s];

        var sType = appSensor['Type'];
        var sStringType = appSensor['StringType'];
        var sVendor = appSensor['Vendor'];
        var sVersion = appSensor['Version'];
        var sValues = appSensor['Values'];

        var lastTimeUpdated = new Date();

        var updated = false;

        for(var i=0;i<sensorsValues.length;i++) {
            var sensor = sensorsValues[i];
            if(sensor.sType == sType) {
                if(sValues !== null || sValues !== undefined)
                {
                    sensor.sValues = sValues;
                } else {
                    sensor.sValues = []
                }
                sensor.sLastTimeUpdated = lastTimeUpdated;
                //console.log('Updated> type: '+ sStringType + ', vendor: ' + sVendor + ', version: ' + sVersion.toString() + ', values: ' + sValues.toString());
                updated = true;
            }
        }
        
        if(updated) continue;

        var sensor = {};
        sensor.sType = sType;
        sensor.sStringType = sStringType;
        sensor.sVendor = sVendor;
        sensor.sVersion = sVersion;
        if(sValues !== null || sValues !== undefined)
        {
            sensor.sValues = sValues;
        } else {
            sensor.sValues = []
        }
        sensor.sLastTimeUpdated = lastTimeUpdated;
        
        sensorsValues.push(sensor);
        //console.log('Added new Sensor> type: '+ sStringType + ', vendor: ' + sVendor + ', version: ' + sVersion.toString() + ', values: ' + sValues.toString());
    }

    console.log('Sensors added and updated.');
    res.send('ok');
});

app.post('/SetSensorValues', (req,res) => {
    var body = req.body;
    var sType = body['SensorType'];
    var sStringType = body['SensorStringType'];
    var sVendor = body['SensorVendor'];
    var sVersion = body['SensorVersion'];
    var sValues = body['SensorValues'];

    var lastTimeUpdated = new Date();

    for(var i=0;i<sensorsValues.length;i++) {
        var sensor = sensorsValues[i];
        if(sensor.sType == sType
        && sensor.sVendor == sVendor
        && sensor.sVersion == sVersion) {
            if(sValues !== null || sValues !== undefined)
            {
                sensor.sValues = sValues;
            } else {
                sensor.sValues = []
            }
            sensor.sLastTimeUpdated = lastTimeUpdated;
            console.log('Updated> type: '+ sStringType + ', vendor: ' + sVendor + ', version: ' + sVersion.toString() + ', values: ' + sValues.toString());
            return;
        }
    }

    var sensor = {};
    sensor.sType = sType;
    sensor.sStringType = sStringType;
    sensor.sVendor = sVendor;
    sensor.sVersion = sVersion;
    if(sValues !== null || sValues !== undefined)
    {
        sensor.sValues = sValues;
    } else {
        sensor.sValues = []
    }
    sensor.sLastTimeUpdated = lastTimeUpdated;

    sensorsValues.push(sensor);
    console.log('Added new Sensor> type: '+ sStringType + ', vendor: ' + sVendor + ', version: ' + sVersion.toString() + ', values: ' + sValues.toString());
    
    res.send('ok');
});

app.post('/SetVal', (req,res) => {
    console.log(req.body)
    var _i = parseInt(req.body['val']);
    i = _i + i
    res.send(`ok i:${i}`)
})

app.get('/', (req,res) => {
    var html = ""+
    "<!DOCTYPE html>"+
        "<html>"+
            "<head>"+
                "<meta http-equiv=\"refresh\" content=\"1\">"+
                "<style>"+
                    "body {" +
                    "}"+
                    // "#main_content {"+
                    //     "border-left: 1px solid black;"+
                    //     "border-right: 1px solid black;"+
                    //     "padding: 20px;"+
                    // "}"+
                    "#main_table { "+
                        "font-family: monospace;"+
                        "font-color: #444"+
                        "width: 50%;"+
                        "margin-left: auto;"+
                        "margin-right: auto;"+  
                    "}"+
                    "table, th, td { "+
                        " border: 1px solid black"+
                    "}"+
                "</style>"+
            "</head>"+
            "<body>"+
            "<div id=\"main_content\">"+
                "<table id=\"main_table\">"+
                    "<thead>"+
                        "<tr>"+
                            "<th>SensorType</th>"+
                            "<th>SensorStringType</th>"+
                            "<th>SensorVendor</th>"+
                            "<th>SensorVersion</th>"+
                            "<th>SensorValues</th>"+
                            "<th>Last time updated</th>"+
                        "</tr>"+
                    "</thead>";

    html = html + "<tbody>";
    for(var i=0;i<sensorsValues.length;i++) {
        var sensor = sensorsValues[i];
        html = html+"<tr>"+
                        "<td>"+sensor.sType.toString()+"</td>"+
                        "<td>"+sensor.sStringType+"</td>"+
                        "<td>"+sensor.sVendor+"</td>"+
                        "<td>"+sensor.sVersion.toString()+"</td>"+
                        "<td>"+sensor.sValues.toString()+"</td>"+
                        "<td>"+sensor.sLastTimeUpdated.toISOString()+"</td>"+
                    "</tr>";
    }
    html = html + "</tbody>"+
                "</table>"+
            "</div>"+
        "</body>"+
    "</html>"+
    res.send(html);
});


app.listen(port, () => console.log(`Example app listening at http://localhost:${port}`))

