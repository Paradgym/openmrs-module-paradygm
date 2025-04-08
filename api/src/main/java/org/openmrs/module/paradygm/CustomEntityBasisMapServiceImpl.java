
package org.openmrs.module.paradygm;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.datafilter.impl.EntityBasisMap;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CustomEntityBasisMapServiceImpl extends BaseOpenmrsService implements CustomEntityBasisMapService {

    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public void save(EntityBasisMap map) {
        sessionFactory.getCurrentSession().saveOrUpdate(map);
    }

    @Transactional
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
}