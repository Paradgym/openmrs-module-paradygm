package org.openmrs.module.paradygm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.idgen.SequentialIdentifierGenerator;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Year;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore({"javax.management.*", "jdk.internal.reflect.*"})
public class ParadygmIdentifierEnhancementFactoryTest {

    private ParadygmIdentifierEnhancementFactory identifierEnhancementFactory;

    private static final String TEST_PARADYGM_PATIENT_IDENTIFIER_PREFIX = "PDG-";
    private static final String TEST_PARADYGM_IDENTIFIER_SOURCE_UUID = "81be3f03-5962-4b90-85e0-ddfd963a4cce";

    @Mock
    private IdentifierSourceService identifierSourceService;

    @Before
    public void setUp() {
        identifierEnhancementFactory = new ParadygmIdentifierEnhancementFactory();
        PowerMockito.mockStatic(Context.class);
        when(Context.getService(IdentifierSourceService.class)).thenReturn(identifierSourceService);
    }

    @Test
    public void shouldAddParadygmIDFormatToPatientIdentifier() {
        Patient patient = setUpPatientData();
        SequentialIdentifierGenerator sequentialIdentifierGenerator = setUpIdentifierSource();
        when(identifierSourceService.getIdentifierSourceByUuid(TEST_PARADYGM_IDENTIFIER_SOURCE_UUID)).thenReturn(sequentialIdentifierGenerator);

        identifierEnhancementFactory.enhanceIdentifier(patient);
        // For current year (e.g., 2025), ID will be PDG-25-000-001
        assertEquals("PDG-" + getCurrentYear() + "-000-001", patient.getPatientIdentifier().getIdentifier());
    }

    @Test
    public void shouldResetParadygmIDSequenceOnNewYear() {
        Patient patient = setUpPatientData();
        patient.getPatientIdentifier().setIdentifier("PDG-999");

        int followingYear = getCurrentYear() + 1;
        setLastRecordedYear(followingYear);

        SequentialIdentifierGenerator sequentialIdentifierGenerator = setUpIdentifierSource();
        when(identifierSourceService.getIdentifierSourceByUuid(TEST_PARADYGM_IDENTIFIER_SOURCE_UUID)).thenReturn(sequentialIdentifierGenerator);

        identifierEnhancementFactory.enhanceIdentifier(patient);
        // For current year (e.g., 2025), ID will be PDG-25-000-001
        assertEquals("PDG-" + getCurrentYear() + "-000-001", patient.getPatientIdentifier().getIdentifier());
    }

    private Patient setUpPatientData() {
        Patient patient = new Patient();
        patient.setGender("M");
        PatientIdentifier patientIdentifier =
                new PatientIdentifier("PDG-1", new PatientIdentifierType(), new Location());
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

    private void setLastRecordedYear(int year) {
        try {
            Field field = ParadygmIdentifierEnhancementFactory.class.getDeclaredField("lastRecordedYear");
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers())) {
                field.set(null, year);
            } else {
                throw new IllegalAccessException("Field is not static");
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentYear() {
        return Year.now().getValue() % 100;
    }
}