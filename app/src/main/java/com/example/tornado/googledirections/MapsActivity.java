package com.example.tornado.googledirections;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

import wiadevelopers.com.directionlib.DirectionCallback;
import wiadevelopers.com.directionlib.GoogleDirection;
import wiadevelopers.com.directionlib.constant.AvoidType;
import wiadevelopers.com.directionlib.constant.TransportMode;
import wiadevelopers.com.directionlib.constant.Unit;
import wiadevelopers.com.directionlib.model.Direction;
import wiadevelopers.com.directionlib.model.RouteInfo;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnRequest;


   //afzudane lat va lang chand shahr
    private LatLng mIRAN= new LatLng(32.2712623,51.2585412);
    private LatLng mBirjand= new LatLng(32.0866534,59.223840);
    private LatLng mTehran= new LatLng(35.6942502,51.3835454);

    //afzudane routeInfo
    private ArrayList<RouteInfo> routeInfos=new ArrayList<>();

    //jahat tag dadan be routha(selcted route)
    private ArrayList<Polyline> polylineArrayList=new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btnRequest=(Button)findViewById(R.id.btnRequest);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


         // move kardan durbin ruye iran                       //5=zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mIRAN,5));

        //hatman baad az onMapReady bashad.
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //farakhani tabee zir
                directionRequest();
            }
        });


        //click bar roye route
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline)
            {
                 //greftan get tag ke dar tabeye DirectConnection zir anra setTag kardim
                String tag=polyline.getTag().toString();

                int index=Integer.parseInt(tag);

                /*
                Toast.makeText(MapsActivity.this, tag, Toast.LENGTH_SHORT).show();

                for(int i=0 ; i<polylineArrayList.size() ; i++)
                {
                    if(i==index)
                    //rang route entekhab shode ra abi mikonad
                   polylineArrayList.get(i).setColor(Direction.SELECTED_ROUTE);

                    else
                        polylineArrayList.get(i).setColor(Direction.UNSELECTED_ROUTE);
                }
               */
                  for(int i=0 ; i<polylineArrayList.size() ; i++)

                      //pak kardan routha
                      polylineArrayList.get(i).remove();
                      polylineArrayList.clear();

                for(int i=0 ; i<routeInfos.size(); i++)
                {

                    //routhai ke click nashodean be sourat zir rasm kon
                    //agar routha anhay nabudand ke royeshan click shode bud
                    if(i != index){
                        final Polyline mPolyline= mMap.addPolyline(routeInfos.get(i).getPolylineOptions());
                        mPolyline.setTag(i);
                        mPolyline.setColor(Direction.UNSELECTED_ROUTE);
                        polylineArrayList.add(mPolyline);
                    }

                }

                // route entekhab shode be range entekhab shode bashad
                final Polyline mPolyline= mMap.addPolyline(routeInfos.get(index).getPolylineOptions());
                mPolyline.setTag(index);
                mPolyline.setColor(Direction.SELECTED_ROUTE);
                polylineArrayList.add(mPolyline);


                //jahat namayesh zaman va masafat route bar hasbe transportMode(walking or driving or bicycling)
               String duration=routeInfos.get(index).getDurationText();
               String distance=routeInfos.get(index).getDistanceText();

              //  int durationValue = routeInfos.get(index).getDurationValue(); //bar hasbe saniye
               // int distanceValue = routeInfos.get(index).getDistanceValue(); //bar hasb meter

                Toast.makeText(MapsActivity.this,duration+ "\n" + distance, Toast.LENGTH_SHORT).show();

            }
        });




    }

  private void directionRequest()

  {

      // jahat obur az masirhaye motafavet ta residan be maghsad
      LatLng mMashad=new LatLng(36.303169,59.561997);
      LatLng mQom=new LatLng(34.682291,50.880894);

      List<LatLng> wayPoints=new ArrayList<>();

      wayPoints.add(mQom);
      wayPoints.add(mMashad);


                                    // key ke qablan sakhtim(value/google_maps_api) //class Constant.API_KEY
      GoogleDirection.withServerKey(Constant.API_KEY)

              .from(mBirjand)
              .to(mTehran)

              //namayesh behtarin masir
            .optimizeWaypoints(true)

              //pass az shahrha
          //    .waypoints(wayPoints)

              //routhaye jaygozin
              .alternativeRoute(true)

              //highway & tolls ra nadide migirad
            //  .avoid(AvoidType.HIGHWAYS)
             // .avoid(AvoidType.TOLLS)

                // betor pishfarz roye halate metric ast
              //.unit(Unit.IMPERIAL)


              //besyrete Pishfarz roye driving ast
             .transportMode(TransportMode.DRIVING)

              .execute(new DirectionCallback() {
                  @Override
                  public void onDirectionSuccess(Direction direction, String s)
                  {
                     if(direction.isOK())
                     {
                         Toast.makeText(MapsActivity.this, "ok", Toast.LENGTH_SHORT).show();

                                                                                 //5=width routh
                         routeInfos=direction.getRouteInfo(getApplicationContext(),5);
                         for(int i=0 ; i<routeInfos.size() ; i++)
                         {

                          //jahat tag dadan be routha va hameye routha ra dakhele object polyline gharar midahim
                          final Polyline polyline =mMap.addPolyline(routeInfos.get(i).getPolylineOptions());
                             polyline.setTag(i);

                             //hameye routha dahkele ArrayList gharar dadim
                             polylineArrayList.add(polyline);
                         }

                     }else{
                         Toast.makeText(MapsActivity.this, direction.getErrorMessage(), Toast.LENGTH_SHORT).show();
                     }
                  }

                  @Override
                  public void onDirectionFailure(Throwable throwable) {
                      Toast.makeText(MapsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                  }
              });
  }

}
