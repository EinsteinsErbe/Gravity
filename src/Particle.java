import java.awt.Graphics;
import java.util.ArrayList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

public class Particle extends Light{
	
	public final static float G_CONST = 1000f;

	private static ArrayList<Particle> particles;

	public float mass;
	public int diameter;
	
	public Vector3f color;

	public Vec2f pos, vel, acc;

	public boolean isAlive;

	public Particle(float x, float y, float mass) {
		super(new Vector2f(x,y),0,0,0);
		pos = new Vec2f(x, y);
		vel = new Vec2f(0, 0);
		acc = new Vec2f(0, 0);
		color = new Vector3f(1, 1, 1);
		setMass(mass);
		isAlive = true;
	}
	
	public Particle setSpeed(float x, float y){
		vel.x = x;
		vel.y = y;
		return this;
	}
	
	public Particle setColor(float x, float y, float z){
		red = mass/100*x;
		green = mass/100*y;
		blue = mass/100*z;
		return this;
	}
	
	public void updateMassAndColor(float mass){
		red *= mass/this.mass;
		green *= mass/this.mass;
		blue *= mass/this.mass;
		setMass(mass);
	}

	public void update(float delta){
		if(!isAlive){
			return;
		}
		acc.reset();
		for(Particle p : particles){
			if(p.equals(this) || !p.isAlive){
				continue;
			}
			Vec2f dis = p.pos.sub(pos);
			float d = dis.getDistance();
			if(d<(diameter+p.diameter)/2){
				if(mass > p.mass){
					eatParticle(p);
				}
				else{
					p.eatParticle(this);
				}
				
				
				continue;
			}
			dis.scale(1/d);
			acc.addScaled(dis, p.mass*G_CONST/(d*d));
		}
		vel.addScaled(acc, delta);
		pos.addScaled(vel, delta);
		location.x = pos.x;
		location.y = pos.y;
	}

	private void eatParticle(Particle p) {
		vel.scale(mass).addScaled(p.vel, p.mass);
		vel.scale(1/(mass+p.mass));
		
		red += p.red;
		green += p.green;
		blue += p.blue;
		
		setMass(mass + p.mass);
		p.delete();
	}

	private void delete() {
		isAlive = false;
	}

	public void setMass(float mass){
		this.mass = mass;
		diameter = (int) Math.sqrt(mass);
	}

	public void paint(Graphics g){
		if(isAlive){
			g.fillOval((int)(pos.x-diameter/2), (int)(pos.y-diameter/2), diameter, diameter);
		}
	}

	public static void setList(ArrayList<Particle> particles) {
		Particle.particles = particles;
	}
}
