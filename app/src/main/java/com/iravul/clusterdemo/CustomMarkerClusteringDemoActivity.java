/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iravul.clusterdemo;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.iravul.clusterdemo.model.Person;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Demonstrates heavy customisation of the look of rendered clusters.
 */
public class CustomMarkerClusteringDemoActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<Person>, ClusterManager.OnClusterInfoWindowClickListener<Person>, ClusterManager.OnClusterItemClickListener<Person>, ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
    private ClusterManager<Person> mClusterManager;
    private Random mRandom = new Random(1984);

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(final Person person, final MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.

            List<Drawable> profilePhotos = new ArrayList<Drawable>();
            if(person.getBitmap()==null)
            {
                setMarkerImage(person, profilePhotos);
                Bitmap icon = mIconGenerator.makeIcon(null);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
            }
            else
            {
                Bitmap bmHalfSize = Bitmap.createScaledBitmap(person.getBitmap(), 50, 50, false);
                Drawable drawable =  new BitmapDrawable(getResources(), bmHalfSize);
                drawable.setBounds(0, 0, 40, 40);
                mClusterImageView.setImageDrawable(drawable);
                Bitmap icon = mClusterIconGenerator.makeIcon(null);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
            }
        }

        private void setMarkerImage(final Person person,  final List<Drawable> profilePhotos) {

            if(!person.photoUrl.equals(""))
            {
                Ion.with(getApplicationContext()).load(person.photoUrl).asBitmap().setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        if (result != null) {

                            Bitmap bmHalfSize = Bitmap.createScaledBitmap(result, 50, 50, false);

                            Drawable drawable = new BitmapDrawable(getResources(), bmHalfSize);
                            drawable.setBounds(0, 0, 40, 40);
                            mClusterImageView.setImageDrawable(drawable);
                            Bitmap icon = mClusterIconGenerator.makeIcon(null);

                            MarkerManager.Collection markerCollection = mClusterManager.getMarkerCollection();
                            Collection<Marker> markers = markerCollection.getMarkers();

                            if (getMarker(person) != null) {
                                getMarker(person).setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                            }
                            else
                            {
                                for (Marker m : markers) {
                                    if(person.getMarkerId()!=null)
                                    {
                                        if (person.getMarkerId().equals(m.getId())) {
                                            m.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
                                            break;
                                        }
                                    }
                                }
                            }
                            profilePhotos.add(drawable);

                        }

                    }
                });
            }

        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;

                Bitmap bmImg,bmHalfSize = null;


                if(p.getBitmap()==null)
                {
                    try {
                        bmImg = Ion.with(getApplicationContext()).load(p.photoUrl).asBitmap().get();
                        bmHalfSize = Bitmap.createScaledBitmap(bmImg, 40, 40, false);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    p.imageBitmap = bmHalfSize;
                }
                else
                {
                    bmHalfSize = p.getBitmap();
                }

                Drawable drawable =  new BitmapDrawable(getResources(), bmHalfSize);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();

        // Zoom in the cluster. Need to create LatLngBounds and including all the cluster items
        // inside of bounds, then animate to center of the bounds.

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (ClusterItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }
        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();

        // Animate camera to the bounds
        try {
            getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 9.5f));

        mClusterManager = new ClusterManager<Person>(this, getMap());
        mClusterManager.setRenderer(new PersonRenderer());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        addItems();
        mClusterManager.cluster();
    }

    private void addItems() {
        // http://www.flickr.com/photos/sdasmarchives/5036248203/
        mClusterManager.addItem(new Person(position(), "Walter", "https://c7.staticflickr.com/8/7368/27938013702_8b7b7ae0f1_n.jpg",null,""));

        // http://www.flickr.com/photos/usnationalarchives/4726917149/
        mClusterManager.addItem(new Person(position(), "Gran", "https://c5.staticflickr.com/8/7271/27925700412_41c5623727_n.jpg",null,""));

        // http://www.flickr.com/photos/nypl/3111525394/
        mClusterManager.addItem(new Person(position(), "Ruth", "https://c8.staticflickr.com/8/7286/28033223855_f28dd31e7b_n.jpg",null,""));

        // http://www.flickr.com/photos/smithsonian/2887433330/
        mClusterManager.addItem(new Person(position(), "Stefan", "https://c6.staticflickr.com/8/7576/27422296533_4a2d0dc12e_n.jpg",null,""));

        // http://www.flickr.com/photos/library_of_congress/2179915182/
        mClusterManager.addItem(new Person(position(), "Mechanic", "https://c3.staticflickr.com/8/7415/27753007970_cbb1130a73_n.jpg",null,""));

        // http://www.flickr.com/photos/nationalmediamuseum/7893552556/
        mClusterManager.addItem(new Person(position(), "Yeats", "https://c4.staticflickr.com/8/7423/27416089923_e28a7c970e_n.jpg",null,""));

        // http://www.flickr.com/photos/sdasmarchives/5036231225/
        mClusterManager.addItem(new Person(position(), "John", "https://c8.staticflickr.com/8/7554/28031171335_6aacd166ca_n.jpg",null,""));

        // http://www.flickr.com/photos/anmm_thecommons/7694202096/
        mClusterManager.addItem(new Person(position(), "Trevor the Turtle", "https://c3.staticflickr.com/8/7603/27927920322_fc9a460ac5_n.jpg",null,""));

        // http://www.flickr.com/photos/usnationalarchives/4726892651/
        mClusterManager.addItem(new Person(position(), "Teach", "https://c7.staticflickr.com/8/7340/27426740934_4e84c51164_n.jpg",null,""));
    }

    private LatLng position() {
        return new LatLng(random(51.6723432, 51.38494009999999), random(0.148271, -0.3514683));
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }
}
