package org.openmrs.module.paradygm;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.DataFilterContext;
import org.openmrs.module.datafilter.DataFilterListener;
import org.springframework.stereotype.Component;

@Component
public class FormLocationFilterListener implements DataFilterListener {

    @Override
    public boolean onEnableFilter(DataFilterContext filterContext) {
        if (Context.isAuthenticated() && Context.getAuthenticatedUser().isSuperUser()) {
            return false; // Skip for super users
        }

        if ("datafilter_locationBasedFormFilter".equals(filterContext.getFilterName())) {
            Location currentLocation = Context.getLocationService().getDefaultLocation();
            if (currentLocation != null) {
                filterContext.setParameter("basisIds", currentLocation.getId().toString());
            } else {
                // If no location, return no forms
                filterContext.setParameter("basisIds", "0");
            }
        }
        return true;
    }

    @Override
    public boolean supports(String filterName) {
        return "datafilter_locationBasedFormFilter".equals(filterName);
    }
}