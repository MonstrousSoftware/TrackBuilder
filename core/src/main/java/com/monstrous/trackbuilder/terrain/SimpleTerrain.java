package com.monstrous.trackbuilder.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class SimpleTerrain implements Disposable {
    private final ModelBatch terrainBatch;
    public float tileSize;    // size of one grid tile in world units
    private boolean wireFrameMode;
    public int gridSize;
    public float worldSize;   // world size of terrain, centered on the origin
    public HeightMap heightMap;
    public final Array<TerrainElement> elements = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private Model squareMxM;
    private final Vector3 focus = new Vector3();
    public boolean frustumCulling = false;
    private float amplitude;
    //private float scale;        // world scale of one height map tile (not clip map tile)
    private float[] vertexData;
    private short[] indexData;
    private int vertexSize; // in floats
    private final Vector3 position = new Vector3();
    private float altitude = -30f;   // world height of terrain base

    /** Construct terrain.
     * @param fileHandle file handle for a (square) noise texture file
     * @param amplitude multiplier for height
     * @param tileSize size of a single tile in world units
     */
    public SimpleTerrain(FileHandle fileHandle, float amplitude, float tileSize) {
        this.tileSize = tileSize;   // world size per height map texel, i.e. per grid cell
        this.amplitude = amplitude;
        this.wireFrameMode = false;

        heightMap = new HeightMap(fileHandle);
        this.gridSize = heightMap.getSize();    // get grid size from texture size, assumed to be square

        generateBlock(heightMap);
        buildTerrain();
        if(!wireFrameMode)
            getCollisionDetectionTriangles();
        terrainBatch = new ModelBatch();
    }

    public void setWireFrameMode(boolean mode){
        this.wireFrameMode = mode;
    }

    public float getHeight(float worldX, float worldZ){
        float worldSize = heightMap.getSize() * tileSize;
        // scale [-0.5*worldSize .. 0.5*worldSize] to [0 .. 1]
        float u = (worldX / worldSize) + 0.5f;
        float v = (worldZ / worldSize) + 0.5f;
        if(u < 0 || u > 1f || v < 0 || v > 1f)
            return 0;
        return amplitude * heightMap.get(u, v);
    }

    public void changeHeight(float x, float z, float radius, float delta){
        float worldSize = heightMap.getSize() * tileSize;
        // scale [-0.5*worldSize .. 0.5*worldSize] to [0 .. 1]
        float u = (x / worldSize) + 0.5f;
        float v = (z / worldSize) + 0.5f;
        if(u < 0 || u > 1f || v < 0 || v > 1f)
            return;
        heightMap.changeHeight(u,v, radius/worldSize, delta);

        // rebuild mesh
        boolean w = wireFrameMode;
        wireFrameMode = false;
        generateBlock(heightMap);   // generate in triangle mode for the sake of col det triangles
        getCollisionDetectionTriangles();
        if(w) {
            wireFrameMode = w;
            generateBlock(heightMap);
        }
        buildTerrain();
    }

    public void setHeight(float x, float z, float radius, float height){
        float worldSize = heightMap.getSize() * tileSize;
        // scale [-0.5*worldSize .. 0.5*worldSize] to [0 .. 1]
        float u = (x / worldSize) + 0.5f;
        float v = (z / worldSize) + 0.5f;
        if(u < 0 || u > 1f || v < 0 || v > 1f)
            return;
        heightMap.setHeight(u,v, radius/worldSize, (height-altitude)/amplitude);

        // rebuild mesh
        boolean w = wireFrameMode;
        wireFrameMode = false;
        generateBlock(heightMap);   // generate in triangle mode for the sake of col det triangles
        getCollisionDetectionTriangles();
        if(w) {
            wireFrameMode = w;
            generateBlock(heightMap);
        }
        buildTerrain();
    }

    /** set terrain amplitude, i.e. height multiplication factor */
    public void setAmplitude(float amplitude){
        this.amplitude = amplitude;
        //terrainShader.setAmplitude(amplitude);
        generateBlock(heightMap);
    }

    public float getAmplitude() {
        return amplitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
        Vector3 pos = new Vector3();
        for(TerrainElement el : elements){
            el.modelInstance.transform.getTranslation(pos);
            pos.y = altitude;
            el.modelInstance.transform.setTranslation(pos);
        }
    }

    public void setScale(float scale) {
        this.tileSize = scale;
    }

    public float getScale() {
        return tileSize;
    }



    /** Generate terrain building block models. This can be called to change the appearance (e.g. wire frame mode).
     *
     */
    public void generateBlock(HeightMap heightMap){
        instances.clear();
        disposeBlocks();
        final int M = gridSize;
        final int primitive = wireFrameMode ? GL20.GL_LINES : GL20.GL_TRIANGLES;


        Texture diffuseTexture  = new Texture(Gdx.files.internal("textures/sand.png"), true);
        diffuseTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearNearest);
        diffuseTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Material mat = new Material(
                ColorAttribute.createDiffuse(Color.WHITE)
             );

        if(!wireFrameMode)
            mat.set(TextureAttribute.createDiffuse(diffuseTexture));

        // vertex positions range is [0..M][0..M]
        squareMxM = GridModelBuilder.makeGridModel( M, M, tileSize, heightMap, amplitude, primitive, mat);

    }


    private void getCollisionDetectionTriangles(){
        int nv = squareMxM.meshes.first().getNumVertices();
        int stride = squareMxM.meshes.first().getVertexSize();  // in bytes
        vertexSize = stride/Float.BYTES;  // from bytes to floats
        vertexData = new float[vertexSize * nv ];
        squareMxM.meshes.first().getVertices(vertexData);
        int ni = squareMxM.meshes.first().getNumIndices();
        indexData = new short[ni];
        squareMxM.meshes.first().getIndices(indexData);
    }

    public boolean intersect(Ray ray, Vector3 intersection ) {
        ray.origin.sub(position);  // make ray relative to terrain space
        boolean hit = Intersector.intersectRayTriangles(ray, vertexData, indexData, vertexSize, intersection);
        intersection.add(position); // convert local terrain coordinate to world coordinate
        return hit;
    }

    /** Enable frustum culling for better performance */
    public void setCulling(boolean culling){
        frustumCulling = culling;
    }




    private final Vector3 previousCameraPosition = new Vector3();

    /** update terrain to have the highest level of detail near the focal instance and perform frustum clipping */
    public void update(Camera camera){
        this.focus.set(camera.position);
        // rebuild terrain if focal point has moved
        if(instances.isEmpty() || focus.dst2(previousCameraPosition) > 0.1f)
            buildTerrain();

        // build list of visible model instances
        // (camera may be in same position but rotated)
        instances.clear();
        if (frustumCulling) {
            for (TerrainElement element : elements) {
                if (camera.frustum.boundsInFrustum(element.bbox)) {
                    instances.add(element.modelInstance);
                }
            }
        } else {
            for (TerrainElement element : elements)
                instances.add(element.modelInstance);
        }

        previousCameraPosition.set(focus);
    }

    public int getNumInstances(){
        return instances.size;
    }

    public void render(Camera cam, Environment environment) {
        terrainBatch.begin(cam);
        terrainBatch.render(instances, environment);
        terrainBatch.end();
    }


    private void buildTerrain(){
        elements.clear();
        position.set(-gridSize*tileSize/2f, altitude, -gridSize*tileSize/2f);
        addSquare(elements, squareMxM, 1f, gridSize, gridSize, position,  0, 0);
    }





    private final Vector3 min = new Vector3();
    private final Vector3 max = new Vector3();

    /** add a terrain element
     * xo,zo: position of level (bottom left corner)
     * x,z: position of this element (in tiles)
     * */
    private void addSquare(Array<TerrainElement> elements, Model model, float scale, int w, int h, Vector3 origin, int x, int z){
        ModelInstance instance = new ModelInstance(model);
        instance.transform.translate(origin.x + x * scale, origin.y, origin.z + z*scale);
        instance.transform.scale(scale, 1f, scale);
        BoundingBox bbox = new BoundingBox();

        min.set(origin.x + x * scale, origin.y, origin.z + z*scale);
        max.set(min);
        max.add(scale * (w-1), amplitude, scale*(h-1));
        bbox.set(min, max);
        elements.add(new TerrainElement(instance, bbox));
    }

    @Override
    public void dispose() {
        heightMap.dispose();
        disposeBlocks();
        terrainBatch.dispose();
    }

    private void disposeBlocks() {
        if(squareMxM == null)
            return;
        squareMxM.dispose();
        squareMxM = null;
    }
}
