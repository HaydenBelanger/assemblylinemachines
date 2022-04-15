package me.haydenb.assemblylinemachines.registry.utils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.MethodUtils;

import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.RegisterableMachine;
import me.haydenb.assemblylinemachines.block.helpers.MachineBuilder.RegisterableMachine.Phases;
import me.haydenb.assemblylinemachines.block.machines.BlockMachines;
import me.haydenb.assemblylinemachines.registry.Registry;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PhasedMap<K, V> extends ConcurrentHashMap<K, V>{

	private static final long serialVersionUID = -1346052825976440901L;
	
	private final Phases phase;
	
	public PhasedMap(Phases phase) {
		this.phase = phase;
	}
	
	public PhasedMap<K, V> add() {
		List<Method> methods = MethodUtils.getMethodsListWithAnnotation(BlockMachines.class, RegisterableMachine.class);
		methods.removeIf((m) -> m.getAnnotation(RegisterableMachine.class).phase() != phase);
		Consumer<Method> typedConsumer = getTypedConsumer(phase);
		methods.forEach((m) -> typedConsumer.accept(m));
		return this;
	}
	
	private static Consumer<Method> getTypedConsumer(Phases phase){
		return switch(phase) {
		case BLOCK -> (m) -> {
			try {
				Registry.createBlock(m.getAnnotation(RegisterableMachine.class).blockName(), (Block) m.invoke(null), true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		case BLOCK_ENTITY -> (m) -> {
			try {
				Registry.createBlockEntity(m.getAnnotation(RegisterableMachine.class).blockName(), (BlockEntityType<?>) m.invoke(null));
			}catch(Exception e) {
				e.printStackTrace();
			}
		};
		case CONTAINER -> (m) -> {
			try {
				Registry.createContainer(m.getAnnotation(RegisterableMachine.class).blockName(), (MenuType<?>) m.invoke(null));
			}catch(Exception e) {
				e.printStackTrace();
			}
		};
		case SCREEN -> (m) -> {
			try {
				m.invoke(null);
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		};
		};
		
	}
}