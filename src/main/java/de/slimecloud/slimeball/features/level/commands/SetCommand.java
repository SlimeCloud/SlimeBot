package de.slimecloud.slimeball.features.level.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "set", description = "Setzt Level/Xp")
public class SetCommand {
	@ApplicationCommand(name = "level", description = "Fügt Level hinzu")
	public static class LevelCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, dessen Level gesetzt werden") Member target,
		                           @Option(description = "Die Anzahl an Level", minValue = 1) int amount
		) {
			//Load and update level
			Level level = bot.getLevel().setLevel(target, bot.getLevel().getLevel(target), amount);

			//Send message
			event.reply(target.getAsMention() + " hat jetzt **" + level.getLevel() + " Level** und **" + level.getXp() + " Xp**").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "xp", description = "Fügt Xp hinzu")
	public static class XpCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, dessen Xp gesetzt werden") Member target,
		                           @Option(description = "Die Anzahl an Xp", minValue = 1) int amount
		) {
			//Load and update xp
			Level level = bot.getLevel().getLevel(target)
					.withXp(amount)
					.upsert();

			//Send message
			event.reply(target.getAsMention() + " hat jetzt **" + level.getLevel() + " Level** und **" + level.getXp() + " Xp**").setEphemeral(true).queue();
		}
	}
}
