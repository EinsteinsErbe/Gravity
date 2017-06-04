
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.Vector2f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static int width;
	public static int height;

	public ArrayList<Light> lights = new ArrayList<Light>();
	public ArrayList<Block> blocks = new ArrayList<Block>();

	private int fragmentShader;
	private int shaderProgram;
	private boolean keyNotPressed = true;
	private boolean key2NotPressed = true;
	private long lastFrame;
	private long delta;
	private long TimeDelta;
	private int ticks;
	
	private Particle p;

	public static ArrayList<Particle> particles;

	Vector2f location;
	Vector2f location2 = new Vector2f();
	private boolean start = false;
/*
	private void setUpObjects() {
		int lightCount = 0 + (int) (Math.random() * 0);
		int blockCount = 5 + (int) (Math.random() * 10);

		for (int i = 1; i <= lightCount; i++) {
			Vector2f location = new Vector2f((float) Math.random() * width, (float) Math.random() * height);
			lights.add(new Light(location, (float) Math.random() * 10, (float) Math.random() * 10, (float) Math.random() * 10));
		}

		for (int i = 1; i <= blockCount; i++) {
			int width = 50;
			int height = 50;
			int x = (int) (Math.random() * (this.width - width));
			int y = (int) (Math.random() * (this.height - height));
			blocks.add(new Block(x, y, width, height));
		}
	}
*/
	private void initialize() {
		try {
			Display.setDisplayMode(Display.getDesktopDisplayMode());	
			Display.setTitle("2D Lighting");
			Display.create(new PixelFormat(0, 16, 1));
			Display.setFullscreen(true);
			width = Display.getWidth();
			height = Display.getHeight();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		shaderProgram = glCreateProgram();
		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		StringBuilder fragmentShaderSource = new StringBuilder();

		try {
			String line;
			BufferedReader reader = new BufferedReader(new FileReader("shader.frag"));
			while ((line = reader.readLine()) != null) {
				fragmentShaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		glShaderSource(fragmentShader, fragmentShaderSource);
		glCompileShader(fragmentShader);
		if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
			System.err.println("Fragment shader not compiled!");
		}

		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		glValidateProgram(shaderProgram);


		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, width, height, 0, 1, -1);
		glMatrixMode(GL_MODELVIEW);

		glEnable(GL_STENCIL_TEST);
		glClearColor(0, 0, 0, 0);
	}

	private void cleanup() {
		glDeleteShader(fragmentShader);
		glDeleteProgram(shaderProgram);
		Display.destroy();
	}
	
	public static void createParticles(){
		particles.clear();
		
		float speed = 300;

		Particle.setList(particles);
		for(int i=0; i<500; i++){
			float r = (float)Math.random();
			float g = (float)Math.random();
			particles.add(new Particle((float) (Math.random()*width), (float) (Math.random()*height), (float) (Math.random()*50))
					.setSpeed((float) (Math.random()*speed-speed/2), (float) (Math.random()*speed-speed/2))
					.setColor(r, g, 1.5f-r-g));
		}
	}

	public static void main(String[] args) {
		Main main = new Main();

		
		main.initialize();
		//main.setUpObjects();
		particles = new ArrayList<>();

		createParticles();

		while (!Display.isCloseRequested()) {
			main.printTickLine();
			main.tick();
			main.render();
		}

		main.cleanup();
		System.exit(0);
	}

	private void tick() {
		float deltaTime = delta / 1000f;
		
		if(start){
			for (Particle p : particles) {
				p.update(deltaTime);
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_RETURN)){
			start = true;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
			createParticles();
			start = false;
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			Particle max = null;
			float mass = 0;
			for (Particle p : particles) {
				if(p.mass>mass && p.isAlive){
					max = p;
					mass = p.mass;
				}
			}
			if(max != null){
				Vec2f d = new Vec2f(width/2, height/2).sub(max.pos);
				for (Particle p : particles) {
					p.pos.add(d);
				}
			}
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
			cleanup();
			System.exit(0);
		}

		location = new Vector2f(Mouse.getX(), height-Mouse.getY());

		for(Light l : particles){
			for(Block b : blocks){
				if(b.intersects(l.location)){
					l.enabled = false;
					break;
				}
				else l.enabled = true;
			}
		}
		if(Mouse.isButtonDown(0) && keyNotPressed){
			float r = (float)Math.random();
			float g = (float)Math.random();
			p = new Particle(location.x, location.y, 100)
					.setSpeed(0,0)
					.setColor(r, g, 1.5f-r-g);
			particles.add(p);
			keyNotPressed = false;
		}
		if(Mouse.isButtonDown(0)){
			p.vel.x = 0;
			p.vel.y = 0;
			p.pos.x = location.x;
			p.pos.y = location.y;
			p.location = location;
			
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
				p.updateMassAndColor(p.mass+deltaTime*500);
			}
		}
		if(!Mouse.isButtonDown(0) && !keyNotPressed){
			p.vel.x = Mouse.getDX()*10;
			p.vel.y = -Mouse.getDY()*10;
			keyNotPressed = true;
		}
		if(Mouse.isButtonDown(1) && key2NotPressed){
			int width = 50;
			int height = 50;

			blocks.add(new Block((int)location.x-25, (int)location.y-25, width, height));
			key2NotPressed = false;
		}
		if(!Mouse.isButtonDown(1)){
			key2NotPressed = true;
		}
		else{
			blocks.get(blocks.size()-1).x = (int) location.x-25;
			blocks.get(blocks.size()-1).y = (int) location.y-25;
			/*
			for(Light l : lights){
				if(blocks.get(blocks.size()-1).intersects(l.location)){
					location.x = location2.x;
					location.y = location2.y;
					break;
				}
			}

			blocks.get(blocks.size()-1).x = (int) location.x-25;
			blocks.get(blocks.size()-1).y = (int) location.y-25;
			 */
		}

		location2 = new Vector2f(location);
	}

	private void printTickLine(){
		delta = Sys.getTime() - lastFrame;
		TimeDelta +=  delta;
		lastFrame = Sys.getTime();
		ticks++;
		if(TimeDelta >= 1000){
			TimeDelta = 0;
			System.out.println(ticks+"fps with "+particles.size()+" Lights and "+blocks.size()+" Blocks");
			ticks = 0;
		}
	}
	
	private void render() {
		glClear(GL_COLOR_BUFFER_BIT);
		for (Particle light : particles) {
			if(light.enabled && light.isAlive){
				glColorMask(false, false, false, false);
				glStencilFunc(GL_ALWAYS, 1, 1);
				glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

				for (Block block : blocks) {
					Vector2f[] vertices = block.getVertices();
					for (int i = 0; i < vertices.length; i++) {
						Vector2f currentVertex = vertices[i];
						Vector2f nextVertex = vertices[(i + 1) % vertices.length];
						Vector2f edge = Vector2f.sub(nextVertex, currentVertex, null);
						Vector2f normal = new Vector2f(edge.getY(), -edge.getX());
						Vector2f lightToCurrent = Vector2f.sub(currentVertex, light.location, null);
						if (Vector2f.dot(normal, lightToCurrent) > 0) {
							Vector2f point1 = Vector2f.add(currentVertex, (Vector2f) Vector2f.sub(currentVertex, light.location, null).scale(800), null);
							Vector2f point2 = Vector2f.add(nextVertex, (Vector2f) Vector2f.sub(nextVertex, light.location, null).scale(800), null);
							glBegin(GL_QUADS); {
								glVertex2f(currentVertex.getX(), currentVertex.getY());
								glVertex2f(point1.getX(), point1.getY());
								glVertex2f(point2.getX(), point2.getY());
								glVertex2f(nextVertex.getX(), nextVertex.getY());
							} glEnd();
						}
					}
				}

				glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP);
				glStencilFunc(GL_EQUAL, 0, 1);
				glColorMask(true, true, true, true);
				
				glUseProgram(shaderProgram);
				glUniform2f(glGetUniformLocation(shaderProgram, "lightLocation"), light.location.getX(), height - light.location.getY());
				glUniform3f(glGetUniformLocation(shaderProgram, "lightColor"), light.red, light.green, light.blue);
				glEnable(GL_BLEND);
				glBlendFunc(GL_ONE, GL_ONE);

				glBegin(GL_QUADS); {
					glVertex2f(0, 0);
					glVertex2f(0, height);
					glVertex2f(width, height);
					glVertex2f(width, 0);
				} glEnd();

				glDisable(GL_BLEND);
				glUseProgram(0);
				glClear(GL_STENCIL_BUFFER_BIT);
			}
		}
		glColor3f(0, 0.1f, 0);
		for (Block block : blocks) {
			glBegin(GL_QUADS); {
				for (Vector2f vertex : block.getVertices()) {
					glVertex2f(vertex.getX(), vertex.getY());
				}
			} glEnd();
		}
		Display.update();
		Display.sync(1000);
	}
}
