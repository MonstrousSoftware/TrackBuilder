package com.monstrous.trackbuilder.terrain;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;

/** A rectangular part of the terrain */
public class TerrainElement {
    public final ModelInstance modelInstance;
    public final BoundingBox bbox;

    public TerrainElement(ModelInstance instance, BoundingBox bbox) {
        this.modelInstance = instance;
        this.bbox = bbox;
    }
}
