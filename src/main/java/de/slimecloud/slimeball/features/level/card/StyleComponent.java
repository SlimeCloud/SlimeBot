package de.slimecloud.slimeball.features.level.card;

import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.button.label.TextLabel;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public class StyleComponent extends ButtonComponent {
	public StyleComponent(@NotNull Field field) {
		super(field.getName(), ButtonColor.BLUE, (TextLabel) state -> {
			try {
				return "Form: " + field.get(state.getCache("profile")).toString();
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});

		appendHandler(state -> {
			try {
				CardProfileData profile = state.getCache("profile");
				field.set(profile, field.get(profile) == Style.ROUND ? Style.SQUARE : Style.ROUND);
				profile.update();

				state.update();
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
