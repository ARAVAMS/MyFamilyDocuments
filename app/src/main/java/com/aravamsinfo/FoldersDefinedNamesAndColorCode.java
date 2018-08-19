package com.aravamsinfo;

/**
 * Created by sivaprakash on 12/22/2017.
 */

enum FoldersDefinedNamesAndColorCode {



    GENERAL_SHOPPING_BILLS("General Shopping Bills",R.drawable.folder_view_generalshoppingbills),
    MEDICAL_BILLS("Medical Bills",R.drawable.folder_view_medicalbills),
    EDUCATIONAL("Educational Professional",R.drawable.folder_view_educational),
    INSURANCE_POLICY("Insurance Policy",R.drawable.folder_view_insurance_policyes),
    INCOME_TAX("Income Tax",R.drawable.folder_view_medicalbills),
    PROPERTY_DOCUMENTS("Propery Documents",R.drawable.folder_view_propertydocuments),
    GOLD_SHOPPING_BILLS("Gold Shopping Bills",R.drawable.folder_view_goldshoppingbills),
    OTHERS("Others",R.drawable.folder_view_anyothers);

    private final String folderName;      // Private variable
    private final int colorCode;

    FoldersDefinedNamesAndColorCode(String folders, int colorCode) {     // Constructor
        this.folderName = folders;
        this.colorCode = colorCode;
    }

    String getFolderName() {              // Getter
        return folderName;
    }
    int getFolderColorCode() {              // Getter
        return colorCode;
    }

}
