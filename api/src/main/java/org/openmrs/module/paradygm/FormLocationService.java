package org.openmrs.module.paradygm;

import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing the association of forms with locations in the system.
 * Provides methods for binding forms to current locations, unbinding them,
 * retrieving forms available at the current location, and verifying their availability.
 */
@Service
public class FormLocationService {

    @Autowired
    private CustomEntityBasisMapService customEntityBasisMapService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private UserService userService;

    @Autowired
    private FormService formService;

    // For testing purposes
    private User authenticatedUser;
    private Location currentLocation;

    /**
     * Binds a form to the current location
     *
     * @param form the form to bind to the current location
     * @return the created mapping or null if mapping already exists
     * @throws APIException if no location is set or user is not authenticated
     */
    @Transactional
    public EntityBasisMap bindFormToCurrentLocation(Form form) {
        if (form == null || form.getId() == null) {
            throw new APIException("Form cannot be null and must be saved before binding");
        }

        Location location = getCurrentLocation();
        User user = getCurrentUser();

        // Check if mapping already exists
        if (!customEntityBasisMapService.hasMapping(form.getId(), location.getId())) {
            EntityBasisMap map = createMapping(form, location, user);
            customEntityBasisMapService.save(map);
            return map;
        }

        return null; // Mapping already existed
    }

    /**
     * Unbinds a form from the current location
     *
     * @param form the form to unbind
     * @throws APIException if form is null or not saved
     */
    @Transactional
    public void unbindFormFromCurrentLocation(Form form) {
        if (form == null || form.getId() == null) {
            throw new APIException("Form cannot be null and must be saved before unbinding");
        }

        Location location = getCurrentLocation();
        customEntityBasisMapService.deleteMapping(form.getId(), location.getId());
    }

    /**
     * Gets all forms available for the current location
     *
     * @return list of forms assigned to the current location
     */
    @Transactional(readOnly = true)
    public List<Form> getFormsForCurrentLocation() {
        Location location = getCurrentLocation();
        List<EntityBasisMap> maps = customEntityBasisMapService.getAllFormsForLocation(location.getId());

        List<Integer> formIds = maps.stream()
                .map(map -> Integer.parseInt(map.getEntityIdentifier()))
                .collect(Collectors.toList());

        List<Form> forms = new ArrayList<>();
        for (Integer formId : formIds) {
            Form form = formService.getForm(formId);
            if (form != null) {
                forms.add(form);
            }
        }

        return forms;
    }

    /**
     * Checks if a form is available in the current location
     *
     * @param form the form to check
     * @return true if form is available in current location, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isFormAvailableInCurrentLocation(Form form) {
        if (form == null || form.getId() == null) {
            return false;
        }

        try {
            Location location = getCurrentLocation();
            return customEntityBasisMapService.hasMapping(form.getId(), location.getId());
        } catch (APIException e) {
            return false;
        }
    }

    /**
     * Creates an EntityBasisMap for a form-location pair
     */
    private EntityBasisMap createMapping(Form form, Location location, User user) {
        EntityBasisMap map = new EntityBasisMap();
        map.setEntityType("org.openmrs.Form");
        map.setEntityIdentifier(form.getId().toString());
        map.setBasisType("org.openmrs.Location");
        map.setBasisIdentifier(location.getId().toString());
        map.setCreator(user);
        map.setDateCreated(new Date());
        map.setUuid(UUID.randomUUID().toString());
        return map;
    }

    /**
     * Gets the current location
     *
     * @return the current location
     * @throws APIException if no location is set
     */
    private Location getCurrentLocation() {
        if (currentLocation != null) {
            return currentLocation;
        }

        // Try to get the session location
        Location location = null;
        Integer sessionLocationId = Context.getUserContext().getLocationId();

        if (sessionLocationId != null) {
            location = locationService.getLocation(sessionLocationId);
        }

        // Fall back to default location if session location not set
        if (location == null) {
            location = locationService.getDefaultLocation();
        }

        if (location == null) {
            throw new APIException("No location set in user session");
        }

        return location;
    }

    /**
     * Gets the current authenticated user
     *
     * @return the current authenticated user
     * @throws APIException if no user is authenticated
     */
    private User getCurrentUser() {
        if (authenticatedUser != null) {
            return authenticatedUser;
        }

        User user = Context.getAuthenticatedUser();
        if (user == null) {
            throw new APIException("No authenticated user found");
        }

        return user;
    }

    // For testing purposes
    public void setAuthenticatedUser(User user) {
        this.authenticatedUser = user;
    }

    // For testing purposes
    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    // Getters and setters for Spring dependencies
    public void setCustomEntityBasisMapService(CustomEntityBasisMapService service) {
        this.customEntityBasisMapService = service;
    }

    public void setLocationService(LocationService service) {
        this.locationService = service;
    }

    public void setUserService(UserService service) {
        this.userService = service;
    }

    public void setFormService(FormService service) {
        this.formService = service;
    }
}
