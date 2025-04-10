package org.openmrs.module.paradygm.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.paradygm.FormLocationService;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * Intercepts the execution of the target method to perform operations before the method execution.
 *
 * This implementation specifically intercepts calls to the "saveForm" method when a {@code Form} object
 * is passed as an argument. If the form is newly created (without an assigned ID), the advice logs the
 * event and exits without performing any additional actions. For existing forms, it attempts to bind
 * the form to the current location using the {@code FormLocationService}.
 *
 * If the {@code FormLocationService} is not available or if an error occurs during the binding process,
 * appropriate messages are logged, but the advice does not disrupt the execution of the intercepted
 * method.
 *
 * This class is intended to be used where automatic form-to-location binding is required as part of
 * the form-save workflow.
 *
 * Implements {@link MethodBeforeAdvice} from the Spring AOP framework.
 */

public class BeforeSaveFormAdvice implements MethodBeforeAdvice {

    private static final Log log = LogFactory.getLog(BeforeSaveFormAdvice.class);

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        // Only intercept saveForm method
        if ("saveForm".equals(method.getName()) && args.length > 0 && args[0] instanceof Form) {
            Form form = (Form) args[0];

            // Wait until after the save to bind if this is a new form (needs an ID)
            if (form.getId() == null) {
                log.debug("New form detected, will bind after save");
                return;
            }

            try {
                // Get the form location service
                FormLocationService formLocationService = Context.getService(FormLocationService.class);

                // Bind the form to the current location
                if (formLocationService != null) {
                    log.debug("Automatically binding form " + form.getName() + " to current location");
                    formLocationService.bindFormToCurrentLocation(form);
                } else {
                    log.warn("FormLocationService not found, form will not be bound to location");
                }
            } catch (Exception e) {
                // Log error but don't prevent form from being saved
                log.error("Failed to bind form to location", e);
            }
        }
    }
}
