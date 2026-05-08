package com.monstrous.trackbuilder;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private Texture roadTexture;
    public PerspectiveCamera editCam;
    public PerspectiveCamera driveCam;
    public CameraInputController inputController;
    public ModelBatch modelBatch;
    public Array<ModelInstance> instances;
    public Array<ModelInstance> debugInstances;
    public Array<ModelInstance> markers;
    public ModelInstance selectedMarker;
    public Array<Disposable> disposables;
    public Environment environment;
    private ShapeRenderer shapeRenderer;
    private CatmullRomSpline<Vector3> positionSpline;
    private CatmullRomSpline<Vector3> normalSpline;
    private final Vector3[] pathPoints = new Vector3[100];	// to render spline (debug)
    private float time = 0;
    private boolean driveMode = false;
    private Model blockModel;
    private Model roadModel;
    private ModelInstance roadModelInstance;
    private Model centreLineModel;
    private ModelInstance centreLineModelInstance;


    @Override
    public void create() {
        disposables = new Array<>();

        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        roadTexture = new Texture("road.jpg");
        disposables.add(batch);
        disposables.add(image);

        modelBatch = new ModelBatch();
        disposables.add(modelBatch);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        editCam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        editCam.position.set(10f, 10f, 10f);
        editCam.lookAt(0, 0, 0);
        editCam.near = 0.1f;
        editCam.far = 150f;
        editCam.update();

        driveCam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        driveCam.position.set(10f, 10f, 10f);
        driveCam.lookAt(0, 0, 0);
        driveCam.near = 0.1f;
        driveCam.far = 150f;
        driveCam.update();


        instances = new Array<>();
        debugInstances = new Array<>();
        markers = new Array<>();


        ModelBuilder modelBuilder = new ModelBuilder();

        Model gridModel = modelBuilder.createLineGrid(200, 200, 1, 1,new Material(ColorAttribute.createDiffuse(Color.GRAY)),VertexAttributes.Usage.Position );
        debugInstances.add( new ModelInstance(gridModel));
        disposables.add(gridModel);

        Model xyzModel = modelBuilder.createXYZCoordinates(10, new Material(),VertexAttributes.Usage.Position|VertexAttributes.Usage.ColorPacked );
        debugInstances.add( new ModelInstance(xyzModel));
        disposables.add(xyzModel);

        blockModel = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.BLUE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(blockModel);

        Gdx.input.setInputProcessor(new InputMultiplexer( inputController = new CameraInputController(editCam)));

        initMarkers();
        buildRoad();

        shapeRenderer = new ShapeRenderer();

        placeDriveCamera(driveCam, 0);
    }

    /** move selected marker (if any) if an arrow key is pressed */
    private void moveSelectedMarker() {
        if (selectedMarker == null)
            return;
        boolean rebuild = false;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP))
                selectedMarker.transform.rotate(Vector3.X, -1f);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                selectedMarker.transform.rotate(Vector3.X, 1f);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                selectedMarker.transform.rotate(Vector3.Z, -1f);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                selectedMarker.transform.rotate(Vector3.Z, 1f);
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.UP))
                selectedMarker.transform.translate(0, 0, -.2f);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                selectedMarker.transform.translate(0, 0, .2f);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                selectedMarker.transform.translate(-.2f, 0, 0);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                selectedMarker.transform.translate(.2f, 0, 0);

        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN) ||
            Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {

            buildRoad();
        }
    }

    private void buildRoad(){
        buildSplineFromMarkers(markers);
        if(roadModel != null) {
            roadModel.dispose();
            centreLineModel.dispose();
        }

        roadModel = buildRoadModelFromSpline(positionSpline);
        roadModelInstance = new ModelInstance(roadModel);
        centreLineModel = buildCentreLineModelFromSpline(positionSpline);
        centreLineModelInstance =  new ModelInstance(centreLineModel);
    }


    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
            driveMode = false;
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
            driveMode = true;
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
            addMarker(editCam, Gdx.input.getX(), Gdx.input.getY());
        if(Gdx.input.isKeyJustPressed(Input.Keys.X))
            removeSelectedMarker();

        moveSelectedMarker();

        float delta = Gdx.graphics.getDeltaTime();
        time += 0.02f * delta;
        if(time > 1.0f)
            time -= 1.0f;
        inputController.update();
        placeDriveCamera(driveCam, time);

        if(!driveMode)
            highlightMarker(editCam, Gdx.input.getX(), Gdx.input.getY());

        ScreenUtils.clear(Color.TEAL, true);

        Camera cam = driveMode ? driveCam : editCam;

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.render(roadModelInstance, environment);
        modelBatch.render(centreLineModelInstance, environment);
        if(!driveMode) {
            modelBatch.render(debugInstances, environment);
            modelBatch.render(markers, environment);
        }
        modelBatch.end();

        if(!driveMode)
            renderSpline(cam);


        batch.begin();
        batch.draw(image, 40, 21);
        batch.end();
    }

    private void placeDriveCamera( PerspectiveCamera driveCam, float t){
        positionSpline.valueAt(driveCam.position, t);
        driveCam.position.y += 0.3f;
        normalSpline.valueAt(driveCam.up, t);
        positionSpline.derivativeAt(driveCam.direction, t);
        driveCam.update();
    }


    private void initMarkers() {
        float ht = 0;
        float scl = 1f;

        markers.clear();

        Vector3[] controlPoints = {
            new Vector3(-20 * scl, ht, 20 * scl),
            new Vector3(20 * scl, ht, 25 * scl),

            new Vector3(25 * scl, ht, -30 * scl),

            new Vector3(-15 * scl, ht, -24 * scl),
            new Vector3(-50 * scl, ht, -5 * scl),

            new Vector3(-15 * scl, ht, 5 * scl),
        };
        for (Vector3 p : controlPoints) {
            ModelInstance instance = new ModelInstance(blockModel, p);
            markers.add(instance);
        }
    }

    private void buildSplineFromMarkers(Array<ModelInstance> markers) {
        Vector3[] controlPoints = new Vector3[markers.size];
        Vector3[] normalControlPoints = new Vector3[markers.size];

        int index = 0;
        for(ModelInstance marker : markers){
            Vector3 pos = new Vector3();
            marker.transform.getTranslation(pos);
            controlPoints[index] = pos;

            Vector3 normalVector = new Vector3(Vector3.Y);
            normalVector.rot(marker.transform);
            normalControlPoints[index] = normalVector;

            index++;
        }
        positionSpline = new CatmullRomSpline<Vector3>(controlPoints, true);
        normalSpline = new CatmullRomSpline<Vector3>(normalControlPoints, true);

        // fill array of points for debug render
        for(int i = 0; i < 100; i++) {
            Vector3 out = new Vector3();
            positionSpline.valueAt(out, i/100f);
            pathPoints[i] = out;
        }
    }

    // render path as a red line (debug)
    private void renderSpline(Camera cam) {
        Vector3 normal = new Vector3();
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1,0,0,1);
        for(int i = 0; i < 100-1; i++)
        {
            shapeRenderer.line(pathPoints[i], pathPoints[i+1]);
            normalSpline.valueAt(normal, i/100f);
            normal.add(pathPoints[i]);
            shapeRenderer.line(pathPoints[i], normal);
        }
        shapeRenderer.line(pathPoints[99], pathPoints[0]);
        shapeRenderer.end();
    }


    private Model buildRoadModelFromSpline(CatmullRomSpline<Vector3> spline){
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates;
        int primitive = GL20.GL_TRIANGLES;
        Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE), TextureAttribute.createDiffuse(roadTexture));
        int N = 100;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshBuilder meshBuilder = (MeshBuilder) modelBuilder.part("road", primitive, attr, material);

        int numVerts = 4 * N;
        int numTris = 2 *N;
        meshBuilder.ensureVertices(numVerts);
        meshBuilder.ensureTriangleIndices(numTris);

        Vector3 p1 = new Vector3();
        Vector3 p2 = new Vector3();
        Vector3 n1 = new Vector3();
        Vector3 n2 = new Vector3();
        Vector3 d1 = new Vector3();
        Vector3 d2 = new Vector3();
        Vector3 c0 = new Vector3();
        Vector3 c1 = new Vector3();
        Vector3 c2 = new Vector3();
        Vector3 c3 = new Vector3();


        float width = 3f;

        for(int i = 0; i <N; i++){
            // p1 and p2 : 2 adjacent points on the spline

            spline.valueAt(p1, i/(float)N);
            spline.valueAt(p2, ((i+1)%N)/(float)N);

            normalSpline.valueAt(n1, i/(float)N);
            normalSpline.valueAt(n2, ((i+1)%N)/(float)N);

            // get derivative at both points, rotate 90 degrees to get a vector towards the side of the road
            spline.derivativeAt(d1, i/(float)N);
            d1.rotate(n1, 90).nor();
            spline.derivativeAt(d2, (i+1)/(float)N);
            d2.rotate(n2, 90).nor();

            // calculate 4 corners of a segment
            c0.set(d1).scl(-width).add(p1);
            c3.set(d1).scl(width).add(p1);
            c1.set(d2).scl(-width).add(p2);
            c2.set(d2).scl(width).add(p2);

            // add rectangle to the mesh
            meshBuilder.rect(c0, c1, c2, c3, Vector3.Y);
        }
        return modelBuilder.end();

    }

    /** create centre line marking as rectangles */
    private Model buildCentreLineModelFromSpline(CatmullRomSpline<Vector3> spline){
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked;
        int primitive = GL20.GL_TRIANGLES;
        Material material = new Material(ColorAttribute.createDiffuse(Color.LIGHT_GRAY));
        int N = 100;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshBuilder meshBuilder = (MeshBuilder) modelBuilder.part("road", primitive, attr, material);

        int numVerts = 4 * N;
        int numTris = 2 *N;
        meshBuilder.ensureVertices(numVerts);
        meshBuilder.ensureTriangleIndices(numTris);

        Vector3 p1 = new Vector3();
        Vector3 p2 = new Vector3();
        Vector3 n1 = new Vector3();
        Vector3 n2 = new Vector3();
        Vector3 d1 = new Vector3();
        Vector3 d2 = new Vector3();
        Vector3 c0 = new Vector3();
        Vector3 c1 = new Vector3();
        Vector3 c2 = new Vector3();
        Vector3 c3 = new Vector3();


        float width = 0.05f;

        for(int i = 0; i <N; i++){
            // p1 and p2 : 2 adjacent points on the spline

            spline.valueAt(p1, i/(float)N);
            spline.valueAt(p2, ((i+0.2f)%N)/(float)N);
            p1.y += 0.05f;
            p2.y += 0.05f;


            normalSpline.valueAt(n1, i/(float)N);
            normalSpline.valueAt(n2, ((i+1)%N)/(float)N);

            // get derivative at both points, rotate 90 degrees to get a vector towards the side of the road
            spline.derivativeAt(d1, i/(float)N);
            d1.rotate(n1, 90).nor();
            spline.derivativeAt(d2, (i+1)/(float)N);
            d2.rotate(n2, 90).nor();

            // calculate 4 corners of a segment
            c0.set(d1).scl(-width).add(p1);
            c3.set(d1).scl(width).add(p1);
            c1.set(d2).scl(-width).add(p2);
            c2.set(d2).scl(width).add(p2);

            // add rectangle to the mesh
            meshBuilder.rect(c0, c1, c2, c3, Vector3.Y);
        }
        return modelBuilder.end();

    }

    private final Vector3 intersection = new Vector3();
    private final Plane plane = new Plane(Vector3.Y, 0);

    /** add a marker in the horizontal plane at the place of the mouse cursor */
    // todo should insert it at the best place in the loop, not per se at the end
    private void addMarker(Camera cam, float screenX, float screenY){
        Ray ray = cam.getPickRay(screenX, screenY);
        Intersector.intersectRayPlane(ray, plane, intersection);
        // don't add a marker if too close to a selected marker
        // this to avoid multiple markers in the same position
        if(selectedMarker != null) {
            selectedMarker.transform.getTranslation(tmpPos);
            if(tmpPos.dst2(intersection) < SELECT_DISTANCE2){
                return;
            }
        }
        ModelInstance marker = new ModelInstance(blockModel, intersection);
        //markers.add(marker);
        insertMarker(marker);
        buildRoad();
    }

    // tries to find the best place in the loop to place the new marker
    // by looking for closest existing marker
    // (but should take into account the track direction)
    private void insertMarker(ModelInstance newMarker){

        int closest = 0;
        float minDistance = Float.MAX_VALUE;
        Vector3 newPos = new Vector3();
        newMarker.transform.getTranslation(newPos);

        int index = 0;
        for(ModelInstance marker : markers){
            marker.transform.getTranslation(tmpPos);
            float dist = tmpPos.dst(newPos);
            if(dist < minDistance){
                minDistance = dist;
                closest = index;
            }
            index++;
        }

        markers.insert(closest, newMarker);
    }

    private void removeSelectedMarker(){
        if(selectedMarker == null)
            return;
        markers.removeValue(selectedMarker, true);
        selectedMarker = null;

        buildRoad();
    }



    private Vector3 tmpPos = new Vector3();
    private final float SELECT_DISTANCE2 = 4f;

    /** highlight the marker under the mouse cursor (if any) */
    // note if the view is at a low angle and the marker is above ground level, the mouse cursor
    // may need to be held below the marker.
    private void highlightMarker(Camera cam, float screenX, float screenY){
        Ray ray = cam.getPickRay(screenX, screenY);
        Plane plane = new Plane(Vector3.Y, 0);

        Intersector.intersectRayPlane(ray, plane, intersection);
        for(ModelInstance marker : markers){
            marker.transform.getTranslation(tmpPos);
            intersection.y = 0;
            tmpPos.y = 0; // project to ground plane to ignore height difference
            if(tmpPos.dst2(intersection) < SELECT_DISTANCE2) {
                if(selectedMarker == marker)    // marker is already selected, do nothing
                    return;
                if(selectedMarker != null) // deselect previous selected marker
                    selectedMarker.materials.get(0).set(ColorAttribute.createDiffuse(Color.BLUE));
                // select chosen marker
                selectedMarker = marker;
                selectedMarker.materials.get(0).set(ColorAttribute.createDiffuse(Color.YELLOW));
                break;
            }
        }
    }

    @Override
    public void dispose() {
        for(Disposable d : disposables)
            d.dispose();
        roadModel.dispose();
        centreLineModel.dispose();
    }
}
