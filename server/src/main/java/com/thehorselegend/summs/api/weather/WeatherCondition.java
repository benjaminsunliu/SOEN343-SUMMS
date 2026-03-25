package com.thehorselegend.summs.api.weather;

public class WeatherCondition {
    private String type;
    private Severity severity;

    public WeatherCondition(String type, Severity severity) {
        this.type = type;
        this.severity = severity;
    }

    public String getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "WeatherCondition{" +
                "type='" + type + '\'' +
                ", severity=" + severity +
                '}';
    }
}
