package com.monstrous.trackbuilder;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** Manages the markers (control points) that are used to define the track shape */
public class Markers implements Disposable {

    private final Array<ModelInstance> markers;
    private ModelInstance selectedMarker;
    private final Model blockModel;
    private final Vector3 intersection = new Vector3();
    private final Plane plane = new Plane(Vector3.Y, 0);
    private final Vector3 tmpPos = new Vector3();

    public Markers() {
        markers = new Array<>();
        selectedMarker = null;
        ModelBuilder modelBuilder = new ModelBuilder();
        blockModel = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.BLUE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
    }

    public Array<ModelInstance> getMarkers() {
        return markers;
    }

    public ModelInstance getSelectedMarker() {
        return selectedMarker;
    }

    public void clear() {
        markers.clear();
    }

    public void initMarkers() {
        float ht = 0;
        float scl = 1f;

        clear();

        Vector3[] controlPoints = {
            new Vector3(-20 * scl, ht, 20 * scl),
            new Vector3(20 * scl, ht, 25 * scl),

            new Vector3(25 * scl, ht, -30 * scl),

            new Vector3(-15 * scl, ht, -24 * scl),
            new Vector3(-50 * scl, ht, -5 * scl),

            new Vector3(-15 * scl, ht, 5 * scl),
        };
        for (Vector3 p : controlPoints) {
            ModelInstance instance = new ModelInstance(blockModel, p);
            markers.add(instance);
        }
    }

    public void appendMarker(Vector3 position, Vector3 normal) {
        ModelInstance instance = new ModelInstance(blockModel, position);
        instance.transform.rotate(Vector3.Y,normal);
        markers.add(instance);
    }

    /** add a marker in the horizontal plane at the place of the mouse cursor.
     * A marker is a control point for the spline that defines the track layout and showns
     * as a little block in edit mode. */
    public void addMarker(Camera cam, float screenX, float screenY){
        Ray ray = cam.getPickRay(screenX, screenY);
        Intersector.intersectRayPlane(ray, plane, intersection);
        // don't add a marker if too close to a selected marker
        // this to avoid multiple markers in the same position
        if(selectedMarker != null) {
            selectedMarker.transform.getTranslation(tmpPos);
            float SELECT_DISTANCE2 = 4f;
            if(tmpPos.dst2(intersection) < SELECT_DISTANCE2){
                return;
            }
        }
        ModelInstance marker = new ModelInstance(blockModel, intersection);
        insertMarker(marker);
    }

    // tries to find the best place in the loop to place the new marker
    // by looking for closest existing marker
    public void insertMarker(ModelInstance newMarker){

        int closest = 0;
        float minDistance = Float.MAX_VALUE;
        Vector3 newPos = new Vector3();
        newMarker.transform.getTranslation(newPos);
        Vector3 nxtPos = new Vector3();
        Vector3 direction = new Vector3();
        Vector3 dirToNew = new Vector3();

        int index = 0;
        for(ModelInstance marker : markers){
            marker.transform.getTranslation(tmpPos);
            markers.get(index < markers.size-1 ? index+1 : 0).transform.getTranslation(nxtPos);
            direction.set(nxtPos).sub(tmpPos);  // vector to next marker
            dirToNew.set(newPos).sub(tmpPos);   // vector to new marker
            if(dirToNew.dot(direction) >= 0) {  // is new marker in front of existing marker?
                float dist = tmpPos.dst(newPos);
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = index;
                }
            }
            index++;
        }

        markers.insert(closest+1, newMarker);
    }

    public void removeSelectedMarker(){
        if(selectedMarker == null)
            return;
        markers.removeValue(selectedMarker, true);
        selectedMarker = null;
    }


    /** highlight the marker under the mouse cursor (if any) */
    public void highlightMarker(Camera cam, float screenX, float screenY){
        Ray ray = cam.getPickRay(screenX, screenY);
        float radius = 0.75f;
        for(ModelInstance marker : markers){
            marker.transform.getTranslation(tmpPos);
            if( Intersector.intersectRaySphere(ray, tmpPos, radius, intersection) ){
                if(selectedMarker == marker)    // marker is already selected, do nothing
                    return;
                if(selectedMarker != null) // deselect previous selected marker
                    selectedMarker.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLUE));
                // select chosen marker
                selectedMarker = marker;
                selectedMarker.materials.get(0).set(ColorAttribute.createDiffuse(Color.YELLOW));
                break;
            }
        }
    }

    @Override
    public void dispose() {
        blockModel.dispose();
    }
}
