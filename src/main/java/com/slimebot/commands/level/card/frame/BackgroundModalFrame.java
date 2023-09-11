package com.slimebot.commands.level.card.frame;

import com.slimebot.database.DataClass;
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
import java.util.Map;
import java.util.function.Supplier;

public class BackgroundModalFrame extends ModalFrameBase {

	private CardProfile profile;

	public BackgroundModalFrame(Menu menu) {
		super(menu);
	}

	@Override
	public void setup() {
		Supplier<CardProfile> sup = () -> new CardProfile(menu.getGuild().getIdLong(), menu.getMember().getIdLong());
		profile = DataClass.load(sup, Map.of("guild", menu.getGuild().getIdLong(), "user", menu.getMember().getIdLong())).orElseGet(sup);
	}

	@Override
	public Modal getModal(String id) {
		return Modal.create(id, "Hintergrund")
				.addActionRow(TextInput.create("image", "Hintergrund Bild URL", TextInputStyle.SHORT)
						.setRequired(false)
						.setPlaceholder("https://example.org/image.png")
						.build())
				.addActionRow(TextInput.create("color", "Hintergrund Farbe", TextInputStyle.SHORT)
						.setRequired(false)
						.setMinLength(3)
						.setMaxLength(15)
						.setPlaceholder("#46eb34")
						.build())
				.build();
	}

	@Override
	public void handle(MenuBase menu, ModalInteractionEvent event) {
		menu.setLoading();
		ModalMapping image = event.getValue("image");
		ModalMapping color = event.getValue("color");
		image = image == null ? null : (image.getAsString().isBlank() ? null : image);
		color = color == null ? null : (color.getAsString().isBlank() ? null : color);
		if (image == null && color == null) {
			menu.display("background");
			return;
		}
		boolean flag = false;
		if (image != null) {
			if (Util.isValidURL(image.getAsString())) {
				profile.setBackgroundImageURL(image.getAsString());
				flag = true;
			} else UIError.URL_ERROR.send(event, image.getAsString(), "image.png");
		} else profile.setBackgroundImageURL("");
		if (color != null) {
			Color c = ColorUtil.parseColor(color.getAsString());
			if (c != null) {
				profile.setBackgroundColor(c.getRGB());
				flag = true;
			} else UIError.COLOR_ERROR.send(event, color.getAsString());
		}

		if (flag) profile.save();
		menu.display("background");
	}
}
