package com.monstrous.trackbuilder.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.monstrous.trackbuilder.terrain.SimpleTerrain;
import com.monstrous.trackbuilder.terrain.TerrainEditor;

public class TerrainWindow extends Window {
    private final SimpleTerrain terrain;
    private final TerrainEditor terrainEditor;
    public boolean showTerrain = true;
    public boolean showWireFrame = false;
    private Slider radiusSlider;
    private Label radiusLabel;
    private Label ampLabel;
    private float amplitude;


    public TerrainWindow(SimpleTerrain terrain, TerrainEditor terrainEditor, Skin skin) {
        super("Terrain", skin);
        this.terrain = terrain;
        this.terrainEditor = terrainEditor;
        build(skin);
    }

    private void build(Skin skin){
        Table controls = new Table();
        controls.left();

        final CheckBox terrainCheckbox = new CheckBox("show terrain", skin);
        terrainCheckbox.setChecked(showTerrain);
        terrainCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showTerrain = terrainCheckbox.isChecked();
            }
        });
        controls.add(terrainCheckbox).left().row();




        final CheckBox linesCheckbox = new CheckBox("terrain wire frame", skin);
        linesCheckbox.setChecked(showWireFrame);
        linesCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showWireFrame = linesCheckbox.isChecked();
                terrain.setWireFrameMode(showWireFrame);
                terrain.generateBlock(terrain.heightMap);
            }
        });
        controls.add(linesCheckbox).left().row();


        // amplitude
        amplitude = terrain.getAmplitude();
        final Slider ampSlider = new Slider(0f, 100f, 1f, false, skin);
        ampSlider.setAnimateDuration(0.1f);
        ampSlider.setValue(amplitude);
        ampSlider.setSize(150, 20);
        controls.add(new Label("terrain amplitude", skin));
        controls.add(ampSlider);

        ampLabel = new Label(String.valueOf(amplitude), skin);
        controls.add(ampLabel).row();
        ampSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                amplitude = ampSlider.getValue();
                ampLabel.setText(String.valueOf((int)amplitude));
                terrain.setAmplitude(amplitude);
                //main.generateVegetation(terrain);

            }
        });


        // altitude
        final Slider altSlider = new Slider(-100f, 100f, 1f, false, skin);
        altSlider.setAnimateDuration(0.1f);
        altSlider.setValue(terrain.getAltitude());
        altSlider.setSize(150, 20);
        controls.add(new Label("terrain altitude", skin));
        controls.add(altSlider);

        Label altLabel = new Label(String.valueOf((int) terrain.getAltitude()), skin);
        controls.add(altLabel).row();
        altSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                terrain.setAltitude(altSlider.getValue());
                altLabel.setText(String.valueOf((int) terrain.getAltitude()));
            }
        });


        // edit radius
        radiusSlider = new Slider(1f, 300f, 1f, false, skin);
        radiusSlider.setAnimateDuration(0.1f);
        radiusSlider.setValue(terrainEditor.terrainEditRadius);
        radiusSlider.setSize(150, 20);
        controls.add(new Label("edit radius", skin));
        controls.add(radiusSlider);
        radiusLabel = new Label(String.valueOf((int) terrainEditor.terrainEditRadius), skin);
        controls.add(radiusLabel).row();
        radiusSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                terrainEditor.terrainEditRadius = (radiusSlider.getValue());
                radiusLabel.setText(String.valueOf((int)  terrainEditor.terrainEditRadius));
            }
        });

        add(controls);
        pack();
    }

    public void update(){
        radiusLabel.setText(String.valueOf((int)  terrainEditor.terrainEditRadius));
        radiusSlider.setValue(terrainEditor.terrainEditRadius);
    }


}
