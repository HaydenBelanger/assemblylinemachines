package me.haydenb.assemblylinemachines.world;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.haydenb.assemblylinemachines.AssemblyLineMachines;
import me.haydenb.assemblylinemachines.item.ItemCreativeUpgradeKit;
import me.haydenb.assemblylinemachines.plugins.PluginPatchouli;
import me.haydenb.assemblylinemachines.registry.utils.FormattingHelper;
import me.haydenb.assemblylinemachines.registry.utils.Utils;
import me.haydenb.assemblylinemachines.world.CapabilityChunkFluids.IChunkFluidCapability;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.ForgeRegistries;

@EventBusSubscriber(bus = Bus.FORGE, modid = AssemblyLineMachines.MODID)
public class ModCommand {

	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("assemblylinemachines").requires((css) -> css.hasPermission(2))
				.then(Commands.literal("guide").executes(ModCommand::giveGuide)
						.then(Commands.argument("targets", EntityArgument.player()).executes(ModCommand::giveGuide)))
				.then(Commands.literal("makecreative")
					.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ModCommand::makeCreative)))
				.then(Commands.literal("chunkfluid")
						.then(Commands.literal("remove").executes(ModCommand::removeFluid)
								.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ModCommand::removeFluid)))
						.then(Commands.literal("set")
								.then(Commands.argument("fluid", FluidArgument.fluid()).executes(ModCommand::setFluid)
										.then(Commands.argument("amount", IntegerArgumentType.integer()).executes(ModCommand::setFluid)
												.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ModCommand::setFluid)))))
						.then(Commands.literal("get").executes(ModCommand::fluidInChunk)
								.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(ModCommand::fluidInChunk)))));
	}

	private static int makeCreative(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
		CommandSourceStack source = context.getSource();
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
		Optional<MutableComponent> result = ItemCreativeUpgradeKit.makeCreative(source.getEntityOrException().getLevel(), pos);
		if(!result.isEmpty()) {
			source.sendSuccess(Component.translatable("commands.assemblylinemachines.makecreative.success", result.get(), pos.toShortString()), true);
			return 1;
		}
		source.sendFailure(Component.translatable("commands.assemblylinemachines.makecreative.error", pos.toShortString()));
		return 0;
	}

	private static int giveGuide(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
		CommandSourceStack source = context.getSource();
		ItemStack bookStack = PluginPatchouli.INTERFACE.get().getBookItem();
		if(bookStack.isEmpty()) {
			source.sendFailure(Component.translatable("commands.assemblylinemachines.guide.missing_mod"));
			return 0;
		}
		Collection<ServerPlayer> players = Utils.containsArgument(context, "targets") ? EntityArgument.getPlayers(context, "targets") :
			source.getEntityOrException() instanceof ServerPlayer ? List.of((ServerPlayer) source.getEntityOrException()) : List.of();

		for(ServerPlayer player : players) {
			ItemStack copy = bookStack.copy();
			boolean given = player.getInventory().add(copy);
			if(given && copy.isEmpty()) {
				copy.setCount(1);
				ItemEntity entity = player.drop(copy, false);
				if(entity != null) entity.makeFakeItem();
				player.level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f, ((player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			}else {
				ItemEntity entity = player.drop(copy, false);
				if(entity != null) {
					entity.setNoPickUpDelay();
					entity.setOwner(player.getUUID());
				}
			}
		}

		source.sendSuccess(Component.translatable("commands.assemblylinemachines.guide.success", players.size()), true);
		return players.size();
	}

	private static int fluidInChunk(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
		CommandSourceStack source = context.getSource();
		Entity entity = source.getEntityOrException();
		BlockPos pos = Utils.containsArgument(context, "pos") ? BlockPosArgument.getLoadedBlockPos(context, "pos") : entity.blockPosition();
		try {
			LazyOptional<IChunkFluidCapability> lazy = CapabilityChunkFluids.getChunkFluidCapability(entity.getLevel().getChunkAt(pos));
			if(lazy.isPresent()) {
				IChunkFluidCapability capability = lazy.orElseThrow(null);
				if(!capability.getChunkFluid().equals(Fluids.EMPTY)) {
					source.sendSuccess(Component.translatable("commands.assemblylinemachines.chunkfluid.get.success",
							FormattingHelper.GENERAL_FORMAT.format(capability.getFluidAmount()), capability.getDisplayName().getString(), pos.toShortString()), false);
					return 1;
				}
			}
			source.sendSuccess(Component.translatable("commands.assemblylinemachines.chunkfluid.get.success_empty", pos.toShortString()), false);
			return 1;
		}catch(ExecutionException e) {
			e.printStackTrace();
			source.sendFailure(Component.translatable("commands.assemblylinemachines.chunkfluid.error"));
			return 0;
		}
	}

	private static int setFluid(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
		CommandSourceStack source = context.getSource();
		Entity entity = source.getEntityOrException();
		BlockPos pos = Utils.containsArgument(context, "pos") ? BlockPosArgument.getLoadedBlockPos(context, "pos") : entity.blockPosition();
		int amount = Utils.containsArgument(context, "amount") ? IntegerArgumentType.getInteger(context, "amount") : 1000000;
		try {
			LazyOptional<IChunkFluidCapability> lazy = CapabilityChunkFluids.getChunkFluidCapability(entity.getLevel().getChunkAt(pos));
			if(lazy.isPresent()) {
				IChunkFluidCapability capability = lazy.orElseThrow(null);
				capability.setFluid(new FluidStack(FluidArgument.getFluid(context, "fluid"), amount));
				source.sendSuccess(Component.translatable("commands.assemblylinemachines.chunkfluid.set.success",
						pos.toShortString(), FormattingHelper.GENERAL_FORMAT.format(amount), capability.getDisplayName().getString()), true);
				return 1;
			}
		}catch(ExecutionException e) {
			e.printStackTrace();
		}
		source.sendFailure(Component.translatable("commands.assemblylinemachines.chunkfluid.error"));
		return 0;
	}

	private static int removeFluid(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
		CommandSourceStack source = context.getSource();
		Entity entity = source.getEntityOrException();
		BlockPos pos = Utils.containsArgument(context, "pos") ? BlockPosArgument.getLoadedBlockPos(context, "pos") : entity.blockPosition();
		try {
			LazyOptional<IChunkFluidCapability> lazy = CapabilityChunkFluids.getChunkFluidCapability(entity.getLevel().getChunkAt(pos));
			if(lazy.isPresent()) {
				IChunkFluidCapability capability = lazy.orElseThrow(null);
				capability.setFluid(FluidStack.EMPTY);
				source.sendSuccess(Component.translatable("commands.assemblylinemachines.chunkfluid.remove.success", pos.toShortString()), true);
				return 1;
			}
		}catch(ExecutionException e) {
			e.printStackTrace();
		}
		source.sendFailure(Component.translatable("commands.assemblylinemachines.chunkfluid.error"));
		return 0;
	}

	public static class FluidArgument implements ArgumentType<Fluid>{

		private static final Collection<String> EXAMPLES = List.of("minecraft:water");
		private static final DynamicCommandExceptionType ERROR_UNKNOWN_FLUID = new DynamicCommandExceptionType((rl) -> Component.translatable("commands.assemblylinemachines.errors.missing_fluid", rl));
		private static final SimpleCommandExceptionType ERROR_FLOWING = new SimpleCommandExceptionType(Component.translatable("commands.assemblylinemachines.errors.flowing_fluid"));

		public static FluidArgument fluid() {
			return new FluidArgument();
		}

		public static Fluid getFluid(CommandContext<CommandSourceStack> context, String argument) {
			return context.getArgument(argument, Fluid.class);
		}

		@Override
		public Fluid parse(StringReader reader) throws CommandSyntaxException {
			ResourceLocation rl = ResourceLocation.read(reader);
			Fluid f = ForgeRegistries.FLUIDS.getValue(rl);
			if(f.defaultFluidState().isEmpty()) throw ERROR_UNKNOWN_FLUID.create(rl);
			if(!f.defaultFluidState().isSource()) throw ERROR_FLOWING.create();
			return f;
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context,
				SuggestionsBuilder builder) {
			return SharedSuggestionProvider.suggestResource(ForgeRegistries.FLUIDS.getKeys().stream().filter((rl) -> {
				Fluid f = ForgeRegistries.FLUIDS.getValue(rl);
				return f.isSource(f.defaultFluidState());
			}), builder);
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}

	}
}