package com.example.qrcodereader.models;


import lombok.Setter;

@Setter
public class ProblemResponse {
    private String image_url;

    public String getImageUrl() {
        return image_url;
    }
}
