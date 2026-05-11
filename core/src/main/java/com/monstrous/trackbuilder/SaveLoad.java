package com.monstrous.trackbuilder;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
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

    public static void saveTrack(FileHandle file, Array<ModelInstance> markers){
        Marker[] saveMarkers = new Marker[markers.size];
        int index = 0;
        Vector3 normalVector = new Vector3();
        for(ModelInstance marker : markers) {
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
        //FileHandle file = Gdx.files.local("saved-track.txt");
        file.writeString(str, false);
    }

    public static void loadTrack(FileHandle file, Array<ModelInstance> markers, Model blockModel){
        Array<Marker> savedMarkers;
        //FileHandle file = Gdx.files.local("saved-track.txt");
        String str = file.readString();
        Json json = new Json();
        JsonValue jsonMarkers = new JsonReader().parse(str);
        int n = jsonMarkers.size;
        savedMarkers =   json.readValue( Array.class, Marker.class, jsonMarkers);
        System.out.println(savedMarkers.size);
        Vector3 normal = new Vector3();
        markers.clear();
        for(int i = 0; i < n; i++){

            ModelInstance instance = new ModelInstance(blockModel, savedMarkers.get(i).position);
            normal.set(savedMarkers.get(i).normal);
            instance.transform.rotate(Vector3.Y, normal);
            markers.add(instance);

        }
        //buildRoad();

    }
}
