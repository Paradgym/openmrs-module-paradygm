package org.openmrs.module.paradygm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.DataFilterContext;
import org.openmrs.module.datafilter.DataFilterListener;
import org.springframework.stereotype.Component;


/**
 * A listener that implements location-based filtering of forms within the system.
 * This class is responsible for enabling or disabling the location filter for forms
 * based on the current user session, system properties, and authenticated user's role.
 * The filter ensures that users can only access forms assigned to their current location.
 *
 * The filtering mechanism utilizes the `DataFilterListener` interface to interact
 * with the filtering framework.
 */
@Component
public class FormLocationFilterListener implements DataFilterListener {

    private static final Log log = LogFactory.getLog(FormLocationFilterListener.class);
    private static final String FILTER_PROPERTY = "paradygm.formLocationFilterEnabled";
    private static final String FILTER_NAME = "paradygm_locationBasedFormFilter";

    @Override
    public boolean onEnableFilter(DataFilterContext filterContext) {
        // Check if filtering is enabled
        AdministrationService adminService = Context.getAdministrationService();
        String filterEnabled = adminService.getGlobalProperty(FILTER_PROPERTY, "true");
        if ("false".equalsIgnoreCase(filterEnabled)) {
            return false;
        }

        // Skip for non-authenticated users
        if (!Context.isAuthenticated()) {
            return false;
        }

        // Skip for super users
        if (Context.getAuthenticatedUser().isSuperUser()) {
            log.debug("Skipping form location filter for superuser");
            return false;
        }

        if (FILTER_NAME.equals(filterContext.getFilterName())) {
            // Try to get the session location first
            Location currentLocation = null;
            Integer sessionLocationId = Context.getUserContext().getLocationId();

            if (sessionLocationId != null) {
                currentLocation = Context.getLocationService().getLocation(sessionLocationId);
            }

            // Fall back to default location if session location not set
            if (currentLocation == null) {
                currentLocation = Context.getLocationService().getDefaultLocation();
            }

            if (currentLocation != null) {
                log.debug("Setting form location filter for location: " + currentLocation.getName());
                filterContext.setParameter("basisIds", currentLocation.getId().toString());
            } else {
                // If no location, return no forms
                log.warn("No location found for user, restricting form access");
                filterContext.setParameter("basisIds", "0");
            }
        }
        return true;
    }

    @Override
    public boolean supports(String filterName) {
        return FILTER_NAME.equals(filterName);
    }
}
