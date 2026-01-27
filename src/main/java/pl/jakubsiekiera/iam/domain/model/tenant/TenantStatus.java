package pl.jakubsiekiera.iam.domain.model.tenant;

public enum TenantStatus {
    PENDING,    // Created, awaiting activation details
    ACTIVE,     // Fully operational
    SUSPENDED   // Temporarily disabled
}