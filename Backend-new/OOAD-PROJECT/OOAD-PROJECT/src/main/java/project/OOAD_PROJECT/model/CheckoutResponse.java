package project.OOAD_PROJECT.model;

public class CheckoutResponse {
    private Long amount;
    private String currency;
    private String checkoutUrl;
    private String status;
    private String sessionId;

    public CheckoutResponse(Long amount, String currency, String checkoutUrl, String status, String sessionId) {
        this.amount = amount;
        this.currency = currency;
        this.checkoutUrl = checkoutUrl;
        this.status = status;
        this.sessionId = sessionId;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getStatus() {
        return status;
    }

    public String getSessionId() {
        return sessionId;
    }
}

