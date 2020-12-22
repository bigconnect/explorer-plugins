define(['configuration/plugins/registry'], function(registry) {
    'use strict';

    registry.registerExtension('org.bigconnect.authentication', {
        componentPath: 'com/mware/web/auth/usernameonly/authentication'
    })
});
