package com.monstrous.trackbuilder.terrain;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

public class GridModelBuilder {


    /** Make a Model consisting of a rectangular grid of size NxM vertices */
   public static Model makeGridModel(int N, int M, float horizontalScale, HeightMap heightMap, float amplitude, int primitive, Material material) {

        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates;
        if(primitive == GL20.GL_LINES)
            attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshBuilder meshBuilder = (MeshBuilder) modelBuilder.part("face", primitive, attr, material);
        final int numVerts = N * M;
        final int numTris = 2 * (N-1) * (M-1);
        Vector3[] vertices = new Vector3[numVerts];

        meshBuilder.ensureVertices(numVerts);
        meshBuilder.ensureTriangleIndices(numTris);

        Vector3 pos = new Vector3();
        float height;

        for (int z = 0; z < M; z++) {
            for (int x = 0; x < N; x++) {
                float wx = x/(float)N;      // scale to [0..1]
                float wz = z/(float)M;
                height =  heightMap.get(wx, wz);
                pos.set(x*horizontalScale , height*amplitude, z*horizontalScale );
                vertices[z * N + x] = new Vector3(pos);
            }
            if (z >= 1) {
                // add to index list to make a row of triangles using vertices at y and y-1
                short v0 = (short) ((z - 1) * N);    // vertex number at top left of this row
                for (short t = 0; t < N-1; t++) {
                    // counter-clockwise winding
                    meshBuilder.triangle(  v0, (short) (v0 + N), (short) (v0 + 1));
                    meshBuilder.triangle(  (short) (v0 + 1), (short) (v0 + N), (short) (v0 + N + 1));
                    v0++;                // next column
                }
            }
        }

        float uvScale = 0.1f;

        // and pass vertex to meshBuilder
        MeshPartBuilder.VertexInfo vert = new MeshPartBuilder.VertexInfo();
        vert.hasColor = false;
        vert.hasNormal = false;
        vert.hasPosition = true;
        vert.hasUV = true;

        for (int i = 0; i < numVerts; i++) {
            vert.position.set(vertices[i]);
            vert.uv.set(vert.position.x*uvScale, vert.position.z*uvScale);
            meshBuilder.vertex(vert);
        }

        return modelBuilder.end();
    }
}
