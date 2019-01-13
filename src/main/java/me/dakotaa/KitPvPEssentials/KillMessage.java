package me.dakotaa.KitPvPEssentials;

public class KillMessage {
    private String label, message;
    private int order;

    public KillMessage(String label, String message, int order) {
        this.label = label;
        this.message = message;
        this.order = order;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
