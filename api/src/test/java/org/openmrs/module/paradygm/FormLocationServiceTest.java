package org.openmrs.module.paradygm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.module.datafilter.impl.EntityBasisMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FormLocationServiceTest {

    @Mock
    private CustomEntityBasisMapService customEntityBasisMapService;

    @Mock
    private LocationService locationService;

    @Mock
    private UserService userService;

    private FormLocationService formLocationService;
    private User mockUser;

    @Before
    public void setUp() throws Exception {
        // Create an instance manually since we're setting private fields
        formLocationService = new FormLocationService();

        // Manually inject mocks
        setPrivateField("customEntityBasisMapService", customEntityBasisMapService);
        setPrivateField("locationService", locationService);
        setPrivateField("userService", userService);

        // Create a mock user
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("doctor1");

        // Inject the mock user into the service
        setPrivateField("authenticatedUser", mockUser);
    }

    // Helper method to set private fields via reflection
    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = FormLocationService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(formLocationService, value);
    }

    @Test
    public void shouldSuccessfullyBindFormToLocation() {
        Form form = new Form();
        form.setId(1);
        form.setName("Test Form");

        Location clinic = new Location();
        clinic.setId(1);
        clinic.setName("Clinic A");

        when(locationService.getDefaultLocation()).thenReturn(clinic);
        when(customEntityBasisMapService.getMapsForFormAndLocation(1, 1))
                .thenReturn(Collections.emptyList());

        formLocationService.bindFormToCurrentLocation(form);

        verify(customEntityBasisMapService).save(argThat(map ->
                "org.openmrs.Form".equals(map.getEntityType()) &&
                        "1".equals(map.getEntityIdentifier()) &&
                        "org.openmrs.Location".equals(map.getBasisType()) &&
                        "1".equals(map.getBasisIdentifier()) &&
                        mockUser.equals(map.getCreator()) &&
                        map.getDateCreated() != null
        ));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenUserHasNoLocation() {
        Form form = new Form();
        form.setId(1);

        when(locationService.getDefaultLocation()).thenReturn(null);

        formLocationService.bindFormToCurrentLocation(form);
    }

    @Test
    public void shouldNotCreateDuplicateBindings() {
        Form form = new Form();
        form.setId(1);

        Location clinic = new Location();
        clinic.setId(1);

        EntityBasisMap existingMap = new EntityBasisMap();
        existingMap.setEntityIdentifier("1");
        existingMap.setBasisIdentifier("1");

        ArrayList<EntityBasisMap> existingMaps = new ArrayList<>();
        existingMaps.add(existingMap);

        when(locationService.getDefaultLocation()).thenReturn(clinic);
        when(customEntityBasisMapService.getMapsForFormAndLocation(1, 1)).thenReturn(existingMaps);

        formLocationService.bindFormToCurrentLocation(form);

        verify(customEntityBasisMapService, never()).save(any());
    }

    @Test
    public void shouldHandleMultipleFormsForSameLocation() {
        Form covidForm = new Form();
        covidForm.setId(1);

        Form hivForm = new Form();
        hivForm.setId(2);

        Location clinic = new Location();
        clinic.setId(1);

        when(locationService.getDefaultLocation()).thenReturn(clinic);
        when(customEntityBasisMapService.getMapsForFormAndLocation(anyInt(), eq(1)))
                .thenReturn(Collections.emptyList());

        formLocationService.bindFormToCurrentLocation(covidForm);
        formLocationService.bindFormToCurrentLocation(hivForm);

        verify(customEntityBasisMapService, times(2)).save(any(EntityBasisMap.class));
    }
}