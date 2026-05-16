package com.monstrous.trackbuilder.terrain;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;


public class HeightMap implements Disposable {

    public final int mapSize = 128;
    private final FileHandle fileHandle;
    private final float[] heightFloats;


    /** Create height map from grey scale texture file (should be 8 bits greyscale) */
    public HeightMap(FileHandle fileHandle) {
        this.fileHandle = fileHandle;


        // convert bytes values (0 to 255) to floats (0.0f to 1.0f)
        heightFloats = new float[mapSize * mapSize];
        load(fileHandle);
    }


    public void load(FileHandle file){
        byte[] bytes = file.readBytes();
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        FloatBuffer fb = bb.asFloatBuffer();
        for(int i = 0; i < fb.limit(); i++)
            heightFloats[i] = fb.get();
    }

    public void save(FileHandle file){
        ByteBuffer bb = ByteBuffer.allocate(heightFloats.length*Float.BYTES);
        for(int i = 0; i < heightFloats.length; i++)
            bb.putFloat(heightFloats[i]);
        file.writeBytes(bb.array(), false);
    }

    public int getSize(){
        return mapSize;
    }

    /** get height at position (u, v). Coordinates must be in range [0.0 to 1.0].
     * Height will be in range [-0 .. 1], scale appropriately*/
    public float get(float u, float v){
        int x = Math.round(u * mapSize);
        int z = Math.round(v * mapSize);
        x = Math.min(x, mapSize-1); // clamp to prevent overflow
        z = Math.min(z, mapSize-1);
        return heightFloats[z*mapSize+x];
    }

    public void set(float u, float v, float h){
        int x = Math.round(u * mapSize);
        int z = Math.round(v * mapSize);
        x = Math.min(x, mapSize-1); // clamp to prevent overflow
        z = Math.min(z, mapSize-1);

        heightFloats[z*mapSize+x]  = h;
    }

    public void changeHeight(float u, float v, float radius, float delta){
        int cx = Math.round(u * mapSize);
        int cz = Math.round(v * mapSize);
        cx = Math.min(cx, mapSize-1); // clamp to prevent overflow
        cz = Math.min(cz, mapSize-1);
        int r = (int)(radius * mapSize);

        int minx = Math.max(0, cx - r);
        int maxx = Math.min(cx + r, mapSize-1);
        int minz = Math.max(0, cz - r);
        int maxz = Math.min(cz + r, mapSize-1);

        for(int x = minx; x <= maxx; x++){
            for(int z = minz; z <= maxz; z++){
                float dist = (float)Math.sqrt((x-cx)*(x-cx)+(z-cz)*(z-cz));
                if(dist<=r) {
                    dist /= (float)r;
                    int index = z * mapSize + x;
                    float h = heightFloats[index];
                    h += delta * bump(dist);
                    heightFloats[index] = h;
                }
            }
        }
    }

    /** bump function, smoothly going from height 1 at distance 0 to height 0 at distance 1 */
    private float bump(float distance){
        if(distance >= 1f)
            return 0;
        return (float) Math.exp(1/(distance*distance - 1));
    }

    @Override
    public void dispose() {
        save(fileHandle);
    }

}
