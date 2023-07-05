var express = require('express');
var app = express();
var fs = require("fs");

const bp_data_file = "BusinessPartner-sample.json";

app.get('/sap/opu/odata/sap/API_BUSINESS_PARTNER/A_BusinessPartner', function (req, res) {
    fs.readFile( __dirname + "/" + bp_data_file, 'utf8', function (err, data) {

        let host = req.get('Host');
        let protocol = req.protocol;

        let url = req.originalUrl;

        let dataArray = JSON.parse(data);
        res.end( JSON.stringify(dataArray) );
    });
 })
  
var server = app.listen(8081, '127.0.0.1', function () {
   var host = server.address().address;
   if (host === '::') {
        host = 'localhost';
    }
   console.log("Host: " + host);
   var port = server.address().port
   console.log("Port: " + port);

   console.log("Business Partner Mock listening at http://%s:%s", host, port)
})