package me.haydenb.assemblylinemachines.block.pipe;

import java.util.HashMap;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PipeProperties {

	public static final HashMap<Direction, EnumProperty<PipeConnOptions>> DIRECTION_BOOL = new HashMap<>();
	
	static {
		for(Direction d : Direction.values()) {
			DIRECTION_BOOL.put(d, EnumProperty.create(d.getSerializedName(), PipeConnOptions.class));
		}
	}
	
	
	public static enum PipeConnOptions implements StringRepresentable{
		NONE, PIPE, CONNECTOR;

		@Override
		public String getSerializedName() {
			return toString().toLowerCase();
		}
		
		

	}
}
