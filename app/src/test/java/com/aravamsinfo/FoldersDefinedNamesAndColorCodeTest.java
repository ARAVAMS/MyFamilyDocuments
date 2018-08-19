package com.aravamsinfo;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by sivaprakash on 12/23/2017.
 */
public class FoldersDefinedNamesAndColorCodeTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getSeconds() throws Exception {

        System.out.println(FoldersDefinedNamesAndColorCode.EDUCATIONAL.getFolderName());
        for (FoldersDefinedNamesAndColorCode light : FoldersDefinedNamesAndColorCode.values()) {
            System.out.printf("%s: %s \n", light, light.getFolderName());
        }
    }

}