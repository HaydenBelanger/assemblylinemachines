package me.haydenb.assemblylinemachines.registry.utils;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrueFalseButton extends Button {

	public final int blitx;
	public final int blity;
	private final TrueFalseButton.TrueFalseButtonSupplier supplier;

	
	public TrueFalseButton(int x, int y, int blitx, int blity, int width, int height, TrueFalseButton.TrueFalseButtonSupplier supplier, OnPress onPress) {
		super(x, y, width, height, Component.literal(""), onPress, Button.DEFAULT_NARRATION);
		
		this.blitx = blitx;
		this.blity = blity;
		this.supplier = supplier;

		this.setTooltip(supplier);
	}

	public TrueFalseButton(int x, int y, int width, int height, String text, OnPress onPress) {
		this(x, y, 0, 0, width, height, new TrueFalseButtonSupplier(text, text, () -> false), onPress);
	}
	
	
	@Override
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		if(this.isValidClickButton(0)) {
			super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		}
		
	}
	
	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		
	}
	
	public int[] getBlitData() {
		return new int[]{this.getX(), this.getY(), blitx, blity, width, height};
	}

	public boolean getSupplierOutput() {
		return supplier.get();
	}
	
	public void addSupplierOverrideTooltipText(Supplier<String> overrideTooltipText) {
		supplier.overrideTooltipText = overrideTooltipText;
	}

	public static class TrueFalseButtonSupplier extends Tooltip {
		private final String trueText;
		private final String falseText;
		
		private Pair<String, List<FormattedCharSequence>> cachedTooltip = null;
		private Supplier<String> overrideTooltipText = null;
		
		private final Supplier<Boolean> supplier;

		public TrueFalseButtonSupplier(String trueText, String falseText, Supplier<Boolean> supplier) {
			super(Component.literal(""), null);
			this.trueText = trueText;
			this.falseText = falseText;
			this.supplier = supplier;
		}

		public boolean get() {
			return supplier != null ? supplier.get() : false;
		}

		public String getTrueText() {
			return trueText;
		}

		public String getFalseText() {
			return falseText;
		}
		
		public String getCurrentText() {
			return this.get() ? trueText : falseText;
		}
		
		@Override
		public List<FormattedCharSequence> toCharSequence(Minecraft minecraft) {
			String currentText = overrideTooltipText != null ? overrideTooltipText.get() : getCurrentText();
			if(currentText == null) currentText = "";
			
			if(cachedTooltip == null || !cachedTooltip.getFirst().equals(currentText)) {
				cachedTooltip = Pair.of(currentText, splitTooltip(minecraft, Component.literal(currentText)));
			}
			
			return cachedTooltip.getSecond();
		}
	}
}