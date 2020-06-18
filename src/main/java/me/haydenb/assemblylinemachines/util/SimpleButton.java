package me.haydenb.assemblylinemachines.util;

import net.minecraft.client.gui.widget.button.Button;

public class SimpleButton extends Button {
	public final int blitx;
	public final int blity;
	
	public final int sizex;
	public final int sizey;

	public SimpleButton(int widthIn, int heightIn, int blitx, int blity, int sizex, int sizey, String text, IPressable onPress) {
		super(widthIn, heightIn, sizex, sizey, text, onPress);
		this.blitx = blitx;
		this.blity = blity;
		this.sizex = sizex;
		this.sizey = sizey;
	}

	public SimpleButton(int widthIn, int heightIn, String text, IPressable onPress) {
		this(widthIn, heightIn, 0, 0, text, onPress);
	}
	
	public SimpleButton(int widthIn, int heightIn, int blitx, int blity, String text, IPressable onPress) {
		this(widthIn, heightIn, blitx, blity, 8, 8, text, onPress);
	}

	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
	}
}
