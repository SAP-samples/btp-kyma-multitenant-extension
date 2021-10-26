# How to Setup AppRouter for Local Development 

- Create `default-env.json` in folder **AppRouter** with following content:

```json
{
    "destinations" : 
    [      
        {"name":"ef-ui","url":"http://localhost:8080","forwardAuthToken" : true},
        {"name":"ef-service","url":"http://localhost:8888","forwardAuthToken" : true}
    ],
	"TENANT_HOST_PATTERN": "^(.*).localhost",
    "VCAP_SERVICES": {
		"xsuaa": [
			{
                "name": "xsuaa-service",                
				"label": "xsuaa",				
				"tags": [
					"xsuaa"
				],
				"credentials": {
					"apiurl": "xxx",
					"clientid": "xxx",
					"clientsecret": "xxx",
                    "credential-type": "xxx",
					"identityzone": "xxx",
					"identityzoneid": "xxx",
					"sburl": "xxx",
					"subaccountid": "xxx",
					"tenantid": "xxx",
					"tenantmode": "xxx",
					"trustedclientidsuffix": "xxx",
					"uaadomain": "xxx",
					"url": "xxx",
					"verificationkey": "xxx",
					"xsappname": "xxx",
					"zoneid": "xxx"
				}
			}
		]
    }
}

```

- Run the following command with correct Kyma cluster KUBECONFIG context, and replace the **credentials** section of previously shown JSON file with the command output:

```sh
kubectl -n integration get secrets xsuaa-service-binding -o go-template='
{{range $k,$v := .data}}{{printf "%q: " $k}}{{if not $v}}{{printf "%q" $v}}{{else}}{{$v | base64decode | printf "%q" }}{{end}}{{",\n"}}{{end}}'

```

- In the folder **AppRouter** run the following command to start AppRouter locally:

```sh
# make sure run 'npm install' once before below command
npm run start

```

- In the folder **UI** run the following command to start UI locally:

```sh
npm run serve
```

- Forward **efservice** port to local port:

```sh
kubectl -n backend port-forward svc/efservice 8888:80

```

- Access the application locally with url http://<subdomain>.localhost:5000/. 

Assuming the sub account domain is **easyfranchisedev**, the URL would be http://easyfranchisedev.localhost:5000. Make sure the sub account has an active subscription and your user exists in that sub account with proper role-collections.