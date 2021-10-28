#!/usr/bin/env bash
# https://saas-manager.cfapps.eu10.hana.ondemand.com/api#/API%20order%20a/getApplicationSubscriptions

# determine the kyma runtime region, e.g. eu10 or eu20
REGION="$(kubectl get ClusterServiceBroker | sed -nr 's/.*service-manager.cfapps.(.*).hana.*/\1/p' | head -n 1)"

echo -e "Checking active subscription in cluster \033[1;31m $(kubectl config current-context) \033[0m of region $REGION"

CLIENT_ID="$(kubectl -n integration get secrets saas-registry-service-binding --ignore-not-found -o jsonpath='{.data.clientid}' | base64 --decode)"
#echo $CLIENT_ID

CLIENT_SECRET="$(kubectl -n integration get secrets saas-registry-service-binding --ignore-not-found -o jsonpath='{.data.clientsecret}' | base64 --decode)"
#echo $CLIENT_SECRET

URL="$(kubectl -n integration get secrets saas-registry-service-binding --ignore-not-found -o jsonpath='{.data.url}' | base64 --decode)"
#echo $URL

TOKEN="$(curl -s -L -X POST "$URL/oauth/token" -H 'Content-Type: application/x-www-form-urlencoded' -u "$CLIENT_ID:$CLIENT_SECRET" -d 'grant_type=client_credentials' | jq -r '.access_token')"
#echo $TOKEN

# check active subscription
# echo "active subscription in region $REGION: "
#curl  -H "Authorization: Bearer $TOKEN"   "https://saas-manager.cfapps.${REGION}.hana.ondemand.com/saas-manager/v1/application/subscriptions"
subscription=$(curl -sS -H "Authorization: Bearer $TOKEN"   "https://saas-manager.cfapps.${REGION}.hana.ondemand.com/saas-manager/v1/application/subscriptions")
echo $subscription
LENGTH="$(echo $subscription | jq '.subscriptions | length')"
echo $LENGTH
