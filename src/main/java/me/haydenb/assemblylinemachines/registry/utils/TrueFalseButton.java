package me.haydenb.assemblylinemachines.registry.utils;

import java.util.Arrays;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrueFalseButton extends Button {

	public final int blitx;
	public final int blity;

	private final TrueFalseButton.TrueFalseButtonSupplier supplier;

	public TrueFalseButton(int x, int y, int blitx, int blity, int width, int height, TrueFalseButton.TrueFalseButtonSupplier supplier, OnPress onPress) {
		super(x, y, width, height, Component.literal(""), onPress, new OnTooltip() {
			@Override
			public void onTooltip(Button pButton, PoseStack mx, int mouseX, int mouseY) {
				Minecraft minecraft = Minecraft.getInstance();
				if(supplier.getTrueText() != null && supplier.getFalseText() != null && supplier.get()) {
					minecraft.screen.renderComponentTooltip(mx, Arrays.asList(Component.literal(supplier.getTrueText())), mouseX, mouseY);
				}else {
					minecraft.screen.renderComponentTooltip(mx, Arrays.asList(Component.literal(supplier.getFalseText())), mouseX, mouseY);
				}

			}
		});
		this.blitx = blitx;
		this.blity = blity;
		this.supplier = supplier;

	}

	public TrueFalseButton(int x, int y, int width, int height, String text, OnPress onPress) {
		super(x, y, width, height, Component.literal(""), onPress, new OnTooltip() {
			@Override
			public void onTooltip(Button pButton, PoseStack mx, int mouseX, int mouseY) {
				Minecraft minecraft = Minecraft.getInstance();
				if(text != null) {
					minecraft.screen.renderComponentTooltip(mx, Arrays.asList(Component.literal(text)), mouseX, mouseY);
				}

			}
		});
		this.blitx = 0;
		this.blity = 0;
		this.supplier = null;

	}

	@Override
	public void renderButton(PoseStack mx, int mouseX, int mouseY, float partialTicks) {
		if(this.isHoveredOrFocused()) {
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

		private final Supplier<Boolean> supplier;

		public TrueFalseButtonSupplier(String trueText, String falseText, Supplier<Boolean> supplier) {
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