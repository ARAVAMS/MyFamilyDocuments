package com.aravamsinfo;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by sivaprakash on 12/25/2017.
 */
public class FolderDetailsAdapterTest {
    @Test
    public void extractFolderDetails() throws Exception {

        List<String> arg = new ArrayList<String>();
        arg.add(String.format("FolderName::%s@#$FolderID::%s@#$WebViewLink::%s",
                "Siva", "asdfasdfasdf23fsdf12","link1"));

        arg.add(String.format("FolderName::%s@#$FolderID::%s@#$WebViewLink::%s",
                "Dad", "a4424dfasdfasdf23fsdf12","link2"));

        List<FolderDetailsBean> folderDetailsBeans = (new FolderDetailsAdapter()).extractFolderDetails(arg);

        for (FolderDetailsBean folderDetailsBean : folderDetailsBeans) {
            System.out.println(folderDetailsBean.getFolderID()+"----"+folderDetailsBean.getFolderName()+"-----"+folderDetailsBean.getWebViewLink());
        }
    }

}