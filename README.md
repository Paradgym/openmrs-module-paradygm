# 🏥 OpenMRS Module - Paradygm EMR

This module includes customizations to the OpenMRS tailored for the Paradygm EMR project. It also includes generic features that support advanced workflows such as location-based form filtering, custom patient identifiers, and improved metadata management.

## 🚀 Features  ✅ Patient Identifier Generator

This feature replaces the default OpenMRS ID with a custom Paradygm EMR ID format used by Paradygm:

1. Format: PDG200-25-000-001

2. Automatically generated upon patient creation.

3. Includes hyphens (-) after every three digits for better readability.

4. Ensures uniqueness across all patients.

## 📍 Location-Based Form Filtering

A core feature of this module is the ability to assign forms to specific locations and filter them accordingly during form entry workflows. This enables multi-practice (multi-tenant) support, where users only see forms relevant to their clinic or group.
How It Works

1. Forms are bound to locations using the EntityBasisMap model.

2. At runtime, the user's default location is resolved using LocationService.

3. Only forms assigned to that location are shown during visit workflows.

4. Prevents cross-location access to encounter templates and ensures clean separation of data and workflows.


## 🔐 Location-Based Access Control with DataFilter Module

In addition to filtering forms:

The module integrates with the DataFilter Module to restrict user access to only the locations they're associated with.


