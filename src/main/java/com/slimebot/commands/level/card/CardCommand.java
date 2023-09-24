package com.slimebot.commands.level.card;

import com.slimebot.commands.level.card.frame.*;
import com.slimebot.database.DataClass;
import com.slimebot.database.Key;
import com.slimebot.level.profile.CardProfile;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.util.ColorUtil;
import com.slimebot.util.Util;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.ui.CallbackState;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@ApplicationCommand(name = "card", description = "Passe deine Rankcard an", feature = "level")
public class CardCommand {

	private static CardProfile loadProfile(Member member) {
		Supplier<CardProfile> sup = () -> new CardProfile(member.getGuild().getIdLong(), member.getIdLong());
		return DataClass.load(sup, Map.of("guild", member.getGuild().getIdLong(), "user", member.getIdLong())).orElseGet(sup);
	}


	@ApplicationCommand(name = "edit", description = "Bearbeite deine Rankcard")
	public static class EditCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			Main.discordUtils.getUIManager().createMenu()
					.addFrame("main", MainFrame::new)
					.addFrame("avatar", AvatarFrame::new)
					.addFrame("background", BackgroundFrame::new)
					.addFrame("background.modal", BackgroundModalFrame::new)
					.addFrame("progressbar", ProgressbarFrame::new)
					.addFrame("progressbar.color", ProgressbarColorFrame::new)
					.addFrame("border", BorderFrame::new)
					.addFrame("reset", ResetWarningFrame::new).start(new CallbackState(event), "main");
		}

	}

	@ApplicationCommand(name = "reset", description = "Setzt deine Rankcard zurück")
	public static class ResetCommand {
		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			Main.discordUtils.getUIManager().createMenu()
					.addMessageFrame("main",
							() -> new EmbedBuilder()
									.setTitle("Zurücksetzen bestätigen")
									.setColor(GuildConfig.getColor(event.getGuild()))
									.setDescription("Möchtest du deine Rankcard wirklich zurücksetzen?")
									.addField(
											"Warnung",
											"Diese Aktion kann nicht rückgängig gemacht werden!",
											false
									)
									.build(),
							frame -> frame.addComponents(
									ComponentRow.of(
											new ButtonComponent("cancel", ButtonColor.GRAY, "Abbrechen").addHandler((m, evt) -> {
												m.close(false);
												evt.editMessage("Löschen abgebrochen").setReplace(true).queue();
											}),
											new ButtonComponent("confirm", ButtonColor.RED, "Bestätigen").addHandler((m, evt) -> {
												m.close(false);
												evt.editMessage("Rankcard erfolgreich zurückgesetzt").setReplace(true).queue();

												Main.database.run(handle -> handle.createUpdate("delete from cardprofile where guild = :guild and \"user\" = :user")
														.bind("guild", event.getGuild().getIdLong())
														.bind("user", event.getUser().getIdLong())
														.execute()
												);
											})
									)
							)
					)
					.start(new CallbackState(event), "main");
		}
	}

	@ApplicationCommand(name = "info", description = "Zeigt deine aktuellen Rankcard Optionen an")
	public static class InfoCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			CardProfile cp = loadProfile(event.getMember());
			EmbedBuilder builder = new EmbedBuilder();
			Field[] fields = cp.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (!DataClass.isValid(field) || field.isAnnotationPresent(Key.class)) continue;
				try {
					field.setAccessible(true);
					String value;
					if (field.getName().toLowerCase().contains("color") && (field.getType().equals(Integer.class) || field.getType().equals(int.class)))
						value = ColorUtil.toString(new Color(field.getInt(cp)));
					else value = String.valueOf(field.get(cp));
					builder.addField(String.join(" ", Util.parseCamelCase(field.getName().replace("BG", "Background"))), value, false);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			builder.setColor(GuildConfig.getColor(event.getGuild()));
			event.getHook().sendMessageEmbeds(builder.build()).queue();
		}

	}

}
