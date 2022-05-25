module.exports = {
	createApiRule: createApiRule
}

function createApiRule(svcName, svcPort, host, clusterName) {
    
    let forwardUrl = host + '.' + clusterName;
    const supportedMethodsList = [
        'GET',
        'POST',
        'PUT',
        'PATCH',
        'DELETE',
        'HEAD',
    ];
    const access_strategy = {
        path: '/.*',
        methods: supportedMethodsList,
        mutators: [
            {
                handler: 'header',
                config: {
                    headers: {
                        "x-forwarded-host": forwardUrl,
                    }
                },
            }
        ],
        accessStrategies: [
          {
            handler: 'noop'
          },
        ],
    };
    
    const apiRuleTemplate = {
        apiVersion: 'gateway.kyma-project.io/v1alpha1',
        kind: 'APIRule',
        metadata: {
          name: host + '-apirule',
        },
        spec: {
          gateway: 'kyma-gateway.kyma-system.svc.cluster.local',
          service: {
            host: host,
            name: svcName,
            port: svcPort,
          },
          rules: [access_strategy],
        },
      };
      return apiRuleTemplate;
  }