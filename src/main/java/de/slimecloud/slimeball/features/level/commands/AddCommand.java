package de.slimecloud.slimeball.features.level.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.features.level.UserGainXPEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "add", description = "Fügt Level/Xp hinzu")
public class AddCommand {
	@ApplicationCommand(name = "level", description = "Fügt Level hinzu")
	public static class LevelCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, dem Level hinzugefügt werden") Member target,
		                           @Option(description = "Die Anzahl an Level", minValue = 1) int amount
		) {
			Level level = bot.getLevel().addLevel(target, amount);
			event.reply(target.getAsMention() + " hat jetzt **" + level.getLevel() + " Level** und **" + level.getXp() + " Xp**").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "xp", description = "Fügt Xp hinzu")
	public static class XpCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, dem Xp hinzugefügt werden") Member target,
		                           @Option(description = "Die Anzahl an Xp", minValue = 1) int amount
		) {
			Level level = bot.getLevel().addXp(target, amount, UserGainXPEvent.Type.MANUAL);
			event.reply(target.getAsMention() + " hat jetzt **" + level.getLevel() + " Level** und **" + level.getXp() + " Xp**").setEphemeral(true).queue();
		}
	}
}
