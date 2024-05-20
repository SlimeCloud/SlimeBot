package de.slimecloud.slimeball.features.level.card.component;

import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.label.TextLabel;
import de.slimecloud.slimeball.features.level.card.CardProfileData;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class RankStyleComponent extends ButtonComponent {
	public RankStyleComponent(@NotNull Field field) {
		super(field.getName(), ButtonColor.BLUE, (TextLabel) state -> {
			try {
				return "Level Farbe: " + field.get(state.getCache("profile")).toString();
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});

		appendHandler(state -> {
			try {
				CardProfileData profile = state.getCache("profile");
				field.set(profile, RankColor.ofId(((RankColor) field.get(profile)).ordinal() + 1));
				profile.update();

				state.update();
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
