# Easy Franchise Components

This section contains all components that are needed to run the Easy Franchise application.

- [backend](backend): JAVA-based backend services that manage database, SAP S/4HANA Cloud system and the logic of the application
    - [Database Service](backend/db-service)
    - [Business Partner Service](backend/bp-service)
    - [Easy Franchise Service](backend/ef-service)
- [email-service](email-service): node.js-based service that sends notifications
- [ui](ui): vue.js-based user interface
- [Approuter](approuter): node.js-based application router for authorization and authentication
- [broker](broker): service that onboards/offboards new customers of the multitenant application
- [business-partner-mock-server](business-partner-mock-server): service to simulate an SAP S/4HANA Cloud system if not available

