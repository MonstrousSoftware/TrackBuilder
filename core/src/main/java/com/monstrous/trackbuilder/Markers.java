package com.monstrous.trackbuilder;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/** Manages the markers (control points) that are used to define the track shape */
public class Markers implements Disposable {


    private final Array<Marker> markers;
    private Marker selectedMarker;
    private CatmullRomSpline<Vector3> positionSpline;
    private CatmullRomSpline<Vector3> normalSpline;
    private final Model blockModel;
    private final Model xyzModel;
    private final Vector3 intersection = new Vector3();
    private final Plane plane = new Plane(Vector3.Y, 0);
    private final Array<ModelInstance> cubes;
    public final Array<ModelInstance> xyzMarkers;

    public Markers() {
        markers = new Array<>();
        cubes = new Array<>();
        xyzMarkers = new Array<>();
        selectedMarker = null;
        ModelBuilder modelBuilder = new ModelBuilder();
        blockModel = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.BLUE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        xyzModel = modelBuilder.createXYZCoordinates(3, new Material(),VertexAttributes.Usage.Position|VertexAttributes.Usage.ColorPacked );

    }

    public Array<Marker> getMarkers() {
        return markers;
    }

    public Marker getSelectedMarker() {
        return selectedMarker;
    }

    public CatmullRomSpline<Vector3> getPositionSpline() {
        return positionSpline;
    }

    public CatmullRomSpline<Vector3> getNormalSpline() {
        return normalSpline;
    }

    public void clear() {
        markers.clear();
    }

    /** set a default set of markers just to get started */
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
            //ModelInstance instance = new ModelInstance(blockModel, p);
            markers.add(new Marker(p));
        }
        updateRenderables();
    }

    /** add a marker at the end of the list */
    public void appendMarker(Vector3 position, Vector3 normal) {
//        ModelInstance instance = new ModelInstance(blockModel, position);
//        instance.transform.rotate(Vector3.Y,normal);
        markers.add(new Marker(position, normal));
        updateRenderables();
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
            float SELECT_DISTANCE2 = 4f;
            if(selectedMarker.position.dst2(intersection) < SELECT_DISTANCE2){
                return;
            }
        }
        insertMarker(new Marker(intersection));
    }

    /** insert new marker at the best place in the loop, between the closest existing markers. */
    public void insertMarker(Marker newMarker){

        int closest = 0;
        float minDistance = Float.MAX_VALUE;
        Vector3 direction = new Vector3();
        Vector3 dirToNew = new Vector3();

        int index = 0;
        for(Marker marker : markers){
            Vector3 nxtPos = markers.get(index < markers.size-1 ? index+1 : 0).position;
            direction.set(nxtPos).sub(marker.position);  // vector to next marker
            dirToNew.set(newMarker.position).sub(marker.position);   // vector to new marker
            if(dirToNew.dot(direction) >= 0) {  // is new marker in front of existing marker?
                float dist = marker.position.dst(newMarker.position);
                if (dist < minDistance) {
                    minDistance = dist;
                    closest = index;
                }
            }
            index++;
        }

        markers.insert(closest+1, newMarker);
        updateRenderables();
    }

    public void removeSelectedMarker(){
        if(selectedMarker == null)
            return;
        markers.removeValue(selectedMarker, true);
        selectedMarker = null;
        updateRenderables();
    }

    public void moveSelectedMarker(float dx, float dy, float dz){
        if(selectedMarker == null)
            return;
        selectedMarker.position.add(dx, dy, dz);
        updateRenderables();
    }

    public void bankSelectedMarker(float dx){
        if(selectedMarker == null)
            return;
        selectedMarker.normal.rotate(selectedMarker.fwd, dx);
        updateRenderables();
    }


    /** highlight (select) the marker under the mouse cursor (if any) */
    public void highlightMarker(Camera cam, float screenX, float screenY){
        Ray ray = cam.getPickRay(screenX, screenY);
        float radius = 0.75f;
        for(Marker marker : markers){
            if( Intersector.intersectRaySphere(ray, marker.position, radius, intersection) ){
                if(selectedMarker == marker)    // marker is already selected, do nothing
                    return;
                // select chosen marker
                selectedMarker = marker;
                updateRenderables();
                break;
            }
        }
    }

    /** call this whenever the markers change */
    public void updateRenderables(){
        Vector3[] controlPoints = new Vector3[markers.size];
        Vector3[] normalControlPoints = new Vector3[markers.size];
        int index = 0;
        for(Marker marker : markers){
            controlPoints[index] = marker.position;
            normalControlPoints[index] = marker.normal;
            index++;
        }
        positionSpline = new CatmullRomSpline<>(controlPoints, true);
        normalSpline = new CatmullRomSpline<>(normalControlPoints, true);

        //Vector3 fwd = new Vector3();
        cubes.clear();
        xyzMarkers.clear();
        for(Marker marker : markers){
            float t = positionSpline.locate(marker.position);   // find advancement on spline
            positionSpline.derivativeAt(marker.fwd, t);    // and use that to find derivative of the marker
            marker.fwd.nor();

            ModelInstance cube = new ModelInstance(blockModel, marker.position);
            cube.materials.get(0).set( ColorAttribute.createDiffuse(marker == selectedMarker ? Color.YELLOW: Color.BLUE));
            cube.transform.rotate(Vector3.Y, marker.normal);
            cube.transform.rotate(Vector3.Z, marker.fwd);
            cubes.add(cube);
            ModelInstance xyz = new ModelInstance(xyzModel, marker.position);
            // the order is important
            xyz.transform.rotate(Vector3.Y, marker.normal);
            xyz.transform.rotate(Vector3.Z, marker.fwd);
            xyzMarkers.add(xyz);
        }
    }


    public void render(ModelBatch batch, Environment environment){
        batch.render(cubes, environment);
        batch.render(xyzMarkers, environment);
    }

    @Override
    public void dispose() {
        blockModel.dispose();
        xyzModel.dispose();
    }
}
