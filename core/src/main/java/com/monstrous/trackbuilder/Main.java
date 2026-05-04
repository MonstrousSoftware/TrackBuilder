package com.monstrous.trackbuilder;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    public PerspectiveCamera cam;
    public CameraInputController inputController;
    public ModelBatch modelBatch;
    public Array<ModelInstance> instances;
    public Array<Disposable> disposables;
    public Environment environment;
    private ShapeRenderer shapeRenderer;
    private CatmullRomSpline<Vector3> spline;
    private final Vector3[] pathPoints = new Vector3[100];	// to render spline (debug)


    @Override
    public void create() {
        disposables = new Array<>();

        batch = new SpriteBatch();
        image = new Texture("libgdx.png");
        disposables.add(batch);
        disposables.add(image);

        modelBatch = new ModelBatch();
        disposables.add(modelBatch);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, .4f, .4f, .4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 10f, 10f);
        cam.lookAt(0, 0, 0);
        cam.near = 0.1f;
        cam.far = 150f;
        cam.update();

        instances = new Array<>();


        ModelBuilder modelBuilder = new ModelBuilder();

        Model gridModel = modelBuilder.createLineGrid(200, 200, 1, 1,new Material(ColorAttribute.createDiffuse(Color.GRAY)),VertexAttributes.Usage.Position );
        instances.add( new ModelInstance(gridModel));
        disposables.add(gridModel);

        Model xyzModel = modelBuilder.createXYZCoordinates(10, new Material(),VertexAttributes.Usage.Position|VertexAttributes.Usage.ColorPacked );
        instances.add( new ModelInstance(xyzModel));
        disposables.add(xyzModel);

        Gdx.input.setInputProcessor(new InputMultiplexer( inputController = new CameraInputController(cam)));

        buildSpline();
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        inputController.update();

        ScreenUtils.clear(Color.TEAL, true);

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();

        renderSpline();


        batch.begin();
        batch.draw(image, 40, 21);
        batch.end();
    }


    private void buildSpline() {
        float ht = 0;
        float scl = 1f;

        Vector3[] controlPoints = {
            new Vector3(-20*scl, ht, 20*scl),
            new Vector3(20*scl, ht, 25*scl),

            new Vector3(25*scl, ht, -30*scl),

            new Vector3(-15*scl, ht, -24*scl),
            new Vector3(-50*scl, ht, -5*scl),

            new Vector3(-15*scl, ht, 5*scl),
        };
        spline = new CatmullRomSpline<Vector3>(controlPoints, true);

        ModelBuilder modelBuilder = new ModelBuilder();
        Model blockModel = modelBuilder.createBox(1f, 1f, 1f, new Material(ColorAttribute.createDiffuse(Color.BLUE)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(blockModel);
        for(Vector3 p : controlPoints){
            ModelInstance instance = new ModelInstance(blockModel, p);
            instances.add(instance);
        }

        // fill array of points for debug render
        for(int i = 0; i < 100; i++) {
            Vector3 out = new Vector3();
            spline.valueAt(out, i/100f);
            pathPoints[i] = out;
        }
    }

    // render path as a red line (debug)
    private void renderSpline() {
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1,0,0,1);
        for(int i = 0; i < 100-1; i++)
        {
            shapeRenderer.line(pathPoints[i], pathPoints[i+1]);
        }
        shapeRenderer.line(pathPoints[99], pathPoints[0]);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        for(Disposable d : disposables)
            d.dispose();
    }
}
