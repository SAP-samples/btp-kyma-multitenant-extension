# Set Up the Provider Subaccount in SAP BTP

## Using btp-setup-automator

You can use the [btp-setup-automator](https://github.com/SAP-samples/btp-setup-automator) to configure the SAP BTP environment needed for the mission. You can find the instructions how to prepare the account in the [use case for our mission](https://github.com/SAP-samples/btp-setup-automator/blob/main/usecases/released/discoverycenter/3638-kyma-multitenant/README.md).

As an alternative you can also setup the account manually following the steps described below.

## Create the Provider Subaccount

To host the Easy Franchise application, we will create a provider subaccount.

1. Open the SAP BTP cockpit and create a new subaccount from the **Account Explorer**.
![](images/Create-Subaccount-1.png)

2. In the dialog that opens, specify the following details:
   * **Display Name**: **Easy Franchise**
   * **Subdomain**: Use the predefined entry to avoid name clashes (it will show up after you choose a region)
   * **Region**: Choose a region close to you (Note: If you go for the Free-Tier-Model stick to AWS regions)

   ![](images/Create-Subaccount-2.png)

## Configure Entitlements

Now that we have created the provider subaccount, we will add entitlements.

1. Navigate to the **Entitlements** page.
1. Choose **Configure Entitlements** and then choose **Add Service Plans**.
1. Search for **Kyma runtime**.
1. Select the suggested plan.
1. Select Entitlement **SAP HANA Cloud** and mark the plan **hana**.
1. Search for **Authorization and Trust Management Service** and choose the plan **broker**
1. Choose **Add 3 Service Plans**.
1. Save your changes.

   ![](images/Configure-Entitlements.png)


## Enable the Cloud Foundry and the Kyma Environments

> Note: The **Cloud Foundry** environment is only needed for the **SAP HANA Cloud Instance** as this is today not possible to have the database directly in the Kyma environment. We will update this section, as soon as possible.

1. Navigate back to the **Overview** page of the subaccount.
2. Choose **Enable Cloud Foundry**.

   ![](images/Enable-Environments.png)

3. In the opened dialogue leave the settings as suggested and press button **Create**.

    ![](images/enableCF.png)

6. Navigate back to the **Overview** page of the subaccount and choose **Enable Kyma**.
7. In the opened dialog:
   * Step 1 - **Basic Info**: Use the suggested parameters
   * Step 2 - **Addional Parameters**: Select a region close to you and choose the smallest machine type. Use the minimal possible value for parameter **Auto Scaler**.
   * Step 3 - **Review**: Review your settings and choose **Create**.
8. Enabling Kyma will take around 30 minutes.

## Create a Cloud Foundry Space

We need a space in the Cloud Foundry environment, where we can create the **SAP HANA Cloud Instance**.

1. Navigate to the **Spaces** page.
2. Choose **Create Space**.
3. Enter a name, for example, **hana**.
4. Choose **Create**.

   ![](images/Create-Space.png)

## Validate Account Setup

Follow the steps to verify that the setup was successful:

1. Choose the **Overview** page of the provider subaccount.
2. Check that **Cloud Foundry Environment** is enabled and has a space **hana**.
3. Check that you have a running Kyma environment.

   ![](images/success.png)

4. Choose **Link to dashboard** under **Kyma Environment**. The Kyma dashboard should open in a new browser tab.

   ![](images/kymadashboard.png)

   Per default, the user that has enabled the Kyma Environment is granted with the cluster admin role. In order to give access to further users have a look at the [Kyma Help Documentation](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/148ae38b7d6f4e61bbb696bbfb3996b2.html?locale=en-US). You need to follow the steps described under Kyma 2.x.
