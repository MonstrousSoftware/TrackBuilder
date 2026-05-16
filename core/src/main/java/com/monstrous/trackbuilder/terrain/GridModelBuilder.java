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

        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
        if(primitive == GL20.GL_LINES)
            attr |= VertexAttributes.Usage.ColorPacked;
        else
            attr |= VertexAttributes.Usage.TextureCoordinates;

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        MeshBuilder meshBuilder = (MeshBuilder) modelBuilder.part("face", primitive, attr, material);
        final int numVertices = N * M;
        final int numTris = 2 * (N-1) * (M-1);
        Vector3[] vertices = new Vector3[numVertices];
        Vector3[] normals = new Vector3[numVertices];

        meshBuilder.ensureVertices(numVertices);
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
                normals[z * N + x] = new Vector3(Vector3.Zero);
            }
            if (z >= 1) {
                // add to index list to make a row of triangles using vertices at y and y-1
                short v0 = (short) ((z - 1) * N);    // vertex number at top left of this row
                for (short t = 0; t < N-1; t++) {
                    // counter-clockwise winding
                    meshBuilder.triangle(  v0, (short) (v0 + N), (short) (v0 + 1));
                    calcNormal(vertices, normals, v0, (short) (v0 + N), (short) (v0 + 1));
                    meshBuilder.triangle(  (short) (v0 + 1), (short) (v0 + N), (short) (v0 + N + 1));
                    calcNormal(vertices, normals, (short) (v0 + 1), (short) (v0 + N), (short) (v0 + N + 1));
                    v0++;                // next column
                }
            }
        }

        float uvScale = 0.1f;

        // and pass vertex to meshBuilder
        MeshPartBuilder.VertexInfo vert = new MeshPartBuilder.VertexInfo();
        vert.hasColor = false;
        vert.hasNormal = true;
        vert.hasPosition = true;
        vert.hasUV = true;

        for (int i = 0; i < numVertices; i++) {
            vert.position.set(vertices[i]);
            vert.uv.set(vert.position.x*uvScale, vert.position.z*uvScale);
            vert.normal.set(normals[i]).nor();
            meshBuilder.vertex(vert);
        }

        return modelBuilder.end();
    }

    /*
     * Calculate the normal
     */
    private static Vector3 u = new Vector3();
    private static Vector3 v = new Vector3();
    private static Vector3 n = new Vector3();

    private static void calcNormal(final Vector3[] vertices, Vector3[] normals, short v0, short v1, short v2) {

        final Vector3 p0 = vertices[v0];
        final Vector3 p1 = vertices[v1];
        final Vector3 p2 = vertices[v2];

        v.set(p2).sub(p1);
        u.set(p0).sub(p1);
        n.set(v).crs(u).nor();

        normals[v0].add(n);
        normals[v1].add(n);
        normals[v2].add(n);
    }
}
