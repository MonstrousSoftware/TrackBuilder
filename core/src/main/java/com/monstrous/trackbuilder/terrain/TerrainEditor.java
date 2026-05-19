package com.monstrous.trackbuilder.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class TerrainEditor extends InputAdapter {

    private final SimpleTerrain terrain;
    private Camera cam;
    public float terrainEditRadius = 10f;
    public Vector3 terrainCursor = new Vector3();
    public Vector3 startPoint = new Vector3();
    private boolean leftButtonDown = false;
    private final ShapeRenderer shapeRenderer;
    public BrushMode brushMode = BrushMode.UP_DOWN;

    public enum BrushMode {
        UP_DOWN, ERASE, FLATTEN, SMOOTH
    }

    public TerrainEditor(SimpleTerrain terrain) {
        this.terrain = terrain;
        shapeRenderer = new ShapeRenderer();
    }

    public void moveTerrainCursor(Camera cam) {
        this.cam = cam;
        Ray ray = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        terrain.intersect(ray, terrainCursor);
    }

    public void renderTerrainCursor(Camera cam) {
        this.cam = cam;
        shapeRenderer.setProjectionMatrix(cam.combined);
        shapeRenderer.identity();
        shapeRenderer.translate(terrainCursor.x,terrainCursor.y,terrainCursor.z);
        shapeRenderer.rotate(1,0,0,90);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1,0,0,1);
        shapeRenderer.circle(0,0, terrainEditRadius, 200);
        shapeRenderer.end();
    }



    private boolean growPressed = false;
    private boolean shrinkPressed = false;
    private boolean shiftPressed = false;
    private boolean controlPressed = false;

    public void update(float deltaTime){
        // pressing shift together with [ or ] changes the radius more slowly
        if(growPressed)
            terrainEditRadius += deltaTime * (shiftPressed ? 20f : 100f);
        if(shrinkPressed)
            terrainEditRadius -= deltaTime * (shiftPressed ? 20f : 100f);

        terrainEditRadius = Math.min(300f, Math.max(1, terrainEditRadius));

        // LMB increases height, shift+LMB decreases height
        if(leftButtonDown){
            switch(brushMode) {
                case UP_DOWN:   terrain.changeHeight(terrainCursor.x, terrainCursor.z, terrainEditRadius, (shiftPressed ? -10f : 10f) * deltaTime); break;
                case ERASE:     terrain.setHeight(terrainCursor.x, terrainCursor.z, terrainEditRadius, terrain.getAltitude()); break;
                case FLATTEN:   terrain.setHeight(terrainCursor.x, terrainCursor.z, terrainEditRadius, startPoint.y); break;
                case SMOOTH:    terrain.smoothHeight(terrainCursor.x, terrainCursor.z, terrainEditRadius); break;
                default:break;
            }
        }

    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // if you press shift then mouse events are passed to the camera controller
        if(button == Input.Buttons.LEFT && !controlPressed) {

            if(!leftButtonDown) {
                Ray ray = cam.getPickRay(screenX, screenY);
                terrain.intersect(ray, startPoint);
            }
            leftButtonDown = true;
            return true;    // event was processed
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT && !controlPressed) {
            leftButtonDown = false;
            return true; // event was processed
        }
        return false;
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return !controlPressed;
    }



    @Override
    public boolean keyDown(int keycode) {
        switch(keycode){
            case Input.Keys.LEFT_BRACKET:
                shrinkPressed = true;
                break;
            case Input.Keys.RIGHT_BRACKET:
                growPressed = true;
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                shiftPressed = true;
                break;
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
                controlPressed = true;
                break;
            default: return false;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode){
            case Input.Keys.LEFT_BRACKET:
                shrinkPressed = false;
                break;
            case Input.Keys.RIGHT_BRACKET:
                growPressed = false;
                break;
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                shiftPressed = false;
            case Input.Keys.ALT_LEFT:
            case Input.Keys.ALT_RIGHT:
                controlPressed = false;
                break;
            default: return false;
        }
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if(!controlPressed){
            terrainEditRadius += 5f*amountY;
            return true;    // event was processed
        }
        return false;
    }
}
