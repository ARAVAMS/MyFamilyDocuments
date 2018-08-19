package com.aravamsinfo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener,GoogleApiClient.OnConnectionFailedListener
        ,EasyPermissions.PermissionCallbacks, SharedPreferences.OnSharedPreferenceChangeListener


{

    ArrayList<ListViewDataModel> listViewDataModels;
    ListView listView;
    GridView folderListView;
    private static ListViewCustomAdapter listViewAdapter;


    private static final String TAG = "MyFamilyDocuments";
    /**
     * Request code for the Drive picker
     */
    protected static final int REQUEST_CODE_OPEN_ITEM = 1;
    /**
     * Tracks completion of the drive picker
     */
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    private SignInButton SignIn;
//    private Button SignOut;

    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    /**
     * Handles high-level drive functions like sync
     */
    private DriveClient mDriveClient;
    private String currentSelectedFolderId;
    /**
     * Handle access to Drive resources/files.
     */
    private DriveResourceClient mDriveResourceClient;

    private Toolbar toolbar;

    // API
    GoogleAccountCredential mCredential;
    ProgressDialog mProgress;

    String accountName ;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

    private static final String BUTTON_TEXT = "Call Drive API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { DriveScopes.DRIVE_METADATA_READONLY };

    MenuItem settingManuItem;

    private Activity mActivity;;


    private String userChoosenTask;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    String navigationFlowMessage;

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        mActivity = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.GONE);
        ((ListView) findViewById(R.id.list)).setVisibility(View.GONE);
        ((GridView) findViewById(R.id.foldersView)).setVisibility(View.GONE);


        SignIn = (SignInButton) findViewById(R.id.bn_login);

        SignIn.setOnClickListener(this);

//        SignOut.setOnClickListener(this);

        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER).build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();


        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        accountName = mCredential.getSelectedAccountName();

        System.out.println("accountName ************* "+accountName);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Drive API ...");

        webView = (WebView) findViewById(R.id.webview);



        webView.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        WebView webView = (WebView) findViewById(R.id.webview);

        ListView listView = (ListView) findViewById(R.id.list);

        System.out.println("Back Button Press *****************************************");

        if(webView.getVisibility() == View.VISIBLE){
            webView.setVisibility(View.GONE);
            ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
        }else if(listView.getVisibility() == View.VISIBLE){
            ((ListView) findViewById(R.id.list)).setVisibility(View.GONE);
            ((GridView) findViewById(R.id.foldersView)).setVisibility(View.VISIBLE);
            ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.GONE);

            final TextView flowMessage = (TextView) findViewById(R.id.flow_message);
            flowMessage.setText(""+navigationFlowMessage+" ->> \n");

        }else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
       }else {
            super.onBackPressed();
       }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        settingManuItem = menu.findItem(R.id.action_settings);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            // Settings
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            // end Settings
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.


        int id = item.getItemId();
        System.out.println("Calling Navigation "+item.getTitle());

        getDataFromGoogleDrive(item.getTitle().toString(),"main_screen");

       /* if (id == R.id.nav_camera) {
            // Handle the camera action
            mainContent(0);
        } else if (id == R.id.nav_gallery) {

            getDataFromGoogleDrive(item.getTitle().toString(),"main_screen");

        } else if (id == R.id.nav_slideshow) {

            getDataFromGoogleDrive(item.getTitle().toString(),"main_screen");

        } else if (id == R.id.nav_manage) {

            getDataFromGoogleDrive(item.getTitle().toString(),"main_screen");
        }*/
//        else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void mainContent(String s) {

        ((TextView)findViewById(R.id.messageTextView)).setText(s);


    }

    private void mainContent(List<String> output, String targetSource, String callingFolderName) {

        System.out.println("********************targetSource: "+targetSource);

        List<FolderDetailsBean> folderDetailsBeans = (new FolderDetailsAdapter()).extractFolderDetails(output);

        if(targetSource.equals("leftNavigation")){

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            final Menu menu = navigationView.getMenu();

  /*          menu.add("Add New Member").setIcon(R.drawable.ic_menu_share).
//                    setActionView(R.layout.add_family_option).
                    setChecked(true);*/



            for (FolderDetailsBean folderDetailsBean : folderDetailsBeans) {
                System.out.println(folderDetailsBean.getFolderID()+"----"+folderDetailsBean.getFolderName());

                // Aravam Set a proper image
                menu.add(folderDetailsBean.getFolderName()).setIcon(R.drawable.side_nav_bar);
            }

            for(int i=0;i<menu.size();i++){
                menu.getItem(i).setCheckable(true);
            }


        }else if (targetSource.equals("SharedPreferenceChange")){

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            final Menu menu = navigationView.getMenu();

//            List<FolderDetailsBean> folderDetailsBeans = (new FolderDetailsAdapter()).extractFolderDetails(output);

            menu.add(callingFolderName).setIcon(R.drawable.side_nav_bar).setCheckable(true);

        }else if (targetSource.equals("fileUpload") || targetSource.equals("fileDelete")){
            mainContentFileList(output,targetSource,callingFolderName);
        }else if (targetSource.equals("LoadFileFromFolder") ){
            mainContentFileList(output,targetSource,callingFolderName);
        }else {

            ((ListView) findViewById(R.id.list)).setVisibility(View.GONE);

            ((GridView) findViewById(R.id.foldersView)).setVisibility(View.VISIBLE);

            final TextView flowMessage = (TextView) findViewById(R.id.flow_message);
            navigationFlowMessage = callingFolderName;
            flowMessage.setText(""+navigationFlowMessage+" ->> ");


            GridView gridView = (GridView) findViewById(R.id.foldersView);
            gridView.setAdapter(new FolderViewAdapter(this,folderDetailsBeans));


            final List<FolderViewAdapter.Item> mItems = new ArrayList<FolderViewAdapter.Item>();

            // Main screen folder view action
            for (FoldersDefinedNamesAndColorCode definedFolder : FoldersDefinedNamesAndColorCode.values()) {

                for (FolderDetailsBean folderDetailsBean : folderDetailsBeans) {

                    if (folderDetailsBean.getFolderName().equalsIgnoreCase(definedFolder.getFolderName())) {
                        mItems.add(new FolderViewAdapter.Item(definedFolder.getFolderName(), folderDetailsBean.getFolderID(), definedFolder.getFolderColorCode()));
                    }

                }
            }
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    FolderViewAdapter.Item item = mItems.get(position);
//
//                Snackbar.make(view, item.name+"\n"+item.drawableId+" API: ", Snackbar.LENGTH_LONG)
//                        .setAction("No action", null).show();

                    System.out.println("********************************>> Folder View id : "+item.folder_id+" :- "+item.folder_name);
                    final TextView flowMessage = (TextView) findViewById(R.id.flow_message);
                    flowMessage.setText(""+navigationFlowMessage+" ->> "+item.folder_name);

                    currentSelectedFolderId = item.folder_id;

                    new MakeRequestTask(mCredential,"","LoadFileFromFolder").execute();

//                    mainContent(1);
                }
            });
        }
    }

    private void mainContentFileList( List<String> output, String targetSource, String callingFolderName) {

        listView=(ListView)findViewById(R.id.list);

        ((ListView)findViewById(R.id.list)).setVisibility(View.VISIBLE);;
        ((GridView)findViewById(R.id.foldersView)).setVisibility(View.GONE);

//            Flotting Action Button on Main Streen
        ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /*     Snackbar.make(view, "Upload Document", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/

                selectImage();
            }
        });


        listViewDataModels = new ArrayList<>();

        List<FolderDetailsBean>  fileDetailsBeans = (new FolderDetailsAdapter()).extractFolderDetails(output);

        for (FolderDetailsBean fileDetailsBean : fileDetailsBeans) {

            System.out.println("fileDetailsBean.getWebViewLink() --- "+fileDetailsBean.getWebViewLink());
            listViewDataModels.add(new ListViewDataModel(fileDetailsBean.getFolderName(),
                    fileDetailsBean.getWebViewLink(),fileDetailsBean.getFolderID(), "November 12, 2014"));

        }

//            listViewDataModels.add(new ListViewDataModel("Marshmallow", "Android 6.0", "23", "October 5, 2015"));

        listViewAdapter = new ListViewCustomAdapter(listViewDataModels,getApplicationContext());

        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListViewDataModel listViewDataModel = listViewDataModels.get(position);

                OpenFileInWebView(view,listViewDataModel.getWebViewLink());
            }
        });
        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(" logn touched me ");
                Snackbar.make(view, "Please Wait asdfasdfasfdasdf", Snackbar.LENGTH_LONG)
                        .setAction("No action", null).show();
                ListViewDataModel listViewDataModel = listViewDataModels.get(position);
                showFilterPopup(view,listViewDataModel);
                return true;

            }
        });

    }

    /*private void mainContent(int i) {

        listView=(ListView)findViewById(R.id.list);

        ((ListView)findViewById(R.id.list)).setVisibility(View.VISIBLE);;
        ((GridView)findViewById(R.id.foldersView)).setVisibility(View.GONE);


//            Flotting Action Button on Main Screen
        ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               *//*     Snackbar.make(view, "Upload Document", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*//*

                selectImage();
            }
        });

        listViewDataModels = new ArrayList<>();
        String url = "https://docs.google.com/file/d/1_L-fUVKl8d1ip9zgorzj9vvZjoCHzlmF/view";
        if(i==1) {
            listViewDataModels.add(new ListViewDataModel("Apple Pie", url, "1", "September 23, 2008"));
            listViewDataModels.add(new ListViewDataModel("Banana Bread", url, "2", "February 9, 2009"));
            listViewDataModels.add(new ListViewDataModel("Cupcake", url, "3", "April 27, 2009"));
            listViewDataModels.add(new ListViewDataModel("Donut", url, "4", "September 15, 2009"));
            listViewDataModels.add(new ListViewDataModel("Eclair", url, "5", "October 26, 2009"));
            listViewDataModels.add(new ListViewDataModel("Froyo", url, "8", "May 20, 2010"));
            listViewDataModels.add(new ListViewDataModel("Gingerbread", url, "9", "December 6, 2010"));
            listViewDataModels.add(new ListViewDataModel("Honeycomb", url, "11", "February 22, 2011"));
            listViewDataModels.add(new ListViewDataModel("Ice Cream Sandwich", url, "14", "October 18, 2011"));
            listViewDataModels.add(new ListViewDataModel("Jelly Bean", url, "16", "July 9, 2012"));
            listViewDataModels.add(new ListViewDataModel("Kitkat", url, "19", "October 31, 2013"));

        }else if(i==2) {
            listViewDataModels.add(new ListViewDataModel("Lollipop", "Android 5.0", "21", "November 12, 2014"));
            listViewDataModels.add(new ListViewDataModel("Marshmallow", "Android 6.0", "23", "October 5, 2015"));
        }
        listViewAdapter = new ListViewCustomAdapter(listViewDataModels,getApplicationContext());

        listView.setAdapter(listViewAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ListViewDataModel listViewDataModel = listViewDataModels.get(position);

                OpenFileInWebView(view,listViewDataModel.getWebViewLink());
            }
        });

        listView.setLongClickable(true);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                showFilterPopup(view);
                System.out.println(" logn touched me ");
                return true;

            }
        });
    }*/

    // Display anchored popup menu based on view selected
    private void showFilterPopup(final View v, final ListViewDataModel listViewDataModel) {
        PopupMenu popup = new PopupMenu(this, v);
        // Inflate the menu from xml
        popup.inflate(R.menu.item_popup_filters);
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        // On Click Delete PopUp

                        new MakeRequestTask(mCredential,"","fileDelete",listViewDataModel.getVersion_number()).execute();
                        Toast.makeText(MainActivity.this, "Delete!", Toast.LENGTH_SHORT).show();
                        return true;
                  /*  case R.id.menu_view:
                        // On Click View PopUp
                        Toast.makeText(MainActivity.this, "View!", Toast.LENGTH_SHORT).show();
                        return true;*/
                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
    }
    private void OpenFileInWebView(View view,String webViewLink) {
        Snackbar.make(view, "Please Wait Loading Is In Progress", Snackbar.LENGTH_LONG)
                .setAction("No action", null).show();


        try {
            String url = "https://docs.google.com/file/d/1_L-fUVKl8d1ip9zgorzj9vvZjoCHzlmF/view";

            url = webViewLink;

            webView.clearHistory();
            webView.setVisibility(View.VISIBLE);
            ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.GONE);

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setAllowContentAccess(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setSupportZoom(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setDisplayZoomControls(true);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setUseWideViewPort(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                webView.getSettings().setAllowFileAccessFromFileURLs(true);
            }
            webView.loadUrl(url);
            onBackPressed();
        } catch (ActivityNotFoundException e) {

            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //File uploads start
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(MainActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }
    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*,application/pdf,text/html,text/plain");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {

                System.out.println("data.getDataString() ---------> "+data.getData());
                String fileAbsolutePath = PathUtils.getPath(getApplicationContext(), data.getData());
                System.out.println("PathUtils.getPath --- "+ fileAbsolutePath);
//                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                new MakeRequestTask(mCredential,"","fileUpload",fileAbsolutePath).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        System.out.println("Environment.getExternalStorageDirectory() :-->>> "+Environment.getExternalStorageDirectory());
        java.io.File destination = new java.io.File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        String fileAbsolutePath ="";
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            System.out.println("destination.getAbsolutePath() ---> "+destination.getAbsolutePath());
            fileAbsolutePath = destination.getAbsolutePath();
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new MakeRequestTask(mCredential,"","fileUpload",fileAbsolutePath).execute();
        System.out.println("Capture Image --------->>>>>>>>>>> "+data.getType());
    }

    private void signIn(){

        Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(intent,REQ_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println(" OnActivityResults *********************************"+requestCode+" resultCode:"+resultCode);
        if(requestCode==REQ_CODE){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE){
                onSelectFromGalleryResult(data);
            }else if (requestCode == REQUEST_CAMERA){
                onCaptureImageResult(data);
            }

        }


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    private void handleResult(GoogleSignInResult googleSignInResult){

        if(googleSignInResult.isSuccess()) {
            GoogleSignInAccount account = googleSignInResult.getSignInAccount();
            String name = account.getDisplayName() +" Family";
            String email = account.getEmail();

            accountName = email;

            initializeDriveClient(account);

            ((TextView)findViewById(R.id.userName)).setText(name);
            ((TextView)findViewById(R.id.userEmail)).setText(email);

            if(account.getPhotoUrl()!=null) {
                String img_url = account.getPhotoUrl().toString();
                Glide.with(this).load(img_url).into((ImageView) findViewById(R.id.imageView));
            }

            getDataFromGoogleDrive(account.getDisplayName() +"(MySelf)","main_screen");

            InitializeUIAfterGoogleLogin(true);

            settingManuItem.setVisible(true);

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(this);
            prefs.registerOnSharedPreferenceChangeListener(this);


        }
    }

    private void InitializeUIAfterGoogleLogin(boolean isLogin) {

        if(isLogin) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            final Menu menu = navigationView.getMenu();

            // Retrieve Folders for left Navingation Menu Items

            getDataFromGoogleDrive("","leftNavigation");

            // Runtime item add left menu item
           /* for (int i = 1; i <= 3; i++) {
                menu.add(1, 1, i, "Runtime item " + i);

            }*/

            SignIn.setVisibility(View.GONE);
        }
    }

    private void signOut(){

        getDataFromGoogleDrive("","SignOut");
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                //After SignOut start Login Screen
                mActivity.finish();
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName() );

                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity(i);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId())
        {
            case R.id.bn_login:
                signIn();
                break;
           /* case R.id.bn_logout:
                signOut();
                break;*/
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }


    //    @Override
    protected void onDriveClientReady() {
        System.out.println("Google Driver Loaded ");
    }


    /**
     * Shows a toast message.
     */
    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    /// Google REST API

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     * @param selectedFolderName
     */
    private void getDataFromGoogleDrive(String selectedFolderName,String callingFor) {

        Log.i(TAG," getDataFromGoogleDrive :: selectedFolderName :"+selectedFolderName +" callingFor : "+callingFor);

        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();

        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount(selectedFolderName,callingFor);

        } else if (! isDeviceOnline()) {
            mainContent("No network connection available.");
        } else {
            if(!callingFor.equalsIgnoreCase("SignOut"))
                 new MakeRequestTask(mCredential,selectedFolderName,callingFor).execute();
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount(String s,String target) {
        if (EasyPermissions.hasPermissions(
                this, Manifest.permission.GET_ACCOUNTS)) {

            // This code not compatable with Nexus 6+
        /*    String accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);*/

            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getDataFromGoogleDrive(s,target);
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS);
        }
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @parax`m list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Log.i(TAG,"Request From Settings : selected Key :- "+key);

        if(key.equalsIgnoreCase("AddNewFamilyMember")){

            String folderName = sharedPreferences.getString(key, "");

        Log.i(TAG,"Request to create new folder :: "+folderName);

            getDataFromGoogleDrive(folderName,"SharedPreferenceChange");
        }else if(key.equalsIgnoreCase("SignOut")){

            if (googleApiClient.isConnected()) {
                signOut();
            } else {
                googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        signOut();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                });
                googleApiClient.connect();
            }
        }

    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;
        private String callingFolderName;
        // target screen left navigation or main screen
        private String target;
        private String fileContent;

        MakeRequestTask(GoogleAccountCredential credential, String callingFamilyMemberName, String callingFor) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            callingFolderName = callingFamilyMemberName;
            target = callingFor;

            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("MyFamilyDocuments")
                    .build();
        }
        MakeRequestTask(GoogleAccountCredential credential, String callingFamilyMemberName, String callingFor,Bitmap fileContent) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            callingFolderName = callingFamilyMemberName;
            target = callingFor;

            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("MyFamilyDocuments")
                    .build();
        }MakeRequestTask(GoogleAccountCredential credential, String callingFamilyMemberName, String callingFor,String fileContent_) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            callingFolderName = callingFamilyMemberName;
            target = callingFor;

            fileContent = fileContent_;
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("MyFamilyDocuments")
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {

                System.out.println("taget ---------: "+target);

                return getDataFromApi();
            } catch (Exception e) {
                e.printStackTrace();
                mLastError = e;
                cancel(true);
                return null;
            }

        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {

            List<String> fileInfo = new ArrayList<String>();
            List<File> files = null;
            if(target.equalsIgnoreCase("fileDelete")){
                System.out.println("--------currentSelectedFolderId----------------------- "+currentSelectedFolderId);
                deleteFileFromGoogleDrive(fileContent);
                files = retrieveFileListFromGoogleDrive(currentSelectedFolderId);
            }
           else if(target.equalsIgnoreCase("fileUpload")){

                System.out.println("--------currentSelectedFolderId----------------------- "+currentSelectedFolderId);

                File fileMetadata = new File();
//                fileMetadata.setName("photo.jpg");
                fileMetadata.setParents(Collections.singletonList(currentSelectedFolderId));
                fileMetadata.setMimeType("image/jpeg");
                System.out.println("fileContent == "+fileContent);
                if(fileContent!=null) {
                    java.io.File filePath = new java.io.File(fileContent);
                    fileMetadata.setName(filePath.getName() != null ? filePath.getName() : "");
                    FileContent mediaContent = new FileContent("image/jpeg", filePath);
                    File file = mService.files().create(fileMetadata, mediaContent)
                            .setFields("id, parents")
                            .execute();
                }else{
                    System.out.println("File Not uploaded as fileContent is null");
                }
                files = retrieveFileListFromGoogleDrive(currentSelectedFolderId);


//                mService.files().export(file.getId(), "application/jpeg").execute();

            }else   if(target.equalsIgnoreCase("LoadFileFromFolder")){

               System.out.println("--------currentSelectedFolderId----------------------- "+currentSelectedFolderId);

               files = retrieveFileListFromGoogleDrive(currentSelectedFolderId);


//                mService.files().export(file.getId(), "application/jpeg").execute();

           }else {

                files = retrieveFolderListFromGoogleDrive("");

                String parentFolderID = retriveAppRootFolder(files);

                files = retrieveFolderListFromGoogleDrive(parentFolderID);

                if (!target.equals("leftNavigation")) {
                    parentFolderID = createFirstChildFolders(files, parentFolderID);

                    files = retrieveFolderListFromGoogleDrive(parentFolderID);

                    createChildFolders(files, parentFolderID);

                    // re execute again because some time folders might not exist and it will create new folders
                    files = retrieveFolderListFromGoogleDrive(parentFolderID);
                }

            }
            if (files != null) {
                for (File file1 : files) {
                    fileInfo.add(String.format("FolderName::%s@#$FolderID::%s@#$WebViewLink::%s",
                            file1.getName(), file1.getId(),file1.getWebViewLink()));

                    System.out.println(String.format("FolderName::%s@#$FolderID::%s",
                            file1.getName(), file1.getId()));
                    System.out.println(file1.getWebContentLink());
                    System.out.println(file1.getWebViewLink());
                    System.out.println(file1.getId());
                }
            }
            return fileInfo;
        }

        private void deleteFileFromGoogleDrive(String fileId) throws IOException {
            System.out.println("asdfasdf Deleted File from Folder ");

            try {
                mService.files().delete(fileId).execute();
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
            }

        }

        private List<File> retrieveFolderListFromGoogleDrive(String parentFolderID) throws IOException {
            if(parentFolderID.equals("")) {
                FileList result = mService.files().list()
                        .setQ("mimeType='application/vnd.google-apps.folder'")
//                    .setPageSize(10)
                        .setFields("nextPageToken, files(id, name, webContentLink)")
                        .execute();
               return result.getFiles();
            }else {
                FileList result = mService.files().list()
                        .setQ("mimeType='application/vnd.google-apps.folder' and '" + parentFolderID + "' in parents")
//                    .setPageSize(10)
                        .setFields("nextPageToken, files(id, name,webContentLink)")
                        .execute();
                return result.getFiles();
            }
        }

        private List<File> retrieveFileListFromGoogleDrive(String parentFolderID) throws IOException {
            String pageToken = null;
                FileList result = mService.files().list()
                        .setQ("mimeType!='application/vnd.google-apps.folder' and '" + parentFolderID + "' in parents")
//                    .setPageSize(10)\
                        .setFields("nextPageToken, files(id, name, webContentLink, webViewLink, modifiedTime)")
                        .execute();
                return result.getFiles();

        }

        private String createFirstChildFolders(List<File> files,String parentFolderID) throws IOException {

                boolean parentFolderExist = false;
                String parentFolderId ="";

                if (files != null) {
                    for (File file : files) {

                        if(file.getName().equals(callingFolderName)){
                            parentFolderExist = true;
                            parentFolderId = file.getId();
                            System.out.println("callingFolderName : "+file.getName() +" callingFolderID : "+parentFolderId);
                            break;
                        }
                    }
                }

                if(!parentFolderExist){

                    File fileMetadata = new File();
                    fileMetadata.setName(callingFolderName);
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(parentFolderID));
                    File file = mService.files().create(fileMetadata)
                            .setFields("id")
                            .execute();

                    parentFolderId = file.getId();
                }
                return parentFolderId;


        }

        private String retriveAppRootFolder(List<File> files) throws IOException {

            boolean parentFolderExist = false;
            String parentFolderId ="";

            if (files != null) {
                for (File file : files) {
                    if(file.getName().equals(getString(R.string.AppFolderName))){
                        parentFolderExist = true;
                        parentFolderId = file.getId();
                        System.out.println("ParentFolder Name :- "+file.getName()+" ParentFolder ID:- "+file.getId() );
                        break;
                    }
                }
            }

            if(!parentFolderExist){
                parentFolderId = createParentFolder();
            }
            return parentFolderId;
        }

        private String createParentFolder() throws IOException {

            String parentFolderId;File fileMetadata = new File();

            fileMetadata.setName(getString(R.string.AppFolderName));
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = mService.files().create(fileMetadata)
                    .setFields("id")
                    .execute();

            parentFolderId = file.getId();
            return parentFolderId;
        }

        private void createChildFolders(List<File> files, String parentFolderID) throws IOException {

            for (FoldersDefinedNamesAndColorCode definedFolder : FoldersDefinedNamesAndColorCode.values()) {
//                System.out.printf("%s: %s \n", definedFolder, definedFolder.getFolderName());
                boolean folderExist = false;

                for (File file : files) {
                    if(definedFolder.getFolderName().equals(file.getName())){
                        folderExist = true;
                    }
                }

                if(!folderExist){
                    File fileMetadata = new File();
                    fileMetadata.setName(definedFolder.getFolderName());
                    fileMetadata.setMimeType("application/vnd.google-apps.folder");
                    fileMetadata.setParents(Collections.singletonList(parentFolderID));

                    File file = mService.files().create(fileMetadata)
                            .setFields("id")
                            .execute();

//                    System.out.println("Newly Created Folder ID: " + file.getId());

                }
            }
        }


        @Override
        protected void onPreExecute() {
            mainContent("");
            if(mProgress!=null)
                 mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            if(mProgress!=null)mProgress.hide();
            /*if (output == null || output.size() == 0) {
                mainContent("No results returned.");
            } else {
//                output.add(0, "Data retrieved using the Drive API:");
                mainContent(output,target,callingFolderName);
            }*/
            mainContent(output,target,callingFolderName);
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mainContent("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mainContent("Request cancelled.");
            }
        }
    }



}
