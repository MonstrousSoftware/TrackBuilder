package com.monstrous.trackbuilder;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
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
import com.badlogic.gdx.utils.*;

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
    public Markers controlPoints;
    public Array<Disposable> disposables;
    public Environment environment;
    private ShapeRenderer shapeRenderer;
    private CatmullRomSpline<Vector3> normalSpline; // todo
    private final Vector3[] pathPoints = new Vector3[100];	// to render spline (debug)
    private float time = 0;
    private boolean driveMode = false;
    private Model roadModel;
    private ModelInstance roadModelInstance;
    private Model centreLineModel;
    private ModelInstance centreLineModelInstance;
    private ModelInstance xyzInstance;
    private boolean wireFrameMode = false;


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

        controlPoints = new Markers();
//        xyzMarkers = new Array<>();


        ModelBuilder modelBuilder = new ModelBuilder();

        Model gridModel = modelBuilder.createLineGrid(200, 200, 1, 1,new Material(ColorAttribute.createDiffuse(Color.GRAY)),VertexAttributes.Usage.Position );
        debugInstances.add( new ModelInstance(gridModel));
        disposables.add(gridModel);

        Model xyzModel = modelBuilder.createXYZCoordinates(10, new Material(),VertexAttributes.Usage.Position|VertexAttributes.Usage.ColorPacked );
        xyzInstance =  new ModelInstance(xyzModel);
        debugInstances.add( xyzInstance );
        disposables.add(xyzModel);

//        xyzSmallModel = modelBuilder.createXYZCoordinates(3, new Material(),VertexAttributes.Usage.Position|VertexAttributes.Usage.ColorPacked );
//        disposables.add(xyzSmallModel);

        inputController = new CameraInputController(editCam);
        // disable WASD controls, because we want to use these keys for something else
        inputController.forwardKey = Input.Keys.BUTTON_A;
        inputController.backwardKey = Input.Keys.BUTTON_A;
        inputController.rotateLeftKey = Input.Keys.BUTTON_A;
        inputController.rotateRightKey = Input.Keys.BUTTON_A;
        Gdx.input.setInputProcessor(new InputMultiplexer( inputController));

        controlPoints.initMarkers();
        buildRoad(controlPoints);

        shapeRenderer = new ShapeRenderer();

        placeDriveCamera(driveCam, 0);
    }

    /** move selected marker (if any) if an arrow key is pressed */
    private void moveSelectedMarker() {
        Marker selectedMarker = controlPoints.getSelectedMarker();
        if (selectedMarker == null)
            return;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {

//            Vector3 localZ = new Vector3(selectedMarker.transform.val[Matrix4.M20], selectedMarker.transform.val[Matrix4.M21],selectedMarker.transform.val[Matrix4.M22]);
//            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
//                selectedMarker.transform.rotate(localZ, -0.1f);
//            }
//            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//                selectedMarker.transform.rotate(localZ, 1f);
//            }
//            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
//                mat.rotate(Vector3.Z, -1f);
//            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
//                mat.rotate(Vector3.Z, 1f);
//
//            if (Gdx.input.isKeyPressed(Input.Keys.UP))
//                selectedMarker.transform.rotate(Vector3.X, -1f);
//            if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
//                selectedMarker.transform.rotate(Vector3.X, 1f);
//            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
//                selectedMarker.transform.rotate(Vector3.Z, -1f);
//            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
//                selectedMarker.transform.rotate(Vector3.Z, 1f);
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.UP))
                controlPoints.moveSelectedMarker(-0.2f, 0f, 0f);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                controlPoints.moveSelectedMarker(0.2f, 0f, 0f);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                controlPoints.moveSelectedMarker(0f, 0f, -0.2f);
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                controlPoints.moveSelectedMarker(0f, 0f, 0.2f);
            if (Gdx.input.isKeyPressed(Input.Keys.PAGE_UP))
                controlPoints.moveSelectedMarker(0, 0.2f,  0f);
            if (Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN))
                controlPoints.moveSelectedMarker(0f, -0.2f, 0f);

        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.DOWN) ||
            Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyPressed(Input.Keys.PAGE_UP) || Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN) ) {

            //controlPoints.updateRenderables();
            buildRoad(controlPoints);
        }
    }

    /** recreate road model from marker positions and normals */
    private void buildRoad(Markers markers){



        buildSplineFromMarkers(markers);

        // place xyz axes per control point
        Vector3 pos = new Vector3();
        Vector3 fwd = new Vector3();


        // todo
//        xyzMarkers.clear();
//        for(ModelInstance marker : markers){
//            ModelInstance xyz = new ModelInstance(xyzSmallModel);
//            xyz.transform.set(marker.transform);
//            xyzMarkers.add(xyz);
//            marker.transform.getTranslation(pos);
//            float t = positionSpline.locate(pos);   // find advancement on spline
//            positionSpline.derivativeAt(fwd, t);    // and use that to find derivative of the marker
//            fwd.nor();
//            xyz.transform.rotate(Vector3.Z, fwd);
//            marker.transform.rotate(Vector3.Z, fwd);
//        }

        if(roadModel != null) {
            roadModel.dispose();
            centreLineModel.dispose();
        }

        roadModel = buildRoadModelFromSpline(controlPoints.getPositionSpline());
        roadModelInstance = new ModelInstance(roadModel);
        centreLineModel = buildCentreLineModelFromSpline(controlPoints.getPositionSpline());
        centreLineModelInstance =  new ModelInstance(centreLineModel);
    }


    @Override
    public void render() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1))
            driveMode = false;
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2))
            driveMode = true;
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            controlPoints.addMarker(editCam, Gdx.input.getX(), Gdx.input.getY());
            buildRoad(controlPoints);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && Gdx.input.isButtonJustPressed(Input.Buttons.LEFT))
            moveCamera(editCam, Gdx.input.getX(), Gdx.input.getY());
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            controlPoints.removeSelectedMarker();
            buildRoad(controlPoints);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            wireFrameMode = !wireFrameMode;
            buildRoad(controlPoints);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            SaveLoad.saveTrack(Gdx.files.local("saved-track.txt"), controlPoints);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            SaveLoad.loadTrack(Gdx.files.local("saved-track.txt"), controlPoints);
            buildRoad(controlPoints);
        }

        moveSelectedMarker();

        float delta = Gdx.graphics.getDeltaTime();
        time += 0.02f * delta;
        if(time > 1.0f)
            time -= 1.0f;
        inputController.update();
        placeDriveCamera(driveCam, time);

        if(!driveMode)
            controlPoints.highlightMarker(editCam, Gdx.input.getX(), Gdx.input.getY());

        ScreenUtils.clear(Color.TEAL, true);

        Camera cam = driveMode ? driveCam : editCam;

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.render(roadModelInstance, environment);
        modelBatch.render(centreLineModelInstance, environment);
        if(!driveMode) {
            controlPoints.render(modelBatch, environment);
            modelBatch.render(debugInstances, environment);
//            modelBatch.render(xyzMarkers, environment);
//            modelBatch.render(controlPoints.getMarkers(), environment);
        }
        modelBatch.end();

        if(!driveMode)
            renderSpline(cam);


        batch.begin();
        batch.draw(image, 40, 21);
        batch.end();
    }

    private void placeDriveCamera( PerspectiveCamera driveCam, float t){
        controlPoints.getPositionSpline().valueAt(driveCam.position, t);
        driveCam.position.y += 0.3f;
        normalSpline.valueAt(driveCam.up, t);
        controlPoints.getPositionSpline().derivativeAt(driveCam.direction, t);
        driveCam.update();
    }


    private void buildSplineFromMarkers(Markers markers) {
        Array<Marker> markerArray = markers.getMarkers();
        //Vector3[] controlPoints = new Vector3[markerArray.size];
        Vector3[] normalControlPoints = new Vector3[markerArray.size];

        int index = 0;
        for(Marker marker : markerArray){
            //controlPoints[index] = marker.position;
//
//            Vector3 normalVector = new Vector3(Vector3.Y);
//            normalVector.rot(marker.transform);
            normalControlPoints[index] = marker.normal;

            index++;
        }
        //positionSpline = new CatmullRomSpline<Vector3>(controlPoints, true);
        normalSpline = new CatmullRomSpline<Vector3>(normalControlPoints, true);

        // fill array of points for debug render
        for(int i = 0; i < 100; i++) {
            Vector3 out = new Vector3();
            controlPoints.getPositionSpline().valueAt(out, i/100f);
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
        int primitive = wireFrameMode ? GL20.GL_LINES : GL20.GL_TRIANGLES;
        Material material = wireFrameMode ? new Material(ColorAttribute.createDiffuse(Color.WHITE)) :
                             new Material(TextureAttribute.createDiffuse(roadTexture));
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
            meshBuilder.rect(c0, c1, c2, c3, n1);
        }
        return modelBuilder.end();

    }

    private final Vector3 tmpVec = new Vector3();

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

            normalSpline.valueAt(n1, i/(float)N);
            normalSpline.valueAt(n2, ((i+1)%N)/(float)N);

            // move p1 and p2 just above the road surface
            tmpVec.set(n1).scl(0.05f);
            p1.add(tmpVec);
            tmpVec.set(n2).scl(0.05f);
            p2.add(tmpVec);

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


    private void moveCamera(Camera cam, float screenX, float screenY){
        Ray ray = cam.getPickRay(screenX, screenY);
        Plane plane = new Plane(Vector3.Y, 0);
        Vector3 intersection = new Vector3();
        Intersector.intersectRayPlane(ray, plane, intersection);
        inputController.target.set(intersection);
        cam.direction.set(intersection).sub(cam.position).nor();
        cam.up.set(Vector3.Y);
        cam.update();
        xyzInstance.transform.setTranslation(intersection);
    }

    @Override
    public void dispose() {
        for(Disposable d : disposables)
            d.dispose();
        roadModel.dispose();
        centreLineModel.dispose();
        controlPoints.dispose();
    }
}
