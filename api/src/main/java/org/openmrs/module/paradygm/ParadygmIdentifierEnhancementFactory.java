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

public class ParadygmIdentifierEnhancementFactory {

    public static final String PARADYGM_IDENTIFIER_SOURCE_UUID = "81be3f03-5962-4b90-85e0-ddfd963a4cce";
    public static final int RESET_IDENTIFIER_SEQUENCE_VALUE = 1;
    private static int lastRecordedYear = Year.now().getValue() % 100;
    protected Log log = LogFactory.getLog(getClass());
    private Boolean isIdentifierSequenceReset;

    public void enhanceIdentifier(Patient patient) {
        IdentifierSourceService identifierSourceService = Context.getService(IdentifierSourceService.class);
        SequentialIdentifierGenerator paradygmIdentifierSource = (SequentialIdentifierGenerator) identifierSourceService
                .getIdentifierSourceByUuid(PARADYGM_IDENTIFIER_SOURCE_UUID);

        if (paradygmIdentifierSource == null) {
            log.error("Identifier Source with uuid " + PARADYGM_IDENTIFIER_SOURCE_UUID +
                    " is not found hence skipping Paradygm ID generation");
            return;
        }

        String prefix = getPrefix(paradygmIdentifierSource);
        PatientIdentifier identifier = patient.getPatientIdentifier();
        String baseId = StringUtils.substringAfter(identifier.getIdentifier(), prefix);

        int translatedBaseId;
        try {
            translatedBaseId = Integer.parseInt(baseId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Paradygm ID: " + baseId, e);
        }

        int currentYearPrefix = Year.now().getValue() % 100;
        if (lastRecordedYear != currentYearPrefix) {
            shouldIdentifierSequenceReset(true);
            log.warn("Resetting identifier sequence since years have changed. Last recorded year is: " +
                    lastRecordedYear + " and Current year is: " + currentYearPrefix);
            translatedBaseId = RESET_IDENTIFIER_SEQUENCE_VALUE;
            lastRecordedYear = currentYearPrefix;
        } else {
            shouldIdentifierSequenceReset(false);
        }

        translatedBaseId = translatedBaseId + (currentYearPrefix * 1000000);
        baseId = String.valueOf(translatedBaseId);

        // Insert a hyphen after every group of three digits in the Identifier
        String regex = "(\\d)(?=(\\d{3})+$)";
        String enhancedId = prefix +
                baseId.replaceAll(regex, "$1-");

        identifier.setIdentifier(enhancedId);
    }

    private String getPrefix(SequentialIdentifierGenerator identifierSourceGenerator) {
        String prefix = identifierSourceGenerator.getPrefix();
        return prefix != null ? prefix : "";
    }

    private void shouldIdentifierSequenceReset(boolean isIdentifierSequenceResetValue) {
        this.isIdentifierSequenceReset = isIdentifierSequenceResetValue;
    }

    public boolean hasIdentifierSequenceReset() {
        return this.isIdentifierSequenceReset;
    }

    public void saveNewIdentifierSequenceValue() {
        if (hasIdentifierSequenceReset()) {
            IdentifierSourceService identifierSourceService = Context.getService(IdentifierSourceService.class);
            SequentialIdentifierGenerator paradygmIdentifierSource = (SequentialIdentifierGenerator) identifierSourceService
                    .getIdentifierSourceByUuid(PARADYGM_IDENTIFIER_SOURCE_UUID);

            identifierSourceService.saveSequenceValue(paradygmIdentifierSource, RESET_IDENTIFIER_SEQUENCE_VALUE + 1);
            log.warn("Identifier sequence successfully reset");
        }
    }
}