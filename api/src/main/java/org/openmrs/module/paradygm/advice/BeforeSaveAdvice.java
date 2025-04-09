package org.openmrs.module.paradygm.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.module.paradygm.IdentifierEnhancementFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * The BeforeSaveAdvice class provides advice for methods that interact with
 * patient objects before and after their persistence. It implements both the
 * `MethodBeforeAdvice` and `AfterReturningAdvice` interfaces.
 *
 * This class is primarily used to handle the following responsibilities:
 * - Enhance the identifier of patients during the "savePatient" operation before the patient object is persisted.
 * - Log relevant information once the patient object has been successfully saved.
 * - Reset and update the identifier sequence if required when the year changes.
 * - Manage thread-local state to ensure proper alignment of patient data during execution.
 *
 * Core functionalities include:
 * - Before advice (via `before` method): Enhances identifiers for patients in the "savePatient" operation
 *   if the patient does not already have an ID.
 * - After returning advice (via `afterReturning` method): Handles post-persistence logic such as logging
 *   successful patient creation and resetting identifier sequences when necessary.
 *
 * This class utilizes the `IdentifierEnhancementFactory` to perform identifier customization
 * and sequence management.
 */
public class BeforeSaveAdvice implements MethodBeforeAdvice, AfterReturningAdvice {

    private static final String METHOD_TO_INTERCEPT = "savePatient";
    private static IdentifierEnhancementFactory identifierEnhancementFactory = new IdentifierEnhancementFactory();
    private ThreadLocal<Patient> patientThreadLocal = new ThreadLocal<>();
    private Log log = LogFactory.getLog(getClass());

    public void before(Method method, Object[] objects, Object o) {
        if (method.getName().equalsIgnoreCase(METHOD_TO_INTERCEPT)) {
            Patient patient = (Patient) objects[0];
            if (patient.getPatientId() == null) {
                identifierEnhancementFactory.enhanceIdentifier(patient);
                patientThreadLocal.set(patient);
            }
        }
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        if (method.getName().equalsIgnoreCase(METHOD_TO_INTERCEPT) && args[0] instanceof Patient) {
            log.warn("Patient Created succesfully." );

            Patient patient = patientThreadLocal.get();
            if (patient != null && identifierEnhancementFactory.hasIsIdentiferSequenceReset()) {
                log.warn("Attempting to save identifier Sequence after patient Creation." );
                patientThreadLocal.remove();
                identifierEnhancementFactory.saveNewIdentifierSequenceValue();;
            }
        }
    }
}
