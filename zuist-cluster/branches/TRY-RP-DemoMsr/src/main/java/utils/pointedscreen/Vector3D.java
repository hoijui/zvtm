package utils.pointedscreen;

public class Vector3D {

	double x, y, z;
	
	public Vector3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vector3D (Vector3D v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	
	public Vector3D add(Vector3D... vects) {
		double vx = x, vy = y, vz = z;
		for (Vector3D v : vects) {
			vx += v.x; vy += v.y; vz += v.z;
		}
		return new Vector3D(vx, vy, vz);
	}
	
	public Vector3D mul(double d) {
		return new Vector3D(d*x, d*y, d*z);
	}
	
	public double mul(Vector3D v) {
		return x*v.x + y*v.y + z*v.z;
	}
	
	public Vector3D div(double d) {
		return new Vector3D(x/d, y/d, z/d);
	}
	
	public Vector3D sub(Vector3D v) {
		return new Vector3D(x-v.x, y-v.y, z-v.z);
	}
	
	public Vector3D prod(Vector3D v) {
		return new Vector3D(y*v.z - z*v.y,  z*v.x - x*v.z,  x*v.y - y*v.x);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}
	
	
}
