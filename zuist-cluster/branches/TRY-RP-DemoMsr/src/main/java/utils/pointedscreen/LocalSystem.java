package utils.pointedscreen;


public class LocalSystem {

	Vector3D origin, ox, oy, oz;
	
	public LocalSystem(Vector3D origin, Vector3D ox, Vector3D oy, Vector3D oz) {
		this.origin = origin;
		this.ox = ox;
		this.oy = oy;
		this.oz = oz;
	}
	
	private Vector3D localVectorToGlobal(Vector3D v) {
		return ox.mul(v.x).add(oy.mul(v.y), oz.mul(v.z));
	}

	private Vector3D localPointToGlobal(Vector3D v) {
		return origin.add(localVectorToGlobal(v));
	}
	
	public Vector3D globalVectorToLocal(Vector3D v) {
		Vector3D ox2 = oy.prod(oz), oy2 = oz.prod(ox), oz2 = ox.prod(oy);
		return new Vector3D(v.mul(ox2)/ox.mul(ox2), v.mul(oy2)/oy.mul(oy2), v.mul(oz2)/oz.mul(oz2));
	}
	
	public Vector3D globalPointToLocal(Vector3D v) {
		return globalVectorToLocal(v.sub(origin));
	}

	public Vector3D getOrigin() {
		return origin;
	}

	public Vector3D getOx() {
		return ox;
	}

	public Vector3D getOy() {
		return oy;
	}

	public Vector3D getOz() {
		return oz;
	}
}
