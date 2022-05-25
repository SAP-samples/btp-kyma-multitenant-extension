# Fast Path to Account Setup
## Automate Account Setup with Bash Scripts, cf CLI, and btp CLI

## Prerequisites

* Supported Systems: Linux / Mac / Ubuntu on Windows
* Bash, ability to modify $PATH, install additional software
* [SAP BTP Command Line Interface](https://tools.hana.ondemand.com/) (btp CLI)
* [Cloud Foundry Command Line Interface](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) (cf CLI)
* JQ: `sudo apt-get install jq`
* (Optional) VS Code
* (Optional) [Shellcheck](https://github.com/vscode-shellcheck/vscode-shellcheck)

The two optional components are useful if you want to understand the scripts in detail or modify them.

## Create Pay-As-You-Go Account

These setup steps are for new users. If you already have an account, some steps can differ. In that case make sure you have the right account type and entitlements, otherwise the automated account setup can fail.

* Visit [SAP Store](https://store.sap.com/dcp/en/)
* In **Browse** menu select **SAP Business Technology Platform** **&rarr;** **All in SAP Business Technology Platform**
* You should find the card **Pay-AsYou-Go for SAP BTP**. After choosing it, you see different options for plans and pricing. Add the plan **Get started free** to your cart.
* You can choose length of the renewal cycle and continue to checkout, where you have to enter payment details if not already stored on your account.
* Make sure you understand that this account only is free as long as you do not book any non-free services! Following our instructions on this page will only activate free services.
* After you received an e-mail that your account is ready to use, go to [SAP BTP Cockpit](https://account.hana.ondemand.com/) and sign in.
* If there are multiple accounts associated with your user, you will see a choice to select one.

## Usage

Start a shell and go to the [setup folder](../../../code/setup).

Make sure that you have installed the btp CLI and cf CLI as mentioned in the prerequisites and that both clients can be found. The script will check that the clients are available.

Our script will create the following items under your Global Account after you provided all the required input:
* Directory
* Subaccount
* Kyma and SAP HANA entitlements
* Kyma and Cloud Foundry enabling
* SAP HANA free-plan creation in Cloud Foundry space

Execute the script:

```shell
./account_setup.sh
```

### Client Login

If you are unsure which URLs to use for login with btp CLI or cf CLI the following overview might help. It depends on your Global Account.

#### SAP BTP Login

* Canary: https://cpcli.cf.sap.hana.ondemand.com
* All others: https://cpcli.cf.eu10.hana.ondemand.com

#### Cloud Foundry Login

You do not need to select an Org after logging into Cloud Foundry. Just skip this question!

* Canary: https://api.cf.sap.hana.ondemand.com/
* EU20: https://api.cf.eu20.hana.ondemand.com
* EU10: https://api.cf.eu10.hana.ondemand.com
* US10: https://api.cf.us10.hana.ondemand.com

## Further Info

* See the [SAP BTP Command Line Interface](https://help.sap.com/products/BTP/65de2977205c403bbc107264b8eccf4b/7c6df2db6332419ea7a862191525377c.html) and [Cloud Foundry Command Line Interface](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) documentation for more details.
