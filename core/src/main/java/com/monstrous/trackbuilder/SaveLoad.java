package com.monstrous.trackbuilder;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class SaveLoad {

    private static class Marker {
        Vector3 position;
        Vector3 normal;
    }

    public static void saveTrack(FileHandle file, Markers controlPoints){
        Marker[] saveMarkers = new Marker[controlPoints.getMarkers().size];
        int index = 0;
        Vector3 normalVector = new Vector3();
        for(ModelInstance marker : controlPoints.getMarkers()) {
            Vector3 pos = new Vector3();
            marker.transform.getTranslation(pos);
            Marker m = new Marker();
            m.position = pos;

            normalVector.set(Vector3.Y);
            normalVector.rot(marker.transform);
            m.normal = new Vector3(normalVector);

            saveMarkers[index++] = m;
        }
        Json json = new Json();
        String str = json.toJson(saveMarkers);
        System.out.println(str);
        file.writeString(str, false);
    }

    public static void loadTrack(FileHandle file, Markers controlPoints){
        String str = file.readString();
        Json json = new Json();
        JsonValue jsonMarkers = new JsonReader().parse(str);
        Array<Marker> savedMarkers =   json.readValue( Array.class, Marker.class, jsonMarkers);
        controlPoints.clear();
        for(int i = 0; i < jsonMarkers.size; i++){
            controlPoints.appendMarker(savedMarkers.get(i).position, savedMarkers.get(i).normal);
        }
    }
}
