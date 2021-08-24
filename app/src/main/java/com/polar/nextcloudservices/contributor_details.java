package com.polar.nextcloudservices;

public class contributor_details {
    public String Name;
    public String contribution;
    public String imageUrl;
    public String github_name;

    public contributor_details(String name, String contribution, String imageUrl, String github_name) {
        this.Name = name;
        this.contribution = contribution;
        this.imageUrl = imageUrl;
        this.github_name = github_name;

    }
}
