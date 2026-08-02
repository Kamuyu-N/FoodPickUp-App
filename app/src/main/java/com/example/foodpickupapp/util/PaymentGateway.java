package com.example.foodpickupapp.util;

/**
 * Simulated third-party payment gateway (e.g., Stripe, PayPal, or Mobile Money).
 *
 * This class encapsulates all payment processing logic behind a simple interface.
 * When a real payment provider SDK is integrated later, only this class needs
 * to be updated — no changes required in activities or DAOs.
 *
 * For testing purposes:
 * - Card number "4000000000000002" simulates a declined payment.
 * - All other valid 16-digit card numbers simulate a successful payment.
 *
 * Related to: FOOD-15 (integrate a payment API)
 */
public class PaymentGateway {

    /** Test card number that always results in a declined payment. */
    private static final String TEST_DECLINE_CARD = "4000000000000002";

    /** Simulated network delay in milliseconds. */
    private static final long SIMULATED_DELAY_MS = 1500;

    /**
     * Holds the result of a payment attempt.
     */
    public static class PaymentResult {
        private final boolean success;
        private final String transactionReference;
        private final String errorMessage;

        private PaymentResult(boolean success, String transactionReference, String errorMessage) {
            this.success = success;
            this.transactionReference = transactionReference;
            this.errorMessage = errorMessage;
        }

        /** Creates a successful payment result with a transaction reference. */
        public static PaymentResult successful(String transactionReference) {
            return new PaymentResult(true, transactionReference, null);
        }

        /** Creates a failed payment result with an error message. */
        public static PaymentResult failed(String errorMessage) {
            return new PaymentResult(false, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public String getTransactionReference() { return transactionReference; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Processes a payment using card details and an amount.
     *
     * This method simulates calling a third-party API:
     * 1. Validates input formats.
     * 2. Introduces a short delay to mimic a network round-trip.
     * 3. Returns a PaymentResult with success or failure.
     *
     * IMPORTANT: This method blocks the calling thread (simulated network call).
     * Always call it from a background thread.
     *
     * @param cardNumber the 16-digit card number (digits only, no spaces)
     * @param expiryDate the expiry date in MM/YY format
     * @param cvv        the 3-digit CVV
     * @param amount     the amount to charge
     * @return a PaymentResult indicating success or failure
     */
    public static PaymentResult processPayment(String cardNumber, String expiryDate,
                                                String cvv, double amount) {
        // Step 1: Validate card number (must be exactly 16 digits)
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            return PaymentResult.failed("Invalid card number. Must be 16 digits.");
        }

        // Step 2: Validate expiry date (MM/YY format)
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            return PaymentResult.failed("Invalid expiry date. Use MM/YY format.");
        }

        // Step 3: Validate CVV (must be exactly 3 digits)
        if (cvv == null || !cvv.matches("\\d{3}")) {
            return PaymentResult.failed("Invalid CVV. Must be 3 digits.");
        }

        // Step 4: Validate amount
        if (amount <= 0) {
            return PaymentResult.failed("Payment amount must be greater than zero.");
        }

        // Step 5: Simulate network delay (API call to payment provider)
        try {
            Thread.sleep(SIMULATED_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return PaymentResult.failed("Payment was interrupted.");
        }

        // Step 6: Check for test decline card
        if (TEST_DECLINE_CARD.equals(cardNumber)) {
            return PaymentResult.failed("Card declined. Please use a different card.");
        }

        // Step 7: Generate transaction reference and return success
        String transactionRef = "TXN-" + System.currentTimeMillis();
        return PaymentResult.successful(transactionRef);
    }
}
