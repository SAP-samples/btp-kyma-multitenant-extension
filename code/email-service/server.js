var express = require("express");
var nodemailer = require('nodemailer');

var app = express();

app.use(express.urlencoded({ extended: true }));
app.use(express.json());

app.put("/easyfranchise/rest/emailservice/v1/notifycoordinator", function (req, res) {  
  var coordinators = req.body.coordinators;
  var count = req.body.newFranchiseCount;
  var newFranchiseNames = req.body.newFranchiseNames;

  var mail = nodemailer.createTransport({
      service: 'gmail',
      auth: {
        user: process.env.EMAIL_USERNAME,
        pass: process.env.EMAIL_PASSWORD
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

    var email = ''; 
    for(var j in coordinators){
      email = email + coordinators[j].email + ', ';      
    }; 

    //remove the last comma and whitespace
    email = email.slice(0, -2);
    
    var mailOptions = {
      from: 'easyfranchisenotifications@gmail.com',
      to: email,
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
});

app.put("/easyfranchise/rest/emailservice/v1/notifymentor", function (req, res) { 
  var mentorName = req.body.mentorName;
  var email = req.body.email;
  var franchiseName = req.body.franchiseName;
  var franchiseEmail = req.body.franchiseEmail;  

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
        user: process.env.EMAIL_USERNAME,
        pass: process.env.EMAIL_PASSWORD
      }
    });

    var mailOptions = {
      from: 'easyfranchisenotifications@gmail.com',
      to: email,
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
});



var port = process.env.PORT || 3002;
app.listen(port, function () {
    console.log("Application listening on port " + port);
});
