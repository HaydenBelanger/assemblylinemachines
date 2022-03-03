package me.haydenb.assemblylinemachines.plugins;

public class PluginTOP {
	/*
	private static boolean registered = false;
	private static final boolean TOP_SUPPORTED = true;
	
	
	public static void register() {
		if(!registered) {
			registered = true;
			if(TOP_SUPPORTED && ModList.get().isLoaded("theoneprobe")) {
				InterModComms.sendTo("theoneprobe", "getTheOneProbe", PluginTOPRegistry::new);
			}
		}
	}
	
	static class PluginTOPRegistry implements Function<ITheOneProbe, Void>{
		
		@Override
		public Void apply(ITheOneProbe probe) {
			AssemblyLineMachines.LOGGER.info("TOP plugin for Assembly Line Machines loaded.");
			
			probe.registerProvider(new IProbeInfoProvider() {
				
				@Override
				public ResourceLocation getID() {
					return new ResourceLocation(AssemblyLineMachines.MODID, "top");
				}
				
				@Override
				public void addProbeInfo(ProbeMode mode, IProbeInfo info, Player player, Level world, BlockState state, IProbeHitData data) {
					
					if(world.getBlockEntity(data.getPos()) instanceof TOPProvider) {
						((TOPProvider) world.getBlockEntity(data.getPos())).addProbeInfo(mode, info, player, world, state, data);
					}else if(state.getBlock() instanceof TOPProvider) {
						((TOPProvider) state.getBlock()).addProbeInfo(mode, info, player, world, state, data);
					}
					
				}
			});
			
			return null;
		}
	}
	
	public static interface TOPProvider{
		public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level world, BlockState state, IProbeHitData data);
	}
	
	*/
	
}
