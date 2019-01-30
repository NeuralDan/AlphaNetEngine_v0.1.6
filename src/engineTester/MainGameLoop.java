package engineTester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.TexturedModel;
import objConverter.OBJFileLoader;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.*;
import models.RawModel;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainGameLoop {

    public static void main(String[] args){
        DisplayManager.createDisplay();
        Loader loader = new Loader();

        RawModel model = OBJFileLoader.loadOBJ("tree").convertToRawModel();

        TexturedModel tree = new TexturedModel(model,new ModelTexture(loader.loadTexture("tree")));

        RawModel fernModel = OBJFileLoader.loadOBJ("fern").convertToRawModel();
        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fernAtlas"));
        fernTextureAtlas.setNumberOfRows(2);
        TexturedModel fern = new TexturedModel(fernModel, fernTextureAtlas);
        fern.getTexture().setHasTransparency(true);
        fern.getTexture().setUseFakeLighting(true);

        Light light = new Light(new Vector3f(20000,20000,2000),new Vector3f(1,1,1));

        //***********TERRAIN TEXTURE STUFF***************

        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

        //***********************************************

        List<Terrain> terrains = new ArrayList<Terrain>();

        Terrain terrain1 = new Terrain(0,-1,loader, texturePack, blendMap, "heightmap");
        Terrain terrain2 = new Terrain(0,0,loader, texturePack, blendMap, "heightmap");
        terrains.add(terrain1);
        terrains.add(terrain2);

        RawModel bunnyModel = OBJFileLoader.loadOBJ("person").convertToRawModel();
        TexturedModel stanfordBunny = new TexturedModel(bunnyModel, new ModelTexture(loader.loadTexture("playerTexture")));
        stanfordBunny.getTexture().setReflectivity(1);
        stanfordBunny.getTexture().setShineDamper(10);
        Player player = new Player(stanfordBunny, new Vector3f(0, 0, 0), 0,180,0,0.5f);

        Camera camera = new Camera(player);
        MasterRenderer renderer = new MasterRenderer();

        List<Entity> entities = new ArrayList<Entity>();
        Random r = new Random();
        for(Terrain terrain : terrains){
            for(int i = 0; i < 400; i++){
                float x = r.nextInt((int) terrain.getSIZE()) + terrain.getX();
                float z = r.nextInt((int) terrain.getSIZE()) + terrain.getZ();
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(tree, new Vector3f(x,y,z), 0, r.nextFloat()*360, 0, 4));
            }
            for(int i = 0; i < 400; i++){
                float x = r.nextInt((int) terrain.getSIZE()) + terrain.getX();
                float z = r.nextInt((int) terrain.getSIZE()) + terrain.getZ();
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(fern, r.nextInt(4), new Vector3f(x,y,z), 0, r.nextFloat()*360, 0, 0.9f));
            }
        }

        List<GuiTexture> guis = new ArrayList<GuiTexture>();
        GuiTexture gui = new GuiTexture(loader.loadTexture("fernAtlas"), new Vector2f(0.5f,0.5f), new Vector2f(0.25f, 0.25f));
        guis.add(gui);

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        while(!Display.isCloseRequested()){
            for(Terrain terrain : terrains){
                if(terrain.getX() <= player.getPosition().x){
                    if(terrain.getX() + terrain.getSIZE() > player.getPosition().x){
                        if(terrain.getZ() <= player.getPosition().z){
                            if(terrain.getZ() + terrain.getSIZE() > player.getPosition().z){
                                player.move(terrain);
                            }
                        }
                    }
                }
            }
            camera.move();
            renderer.processEntity(player);
            for(Terrain terrain : terrains){
                renderer.processTerrain(terrain);
            }
            for(Entity entity:entities){
                renderer.processEntity(entity);
            }
            renderer.render(light, camera);
            guiRenderer.render(guis);
            DisplayManager.updateDisplay();
        }

        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

}
