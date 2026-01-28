package com.example.smarttemplatefiller.license;

/**
 * Represents the outcome of license validation.
 * Uses factory methods for consistent creation and encapsulates error details.
 */
public class ValidationResult {

    /**
     * Error codes for license validation failures.
     */
    public enum ErrorCode {
        /** Validation passed successfully */
        SUCCESS,
        /** License file not found at expected path */
        FILE_NOT_FOUND,
        /** JSON parsing failed or malformed structure */
        INVALID_FORMAT,
        /** HMAC signature verification failed */
        SIGNATURE_MISMATCH,
        /** AES decryption failed */
        DECRYPTION_FAILED,
        /** DeviceId doesn't match current hardware */
        HARDWARE_MISMATCH,
        /** Current date is past expiry timestamp */
        LICENSE_EXPIRED,
        /** Schema version not recognized */
        UNSUPPORTED_VERSION
    }

    private final boolean valid;
    private final ErrorCode errorCode;
    private final String errorMessage;

    /**
     * Private constructor - use factory methods instead.
     */
    private ValidationResult(boolean valid, ErrorCode errorCode, String errorMessage) {
        this.valid = valid;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a successful validation result.
     *
     * @return ValidationResult with valid=true and SUCCESS error code
     */
    public static ValidationResult success() {
        return new ValidationResult(true, ErrorCode.SUCCESS, null);
    }

    /**
     * Creates a failed validation result with specified error details.
     *
     * @param errorCode    The specific reason for failure
     * @param errorMessage Human-readable error description
     * @return ValidationResult with valid=false and specified error details
     */
    public static ValidationResult failure(ErrorCode errorCode, String errorMessage) {
        return new ValidationResult(false, errorCode, errorMessage);
    }

    /**
     * Creates a failed validation result with error code only.
     * Uses a default message based on the error code.
     *
     * @param errorCode The specific reason for failure
     * @return ValidationResult with valid=false and default error message
     */
    public static ValidationResult failure(ErrorCode errorCode) {
        String defaultMessage = getDefaultMessage(errorCode);
        return new ValidationResult(false, errorCode, defaultMessage);
    }

    /**
     * Gets a default error message for each error code.
     */
    private static String getDefaultMessage(ErrorCode code) {
        return switch (code) {
            case SUCCESS -> "Validation successful";
            case FILE_NOT_FOUND -> "License file not found";
            case INVALID_FORMAT -> "Invalid license file format";
            case SIGNATURE_MISMATCH -> "License signature verification failed";
            case DECRYPTION_FAILED -> "Failed to decrypt license data";
            case HARDWARE_MISMATCH -> "License is not valid for this hardware";
            case LICENSE_EXPIRED -> "License has expired";
            case UNSUPPORTED_VERSION -> "Unsupported license version";
        };
    }

    /**
     * Checks if the validation passed.
     *
     * @return true if license is valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the error code.
     *
     * @return ErrorCode indicating success or specific failure reason
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the human-readable error message.
     *
     * @return Error message or null for successful validation
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{valid=true}";
        }
        return "ValidationResult{valid=false, errorCode=" + errorCode + ", message='" + errorMessage + "'}";
    }
}
