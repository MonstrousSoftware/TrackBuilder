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
    //public boolean terrainEditMode = false;
    public float terrainEditRadius = 7f;
    public Vector3 terrainCursor = new Vector3();
    public float terrainDelta = 0.1f;
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

    public void update(float deltaTime){
        terrainEditRadius += terrainRadiusDelta;
        terrainEditRadius = Math.min(300f, Math.max(1, terrainEditRadius));
        if(leftButtonDown)
            terrain.changeHeight(terrainCursor.x,terrainCursor.z, terrainEditRadius, terrainDelta);

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



    private float terrainRadiusDelta = 0;

    @Override
    public boolean keyDown(int keycode) {
        switch(keycode){
            case Input.Keys.PAGE_UP:terrainDelta = 0.1f; break;
            case Input.Keys.PAGE_DOWN:terrainDelta = -0.1f; break;

            case Input.Keys.EQUALS:
                terrainRadiusDelta = 1;
                break;
            case Input.Keys.MINUS:
                terrainRadiusDelta = -1;
                break;
            default: return false;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode){
            case Input.Keys.EQUALS:
            case Input.Keys.MINUS:
                terrainRadiusDelta = 0;
                break;
            default: return false;
        }
        return true;
    }

    //    @Override
//    public boolean scrolled(float amountX, float amountY) {
//        return super.scrolled(amountX, amountY);
//    }
}
