package org.openmrs.module.paradygm;

import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class FormLocationService {

    @Autowired
    private CustomEntityBasisMapService customEntityBasisMapService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService userService;

    // For testing purposes
    private User authenticatedUser;

    @Transactional
    public void bindFormToCurrentLocation(Form form) {
        Location currentLocation = locationService.getDefaultLocation();
        if (currentLocation == null) {
            throw new IllegalStateException("No location set in user session");
        }

        User user = authenticatedUser != null ? authenticatedUser : userService.getUser(1); // Get authenticated user

        // Check if mapping already exists
        if (customEntityBasisMapService.getMapsForFormAndLocation(form.getId(), currentLocation.getId()).isEmpty()) {
            EntityBasisMap map = new EntityBasisMap();
            map.setEntityType("org.openmrs.Form");
            map.setEntityIdentifier(form.getId().toString());
            map.setBasisType("org.openmrs.Location");
            map.setBasisIdentifier(currentLocation.getId().toString());
            map.setCreator(user);
            map.setDateCreated(new Date());

            customEntityBasisMapService.save(map);
        }
    }

    // For testing purposes
    public void setAuthenticatedUser(User user) {
        this.authenticatedUser = user;
    }
}