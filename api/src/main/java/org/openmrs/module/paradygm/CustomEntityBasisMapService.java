package org.openmrs.module.paradygm;

import org.openmrs.module.datafilter.impl.EntityBasisMap;

import java.util.List;

public interface CustomEntityBasisMapService {
    void save(EntityBasisMap map);
    List<EntityBasisMap> getMapsForFormAndLocation(Integer formId, Integer locationId);
}
