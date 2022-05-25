const jwt_decode = require('jwt-decode');
const approuter = require('@sap/approuter');
const xssec = require('@sap/xssec');
const xsenv = require('@sap/xsenv');

var ar = approuter();
ar.beforeRequestHandler.use('/backend', function (req, res, next) {
    const token = req.user.token.accessToken;
    if (!token) {    
        res.statusCode = 403;
        res.end("Missing JWT Token");
    } else {
        const decodedToken = jwt_decode(token);
        const tenant = decodedToken && decodedToken.zid;
        req.headers['x-tenant-id'] = tenant;
        next();
    }
});

ar.beforeRequestHandler.use('/userInfo', function (req, res, next) {
    const token = req.user.token.accessToken;
    if (!token) {    
        res.statusCode = 403;
        res.end("Missing JWT Token");
    } else {
        res.statusCode = 200;
        let decodedToken = jwt_decode(token);
        res.end(JSON.stringify({
            userid: decodedToken.user_name,
            email: decodedToken.email,
            firstname: decodedToken.given_name,
            lastname: decodedToken.family_name
        }));
    }
});
ar.start();
