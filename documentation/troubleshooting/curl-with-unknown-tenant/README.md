# A CURL responding with "unknown tenant"

Symptom: 
* You are executing a curl command where a tenant is required but existing hence you get an error: ```unknown tenant: 123456789-local-tenant-id``` 

**If you are in a local test mode:**
1.  Check your configuration```hiddenconfig.properties``` files as described here: [Configure Properties](../../../documentation/prepare/test-app-locally/README.md#configure-properties). Check the ```devmode.tenantid``` which is used for local testing. 
2. Check which tenants already exist:

   ```
   curl --verbose -X GET http://localhost:8090/easyfranchise/rest/dbadminservice/v1/config/tenants
   ```
   If your expected tenant does not exist, onboard a tenant as described in Chapter [Onboard the First Tenant](../../../documentation/prepare/test-app-locally/README.md#onboard-the-first-tenant). The tenant you have configured in your ```hiddenconfig.propertie``` via the property ```devmode.tenantid``` have to match to the onboard process.
3. If all of this doesn't help redo all steps of [Test the Easy Franchise Application Locally](../../../documentation/prepare/test-app-locally/README.md)

**If running non local**
1. Check the subscribe steps of chapter [Subscribe to the Easy Franchise Application](../../../documentation/test-customer-onboarding/subscribe-easyfranchise-app/README.md)
2. Check if you can find expected Tenants in the according Database Table **TENANT**. 

 
