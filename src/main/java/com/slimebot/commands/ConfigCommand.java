package com.slimebot.commands;

import com.slimebot.main.CommandPermission;
import com.slimebot.utils.Config;
import de.mineking.discord.commands.Choice;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.Arrays;
import java.util.List;

@ApplicationCommand(name = "config", description = "Nehme Änderungen an der Konfiguration vor", guildOnly = true)
public class ConfigCommand {
	public final CommandPermission permission = CommandPermission.ADMINISTRATOR;

	public final List<Choice> fields = Arrays.asList(
			new Choice("Log Channel (ID)", "logChannel"),
			new Choice("Blockliste", "blocklist"),
			new Choice("Team Rolle (ID)", "staffRoleID"),
			new Choice("Verification Rolle (ID)", "verifyRoleID"),
			new Choice("Warning Channel (ID)", "punishmentChannelID"),
			new Choice("Embed Color (RGB) Rot", "embedColor.rgb.red"),
			new Choice("Embed Color (RGB) Grün", "embedColor.rgb.green"),
			new Choice("Embed Color (RGB) Blau", "embedColor.rgb.blue")

	);

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(name = "type", description = "Welcher Config-Bereich?") String type,
	                           @Option(name = "field", description = "Welches Feld soll angepasst werden?", choices = "fields") String field,
	                           @Option(name = "value", description = "Welcher Wert soll bei dem Feld gesetzt werden?") String value
	) {
		YamlFile config = Config.getConfig(event.getGuild().getId(), "mainConfig");

		try {
			if(type.equals("config")) {
				config.load();
				config.set(field, value);
				config.save();
			}

			event.reply("Du hast erfolgreich Anpassungen getätigt!").setEphemeral(true).queue();
		} catch(Exception e) {
			event.reply("Bei deinen Anpassungen sind fehler aufgetreten...").setEphemeral(true).queue();
		}
	}
}
