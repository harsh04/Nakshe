package harshmathur.nic.soi.com.nakshe;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.shockwave.pdfium.PdfDocument;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnCameraChangeListener, MaterialSearchBar.OnSearchActionListener, PopupMenu.OnMenuItemClickListener, OnPageChangeListener, OnLoadCompleteListener {


    private LatLngBounds India_bound = new LatLngBounds(new LatLng(7.80822, 68.00000), new LatLng(38.20570, 97.00000));

    private GoogleMap mMap;
    double latitude_val;
    double longitude_val;
    String user_state;
    ProgressDialog progressMap;

    RecyclerView recyclerView;
    ArrayList<String> Name;
    ArrayList<String> State;
    ArrayList<Double> Longitude;
    ArrayList<Double> Latitude;
    ArrayList<String> OSM;

    private boolean tehsil = true;
    private boolean district = true;
    private boolean state = true;

    TextView heading, subheading, latitude, longitude, osm;
    private static final String TAG = MainActivity.class.getSimpleName();
    PDFView pdfView;
    Integer pageNumber = 0;
    String pdfFileName;

    RecyclerView.LayoutManager RecyclerViewLayoutManager;
    RecyclerViewAdapter RecyclerViewHorizontalAdapter;
    LinearLayoutManager HorizontalLayout;
    SlidingUpPanelLayout slidingUpPanelLayout;
    View ChildView;
    private int RecyclerViewItemPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //For full screen display
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        slidingUpPanelLayout.setEnabled(false);
        progressMap = ProgressDialog.show(MainActivity.this, "Loading", "Getting Map Ready for you...", true);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        Name = new ArrayList<>();
        State = new ArrayList<>();
        Longitude = new ArrayList<>();
        Latitude = new ArrayList<>();
        OSM = new ArrayList<>();

        heading = (TextView) findViewById(R.id.heading);
        subheading = (TextView) findViewById(R.id.subheading);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        osm = (TextView) findViewById(R.id.length);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        SearchResultList();

        MaterialSearchBar searchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
        searchBar.inflateMenu(R.menu.navigation);
        searchBar.getMenu().setOnMenuItemClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setLatLngBoundsForCameraTarget(India_bound);
        mMap.setOnCameraChangeListener(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                progressMap.dismiss();
                latitude_val = getIntent().getDoubleExtra("user_latitude",0);
                longitude_val = getIntent().getDoubleExtra("user_longitude",0);
                user_state = getIntent().getStringExtra("user_state");
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude_val,longitude_val), 8);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(new LatLng(latitude_val,longitude_val));
                markerOptions.title("Your location");
                mMap.animateCamera(cameraUpdate);
                mMap.addMarker(markerOptions);
                resetResultData();
                if(user_state.contains("madhya pradesh")){
                    updateResultData(new LatLng(latitude_val,longitude_val));// Toast.makeText(MainActivity.this,"called",Toast.LENGTH_SHORT).show();
                }else {
                    try{
                        AssetManager am = getAssets();
                        InputStream is = am.open("MADHYA_PRADESH_MTR.xls");
                        Workbook wb = Workbook.getWorkbook(is);
                        Sheet s = wb.getSheet(0);
                        double lati;
                        double longi;
                        for(int i=1; i<=3; i++){
                            lati = (Double.parseDouble(s.getCell(11,i).getContents())+Double.parseDouble(s.getCell(13,i).getContents())+Double.parseDouble(s.getCell(15,i).getContents())+Double.parseDouble(s.getCell(17,i).getContents()))/4;
                            longi = (Double.parseDouble(s.getCell(10,i).getContents())+Double.parseDouble(s.getCell(12,i).getContents())+Double.parseDouble(s.getCell(14,i).getContents())+Double.parseDouble(s.getCell(16,i).getContents()))/4;
                                Log.d("found : ",s.getCell(6,i).getContents()+", "+s.getCell(8,i).getContents());
                                Name.add(s.getCell(6,i).getContents()+", "+s.getCell(8,i).getContents());
                                State.add(s.getCell(9,i).getContents());
                                Longitude.add(longi);
                                Latitude.add(lati);
                                OSM.add(s.getCell(2,i).getContents());
                        }
                        RecyclerViewHorizontalAdapter.notifyDataSetChanged();
                    }catch (Exception e){

                    }
                }
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                mMap.clear();
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(markerOptions);
                resetResultData();
                updateResultData(latLng);
                
            }
        });
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        float minZoom = 4;
        if (cameraPosition.zoom < minZoom) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(minZoom));
            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(India_bound.getCenter(), 4) );
        }
    }

    public void resetResultData(){
        Name.clear();
        State.clear();
        Longitude.clear();
        Latitude.clear();
        OSM.clear();

        RecyclerViewHorizontalAdapter.notifyDataSetChanged();
    }

    private void updateResultData(LatLng latLng) {
        try{
            AssetManager am = getAssets();
            InputStream is = am.open("MADHYA_PRADESH_MTR.xls");
            Workbook wb = Workbook.getWorkbook(is);
            Sheet s = wb.getSheet(0);
            int row = s.getRows();
            double lati;
            double longi;
            for(int i=1; i<=row; i++){
                lati = (Double.parseDouble(s.getCell(11,i).getContents())+Double.parseDouble(s.getCell(13,i).getContents())+Double.parseDouble(s.getCell(15,i).getContents())+Double.parseDouble(s.getCell(17,i).getContents()))/4;
                longi = (Double.parseDouble(s.getCell(10,i).getContents())+Double.parseDouble(s.getCell(12,i).getContents())+Double.parseDouble(s.getCell(14,i).getContents())+Double.parseDouble(s.getCell(16,i).getContents()))/4;
                if(((lati >= latLng.latitude-0.25) && (lati <= latLng.latitude+0.25)) && ((longi >= latLng.longitude-0.25) && (longi <= latLng.longitude+0.25))){
                    Log.d("found : ",s.getCell(6,i).getContents()+", "+s.getCell(8,i).getContents());
                    Name.add(s.getCell(6,i).getContents()+", "+s.getCell(8,i).getContents());
                    State.add(s.getCell(9,i).getContents());
                    Longitude.add(longi);
                    Latitude.add(lati);
                    OSM.add(s.getCell(2,i).getContents());
                }
            }
            RecyclerViewHorizontalAdapter.notifyDataSetChanged();
        }catch (Exception e){

        }
    }
    @Override
    public void onSearchStateChanged(boolean b) {
        //String s = b ? "enabled" : "disabled";
        //Toast.makeText(MainActivity.this, "Search " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSearchConfirmed(CharSequence charSequence) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        String searchedValue = charSequence.toString();
        resetResultData();
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(4));
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(India_bound.getCenter(), 4) );
        try{
            AssetManager am = getAssets();
            InputStream is = am.open("MADHYA_PRADESH_MTR.xls");
            Workbook wb = Workbook.getWorkbook(is);
            Sheet s = wb.getSheet(0);
            int row = s.getRows();
            double lati;
            double longi;
            Cell z;
            mainloop:
            for(int i=1; i<=row; i++){
                for(int j=9; j>5; j--){
                    if((!tehsil && j==7) || (!district && j==8) || (!state && j==9)){
                        continue ;
                    }
                    z = s.getCell(j,i);             // keep column first then rows
                    if(z.getContents().contains(searchedValue.toUpperCase())){
                        lati = (Double.parseDouble(s.getCell(11,i).getContents())+Double.parseDouble(s.getCell(13,i).getContents())+Double.parseDouble(s.getCell(15,i).getContents())+Double.parseDouble(s.getCell(17,i).getContents()))/4;
                        longi = (Double.parseDouble(s.getCell(10,i).getContents())+Double.parseDouble(s.getCell(12,i).getContents())+Double.parseDouble(s.getCell(14,i).getContents())+Double.parseDouble(s.getCell(16,i).getContents()))/4;
                        Name.add(s.getCell(j,i).getContents());
                        State.add(s.getCell(9,i).getContents());
                        Longitude.add(longi);
                        Latitude.add(lati);
                        OSM.add(s.getCell(2,i).getContents());
                        RecyclerViewHorizontalAdapter.notifyDataSetChanged();
                        if(j==9){
                            break mainloop;
                        }
                    }
                }
            }


        }catch (Exception e){
            //Toast.makeText(MainActivity.this,"Excel report not fetched",Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onButtonClicked(int i) {
        // for search bar button action
    }

    public void SearchResultList() {
        recyclerView = (RecyclerView)findViewById(R.id.recyclerview1);
        RecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(RecyclerViewLayoutManager);

        RecyclerViewHorizontalAdapter = new RecyclerViewAdapter(Name,State,OSM);
        HorizontalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.setAdapter(RecyclerViewHorizontalAdapter);

        // Adding on item click listener to RecyclerView.
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {
                ChildView = Recyclerview.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if(ChildView != null && gestureDetector.onTouchEvent(motionEvent)) {
                    //Getting clicked value.
                    RecyclerViewItemPosition = Recyclerview.getChildAdapterPosition(ChildView);
                    // Showing clicked item value on screen using toast message.
                    //Toast.makeText(MainActivity.this, Name.get(RecyclerViewItemPosition), Toast.LENGTH_LONG).show();
                    slidingUpPanelLayout.setEnabled(true);
                    heading.setText(Name.get(RecyclerViewItemPosition));
                    subheading.setText(State.get(RecyclerViewItemPosition));
                    latitude.setText(new StringBuilder("Lat : ").append(String.valueOf(Latitude.get(RecyclerViewItemPosition))));
                    longitude.setText(new StringBuilder("Long : ").append(String.valueOf(Longitude.get(RecyclerViewItemPosition))));
                    osm.setText((new StringBuilder("OSM Sheet Number : ").append(OSM.get(RecyclerViewItemPosition))));
                    slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public void downloadAndOpenPDF(final Context context, final String pdfUrl) {
        // Get filename
        final String filename = pdfUrl.substring( pdfUrl.lastIndexOf( "/" ) + 1 );
        // The place where the downloaded PDF file will be put
        final File tempFile = new File( context.getExternalFilesDir( Environment.DIRECTORY_DOWNLOADS ), filename );
        if ( tempFile.exists() ) {
            // If we have downloaded the file before, just go ahead and show it.
            Button button = (Button) findViewById(R.id.downloadButton);
            button.setVisibility(View.GONE);
            displayFromAsset(Uri.fromFile(tempFile));
            return;
        }

        // Show progress dialog while downloading
        final ProgressDialog progress = ProgressDialog.show( context, "Downloading..." , "Please wait while we are downloading the PDF : "+filename, true );

        // Create the download request
        DownloadManager.Request r = new DownloadManager.Request( Uri.parse( pdfUrl ) );
        r.setDestinationInExternalFilesDir( context, Environment.DIRECTORY_DOWNLOADS, filename );
        final DownloadManager dm = (DownloadManager) context.getSystemService( Context.DOWNLOAD_SERVICE );
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ( !progress.isShowing() ) {
                    return;
                }
                context.unregisterReceiver( this );

                progress.dismiss();
                long downloadId = intent.getLongExtra( DownloadManager.EXTRA_DOWNLOAD_ID, -1 );
                Cursor c = dm.query( new DownloadManager.Query().setFilterById( downloadId ) );

                if ( c.moveToFirst() ) {
                    int status = c.getInt( c.getColumnIndex( DownloadManager.COLUMN_STATUS ) );
                    if ( status == DownloadManager.STATUS_SUCCESSFUL ) {
                        Button button = (Button) findViewById(R.id.downloadButton);
                        button.setVisibility(View.GONE);
                        displayFromAsset(Uri.fromFile( tempFile ));
                    }
                }
                c.close();
            }
        };
        context.registerReceiver( onComplete, new IntentFilter( DownloadManager.ACTION_DOWNLOAD_COMPLETE ) );

        // Enqueue the request
        dm.enqueue( r );

    }



    private void displayFromAsset(Uri assetFileName) {
        //showInputDialog();
        slidingUpPanelLayout.setTouchEnabled(false);
        pdfView.useBestQuality(true);
        pdfView.setSwipeVertical(true);

        pdfView.fromUri(assetFileName)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .enableDoubletap(true)
                .swipeHorizontal(true)
                .onPageChange(this)
                .enableAnnotationRendering(false)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .password("60708299")
                .load();
    }


    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }
    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case R.id.tehsil:
                if(item.isChecked()){
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    tehsil = false;
                }else{
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    tehsil = true;
                }
                return true;
            case R.id.district:
                if(item.isChecked()){
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    district = false;
                }else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    district = true;
                }
                return true;
            case R.id.state:
                if(item.isChecked()){
                    // If item already checked then unchecked it
                    item.setChecked(false);
                    state = false;
                }else {
                    // If item is unchecked then checked it
                    item.setChecked(true);
                    state = true;
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        // to reset everting on pressing back
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        resetResultData();
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(India_bound.getCenter(), 4) );

        // to close app on double backpress
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();
        // to reset value after 2 sec
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
    public void zoomOut(View view) {
        mMap.animateCamera(CameraUpdateFactory.zoomOut());
    }

    public void zoomIn(View view) {
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
    }

    public void reset(View view) {
        resetResultData();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(4));
        mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(India_bound.getCenter(), 4) );
    }

    public void closeResult(View view) {
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void startDowload(View view) {
        String url_to_file = "http://webconsole.website/SOI/"+OSM.get(RecyclerViewItemPosition).replace("/","_");  //replacing '/' with '_' to avoid error in url
        downloadAndOpenPDF(MainActivity.this,"http://webconsole.website/SOI/54F_3.pdf");  //TODO: replace hardcoded link with url_to_file String variable
    }

    public void gotoMyLocation(View view) {
        LatLng latLng = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
        resetResultData();
        updateResultData(latLng);
    }
}