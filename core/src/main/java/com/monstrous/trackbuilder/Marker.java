package com.monstrous.trackbuilder;

import com.badlogic.gdx.math.Vector3;

public class Marker {
    public Vector3 position;
    public Vector3 normal;
    public Vector3 fwd;

    public Marker() {   // no args constructor for serialization

    }

    public Marker(Vector3 position, Vector3 normal) {
        this.position = new Vector3(position);
        this.normal = new Vector3(normal);
        this.fwd = new Vector3();
    }

    public Marker(Vector3 position) {
        this.position = new Vector3(position);
        this.normal = new Vector3(Vector3.Y);
        this.fwd = new Vector3();
    }
}
