package com.slimebot.commands.level.card.frame;

import com.slimebot.commands.level.card.CardComponent;
import com.slimebot.database.DataClass;
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

public class BorderFrame extends ModalFrameBase {

	private CardProfile profile;

	public BorderFrame(Menu menu) {
		super(menu);
	}

	@Override
	public void setup() {
		Supplier<CardProfile> sup = () -> new CardProfile(menu.getGuild().getIdLong(), menu.getMember().getIdLong());
		profile = DataClass.load(sup, Map.of("guild", menu.getGuild().getIdLong(), "user", menu.getMember().getIdLong())).orElseGet(sup);
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
								.build()
				)
				.addActionRow(
						TextInput.create("color", "Farbe", TextInputStyle.SHORT)
								.setMaxLength(15)
								.setMinLength(3)
								.setRequired(false)
								.setPlaceholder("#46eb34")
								.build()
				).build();
	}

	private CardComponent.Part getPart() {
		return menu.getData("part", CardComponent.Part.class);
	}

	@Override
	public void handle(MenuBase menu, ModalInteractionEvent event) {
		menu.setLoading();
		ModalMapping size = event.getValue("size");
		ModalMapping color = event.getValue("color");
		CardComponent.Part part = getPart();
		boolean flag = false;
		if (size != null && Util.isInteger(size.getAsString())) {
			int width = Integer.parseInt(size.getAsString());
			switch (part) {
				case AVATAR -> profile.setAvatarBorderWidth(width);
				case BACKGROUND -> profile.setBackgroundBorderWidth(width);
				case PROGRESSBAR -> profile.setProgressBarBorderWidth(width);
			}
			flag = true;
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
				flag = true;
			}
		}
		if (flag) profile.save();
		menu.display(part.name().toLowerCase());
	}
}
