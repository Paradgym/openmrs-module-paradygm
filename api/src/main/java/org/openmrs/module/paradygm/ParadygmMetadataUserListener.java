package org.openmrs.module.paradygm;

import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.UserSessionListener;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of UserSessionListener for assigning appropriate roles
 * to users in the Paradygm EMR system upon login
 */
@Component
@Qualifier("paradygmMetadataUserListener")
public class ParadygmMetadataUserListener implements UserSessionListener {
    private static final Logger log = LoggerFactory.getLogger(ParadygmMetadataUserListener.class);

    @Override
    public void loggedInOrOut(User user, Event event, Status status) {
        try {
            if (event == Event.LOGIN) {
                String username = user.getUsername();
                String roleName = ParadygmMetadataConstants.USER_ROLES.get(username);
                if (roleName != null) {
                    Role paradigmRole = Context.getUserService().getRole(roleName);
                    verifyUserRole(user, paradigmRole);
                } else {
                    log.debug("User: '{}' is not a Paradygm EMR user, skipping role assignment", user.getUsername());
                }
            }
        } catch (Exception e) {
            log.error("Unable to assign Paradygm EMR user corresponding role", e);
        }
    }

    private void verifyUserRole(User user, Role role) {
        if (!user.hasRole(role.getRole())) {
            user.addRole(role);
            Context.getUserService().saveUser(user);
            log.debug("Granted Paradygm EMR user: '{}' role: '{}'", user.getUsername(), role.getRole());
        } else {
            log.debug("Paradygm EMR user: '{}' already has role: '{}'. Skipping...", user.getUsername(), role.getRole());
        }
    }
}