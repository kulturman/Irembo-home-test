package com.kulturman.irembotest.domain.ports;

import java.util.UUID;

public interface TenancyProvider {
    UUID getCurrentTenantId();
}
