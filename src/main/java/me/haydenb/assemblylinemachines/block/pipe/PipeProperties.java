package me.haydenb.assemblylinemachines.block.pipe;

import java.util.HashMap;

import net.minecraft.state.EnumProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;

public class PipeProperties {

	public static final HashMap<Direction, EnumProperty<PipeConnOptions>> DIRECTION_BOOL = new HashMap<>();
	
	static {
		for(Direction d : Direction.values()) {
			DIRECTION_BOOL.put(d, EnumProperty.create(d.getName().toLowerCase(), PipeConnOptions.class));
		}
	}
	
	
	public static enum PipeConnOptions implements IStringSerializable{
		NONE, PIPE, CONNECTOR;

		@Override
		public String getName() {
			return toString().toLowerCase();
		}
	}
}
