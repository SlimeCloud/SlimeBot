package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import com.slimebot.graphic.UIError;
import com.slimebot.level.profile.CardProfile;
import com.slimebot.util.ColorUtil;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.ModalFrameBase;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.awt.*;

public class BorderFrame extends ModalFrameBase {

	private CardProfile profile;

	public BorderFrame(Menu menu) {
		super(menu);
	}

	@Override
	public void setup() {
		profile = CardProfile.loadProfile(menu.getMember());
	}

	@Override
	public Modal getModal(String id) {
		return Modal.create(id, "Border")
				.addActionRow(
						TextInput.create("size", "Größe", TextInputStyle.SHORT)
								.setMaxLength(2)
								.setMinLength(1)
								.setPlaceholder("12")
								.setRequired(false)
								.setValue(
										String.valueOf(
												switch (getPart()) {
													case AVATAR -> profile.getAvatarBorderWidth();
													case BACKGROUND -> profile.getBackgroundBorderWidth();
													case PROGRESSBAR -> profile.getProgressBarBorderWidth();
												}
										)
								)
								.build()
				)
				.addActionRow(
						TextInput.create("color", "Farbe", TextInputStyle.SHORT)
								.setMaxLength(15)
								.setMinLength(3)
								.setRequired(false)
								.setPlaceholder("#46eb34")
								.setValue(
										ColorUtil.toHex(ColorUtil.ofCode(
												switch (getPart()) {
													case AVATAR -> profile.getAvatarBorderColor();
													case BACKGROUND -> profile.getBackgroundBorderColor();
													case PROGRESSBAR -> profile.getProgressBarBorderColor();
												}
										))
								)
								.build()
				).build();
	}

	private CardComponent.Part getPart() {
		return menu.getData("part", CardComponent.Part.class);
	}

	@Override
	public void handle(MenuBase menu, ModalInteractionEvent event) {
		menu.setLoading();

		CardComponent.Part part = getPart();

		ModalMapping size = event.getValue("size");
		ModalMapping color = event.getValue("color");

		if (size != null) {
			try {
				int width = Integer.parseInt(size.getAsString());
				switch (part) {
					case AVATAR -> profile.setAvatarBorderWidth(width);
					case BACKGROUND -> profile.setBackgroundBorderWidth(width);
					case PROGRESSBAR -> profile.setProgressBarBorderWidth(width);
				}
			} catch(NumberFormatException e) {
				UIError.NUMBER.send(event, size.getAsString());
			}
		}

		if (color != null) {
			Color c = ColorUtil.parseColor(color.getAsString());
			if (c != null) {
				int rgba = c.getRGB();
				switch (part) {
					case AVATAR -> profile.setAvatarBorderColor(rgba);
					case BACKGROUND -> profile.setBackgroundBorderColor(rgba);
					case PROGRESSBAR -> profile.setProgressBarBorderColor(rgba);
				}
			}
			else UIError.COLOR.send(event, color.getAsString());
		} else {
			switch (part) {
				case AVATAR -> profile.setAvatarBorderColor(CardProfile.DEFAULT.getAvatarBorderColor());
				case BACKGROUND -> profile.setBackgroundBorderColor(CardProfile.DEFAULT.getBackgroundBorderColor());
				case PROGRESSBAR -> profile.setProgressBarBorderColor(CardProfile.DEFAULT.getProgressBarBorderColor());
			}
		}

		profile.save();
		menu.display(part.name().toLowerCase());
	}
}
