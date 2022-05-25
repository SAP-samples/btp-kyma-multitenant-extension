var express = require("express");
var nodemailer = require('nodemailer');
const axios = require('axios').default;
var app = express();

var username;
var password;

app.use(express.urlencoded({ extended: true }));
app.use(express.json());

app.put("/easyfranchise/rest/emailservice/v1/:tenantId/notifymentor", async function (req, res) { 
  try{
    const servicelist = process.env.SERVICE_LIST;
    let dbRegEx = /db.service:\s*(?<dbservice>.*)/;
    let match = servicelist.match(dbRegEx);
    if(match === null){
        res.status(400).send("db.service url does not exist in configmap!");
        console.log("missing db.service in configmap, SERVICE_LIST: " + servicelist);
        return;
    }
  
    const url = match.groups.dbservice + req.params.tenantId + '/config/notificationconfig';
    console.log("Calling DB Service: " + url)
    const response = await axios.get(url);
    if(response.status == 200){
        console.log("Call to DB Service successful");        
        notifyMentor(response.data.email,response.data.password,req,res)
    }
  }catch (err) {
    console.log("Failed to retrieve username and password from Database");
    console.error(err);
    res.status(500).send("Failed to notify mentor");
    return;
  }  
});  

app.put("/easyfranchise/rest/emailservice/v1/:tenantId/notifycoordinator", async function (req, res) {  
  try{
    const servicelist = process.env.SERVICE_LIST;
    let dbRegEx = /db.service:\s*(?<dbservice>.*)/;
    let match = servicelist.match(dbRegEx);
    if(match === null){
        res.status(400).send("db.service url does not exist in configmap!");
        console.log("missing db.service in configmap, SERVICE_LIST: " + servicelist);
        return;
    }
  
    const url = match.groups.dbservice + req.params.tenantId + '/config/notificationconfig';
    console.log("Calling DB Service: " + url)
    const response = await axios.get(url);
    if(response.status == 200){
        console.log("Call to DB Service successful");        
        notifyCoordinator(response.data.email,response.data.password,req,res)
    }
  }catch (err) {
    console.log("Failed to retrieve username and password from Database");
    console.error(err);
    res.status(500).send("Failed to notify coordinators");
    return;
  }  
});


function notifyMentor(email,password,req,res){
  if (!!email) {
    var mentorName = req.body.mentorName;
    var recipient = req.body.email;
    var franchiseName = req.body.franchiseName;
    var franchiseEmail = req.body.franchiseEmail;  
    
    console.log("Mail: " + email);
    console.log("PWD: " + password);

    var mailText = 
      'Hello ' + mentorName + ', <br><br>' +
      'a new franchisee has been assigned to you, please get in contact with our new franchisee to ensure a good start. <br><br>' +
      '<b>Franchisee Details:</b> <br>' +  
      '<ul> <li>Franchisee Name: ' + franchiseName + '</li>' +
      '<li>Contact E-Mail: ' + franchiseEmail  + '</li></ul>' +
      'Best regards <br> EasyFranchise Notification Service';
  
    var mail = nodemailer.createTransport({
        service: 'gmail',
        auth: {
          user: email,
          pass: password
        }
      });
  
      var mailOptions = {
        from: 'easyfranchisenotifications@gmail.com',
        to: recipient,
        subject: '[ACTION NEEDED] EasyFranchise: A new franchisee has been assigned to you',
        html: mailText
      }
  
      mail.sendMail(mailOptions, function(error, info){
        if (error) {
          console.log(error);
          res.status(500).send(error)
        } else {
          console.log('Email sent: ' + info.response)
          res.send('Email sent: ' + info.response)
        }
      });
  } else {
    res.send("E-Mail configuration missing")
  }
}

function notifyCoordinator(email,password,req,res) {
  if (!!email) {
    var coordinators = req.body.coordinators;
  var count = req.body.newFranchiseCount;
  var newFranchiseNames = req.body.newFranchiseNames;

  var mail = nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: email,
        pass: password
      }
    });

    var mailText;

    if (count>1) {
      mailText = 
      'Dear Coordinator, <br><br>' +
      'there are ' + count +' new franchisees available for onboarding. Open to the EasyFranchise app to assign a mentor to the new franchisees. <br><br>' +
      '<b>New franchisees details:</b> <br>' +
      '<ul>';
    } else {
      mailText = 
      'Dear Coordinator, <br><br>' +
      'there is one new franchisee available for onboarding. Open to the EasyFranchise app to assign a mentor to the new franchisee. <br><br>' +
      '<b>New franchisee details:</b> <br>' +
      '<ul>';
    }

    for(var i in newFranchiseNames){
      mailText = mailText + 
      '<li>' + newFranchiseNames[i] + '</li>'  
    };

    mailText = mailText + 
    '</ul> <br>' +
    'Best regards <br> EasyFranchise Notification Service';

    var recipients = ''; 
    for(var j in coordinators){
      recipients = recipients + coordinators[j].email + ', ';      
    }; 

    //remove the last comma and whitespace
    recipients = recipients.slice(0, -2);
    
    var mailOptions = {
      from: 'easyfranchisenotifications@gmail.com',
      to: recipients,
      subject: '[ACTION NEEDED] EasyFranchise: New franchisees to be onborded',
      html: mailText
    };

    mail.sendMail(mailOptions, function(error, info){
      if (error) {
        console.log(error);
        res.status(500).send(error)
      } else {
        console.log('Email sent: ' + info.response)
        res.send('Email sent: ' + info.response)
      }
    });  
  } else {
    res.send("E-Mail configuration missing")
  }  
}

var port = process.env.PORT || 3002;
app.listen(port, function () {
    console.log("Application listening on port " + port);
});
