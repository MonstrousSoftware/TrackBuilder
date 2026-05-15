package com.monstrous.trackbuilder.terrain;



import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

// BROKEN?

public class HeightMapGenerated implements HeightMap, Disposable {
    final int PERLIN_GRID_SIZE = 16;

    public int mapSize;
    private float[][] heightMap;
    private Noise noise;
    private Texture heightMapTexture;
    private Pixmap pixmap;


    /** Create height map using Perlin noise */
    public HeightMapGenerated(int mapSize) {
        this.mapSize = mapSize;
        noise = new Noise();
        // generate a noise map
        heightMap = noise.generateSmoothedPerlinMap(mapSize, mapSize, 0,0, PERLIN_GRID_SIZE);
    }

    @Override
    public int getSize(){
        return mapSize;
    }

    public Texture getHeightMapTexture(){
        // create on demand
        if(heightMapTexture == null){
            // copy to a texture (for debug)
            pixmap = noise.generatePixmap(heightMap, mapSize);

            heightMapTexture = new Texture(pixmap);
            heightMapTexture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
            heightMapTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        return heightMapTexture;
    }

    /** get height at position (wx, wz). Coordinates must be in range [0.0 to 1.0]. */
    public float get(float wx, float wz){
        int x = Math.round(wx * mapSize);
        int z = Math.round(wz * mapSize);

        return heightMap[z][x];
    }

    public void set(float u, float v, float h){
//        int x = Math.round(u * mapSize);
//        int z = Math.round(v * mapSize);
//        x = Math.min(x, mapSize-1); // clamp to prevent overflow
//        z = Math.min(z, mapSize-1);
//
//        heightData[4*(z*mapSize+x)]  = (byte)(h * 255f);
    }

    @Override
    public void dispose() {
        if(heightMapTexture != null)
            heightMapTexture.dispose();
    }

}
