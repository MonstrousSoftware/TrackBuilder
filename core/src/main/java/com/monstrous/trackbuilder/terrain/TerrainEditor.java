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
    public float terrainEditRadius = 10f;
    public Vector3 terrainCursor = new Vector3();
    private boolean leftButtonDown = false;
    private final ShapeRenderer shapeRenderer;

    public TerrainEditor(SimpleTerrain terrain) {
        this.terrain = terrain;
        shapeRenderer = new ShapeRenderer();
    }

    public void moveTerrainCursor(Camera cam) {
        Ray ray = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        terrain.intersect(ray, terrainCursor);
    }

    public void renderTerrainCursor(Camera cam) {
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

    public void update(float deltaTime){
        // pressing shift together with [ or ] changes the radius more slowly
        if(growPressed)
            terrainEditRadius += deltaTime * (shiftPressed ? 20f : 100f);
        if(shrinkPressed)
            terrainEditRadius -= deltaTime * (shiftPressed ? 20f : 100f);

        terrainEditRadius = Math.min(300f, Math.max(1, terrainEditRadius));

        // LMB increases height, shift+LMB decreases height
        if(leftButtonDown)
            terrain.changeHeight(terrainCursor.x,terrainCursor.z, terrainEditRadius, (shiftPressed ? - 10f : 10f) * deltaTime);

    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT)
            leftButtonDown = true;
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT)
            leftButtonDown = false;
        return false;
    }

;

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
            default: return false;
        }
        return true;
    }

    //    @Override
//    public boolean scrolled(float amountX, float amountY) {
//        return super.scrolled(amountX, amountY);
//    }
}
