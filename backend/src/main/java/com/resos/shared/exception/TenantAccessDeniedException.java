package com.resos.shared.exception;

public class TenantAccessDeniedException extends BusinessException {

    public TenantAccessDeniedException(String message) {
        super("TENANT_ACCESS_DENIED", message);
    }
}
