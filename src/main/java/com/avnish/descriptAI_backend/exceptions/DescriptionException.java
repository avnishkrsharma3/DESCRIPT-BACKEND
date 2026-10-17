package com.avnish.descriptAI_backend.exceptions;

/**
 * @author Avnish
 * @date 11-10-2026
 */

public final class DescriptionException {

    private DescriptionException() { }

    /** Asked for a description that does not exist -> 404 */
    public static class NotFound extends RuntimeException {
        public NotFound(String message) { super(message); }
    }

    /** Action not allowed in the current status (e.g. approving a REJECTED one) -> 409 */
    public static class InvalidState extends RuntimeException {
        public InvalidState(String message) { super(message); }
    }
}