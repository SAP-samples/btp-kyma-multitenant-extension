# Easy Franchise Components

This section contains all components that are needed to run the application EasyFranchise.

- [backend](backend): JAVA based backend services that manage database, S/4HANA system and the logic of the application
    - [DB Service](backend/db-service)
    - [BP Service](backend/bp-service)
    - [EF Service](backend/ef-service)
- [email-service](email-service): node.js based service that sends notifications
- [ui](ui): vue.js based user interface
- [Approuter](approuter): node.js based application router for authorization and authentication
- [broker](broker): service that onboards/offboards new customers of the multitenant application
- [business-partner-mock-server](business-partner-mock-server): service to simulate a S/4HANA system if not available

