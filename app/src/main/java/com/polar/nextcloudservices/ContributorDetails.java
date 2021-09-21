package com.polar.nextcloudservices;

public class ContributorDetails {
    public String Name;
    public String contribution;
    public String imageUrl;
    public String github_name;

    public ContributorDetails(String name, String contribution, String imageUrl, String github_name) {
        this.Name = name;
        this.contribution = contribution;
        this.imageUrl = imageUrl;
        this.github_name = github_name;

    }
}
