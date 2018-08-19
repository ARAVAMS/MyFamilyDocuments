package com.aravamsinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sivaprakash on 12/21/2017.
 */

public class FolderViewAdapter extends BaseAdapter {
    private final List<Item> mItems = new ArrayList<Item>();
    private final LayoutInflater mInflater;

    public FolderViewAdapter(Context context) {
        mInflater = LayoutInflater.from(context);


        // Apply defined folder names in Main view screen
        for (FoldersDefinedNamesAndColorCode definedFolder : FoldersDefinedNamesAndColorCode.values()) {
            mItems.add(new Item(definedFolder.getFolderName(),"", definedFolder.getFolderColorCode()));
        }
    }
    public FolderViewAdapter(Context context,List<FolderDetailsBean> folderDetailsBeans) {
        mInflater = LayoutInflater.from(context);


        // Apply defined folder names in Main view screen
        for (FoldersDefinedNamesAndColorCode definedFolder : FoldersDefinedNamesAndColorCode.values()) {

            for (FolderDetailsBean folderDetailsBean : folderDetailsBeans) {

                if(folderDetailsBean.getFolderName().equalsIgnoreCase(definedFolder.getFolderName())){
                    mItems.add(new Item(definedFolder.getFolderName(),folderDetailsBean.getFolderID(), definedFolder.getFolderColorCode()));

                }
            }
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Item getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mItems.get(i).drawableId;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView picture;
        TextView name;

        if (v == null) {
            v = mInflater.inflate(R.layout.grid_item, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));

        }

        picture = (ImageView) v.getTag(R.id.picture);
        name = (TextView) v.getTag(R.id.text);

        Item item = getItem(i);

        picture.setImageResource(item.drawableId);
        name.setText(item.folder_name);

        return v;
    }

    public static class Item {
        public final String folder_name;
        public final int drawableId;
        public final String folder_id;

        Item(String name, String folder_id, int drawableId) {
            this.folder_name = name;
            this.folder_id = folder_id;
            this.drawableId = drawableId;
        }
    }
}