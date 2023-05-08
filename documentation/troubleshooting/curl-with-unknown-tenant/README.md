# CURL Response Returns a "unknown tenant"

Symptom: 
* You are executing a curl command where a tenant is required but not existing. As a consequence, you get an error: ```unknown tenant: 123456789-local-tenant-id```.

**If you are in a local test mode**
1. Check your configuration ```hiddenconfig.properties``` file as described here: [Configure Properties](../../../documentation/prepare/test-app-locally/README.md#configure-properties). Check the, which value you have configured for the property  ```devmode.tenantid``` which is used for local testing. 
3. Check which tenants already exist:

   ```
   curl --verbose -X GET http://localhost:8090/easyfranchise/rest/dbadminservice/v1/config/tenants
   ```
   If the expected tenant doesn't exist, onboard the tenant as described in the chapter [Onboard the First Tenant](../../../documentation/prepare/test-app-locally/README.md#onboard-the-first-tenant). The tenant you have configured in your ```hiddenconfig.propertie``` via the property ```devmode.tenantid``` have to match to the onboard process.
4. If all of this doesn't help redo all steps of [Test the Easy Franchise Application Locally](../../../documentation/prepare/test-app-locally/README.md).

**If you aren't in a local test mode**
1. Check the steps of the chapter [Subscribe to the Easy Franchise Application](../../../documentation/test-customer-onboarding/subscribe-easyfranchise-app/README.md).
2. Check if you can find the expected tenants in the according database table **TENANT**. 

 
