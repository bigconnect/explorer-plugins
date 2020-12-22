package com.mware.web.auth.usernameonly.routes;

import com.google.inject.Inject;
import com.mware.core.model.user.AuthorizationContext;
import com.mware.core.model.user.UserNameAuthorizationContext;
import com.mware.core.model.user.UserRepository;
import com.mware.core.security.AuditService;
import com.mware.core.user.User;
import com.mware.web.CurrentUser;
import com.mware.web.framework.ParameterizedHandler;
import com.mware.web.framework.annotations.Handle;
import com.mware.web.framework.utils.UrlUtils;
import com.mware.web.util.RemoteAddressUtil;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

public class Login implements ParameterizedHandler {
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Inject
    public Login(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Handle
    public JSONObject handle(HttpServletRequest request) {
        final String username = UrlUtils.urlDecode(request.getParameter("username")).trim().toLowerCase();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            // For form based authentication, username and displayName will be the same
            String randomPassword = UserRepository.createRandomPassword();
            user = userRepository.findOrAddUser(
                    username,
                    username,
                    null,
                    randomPassword
            );
        }

        AuthorizationContext authorizationContext = new UserNameAuthorizationContext(
                username,
                RemoteAddressUtil.getClientIpAddr(request)
        );
        userRepository.updateUser(user, authorizationContext);

        CurrentUser.set(request, user);
        auditService.auditLogin(user);
        JSONObject json = new JSONObject();
        json.put("status", "OK");
        return json;
    }
}
