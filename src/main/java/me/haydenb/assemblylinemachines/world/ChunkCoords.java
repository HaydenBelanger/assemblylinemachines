package me.haydenb.assemblylinemachines.world;

public class ChunkCoords {
	public int dimID;
	public int posX;
	public int posZ;

	public ChunkCoords(int dim, int x, int z) {
		dimID = dim;
		posX = x;
		posZ = z;
	}
	
	@Override
	public int hashCode() {
		return dimID * posX * posZ;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ChunkCoords) {
			ChunkCoords chunk = (ChunkCoords) obj;
			return chunk.dimID == dimID && chunk.posX == posX && chunk.posZ == posZ;
		}else {
			return false;
		}
		
	}
	
	@Override
	public String toString() {
		return "[" + dimID + "]: " + posX + ", " + posZ;
	}
}