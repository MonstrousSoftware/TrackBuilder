package com.monstrous.trackbuilder.terrain;


import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;


public class HeightMap implements Disposable {

    public int mapSize;
    private Texture heightMapTexture;
    public Pixmap pixmap;
    private byte[] heightData;
    private float[] heightFloats;


    /** Create height map from grey scale texture file (should be 8 bits greyscale) */
    public HeightMap(FileHandle textureFile) {
        pixmap = new Pixmap(textureFile);

        // read heights into an array
        ByteBuffer bytes = pixmap.getPixels();
        int numBytes = bytes.limit();
        heightData = new byte[numBytes];
        bytes.get(heightData);
        bytes.rewind();

        // convert bytes values (0 to 255) to floats (0.0f to 1.0f)
        heightFloats = new float[numBytes];
        for(int i = 0; i < numBytes; i++) {
            int hi = heightData[i] & 0xFF;    // interpret as unsigned byte
            heightFloats[i] = hi/255f;
        }

        heightData = null;

        heightMapTexture = new Texture(pixmap, true);

        heightMapTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        heightMapTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        mapSize = heightMapTexture.getWidth();  // assumes a square
    }

    public Texture getHeightMapTexture(){
        return heightMapTexture;
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
        return heightFloats[4*(z*mapSize+x)];
//        int hi = heightData[4*(z*mapSize+x)] & 0xFF;    // interpret as unsigned byte
//        return hi/255f;
    }

    public void set(float u, float v, float h){
        int x = Math.round(u * mapSize);
        int z = Math.round(v * mapSize);
        x = Math.min(x, mapSize-1); // clamp to prevent overflow
        z = Math.min(z, mapSize-1);

        heightFloats[4*(z*mapSize+x)]  = h;
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
                    int index = 4 * (z * mapSize + x);
                    float h = heightFloats[index];
                    h += delta * (1f-dist);
                    //h = Math.min(1f, Math.max(0f, h));  // clamp
                    heightFloats[index] = h;
                }
            }
        }

    }

    @Override
    public void dispose() {
        if(heightMapTexture != null)
            heightMapTexture.dispose();
    }

}
