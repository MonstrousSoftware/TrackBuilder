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

    public final int mapSize;
    private FileHandle fileHandle;
    private final float[] heightFloats;


    /** Create height map from height map file (array of floats, assumed to be a square height map) */
    public HeightMap(FileHandle fileHandle) {
        this.fileHandle = fileHandle;

        long fileSize = fileHandle.length();
        long numFloats = fileSize / Float.BYTES;
        mapSize = (int)Math.sqrt((double)numFloats);


        heightFloats = new float[mapSize * mapSize];
        load(fileHandle);
    }

    /** Create empty height map of given size. (= number of vertices on each edge)
     * Size cannot exceed 128x128 because mesh indices are represented with shorts.
     * Will be saved as "temp.bin".
     * */
    public HeightMap(int mapSize) {
        if(mapSize < 2 || mapSize > 128)
            Gdx.app.error("HeightMap constructor", "size must be >= 2 and <= 128");
        this.fileHandle = Gdx.files.local("temp.bin");
        this.mapSize = mapSize;
        heightFloats = new float[mapSize * mapSize];
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
        if(r == 0) // prevent divide by zero
            return;

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

    public void setHeight(float u, float v, float radius, float height){
        int cx = Math.round(u * mapSize);
        int cz = Math.round(v * mapSize);
        cx = Math.min(cx, mapSize-1); // clamp to prevent overflow
        cz = Math.min(cz, mapSize-1);
        int r = (int)(radius * mapSize);
        if(r == 0) // prevent divide by zero
            return;

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
                    float delta = height - h;
                    heightFloats[index] = height; //+= bump(dist)*delta;
                }
            }
        }
    }

    public void smoothHeight(float u, float v, float radius){
        float strength= 0.1f;
        int cx = Math.round(u * mapSize);
        int cz = Math.round(v * mapSize);
        cx = Math.min(cx, mapSize-1); // clamp to prevent overflow
        cz = Math.min(cz, mapSize-1);
        int r = (int)(radius * mapSize);
        if(r == 0) // prevent divide by zero
            return;

        int minx = Math.max(0, cx - r);
        int maxx = Math.min(cx + r, mapSize-1);
        int minz = Math.max(0, cz - r);
        int maxz = Math.min(cz + r, mapSize-1);

        // set height to average of 3x3 kernel
        for(int x = minx; x <= maxx; x++){
            for(int z = minz; z <= maxz; z++){
                float dist = (float)Math.sqrt((x-cx)*(x-cx)+(z-cz)*(z-cz));
                if(dist<=r) {
                    int count = 0;
                    float h = 0;
                    for(int dx = -1; dx <= 1; dx++){
                        for(int dz = -1; dz <= 1; dz++){
                            if(z + dz >= minz && z + dz <= maxz && x + dx >= minx && x + dx <= maxx){
                                h += heightFloats[(z+dz) * mapSize + (x+dx)];
                                count++;
                            }
                        }
                    }
                    h /= count;
                    float ho = heightFloats[z * mapSize + x];
                    heightFloats[z * mapSize + x] = ho + strength * (h-ho);
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
        if(fileHandle != null)
            save(fileHandle);
    }

}
