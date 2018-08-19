package com.aravamsinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by sivaprakash on 12/25/2017.
 */

public class FolderDetailsAdapter {

    public  List<FolderDetailsBean> folderDetails;

    public  List<FolderDetailsBean>  extractFolderDetails(List<String> arg){

        folderDetails = new ArrayList<FolderDetailsBean>();

        for (String s : arg) {
            StringTokenizer st2 = new StringTokenizer(s, "@#$");

            FolderDetailsBean folderDetailsBean = new FolderDetailsBean();

            while (st2.hasMoreElements()) {
                String value = st2.nextElement().toString();

                if(value.contains("FolderName::")){
                    folderDetailsBean.setFolderName(value.substring("FolderName::".length()));
                }else if(value.contains("FolderID::")){
                    folderDetailsBean.setFolderID(value.substring("FolderID::".length()));
                }else if(value.contains("WebViewLink::")){
                    folderDetailsBean.setWebViewLink(value.substring("WebViewLink::".length()));
                }

            }
            folderDetails.add(folderDetailsBean);
        }

        return folderDetails;
    }

}
