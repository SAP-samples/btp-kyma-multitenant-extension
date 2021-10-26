const EF_SERVICE_NAME = 'approuter';
const EF_SERVICE_PORT = 5000;
const EF_APIRULE_DEFAULT_NAMESPACE = 'integration';
const KYMA_APIRULE_GROUP = 'gateway.kyma-project.io';
const KYMA_APIRULE_VERSION = 'v1alpha1';
const KYMA_APIRULE_PLURAL = 'apirules';

const k8s = require('@kubernetes/client-node');
const createApiRule = require('./createApiRule');

var express = require("express");
var xsenv = require("@sap/xsenv");
var passport = require("passport");
var JWTStrategy = require("@sap/xssec").JWTStrategy;
var axios = require('axios').default;

var app = express();
var kyma_cluster = process.env.CLUSTER_DOMAIN || "UNKNOWN";
var services = xsenv.readServices(); //https://github.wdf.sap.corp/xs2/node-xsenv/blob/master/README.md, https://github.tools.sap/CPES/CPAppDevelopment/blob/master/docs/KymaAuth.md
console.log(services); //https://github.com/dvankempen/XSUAA/blob/master/node/4b-authentication/myapp/server.js

passport.use(new JWTStrategy(services.xsuaaserviceinstance.credentials)); // use xsuaa service, token exchange happens transparently, see https://github.wdf.sap.corp/CPSecurity/node-xs2sec/blob/master/doc/IAStoXSUAA.md

app.use(passport.initialize());
app.use(passport.authenticate("JWT", { session: false }));

// Parse urlencoded request bodies into req.body, https://stackoverflow.com/questions/24330014/bodyparser-is-deprecated-express-4/59892173#59892173
app.use(express.urlencoded({ extended: true }));
app.use(express.json());

app.get("/users", function (req, res, next) {
    var userIsAuthorized = req.authInfo.checkScope("$XSAPPNAME.Display"); //?
    if (userIsAuthorized) {
        //res.send("User Info: " + req.user.id);
        const headerTenantID = req.headers['x-tenant-id'];
        res.send('<h1>Business service based on XSUAA</h1><br>' +
                `<h2>User info: ${req.authInfo.getEmail()} in zone ${req.authInfo.getZoneId()} with audiences [${req.authInfo.getTokenInfo().getAudiencesArray()}] </h2><br/>` +
                `<h2>Token Info:</h2> <pre><code> ${JSON.stringify(req.authInfo.getTokenInfo().getPayload(), undefined, 2)}</code></pre> <br/>` +
                `<h2>Token Raw:</h2> ${req.authInfo.getAppToken()} ` +
                `<p><a href="${req.headers['referer']}">Back</a></p>` +
                '<p>testing header forwarding ' + headerTenantID + '</p>' +
                '<p><a href="./logout">Logout</a></p>'
        );      

    } else {
        res.status(403).send("Access Forbidden");
    }
});

/***** On-boarding / Off-boarding tenant *******
For each new tenant, configure following items:
 - an unique URL should be created, 
 - corresponding APIRule for above URL should also be created
 - database schema should be created
******************************************/

// On-boarding tenant
app.put('/callback/v1.0/tenants/*', async function (req, res) {

    if (!req.authInfo.checkLocalScope('Callback')) {
        log('Missing the expected scope - Callback - in the call of on-boarding');
        res.status(403).end('Forbidden');
        return;
    }

    var consumerSubdomain = req.body.subscribedSubdomain;
    console.log(req.body);
    if(!req.body.subscribedTenantId || !consumerSubdomain){
        res.status(400).send("subscribedTenantId or subscribedSubdomain do not exist!");
        return;
    }

    //1. create tenant unique URL, e.g.https://d049740consume.c-2e512db.kyma-stage.shoot.live.k8s-hana.ondemand.com/

    var tenantAppURL = 'https://' + consumerSubdomain + "." + kyma_cluster;
    console.log("On-boarding consumerSubdomain = " + consumerSubdomain);
    console.log("cluster domain: " + kyma_cluster);
    console.log("On-boarding tenant URL: " + tenantAppURL);
    
    //2. extract db.service url from env variable
    const servicelist = process.env.SERVICE_LIST;
    let dbRegEx = /db.service:\s*(?<dbservice>.*)/;
    let match = servicelist.match(dbRegEx);
    if(match === null){
        res.status(400).send("db.service url does not exist in configmap!");
        console.log("missing db.service in configmap, SERVICE_LIST: " + servicelist);
        return;
    }

    const url = match.groups.dbservice + req.body.subscribedTenantId + '/onboard';
    
    const tenantInfo = {
        subaccountId: req.body.subscribedSubaccountId,
        subdomain: consumerSubdomain
    };
    
    // const content = {broker: 'onboarding'};

    const config = { headers: {'Content-Type': 'application/json'} };
    console.log("calling db service: " + url);

   //create db schema for new tenant
   
    try{
        const response = await axios.put(url, tenantInfo, config );
        if(response.status == 200){
            console.log("create database schema finished OK " + response.data);
        }
    }catch (err) {
        console.log("create database schema finished with error");
        console.error(err);
        res.status(500).send("create database schema error");
        return;
    }

    //3. create apirules with subdomain,
    const kc = new k8s.KubeConfig();
    kc.loadFromCluster();
    const k8sApi = kc.makeApiClient(k8s.CustomObjectsApi);
    const apiRuleTempl = createApiRule.createApiRule(
                            EF_SERVICE_NAME, 
                            EF_SERVICE_PORT, 
                            consumerSubdomain, 
                            kyma_cluster);

    try{
        console.log("calling getNamespacedCustomObject");
        const result = await k8sApi.getNamespacedCustomObject(KYMA_APIRULE_GROUP,
                            KYMA_APIRULE_VERSION,
                            EF_APIRULE_DEFAULT_NAMESPACE,
                            KYMA_APIRULE_PLURAL,
                            apiRuleTempl.metadata.name);
        //console.log(result.response);
        if(result.response.statusCode == 200){
            console.log(apiRuleTempl.metadata.name + ' already exists.');
            res.status(200).send(tenantAppURL);
        }
    }catch (err) {
        //create apirule if non-exist
        console.warn(apiRuleTempl.metadata.name + ' does not exist, creating one...');
        try {
            const createResult = await k8sApi.createNamespacedCustomObject(KYMA_APIRULE_GROUP,
                                            KYMA_APIRULE_VERSION,
                                            EF_APIRULE_DEFAULT_NAMESPACE,
                                            KYMA_APIRULE_PLURAL,
                                            apiRuleTempl);
            //console.log(createResult.response);
    
            if(createResult.response.statusCode == 201){
                console.log("API Rule created!");
                res.status(200).send(tenantAppURL);
            }
        }catch (err) {
            console.log(err);
            console.error("Fail to create APIRule");
            res.status(500).send("create APIRule error");
        }
    }  
    console.log("exiting onboarding...");

});


//  Off-boarding tenant
app.delete('/callback/v1.0/tenants/*', async function (req, res) {

    if (!req.authInfo.checkLocalScope('Callback')) {
        log('Missing the expected scope - Callback - in the call of off-boarding tenant');
        res.status(403).end('Forbidden');
        return;
    }

    var consumerSubdomain = req.body.subscribedSubdomain;
    if(!req.body.subscribedTenantId || !consumerSubdomain){
        res.status(400).send("subscribedTenantId or subscribedSubdomain do not exist!");
        return;
    }

    //1. create tenant unique URL, e.g.https://d049740consume.c-2e512db.kyma-stage.shoot.live.k8s-hana.ondemand.com/

    var tenantAppURL = 'https://' + consumerSubdomain + "." + kyma_cluster;
    console.log("Off-boarding consumerSubdomain = " + consumerSubdomain);
    console.log("cluster domain: " + kyma_cluster);
    console.log("Off-boarding tenant URL: " + tenantAppURL);
    
    //2. extract db.service url from env variable
    const servicelist = process.env.SERVICE_LIST;
    let dbRegEx = /db.service:\s*(?<dbservice>.*)/;
    let match = servicelist.match(dbRegEx);
    if(match === null){
        res.status(400).send("db.service url does not exist in configmap!");
        console.log("missing db.service in configmap: SERVICE_LIST: " + servicelist);
        return;
    }

    //Example of db service call: 'http://dbservice.integration/easyfranchise/rest/dbservice/v1/344324-4242423-42423-23423/offboard';
    const url = match.groups.dbservice + req.body.subscribedTenantId + '/offboard';
    const content = {broker: 'offboarding'};
    const config = { headers: {'Content-Type': 'application/json'} };
    console.log("calling db service: " + url);
    
    //delete db schema for tenant
    try {
        const response = await axios.put(url, content, config );
        if(response.status == 200){
            console.log("delete database schema finished succesfully " + response.data);
        }
    }catch (err) {
        console.log("delete database schema finished with error");
        console.error(err);
    }

    //3. delete apirule with subdomain
    const kc = new k8s.KubeConfig();
    kc.loadFromCluster();
    
    const k8sApi = kc.makeApiClient(k8s.CustomObjectsApi);

    const apiRuleTempl = createApiRule.createApiRule(
                            EF_SERVICE_NAME, 
                            EF_SERVICE_PORT, 
                            consumerSubdomain, 
                            kyma_cluster);

    try{
        const result = await k8sApi.deleteNamespacedCustomObject(
                                KYMA_APIRULE_GROUP,
                                KYMA_APIRULE_VERSION,
                                EF_APIRULE_DEFAULT_NAMESPACE,
                                KYMA_APIRULE_PLURAL, 
                                apiRuleTempl.metadata.name);
        if(result.response.statusCode == 200){
            console.log("API Rule deleted!");
        }
    }catch (err) {
        console.error(err);
        console.error("API Rule deletion error");
    }
    
    res.status(200).send(tenantAppURL);
});

// getDependencies
app.get('/callback/v1.0/dependencies', function (req, res) {
    if (!req.authInfo.checkLocalScope('Callback')) {
        log('Missing the expected scope - Callback - in the call to get Dependencies');
        res.status(403).end('Forbidden');
        return;
    }

    let services = xsenv.readServices();
    let dependencies = [];
    let appId = null;
    let appName = null;
    let xsappname = null;
    
    for (let service in services) {
        xsappname = appId = appName = null;

        let cred = services[service].credentials;
        let svcName = services[service].label;

        if (svcName === 'destination' || svcName === 'connectivity' ) {
            appId = cred.xsappname;
            appName = svcName;
        } else if (cred.saasregistryappname){
            appId = cred.uaa.xsappname;
            appName = cred.saasregistryappname;
        } else if (cred.saasregistryenabled){
            xsappname = cred.uaa.xsappname;
        }

        var obj = null;
        if (appId && appName) {
          obj = {'appId': appId, 'appName': appName};
        } else if (xsappname){
          obj = {'xsappname': xsappname};
        }
        
        if (obj){
            console.log("appId: " + appId + " appName: " + appName);
            console.log("xsappname:" + xsappname);
            dependencies.push(obj);
        }   
    }
    console.log(services);
    console.log("dependencies output: ");
    console.log(JSON.stringify(dependencies));
    res.setHeader('Content-Type', 'application/json');
    res.end(JSON.stringify(dependencies));
});


var port = process.env.PORT || 3002;
app.listen(port, function () {
    console.log("Application listening on port " + port);
});
