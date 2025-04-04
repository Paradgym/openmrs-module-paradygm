package org.openmrs.module.paradygm;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;

import java.time.Year;
import java.util.HashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class IdentifierEnhancementFactoryTest {

    private static final String TEST_PARADYGM_PATIENT_IDENTIFIER_PREFIX = "PD200-";
    private static final String TEST_PARADYGM_IDENTIFIER_SOURCE_UUID = "8549f706-7e85-4c1d-9424-217d50a2988b";

    @Mock
    private IdentifierSourceService identifierSourceService;

    private IdentifierEnhancementFactory identifierEnhancementFactory;

    @Before
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Create factory and inject mock service directly
        identifierEnhancementFactory = new IdentifierEnhancementFactory();
        identifierEnhancementFactory.setIdentifierSourceService(identifierSourceService);
    }

    @Test
    public void shouldAddParadygmIDFormatToPatientIdentifier() {
        Patient patient = setUpPatientData();
        SequentialIdentifierGenerator sequentialIdentifierGenerator = setUpIdentifierSource();
        when(identifierSourceService.getIdentifierSourceByUuid(TEST_PARADYGM_IDENTIFIER_SOURCE_UUID)).thenReturn(sequentialIdentifierGenerator);

        identifierEnhancementFactory.enhanceIdentifier(patient);
        // for year 2025, id will be PD200-25-000-001
        assertEquals("PD200-" + getCurrentYear() + "-000-001", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void shouldResetParadygmIDSequenceOnNewYear() {
        Patient patient = setUpPatientData();
        patient.getPatientIdentifier().setIdentifier("PD200-999");

        int followingYear = getCurrentYear() + 1;

        // Use the new method to set the testing year
        identifierEnhancementFactory.setLastRecordedYearForTesting(followingYear);

        SequentialIdentifierGenerator sequentialIdentifierGenerator = setUpIdentifierSource();
        when(identifierSourceService.getIdentifierSourceByUuid(TEST_PARADYGM_IDENTIFIER_SOURCE_UUID)).thenReturn(sequentialIdentifierGenerator);

        identifierEnhancementFactory.enhanceIdentifier(patient);
        // for year 2025, id will be PDG200-25-000-001
        assertEquals("PD200-" + getCurrentYear() + "-000-001", patient.getPatientIdentifier().getIdentifier());
    }

    private Patient setUpPatientData() {
        Patient patient = new Patient();
        patient.setGender("M");
        PatientIdentifier patientIdentifier =
                new PatientIdentifier("PD200-1", new PatientIdentifierType(), new Location());
        HashSet<PatientIdentifier> patientIdentifiers = new HashSet<>();
        patientIdentifiers.add(patientIdentifier);
        patient.setIdentifiers(patientIdentifiers);
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("personAttribute");
        PersonAttribute personAttribute = new PersonAttribute(personAttributeType, "100");
        HashSet<PersonAttribute> personAttributes = new HashSet<>();
        personAttributes.add(personAttribute);
        patient.setAttributes(personAttributes);
        return patient;
    }

    private SequentialIdentifierGenerator setUpIdentifierSource() {
        SequentialIdentifierGenerator sequentialIdentifierGenerator = new SequentialIdentifierGenerator();
        sequentialIdentifierGenerator.setPrefix(TEST_PARADYGM_PATIENT_IDENTIFIER_PREFIX);
        return sequentialIdentifierGenerator;
    }

    private int getCurrentYear() {
        return Year.now().getValue() % 100;
    }
}