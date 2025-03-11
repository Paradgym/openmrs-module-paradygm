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

    public void enhanceIdentifier(Patient patient) {
        IdentifierSourceService identifierSourceService = Context.getService(IdentifierSourceService.class);
        SequentialIdentifierGenerator paradygmIdentifierSource = (SequentialIdentifierGenerator) identifierSourceService.getIdentifierSourceByUuid(PARADYGM_IDENTIFIER_SOURCE_UUID);

        if (paradygmIdentifierSource == null) {
            log.error("Identifier Source with uuid " + PARADYGM_IDENTIFIER_SOURCE_UUID + " is not found hence skipping Paradygm ID generation");
            return;
        }

        String prefix = getPrefix(paradygmIdentifierSource);
        PatientIdentifier identifier = patient.getPatientIdentifier();
        String bashId = StringUtils.substringAfter(identifier.getIdentifier(), prefix);

        int translatedBashId;
        try {
            translatedBashId = Integer.valueOf(bashId);
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Invalid Paradygm ID: " + bashId, e);
        }

        int currentYearPrefix = Year.now().getValue() % 100;
        if (lastRecordedYear != currentYearPrefix) {
            shouldIdentiferSequenceReset(true);
            log.warn("Resetting identifier Sequence since years have changed.  Last recorded year is: "+ lastRecordedYear + " and Current year is: "+currentYearPrefix );
            translatedBashId = RESET_IDENTIFIER_SEQUENCE_VALUE;
            lastRecordedYear = currentYearPrefix;
        }else {
            shouldIdentiferSequenceReset(false);
        }
        translatedBashId = translatedBashId +  (currentYearPrefix * 1000000);
        bashId = String.valueOf(translatedBashId);

        // regex below inserts a hyphen after every group of three digits in the Identifier.
        String regex = "(\\d)(?=(\\d{3})+$)";
        StringBuilder enhancedId = new StringBuilder();
        enhancedId.append(prefix)
                .append(bashId.replaceAll(regex, "$1-"));
        identifier.setIdentifier(enhancedId.toString());
    }

    private String getPrefix(SequentialIdentifierGenerator IdentifierSourceGenerator) {
        String prefix = IdentifierSourceGenerator.getPrefix();
        return prefix != null ? prefix : "";
    }

    private void shouldIdentiferSequenceReset(boolean isIdentiferSequenceResetValue) {
        this.isIdentiferSequenceReset = isIdentiferSequenceResetValue;
    }

    public boolean hasIsIdentiferSequenceReset() {
        return this.isIdentiferSequenceReset;
    }

    public void saveNewIdentifierSequenceValue() {
        if(hasIsIdentiferSequenceReset()) {
            IdentifierSourceService identifierSourceService = Context.getService(IdentifierSourceService.class);
            SequentialIdentifierGenerator paradygmIdentifierSource = (SequentialIdentifierGenerator) identifierSourceService.getIdentifierSourceByUuid(PARADYGM_IDENTIFIER_SOURCE_UUID);
            identifierSourceService.saveSequenceValue(paradygmIdentifierSource, RESET_IDENTIFIER_SEQUENCE_VALUE + 1);
            log.warn("identifier Sequence Succesfuly Reset" );
        }
    }
}