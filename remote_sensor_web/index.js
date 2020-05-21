const express = require('express');
const bodyParser = require('body-parser')
const logger = require('morgan');
const app = express();
const port = 3000;

var sensorsValues = []

app.use(bodyParser.json());

var i = 0;

app.use(logger('dev'));

app.disable('etag');

app.post('/SetSensorsValues', (req,res) => {
    var body = req.body;
    
    console.log(body);

    return;
    if(body['Sensors'] === null || body['Sensors'] === undefined) {
        console.log('INVALID SetSensorsValues, no sensors in body');
    }

    var appSensors = body['Sensors'];

    for(var s=0;s<sensors.length;s++) {
        var appSensor = sensors[s];

        var sType = appSensor['SensorType'];
        var sStringType = appSensor['SensorStringType'];
        var sVendor = appSensor['SensorVendor'];
        var sVersion = appSensor['SensorVersion'];
        var sValues = appSensor['SensorValues'];

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
        
    }
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
            "</head>"+
            "<body>"+
                "<table>"+
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
    html = html + "</tbody>"
    html = html + "</table>";
    html += "</body>"+
        "</html>"+
    res.send(html);
});


app.listen(port, () => console.log(`Example app listening at http://localhost:${port}`))

