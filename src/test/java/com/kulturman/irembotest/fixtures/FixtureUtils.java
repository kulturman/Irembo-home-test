package com.kulturman.irembotest.fixtures;

import java.util.UUID;

/**
 * Utility class containing test fixture UUIDs that correspond to data in DBUnit XML files.
 */
public final class FixtureUtils {

    private FixtureUtils() {
        // Utility class - prevent instantiation
    }

    // Fixture UUIDs from users.xml
    public static final UUID USER_A_ID = UUID.fromString("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d");
    public static final UUID TENANT_A_ID = UUID.fromString("f1e2d3c4-b5a6-4d5e-9f0a-1b2c3d4e5f6a");
    public static final UUID USER_B_ID = UUID.fromString("b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e");

    // Fixture UUIDs from certificate-test-data.xml
    public static final UUID TEMPLATE_A_ID = UUID.fromString("c3d4e5f6-a7b8-4c7d-0e1f-2a3b4c5d6e7f");
}
