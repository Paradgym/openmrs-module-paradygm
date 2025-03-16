package org.openmrs.module.paradygm;

import java.time.Year;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.idgen.service.IdentifierSourceService;

public class IdentifierEnhancementFactory {

    public static final String PARADYGM_IDENTIFIER_SOURCE_UUID = "8549f706-7e85-4c1d-9424-217d50a2988b";
    public static final int RESET_IDENTIFIER_SEQUENCE_VALUE = 1;
    private static int lastRecordedYear = Year.now().getValue() % 100;
    protected Log log = LogFactory.getLog(getClass());
    private Boolean isIdentiferSequenceReset;

    // Add this field to allow dependency injection in tests
    private IdentifierSourceService identifierSourceService;

    public void enhanceIdentifier(Patient patient) {
        System.out.println("DEBUG: Starting ID enhancement for patient: " + patient.getPatientId());

        // Check if patient has an identifier
        if (patient.getPatientIdentifier() == null) {
            System.out.println("DEBUG: ERROR - Patient has no identifier");
            log.error("Patient has no identifier to enhance");
            return;
        }

        // Use the injected service if available, otherwise get from Context
        IdentifierSourceService service = getIdentifierSourceService();
        System.out.println("DEBUG: Got IdentifierSourceService: " + (service != null ? "Yes" : "No"));

        SequentialIdentifierGenerator paradygmIdentifierSource = null;
        try {
            paradygmIdentifierSource = (SequentialIdentifierGenerator)
                    service.getIdentifierSourceByUuid(PARADYGM_IDENTIFIER_SOURCE_UUID);
            System.out.println("DEBUG: Got identifier source: " + (paradygmIdentifierSource != null ? "Yes" : "No"));
        } catch (Exception e) {
            System.out.println("DEBUG: Exception getting identifier source: " + e.getMessage());
            e.printStackTrace();
        }

        if (paradygmIdentifierSource == null) {
            System.out.println("DEBUG: Identifier Source not found with UUID: " + PARADYGM_IDENTIFIER_SOURCE_UUID);
            log.error("Identifier Source with uuid " + PARADYGM_IDENTIFIER_SOURCE_UUID + " is not found hence skipping Paradygm ID generation");
            return;
        }

        String prefix = getPrefix(paradygmIdentifierSource);
        System.out.println("DEBUG: Using prefix: '" + prefix + "'");

        PatientIdentifier identifier = patient.getPatientIdentifier();
        System.out.println("DEBUG: Original identifier: '" + identifier.getIdentifier() + "'");

        String bashId = StringUtils.substringAfter(identifier.getIdentifier(), prefix);
        System.out.println("DEBUG: Extracted bashId: '" + bashId + "'");

        int translatedBashId;
        try {
            translatedBashId = Integer.valueOf(bashId);
            System.out.println("DEBUG: Parsed translatedBashId: " + translatedBashId);
        }
        catch (Exception e) {
            System.out.println("DEBUG: Failed to parse bashId: " + e.getMessage());
            throw new IllegalArgumentException("Invalid Paradygm ID: " + bashId, e);
        }

        int currentYearPrefix = Year.now().getValue() % 100;
        System.out.println("DEBUG: Current year: " + currentYearPrefix + ", Last recorded year: " + lastRecordedYear);

        if (lastRecordedYear != currentYearPrefix) {
            shouldIdentiferSequenceReset(true);
            System.out.println("DEBUG: Year changed - resetting sequence");
            log.warn("Resetting identifier Sequence since years have changed. Last recorded year is: "+ lastRecordedYear + " and Current year is: "+currentYearPrefix);
            translatedBashId = RESET_IDENTIFIER_SEQUENCE_VALUE;
            lastRecordedYear = currentYearPrefix;
        } else {
            shouldIdentiferSequenceReset(false);
            System.out.println("DEBUG: Same year - no reset needed");
        }

        translatedBashId = translatedBashId + (currentYearPrefix * 1000000);
        System.out.println("DEBUG: Final translatedBashId: " + translatedBashId);

        bashId = String.valueOf(translatedBashId);
        System.out.println("DEBUG: Final bashId string: " + bashId);

        // regex below inserts a hyphen after every group of three digits in the Identifier.
        String regex = "(\\d)(?=(\\d{3})+$)";
        StringBuilder enhancedId = new StringBuilder();
        enhancedId.append(prefix)
                .append(bashId.replaceAll(regex, "$1-"));

        String finalId = enhancedId.toString();
        System.out.println("DEBUG: Setting enhanced ID: '" + finalId + "'");

        identifier.setIdentifier(finalId);
        System.out.println("DEBUG: ID enhancement complete");
    }

    private String getPrefix(SequentialIdentifierGenerator IdentifierSourceGenerator) {
        String prefix = IdentifierSourceGenerator.getPrefix();
        System.out.println("DEBUG: Raw prefix from generator: '" + prefix + "'");
        return prefix != null ? prefix : "";
    }

    private void shouldIdentiferSequenceReset(boolean isIdentiferSequenceResetValue) {
        this.isIdentiferSequenceReset = isIdentiferSequenceResetValue;
        System.out.println("DEBUG: Set isIdentifierSequenceReset to: " + isIdentiferSequenceResetValue);
    }

    public boolean hasIsIdentiferSequenceReset() {
        return this.isIdentiferSequenceReset;
    }

    public void saveNewIdentifierSequenceValue() {
        System.out.println("DEBUG: saveNewIdentifierSequenceValue called, reset needed: " + hasIsIdentiferSequenceReset());
        if(hasIsIdentiferSequenceReset()) {
            IdentifierSourceService service = getIdentifierSourceService();
            System.out.println("DEBUG: Got service for saving sequence: " + (service != null ? "Yes" : "No"));

            try {
                SequentialIdentifierGenerator paradygmIdentifierSource = (SequentialIdentifierGenerator)
                        service.getIdentifierSourceByUuid(PARADYGM_IDENTIFIER_SOURCE_UUID);
                System.out.println("DEBUG: Got identifier source for saving: " + (paradygmIdentifierSource != null ? "Yes" : "No"));

                if (paradygmIdentifierSource != null) {
                    service.saveSequenceValue(paradygmIdentifierSource, RESET_IDENTIFIER_SEQUENCE_VALUE + 1);
                    System.out.println("DEBUG: Successfully saved new sequence value: " + (RESET_IDENTIFIER_SEQUENCE_VALUE + 1));
                    log.warn("identifier Sequence Successfully Reset");
                } else {
                    System.out.println("DEBUG: Failed to save sequence - source not found");
                }
            } catch (Exception e) {
                System.out.println("DEBUG: Exception while saving sequence: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Method to get the service - enables dependency injection for testing
    private IdentifierSourceService getIdentifierSourceService() {
        if (identifierSourceService != null) {
            System.out.println("DEBUG: Using injected identifierSourceService");
            return identifierSourceService;
        }
        System.out.println("DEBUG: Getting identifierSourceService from Context");
        try {
            IdentifierSourceService service = Context.getService(IdentifierSourceService.class);
            System.out.println("DEBUG: Service from Context: " + (service != null ? "Success" : "Null"));
            return service;
        } catch (Exception e) {
            System.out.println("DEBUG: Exception getting service from Context: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Setter for dependency injection in tests
    public void setIdentifierSourceService(IdentifierSourceService identifierSourceService) {
        System.out.println("DEBUG: Setting injected identifierSourceService: " + (identifierSourceService != null ? "Not null" : "Null"));
        this.identifierSourceService = identifierSourceService;
    }

    // Method for testing to set the last recorded year
    public void setLastRecordedYearForTesting(int year) {
        System.out.println("DEBUG: Setting lastRecordedYear from " + lastRecordedYear + " to " + year);
        lastRecordedYear = year;
    }
}