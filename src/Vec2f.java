
public class Vec2f {
	
	public float x, y;

	public Vec2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vec2f add(Vec2f v){
		x += v.x;
		y += v.y;
		return this;
	}
	
	public Vec2f sub(Vec2f v){
		return new Vec2f(x-v.x, y-v.y);
	}
	
	public Vec2f scale(float s){
		x *= s;
		y *= s;
		return this;
	}
	
	public void addScaled(Vec2f v, float s){
		x += v.x*s;
		y += v.y*s;
	}
	
	public float getDistance(){
		return (float) Math.sqrt(x*x+y*y);
	}
	
	public void reset(){
		x = 0;
		y = 0;
	}
}
