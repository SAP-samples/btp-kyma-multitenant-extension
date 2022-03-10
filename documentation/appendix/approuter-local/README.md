# How to Set Up Approuter for Local Development 

1. Create `default-env.json` in the folder **approuter** with the following content:

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

1. Run the following command with correct Kyma cluster KUBECONFIG context, and replace the **credentials** section of previously shown JSON file with the command output:

	```sh
	kubectl -n integration get secrets xsuaa-service-binding -o go-template='
	{{range $k,$v := .data}}{{printf "%q: " $k}}{{if not $v}}{{printf "%q" $v}}{{else}}{{$v | base64decode | printf "%q" }}{{end}}{{",\n"}}{{end}}'

	```

1. In the folder **approuter**, run the following command to start Approuter locally:

	```sh
	# make sure run 'npm install' once before below command
	npm run start

	```

1. In the folder **UI**, run the following command to start the UI locally:

	```sh
	npm run serve
	```

1. Forward **efservice** port to local port:

	```sh
	kubectl -n backend port-forward svc/efservice 8888:80

	```

1. Access the application locally with this URL: http://<subdomain>.localhost:5000/. 

Assuming the subaccount domain is **easyfranchisedev**, the URL would be http://easyfranchisedev.localhost:5000. Make sure the subaccount has an active subscription and your user exists in that subaccount with the proper role collections assigned.
