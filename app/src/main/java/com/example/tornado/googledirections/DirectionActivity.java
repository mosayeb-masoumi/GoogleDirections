package com.example.tornado.googledirections;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wiadevelopers.com.directionlib.DirectionCallback;
import wiadevelopers.com.directionlib.GoogleDirection;
import wiadevelopers.com.directionlib.constant.TransportMode;
import wiadevelopers.com.directionlib.constant.Unit;
import wiadevelopers.com.directionlib.model.Direction;
import wiadevelopers.com.directionlib.model.RouteInfo;
import wiadevelopers.com.directionlib.util.MapUtils;

public class DirectionActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    TextView txtDestination, txtOrigin, txtWalking, txtDriving;
    ImageView imgDriving, imgWalking;
    RelativeLayout rltvDriving, rltvWalking;
    FloatingActionButton btnRequestDirection;

    private static int COLOR_WHITE;
    private static int COLOR_PRIMARY;

    private final static int NONE = 65;
    private final static int WALKING = 97;
    private final static int DRIVING = 51;

    private final LatLng mIRAN = new LatLng(32.2712623, 51.2585412);

    //taarif marker mabda & maghsad
    private Marker markerOrigin = null;
    private Marker markerDestination = null;


    //jahat masir keshi bein mabda va maghsad braye driving
    private ArrayList<RouteInfo> routeInfosDriving = new ArrayList<>();
    private ArrayList<Polyline> polylinesDriving = new ArrayList<>();

    //jahat masir keshi bein mabda va maghsad braye Walking
    private ArrayList<RouteInfo> routeInfosWalking = new ArrayList<>();
    private ArrayList<Polyline> polylinesWalking = new ArrayList<>();


    // jahat estefade dar methode setOnPolylineclick baraye khakestary kardane routhaye alternative va abi kardan route asli
    private int index = -1;

    //jahat click roye routhay alternative walking
    private String transportMode = TransportMode.DRIVING;

    //--------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

        initialize();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mIRAN, 5));


        setListeners();
    }

    //----------------------------------------------------------------------------------------------

    //(click roye naghshe be modat toolany_marker ra namaysh dahad)
    private void setListeners() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .draggable(false)
                        .rotation(0);

                //agar marker mabda zade nashode bud
                if (markerOrigin == null)
                    markerOrigin = mMap.addMarker(markerOptions);

                    // agar markere maghsad zade nashode bud
                else if (markerDestination == null)
                    markerDestination = mMap.addMarker(markerOptions);


                // agar  markere mabda & va maghsad zade shode bud
                if (markerDestination != null && markerOrigin != null)
                    btnRequestDirection.setVisibility(View.VISIBLE);


                //put kardan mokhtasat be dakhele TextView ha  //esme class
                Geocoder geocoder = new Geocoder(DirectionActivity.this);
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    String location = addresses.get(0).getLocality();


                    // agar esme shahr nabud esme jaadde re beriz
                    if (location == null || location.equals(""))
                        location = addresses.get(0).getSubThoroughfare();

                    // agar eme shahr banud  & esme jaadde ham nabue ,mokhtasat ra namayesh bede
                    if (location == null || location.equals(""))
                        location = addresses.get(0).getLatitude() + "," + addresses.get(0).getLongitude();


                    // put kardan mokhtasat ya esme shahr dakhke textView mabda ye maghsad
                    if (markerOrigin != null && markerDestination == null)
                        txtOrigin.setText(location);
                    else if (markerOrigin != null && markerDestination != null)
                        txtDestination.setText(location);


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

        //neveshtan click braye float action bar
        btnRequestDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                drivingRequest();
                walkingRequest();

            }
        });



        //taghire rang relative layout rltDriving
        rltvDriving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(routeInfosDriving.size() != 0)
                activator(DRIVING);
            }
        });

        //taghire rang relative layout rltWalking
        rltvWalking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(routeInfosWalking.size() != 0)
                activator(WALKING);
            }
        });


        // khakestary kardane rout haye alternative va abi kardan route klik shode
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                String tag = polyline.getTag().toString();
                index = Integer.parseInt(tag);

                if(transportMode.equals(TransportMode.DRIVING)) {


                    // routi ke rouyash click shod ra pak kon
                    polylinesDriving.get(index).remove();

                    final Polyline mPolyline = mMap.addPolyline(routeInfosDriving.get(index).getPolylineOptions());
                    mPolyline.setTag(index);
                    polylinesDriving.set(index, mPolyline);


                    // halghe for baraye rang kardan  routha
                    for (int i = 0; i < polylinesDriving.size(); i++) {
                        //agar route click nashode bud
                        if (i != index)
                            polylinesDriving.get(i).setColor(Direction.UNSELECTED_ROUTE);
                        else
                            polylinesDriving.get(i).setColor(Direction.SELECTED_ROUTE);
                    }

                    txtDriving.setText(routeInfosDriving.get(index).getDurationText());

                }else if(transportMode.equals(TransportMode.WALKING))
                {

                    // routi ke rouyash click shod ra pak kon
                    polylinesWalking.get(index).remove();

                    final Polyline mPolyline = mMap.addPolyline(routeInfosWalking.get(index).getPolylineOptions());
                    mPolyline.setTag(index);
                    polyline.setPattern(MapUtils.getPattern(MapUtils.patternType.DOT));
                    polylinesWalking.set(index, mPolyline);


                    // halghe for baraye rang kardan  routha
                    for (int i = 0; i < polylinesWalking.size(); i++) {
                        //agar route click nashode bud
                        if (i != index)
                            polylinesWalking.get(i).setColor(Direction.UNSELECTED_ROUTE);
                        else
                            polylinesWalking.get(i).setColor(Direction.SELECTED_ROUTE);
                    }

                    txtWalking.setText(routeInfosWalking.get(index).getDurationText());

                }
            }
        });

    }

    //-------------------------------------------------------------------------------------------------
    private void drivingRequest() {
        // darkhast ma ra ersal kon    //mitanestin benevisim ("API_KEY ra paste konim")
        GoogleDirection.withServerKey(Constant.API_KEY)
                .from(markerOrigin.getPosition())
                .to(markerDestination.getPosition())
                .alternativeRoute(true)
                .unit(Unit.METRIC)
                .transportMode(TransportMode.DRIVING)

                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String s) {

                        if (direction.isOK()) {                        //context        // 5= width route
                            routeInfosDriving = direction.getRouteInfo(DirectionActivity.this, 5);

                            // zaman ra dar kenar imgView Ashin touye TextView namayesh bede*/
                            txtDriving.setText(routeInfosDriving.get(routeInfosDriving.size() - 1).getDurationText());

                            activator(DRIVING);

                        }else
                            Toast.makeText(DirectionActivity.this, " NOT OK", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDirectionFailure(Throwable throwable) {

                    }
                });
    }



//-------------------------------------------------------------------------------------------------

     private void walkingRequest(){

         // darkhast ma ra ersal kon    //mitanestin benevisim ("API_KEY ra paste konim")
         GoogleDirection.withServerKey(Constant.API_KEY)
                 .from(markerOrigin.getPosition())
                 .to(markerDestination.getPosition())
                 .alternativeRoute(true)
                 .unit(Unit.METRIC)
                 .transportMode(TransportMode.WALKING)

                 .execute(new DirectionCallback() {
                     @Override
                     public void onDirectionSuccess(Direction direction, String s) {

                         if (direction.isOK()) {                         //context        // 5= width route
                             routeInfosWalking = direction.getRouteInfo(DirectionActivity.this, 5);

                             // zaman ra dar kenar imgView Ashin touye TextView namayesh bede*/
                             txtWalking.setText(routeInfosWalking.get(routeInfosWalking.size() - 1).getDurationText());
                             activator(WALKING);

                         }else
                             Toast.makeText(DirectionActivity.this, "NOT OK", Toast.LENGTH_SHORT).show();
                     }

                     @Override
                     public void onDirectionFailure(Throwable throwable) {

                     }
                 });
     }


    private void initialize() {
        findViews();
        setup();
        activator(NONE);
    }

    private void findViews() {
        txtDestination = (TextView) findViewById(R.id.txtDestination);
        txtOrigin = (TextView) findViewById(R.id.txtOrigin);
        txtDriving = (TextView) findViewById(R.id.txtDriving);
        txtWalking = (TextView) findViewById(R.id.txtWalking);

        imgDriving = (ImageView) findViewById(R.id.imgDriving);
        imgWalking = (ImageView) findViewById(R.id.imgWalking);

        rltvDriving = (RelativeLayout) findViewById(R.id.rltvDriving);
        rltvWalking = (RelativeLayout) findViewById(R.id.rltvWalking);

        btnRequestDirection = (FloatingActionButton) findViewById(R.id.btnRequestDirection);

    }

    //-----------------------------------------------------------------------------------

    //(jahat rasm route bar asas click kardan relativeLayout mashin ya adamak
    private void drawRouts(int num)
    {

        // halghe jahat pak kardan routha
        for (int i=0 ; i<polylinesDriving.size() ; i++)
            polylinesDriving.get(i).remove();
        for (int i=0 ; i<polylinesWalking.size() ; i++)
            polylinesWalking.get(i).remove();

        polylinesDriving.clear();
        polylinesWalking.clear();


       if( num == DRIVING)
       {
           for (int i = 0; i < routeInfosDriving.size(); i++) {
               final Polyline polyline = mMap.addPolyline(routeInfosDriving.get(i).getPolylineOptions());
               polyline.setTag(i);
               polylinesDriving.add(polyline);
           }

       }
       else if( num == WALKING)
           {
               for (int i = 0; i < routeInfosWalking.size(); i++) {
                   final Polyline polyline = mMap.addPolyline(routeInfosWalking.get(i).getPolylineOptions());
                   polyline.setPattern(MapUtils.getPattern(MapUtils.patternType.DOT));
                   polyline.setTag(i);
                   polylinesWalking.add(polyline);
               }

           }
    }

//---------------------------------------------------------------------------------------------------
    private void setup() {
        txtDriving.setText("-");
        txtWalking.setText("-");

        COLOR_WHITE = ContextCompat.getColor(getApplicationContext(), R.color.white);
        COLOR_PRIMARY = ContextCompat.getColor(getApplicationContext(), R.color.blue);

        btnRequestDirection.setVisibility(View.GONE);
    }


//--------------------------------------------------------------------------------------------------

    private void activator(int num) {
        final PorterDuff.Mode mode = android.graphics.PorterDuff.Mode.SRC_IN;
        if (num == NONE) {

            rltvWalking.setBackgroundResource(R.drawable.button_blue);
            txtWalking.setTextColor(COLOR_WHITE);
            imgWalking.setColorFilter(COLOR_WHITE, mode);

            rltvDriving.setBackgroundResource(R.drawable.button_blue);
            txtDriving.setTextColor(COLOR_WHITE);
            imgDriving.setColorFilter(COLOR_WHITE, mode);
        }


        // taghire rang relativeLayout kr havie imageView mashin va textView Marbute ast
        else if (num == DRIVING) {


            rltvWalking.setBackgroundResource(R.drawable.button_blue);
            txtWalking.setTextColor(COLOR_WHITE);
            imgWalking.setColorFilter(COLOR_WHITE, mode);

            rltvDriving.setBackgroundResource(R.drawable.button_white);
            txtDriving.setTextColor(COLOR_PRIMARY);
            imgDriving.setColorFilter(COLOR_PRIMARY, mode);
            drawRouts(DRIVING);
            transportMode=TransportMode.DRIVING;
        }

        else if(num==WALKING)
        {
            rltvWalking.setBackgroundResource(R.drawable.button_white);
            txtWalking.setTextColor(COLOR_PRIMARY);
            imgWalking.setColorFilter(COLOR_PRIMARY, mode);

            rltvDriving.setBackgroundResource(R.drawable.button_blue);
            txtDriving.setTextColor(COLOR_WHITE);
            imgDriving.setColorFilter(COLOR_WHITE, mode);
            drawRouts(WALKING);
            transportMode=TransportMode.WALKING;
        }
    }
}
