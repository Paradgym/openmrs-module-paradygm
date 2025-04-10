package org.openmrs.module.paradygm;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


/**
 * Implementation of the CustomEntityBasisMapService interface, providing operations
 * for managing mappings between entities (e.g., forms) and their corresponding bases
 * (e.g., locations). This service supports saving, retrieving, checking existence,
 * and deleting mappings via database interactions.
 */
@Service
public class CustomEntityBasisMapServiceImpl extends BaseOpenmrsService implements CustomEntityBasisMapService {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public void save(EntityBasisMap map) {
        sessionFactory.getCurrentSession().saveOrUpdate(map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityBasisMap> getMapsForFormAndLocation(Integer formId, Integer locationId) {
        return sessionFactory.getCurrentSession()
                .createQuery("from EntityBasisMap where entityType = 'org.openmrs.Form' " +
                        "and entityIdentifier = :formId " +
                        "and basisType = 'org.openmrs.Location' " +
                        "and basisIdentifier = :locationId", EntityBasisMap.class)
                .setParameter("formId", formId.toString())
                .setParameter("locationId", locationId.toString())
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityBasisMap> getAllFormsForLocation(Integer locationId) {
        return sessionFactory.getCurrentSession()
                .createQuery("from EntityBasisMap where entityType = 'org.openmrs.Form' " +
                        "and basisType = 'org.openmrs.Location' " +
                        "and basisIdentifier = :locationId", EntityBasisMap.class)
                .setParameter("locationId", locationId.toString())
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasMapping(Integer formId, Integer locationId) {
        return !getMapsForFormAndLocation(formId, locationId).isEmpty();
    }

    @Override
    @Transactional
    public void deleteMapping(EntityBasisMap map) {
        sessionFactory.getCurrentSession().delete(map);
    }

    @Override
    @Transactional
    public void deleteMapping(Integer formId, Integer locationId) {
        List<EntityBasisMap> maps = getMapsForFormAndLocation(formId, locationId);
        for (EntityBasisMap map : maps) {
            deleteMapping(map);
        }
    }
}