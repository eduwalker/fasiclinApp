package com.example.fasipemobilej.network;

public enum ApiEnvironment {
    DEVELOPMENT("https://172.20.10.2:8443/"),
    PRODUCTION("https://172.20.10.2:8443/");

    private String baseUrl;

    ApiEnvironment(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
