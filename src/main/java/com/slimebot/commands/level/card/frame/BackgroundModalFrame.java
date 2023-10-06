package com.slimebot.commands.level.card.frame;

import com.slimebot.graphic.UIError;
import com.slimebot.level.profile.CardProfile;
import com.slimebot.util.ColorUtil;
import com.slimebot.util.Util;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.ModalFrameBase;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.awt.*;

public class BackgroundModalFrame extends ModalFrameBase {
	private CardProfile profile;

	public BackgroundModalFrame(Menu menu) {
		super(menu);
	}

	@Override
	public void setup() {
		profile = CardProfile.loadProfile(menu.getMember());
	}

	@Override
	public Modal getModal(String id) {
		return Modal.create(id, "Hintergrund")
				.addActionRow(TextInput.create("image", "Hintergrund Bild URL", TextInputStyle.SHORT)
						.setRequired(false)
						.setPlaceholder("https://example.org/image.png")
						.setValue(profile.getBackgroundImageURL().isBlank() ? null : profile.getBackgroundImageURL())
						.build()
				)
				.addActionRow(TextInput.create("color", "Hintergrund Farbe", TextInputStyle.SHORT)
						.setRequired(false)
						.setMinLength(3)
						.setMaxLength(15)
						.setPlaceholder("#46eb34")
						.setValue(ColorUtil.toHex(ColorUtil.ofCode(profile.getAvatarBorderColor())))
						.build()
				)
				.build();
	}

	@Override
	public void handle(MenuBase menu, ModalInteractionEvent event) {
		menu.setLoading();

		ModalMapping image = event.getValue("image");
		ModalMapping color = event.getValue("color");

		if (image != null && !image.getAsString().isBlank()) {
			if (Util.isValidURL(image.getAsString())) profile.setBackgroundImageURL(image.getAsString());
			else UIError.URL.send(event, image.getAsString(), "image.png");
		} else profile.setBackgroundImageURL("");

		if (color != null && !color.getAsString().isBlank()) {
			Color c = ColorUtil.parseColor(color.getAsString());

			if (c != null) profile.setBackgroundColor(c.getRGB());
			else UIError.COLOR.send(event, color.getAsString());
		} else profile.setBackgroundColor(CardProfile.DEFAULT.getBackgroundColor());

		profile.save();

		menu.display("background");
	}
}
