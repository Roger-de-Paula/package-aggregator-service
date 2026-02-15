package com.example.packageaggregator.exception;

import java.util.UUID;

public class PackageNotFoundException extends RuntimeException {

    public PackageNotFoundException(UUID id) {
        super("Package not found: " + id);
    }
}
