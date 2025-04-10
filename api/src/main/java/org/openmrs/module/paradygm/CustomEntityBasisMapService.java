package org.openmrs.module.paradygm;

import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.module.datafilter.impl.EntityBasisMap;

import java.util.List;

/**
 * Provides operations for managing mappings between entities and their respective bases,
 * such as forms and locations. This service enables saving, retrieving, checking,
 * and deleting mappings.
 */
public interface CustomEntityBasisMapService {
    /**
     * Saves or updates an entity basis map
     *
     * @param map the entity basis map to save
     */
    void save(EntityBasisMap map);

    /**
     * Gets mappings for a specific form and location
     *
     * @param formId the form ID
     * @param locationId the location ID
     * @return list of EntityBasisMap objects for the form-location pair
     */
    List<EntityBasisMap> getMapsForFormAndLocation(Integer formId, Integer locationId);

    /**
     * Gets all forms mapped to a specific location
     *
     * @param locationId the location ID
     * @return list of EntityBasisMap objects for the location
     */
    List<EntityBasisMap> getAllFormsForLocation(Integer locationId);

    /**
     * Checks if a form is mapped to a location
     *
     * @param formId the form ID
     * @param locationId the location ID
     * @return true if mapping exists, false otherwise
     */
    boolean hasMapping(Integer formId, Integer locationId);

    /**
     * Deletes a mapping between form and location
     *
     * @param map the entity basis map to delete
     */
    void deleteMapping(EntityBasisMap map);

    /**
     * Deletes mappings between form and location
     *
     * @param formId the form ID
     * @param locationId the location ID
     */
    void deleteMapping(Integer formId, Integer locationId);
}