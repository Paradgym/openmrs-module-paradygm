package org.openmrs.module.paradygm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ParadygmMetadataConstants {
    /**
     * Users Names
     */
    public static final String REGISTRATION_OFFICER_USERNAME = "REG_officer";
    public static final String NURSE_USERNAME = "NURSE";
    public static final String DOCTOR_USERNAME = "DOCTOR";
    public static final String CLINICIAN_USERNAME = "CLINICIAN";
    public static final String ACCOUNTANT_USERNAME = "ACCOUNTANT";
    public static final String MANAGEMENT_USERNAME = "MG_Staff";
    /**
     * Roles Names
     */
    public static final String REGISTRATION_OFFICER_ROLE_NAME = "Registration Officer";
    public static final String NURSE_ROLE_NAME = "Nurse";
    public static final String DOCTOR_ROLE_NAME = "Doctor";
    public static final String CLINICIAN_ROLE_NAME = "Clinician";
    public static final String ACCOUNTANT_ROLE_NAME = "Accountant";
    public static final String MANAGEMENT_ROLE_NAME = "Manager";

    /**
     * Usernames and their corresponding role names
     */
    public static final Map<String, String> USER_ROLES = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put(REGISTRATION_OFFICER_USERNAME, REGISTRATION_OFFICER_ROLE_NAME);
        put(NURSE_USERNAME, NURSE_ROLE_NAME);
        put(DOCTOR_USERNAME, DOCTOR_ROLE_NAME);
        put(CLINICIAN_USERNAME, CLINICIAN_ROLE_NAME);
        put(ACCOUNTANT_USERNAME, ACCOUNTANT_ROLE_NAME);
        put(MANAGEMENT_USERNAME, MANAGEMENT_ROLE_NAME);
    }});
}