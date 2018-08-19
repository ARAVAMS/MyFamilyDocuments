package com.aravamsinfo;

/**
 * Created by sivaprakash on 11/29/2017.
 */

public class ListViewDataModel {

    String name;
    String webViewLink;
    String version_number;
    String feature;

    public ListViewDataModel(String name, String webViewLink, String version_number, String feature ) {
        this.name=name;
        this.webViewLink=webViewLink;
        this.version_number=version_number;
        this.feature=feature;

    }

    public String getName() {
        return name;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public String getVersion_number() {
        return version_number;
    }

    public String getFeature() {
        return feature;
    }

}
