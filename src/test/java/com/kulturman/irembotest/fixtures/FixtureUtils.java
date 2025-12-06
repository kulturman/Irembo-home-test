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
    public static final UUID ADMIN_USER_ID = UUID.fromString("1a2b3c4d-5e6f-4a1b-2c3d-4e5f6a7b8c9d");
    public static final UUID USER_A_ID = UUID.fromString("a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d");
    public static final UUID TENANT_A_ID = UUID.fromString("f1e2d3c4-b5a6-4d5e-9f0a-1b2c3d4e5f6a");
    public static final UUID USER_B_ID = UUID.fromString("b2c3d4e5-f6a7-4b6c-9d0e-1f2a3b4c5d6e");
    public static final UUID TENANT_B_ID = UUID.fromString("e2d3c4b5-a6f7-4e5d-0f9a-2b3c4d5e6f7a");
    public static final UUID REGULAR_USER_ID = UUID.fromString("2b3c4d5e-6f7a-4b2c-3d4e-5f6a7b8c9d0e");

    // Fixture UUIDs from certificate-test-data.xml
    public static final UUID TEMPLATE_A_ID = UUID.fromString("c3d4e5f6-a7b8-4c7d-0e1f-2a3b4c5d6e7f");
    public static final UUID TEMPLATE_A1_ID = UUID.fromString("d4e5f6a7-b8c9-4d8e-1f0a-3b4c5d6e7f8a");
    public static final UUID TEMPLATE_A2_ID = UUID.fromString("e5f6a7b8-c9d0-4e9f-2a1b-4c5d6e7f8a9b");
    public static final UUID TEMPLATE_B1_ID = UUID.fromString("f6a7b8c9-d0e1-4f0a-3b2c-5d6e7f8a9b0c");
    public static final UUID TEMPLATE_CERTIFICATE_GENERATION_ID = UUID.fromString("3c4d5e6f-7a8b-4c9d-0e1f-2a3b4c5d6e7f");
    public static final UUID TEMPLATE_PUBLIC_DOWNLOAD_ID = UUID.fromString("4d5e6f7a-8b9c-4d0e-1f2a-3b4c5d6e7f8a");
}
