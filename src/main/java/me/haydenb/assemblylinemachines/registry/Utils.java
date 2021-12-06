package me.haydenb.assemblylinemachines.registry;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import me.haydenb.assemblylinemachines.item.ItemPowerTool.PowerToolType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import net.minecraftforge.items.IItemHandler;

public class Utils {

	public static final Direction[] CARDINAL_DIRS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
	
	public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
		VoxelShape[] buffer = new VoxelShape[] { shape, Shapes.empty() };

		int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
		for (int i = 0; i < times; i++) {
			buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1],
					Shapes.create(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
			buffer[0] = buffer[1];
			buffer[1] = Shapes.empty();
		}

		return buffer[0];
	}

	public static <T extends BlockEntity> T getBlockEntity(final Inventory pInv, final FriendlyByteBuf data,
			Class<T> clazz) {
		Objects.requireNonNull(pInv, "This object cannot be null.");
		Objects.requireNonNull(data, "This object cannot be null.");

		BlockEntity posEntity = pInv.player.getCommandSenderWorld().getBlockEntity(data.readBlockPos());

		return clazz.cast(posEntity);
	}

	public static void spawnItem(ItemStack stack, BlockPos pos, Level world) {
		ItemEntity ent = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
		world.addFreshEntity(ent);
	}

	/**
	 * Attempts to deposit into all slots in a handler. Returns the remainder.
	 */
	public static ItemStack attemptDepositIntoAllSlots(ItemStack stack, IItemHandler handler) {
		for(int i = 0; i < handler.getSlots(); i++) {
			if(stack.isEmpty()) {
				break;
			}
			stack = handler.insertItem(i, stack, false);
		}
		return stack;
	}


	public static interface IToolWithCharge{
		default public int getMaxPower(ItemStack stack) {
			return stack.isEnchanted() ? Math.round(this.getPowerToolType().getMaxCharge() * (1 + (EnchantmentHelper.getItemEnchantmentLevel(Registry.getEnchantment("overclock"), stack) * 0.2f))) : this.getPowerToolType().getMaxCharge();
		}
		
		default public ItemStack damageItem(ItemStack stack, int amount) {
			if (stack.hasTag()) {
				CompoundTag compound = stack.getTag();

				PowerToolType ptt = this.getPowerToolType();
				if (compound.contains(ptt.getKeyName())) {

					int power = compound.getInt(ptt.getKeyName());
					if ((power - (amount * ptt.getCostMultiplier())) < 1) {
						compound.remove(ptt.getKeyName());
						compound.remove("assemblylinemachines:canbreakblackgranite");
						compound.remove("assemblylinemachines:secondarystyle");
					} else {
						compound.putInt(ptt.getKeyName(), power - (amount * ptt.getCostMultiplier()));
					}

					stack.setTag(compound);
					return stack;
				}
			}
			return null;
		}
		
		default public int addCharge(ItemStack stack, int amount, boolean simulated) {
			CompoundTag nbt = stack.hasTag() ? stack.getTag() : new CompoundTag();
			
			PowerToolType ptt = this.getPowerToolType();
			int current = nbt.getInt(ptt.getKeyName());
			
			if(current + (amount * ptt.getChargeMultiplier()) > getMaxPower(stack)) {
				amount = getMaxPower(stack) - current;
				if(!simulated) nbt.putInt(ptt.getKeyName(), getMaxPower(stack));
			}else {
				if(!simulated) nbt.putInt(ptt.getKeyName(), current + (amount * ptt.getChargeMultiplier()));
				
			}
			if((simulated && current > 0) || (!simulated && current + (amount * ptt.getChargeMultiplier()) > 0)) {
				nbt.putBoolean("assemblylinemachines:canbreakblackgranite", true);
			}else {
				nbt.remove("assemblylinemachines:canbreakblackgranite");
			}
			
			stack.setTag(nbt);
			
			return amount;
		}
		
		public PowerToolType getPowerToolType();
		
		public String getToolType();
		
		default public int getCurrentCharge(ItemStack stack) {
			return stack.hasTag() ? stack.getTag().getInt(this.getPowerToolType().getKeyName()) : 0;
		}
		
		default public boolean canUseSecondaryAbilities(ItemStack stack, String secondaryName) {
			return this.getPowerToolType().getHasSecondaryAbilities() && stack.hasTag() && stack.getTag().contains(getPowerToolType().getKeyName())
					&& stack.getTag().contains("assemblylinemachines:secondarystyle") && secondaryName.equals(this.getToolType());
		}
		
		default public void addEnergyInfoToHoverText(ItemStack stack, List<Component> tooltip) {
			DecimalFormat df = Formatting.GENERAL_FORMAT;
			CompoundTag compound = stack.hasTag() ? stack.getTag() : new CompoundTag();
			PowerToolType ptt = this.getPowerToolType();
			String colorChar = compound.getInt(ptt.getKeyName()) == 0 ? "c" : "a";
			tooltip.add(new TextComponent("§" + colorChar + df.format(compound.getInt(ptt.getKeyName())) + "/" + df.format(this.getMaxPower(stack)) + " " + ptt.getFriendlyNameOfUnit()));
			if(compound.getBoolean("assemblylinemachines:secondarystyle")) {
				tooltip.add(new TextComponent("§bSecondary Ability Enabled"));
			}
		}
	}
	
	public static int breakAndBreakConnected(Level world, BlockState origState, int ctx, int cmax, BlockPos posx, LivingEntity player) {
		world.destroyBlock(posx, true, player);

		int cost = 2;
		Iterator<BlockPos> iter = BlockPos.betweenClosedStream(posx.below().north().west(), posx.above().south().east()).iterator();

		while(iter.hasNext()) {
			BlockPos posq = iter.next();

			BlockState bs = world.getBlockState(posq);
			if(bs.getBlock() == origState.getBlock() && ctx <= cmax) {
				ctx++;
				cost = cost + breakAndBreakConnected(world, origState, ctx, cmax, posq, player);
			}
		}

		return cost;
	}

	public static <H> H getCapabilityFromDirection(BlockEntity pte, NonNullConsumer<LazyOptional<H>> consumer, Direction dir, Capability<H> capType) {
		BlockEntity te = pte.getLevel().getBlockEntity(pte.getBlockPos().relative(dir));

		if(te != null) {
			LazyOptional<H> cap = te.getCapability(capType, dir.getOpposite());
			H output = cap.orElse(null);
			if(output != null) {
				cap.addListener(consumer);

				return output;
			}
		}

		return null;
	}

	@OnlyIn(Dist.CLIENT)
	public static void drawCenteredStringWithoutShadow(PoseStack pPoseStack, Font pFont, Component pText, int pX, int pY, int pColor) {
		FormattedCharSequence formattedcharsequence = pText.getVisualOrderText();
		pFont.draw(pPoseStack, formattedcharsequence, (float)(pX - pFont.width(formattedcharsequence) / 2), (float)pY, pColor);
	}
	
	public static class Formatting {

		private static final NavigableMap<Long, String> SUFFIX = new TreeMap<>();
		
		public static final DecimalFormat GENERAL_FORMAT = new DecimalFormat("###,###,###,###,###,###,###");
		public static final DecimalFormat FEPT_FORMAT = new DecimalFormat("###,##0.#");
		
		
		static {
			SUFFIX.put(1_000L, "K");
			SUFFIX.put(1_000_000L, "M");
			SUFFIX.put(1_000_000_000L, "G");
			SUFFIX.put(1_000_000_000_000L, "T");
			SUFFIX.put(1_000_000_000_000_000L, "P");
			SUFFIX.put(1_000_000_000_000_000_000L, "E");
		}
		
		public static String formatToSuffix(long value) {
			// Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
			if (value == Long.MIN_VALUE)
				return formatToSuffix(Long.MIN_VALUE + 1);
			if (value < 0)
				return "-" + formatToSuffix(-value);
			if (value < 1000)
				return Long.toString(value); // deal with easy case

			Entry<Long, String> e = SUFFIX.floorEntry(value);
			Long divideBy = e.getKey();
			String suffix = e.getValue();

			long truncated = value / (divideBy / 10); // the number part of the output times 10
			boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
			return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
		}
	}
	
	public static class MathHelper {
		
		
		//Claimed from ItemRenderer#renderItemOverlayIntoGUI, modified.
		@OnlyIn(Dist.CLIENT)
		public static void renderItemSlotBoundScaledText(Font fr, int xpos, int ypos, float scale, String text) {
			
			xpos = Math.round(xpos * (1.0f / scale));
			ypos = Math.round(ypos * (1.0f / scale));
			PoseStack mx = new PoseStack();
			
			mx.translate(0.0D, 0.0D, 200D);
			mx.scale(scale, scale, scale);
			BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			fr.drawInBatch(text, (float) (xpos + 19 - 2 - fr.width(text)), (float) (ypos + 6 + 3), 16777215, true, mx.last().pose(),
					irendertypebuffer$impl, false, 0, 15728880);
			
			irendertypebuffer$impl.endBatch();
		}
		
		@OnlyIn(Dist.CLIENT)
		public static void renderScaledText(Font fr, int xpos, int ypos, float scale, String text, boolean dropShadow, int color) {
			
			xpos = Math.round(xpos * (1.0f / scale));
			ypos = Math.round(ypos * (1.0f / scale));
			PoseStack mx = new PoseStack();
			
			mx.translate(0.0D, 0.0D, 200D);
			mx.scale(scale, scale, scale);
			BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			fr.drawInBatch(text, (float) xpos, ypos, color, dropShadow, mx.last().pose(),
					irendertypebuffer$impl, false, 0, 15728880);
			irendertypebuffer$impl.endBatch();
		}
		
		@OnlyIn(Dist.CLIENT)
		public static void renderScaledText(Font fr, int xpos, int ypos, float scale, String text) {
			
			renderScaledText(fr, xpos, ypos, scale, text, false, 0xffffff);
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class TrueFalseButton extends Button {

		public final int blitx;
		public final int blity;
		
		private final TrueFalseButtonSupplier supplier;
		
		public TrueFalseButton(int x, int y, int blitx, int blity, int width, int height, TrueFalseButtonSupplier supplier, OnPress onPress) {
			super(x, y, width, height, new TextComponent(""), onPress, new OnTooltip() {
				@Override
				public void onTooltip(Button pButton, PoseStack mx, int mouseX, int mouseY) {
					Minecraft minecraft = Minecraft.getInstance();
					if(supplier.getTrueText() != null && supplier.getFalseText() != null && supplier.get()) {
						minecraft.screen.renderComponentTooltip(mx, Arrays.asList(new TextComponent(supplier.getTrueText())), mouseX, mouseY);
					}else {
						minecraft.screen.renderComponentTooltip(mx, Arrays.asList(new TextComponent(supplier.getFalseText())), mouseX, mouseY);
					}
					
				}
			});
			this.blitx = blitx;
			this.blity = blity;
			this.supplier = supplier;
			
		}
		
		public TrueFalseButton(int x, int y, int width, int height, String text, OnPress onPress) {
			super(x, y, width, height, new TextComponent(""), onPress, new OnTooltip() {
				@Override
				public void onTooltip(Button pButton, PoseStack mx, int mouseX, int mouseY) {
					Minecraft minecraft = Minecraft.getInstance();
					if(text != null) {
						minecraft.screen.renderComponentTooltip(mx, Arrays.asList(new TextComponent(text)), mouseX, mouseY);
					}
					
				}
			});
			this.blitx = 0;
			this.blity = 0;
			this.supplier = null;
			
		}
		
		@Override
		public void renderButton(PoseStack mx, int mouseX, int mouseY, float partialTicks) {
			if(this.isHovered()) {
				this.renderToolTip(mx, mouseX, mouseY);
			}
			
		}
		
		public int[] getBlitData() {
			return new int[]{x, y, blitx, blity, width, height};
		}
		
		public boolean getSupplierOutput() {
			if(supplier == null) {
				return false;
			}
			return supplier.get();
		}
		
		public static class TrueFalseButtonSupplier {
			private final String trueText;
			private final String falseText;
			
			public final Supplier<Boolean> supplier;

			public TrueFalseButtonSupplier(String trueText, String falseText, Supplier<Boolean> supplier) {
				this.trueText = trueText;
				this.falseText = falseText;
				this.supplier = supplier;

			}
			
			public TrueFalseButtonSupplier(String text, Supplier<Boolean> supplier) {
				this.trueText = text;
				this.falseText = text;
				this.supplier = supplier;

			}
			
			public boolean get() {
				return supplier.get();
			}
			
			public String getTrueText() {
				return trueText;
			}
			
			public String getFalseText() {
				return falseText;
			}
			
			@Deprecated
			public String getTextFromSupplier() {
				if(get()) {
					return trueText;
				}else {
					return falseText;
				}
			}
			
			
		}

		
	}
}
