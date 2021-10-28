# Set Up the Provider Subaccount in SAP BTP

To host the Easy Franchise application, we will create a provider subaccount. 

1. Open the SAP BTP cockpit and create a new subaccount from the **Account Explorer**.
![](images/Create-Subaccount-1.png) 

1. In the dialog that opens, specify the following details:
   * **Display Name**: **Easy Franchise**  
   * **Subdomain**: Use the predefined entry to avoid name clashes (it will show up after you choose a region)
   * **Region**: Choose a region close to you (Note: If you go for the Free-Tier-Model stick to AWS regions)
![](images/Create-Subaccount-2.png)

Now that we have created the provider subaccount, we will set up the environments.

1. Add the Kyma runtime and SAP HANA Cloud entitlements to the subaccount.
   1. Navigate to the **Entitlements** page.
   2. Choose **Configure** and then choose **Add Service Plans**.
   4. Search for **Kyma runtime**.
   5. Select the suggested plan. Repeat step 4 and 5 for **SAP HANA Cloud** with plan **hana**.
   6. Choose **Add 2 Service Plans**.
   7. Save your changes.
   ![](images/Configure-Entitlements.png)

2. Enable the Cloud Foundry and the Kyma environments.
   1. Navigate back to the **Overview** page of the subaccount.
   2. Choose **Enable Cloud Foundry** and leave the settings as suggested.
   3. Choose **Enable Kyma**.
      1. Use the suggested parameters
      2. Select a region close to you and choose the smallest machine type. For Autoscaller set min to 1.
      3. Review your settings and choose **Create**. This will takesaround 30 minutes.
     ![](images/Enable-Environments.png)
3. Create a Cloud Foundry space.
   1. Navigate to the **Spaces** page.
   2. Choose **Create Space**.
   3. Enter a name, for example, **hana**.
   4. Choose **Create**.
   ![](images/Create-Space.png)
   
4. Assign Role Collection for the Kyma environment. This is available only when Kyma environment is ready.
   1. Navigate to the **Role Collections**.
   2. You should see two Kyma-related role collections: **KymaRuntimeNamespaceAdmin** and **KymaRuntimeNamespaceDeveloper**.
   3. Choose **KymaNamespaceAdmin**.
   4. In the new screen, choose **Edit and assign your user to the role collection** and save the changes.
   5. Repeate the assignement with the second role collection.
  ![](images/Assign-Role-Collection.png)
  
5. Verify the environment.
If everthing is configured properly, you should see a similar picture in the account overview. 
   1. Choose the **Overview** page.
   2. Check that you have a Cloud Foundry space.
   3. Check that you have a running Kyma environment.
   4. Click the link to open the Kyma Dashboard.
   ![](images/success.png)
