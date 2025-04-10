package org.openmrs.module.paradygm.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.paradygm.FormLocationService;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * Advice for automatically binding newly created forms to location after they receive an ID
 */
public class AfterSaveFormAdvice implements AfterReturningAdvice {

    private static final Log log = LogFactory.getLog(AfterSaveFormAdvice.class);

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        // Only intercept saveForm method
        if ("saveForm".equals(method.getName()) && args.length > 0 && args[0] instanceof Form) {
            Form form = (Form) args[0];
            Form savedForm = (Form) returnValue;

            // Only process new forms that now have an ID
            if (form.getId() == null && savedForm != null && savedForm.getId() != null) {
                try {
                    // Get the form location service
                    FormLocationService formLocationService = Context.getService(FormLocationService.class);

                    // Bind the form to the current location
                    if (formLocationService != null) {
                        log.debug("Automatically binding new form " + savedForm.getName() + " to current location");
                        formLocationService.bindFormToCurrentLocation(savedForm);
                    } else {
                        log.warn("FormLocationService not found, form will not be bound to location");
                    }
                } catch (Exception e) {
                    // Log error but don't affect anything else
                    log.error("Failed to bind new form to location", e);
                }
            }
        }
    }
}
