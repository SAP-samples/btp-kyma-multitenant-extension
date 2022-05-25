# Failing Application Subscription

Issue: The subscription as described in section [Subscribe to the Easy Franchise Application](../../../documentation/test-customer-onboarding/subscribe-easyfranchise-app/README.md) fails.

Reason: SAP HANA doesn't allow access from all IP addresses.

Symptom: The subscription fails and the error message looks like `Reason: returned value from application: 500 INTERNAL_SERVER_ERROR, with body: create database schema error`.

Check if you allowed the access from all IP addresses:
1. Open the **SAP HANA Cloud Central** of your used database:
   * Open Subaccount **Easy Franchise** in browser
   * Open its space **hana**
   * In the opened space **hana** select **SAP HANA Cloud** in the left side menu. Select your database.
   * Open the **Actions** menu and select **Open in SAP HANA Database Explorer**
   ![](images/open-sap-hana-cloud-central.png)
2. In the **SAP HANA Cloud Central**, select your database and press on the 3 dots and select **Edit**.

   ![](images/edit-db.png)
3. Check in the upcoming dialog for the **Connections** properties and check if **Allow all IP addresses** is selected. If not, change it and **Save**.

   ![](images/allow-all-ip.png)
4. Try to subscribe again.
