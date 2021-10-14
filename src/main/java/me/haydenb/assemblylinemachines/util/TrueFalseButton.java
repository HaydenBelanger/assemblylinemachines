package me.haydenb.assemblylinemachines.util;

import java.util.Arrays;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;

public class TrueFalseButton extends Button {

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
	
	
}