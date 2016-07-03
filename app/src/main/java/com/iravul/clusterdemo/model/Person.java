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

package com.iravul.clusterdemo.model;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Person implements ClusterItem {
    public String name;
    public String photoUrl;
    public LatLng mPosition;
    public Bitmap imageBitmap;
    public String markerId;
    public Person(LatLng position, String name, String photoUrl, Bitmap bitmap, String markerId) {
        this.name = name;
        mPosition = position;
        this.photoUrl = photoUrl;
        imageBitmap = bitmap;
        this.markerId = markerId;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public Bitmap getBitmap() {
        return imageBitmap;
    }

    public String getMarkerId() {
        return markerId;
    }
}
