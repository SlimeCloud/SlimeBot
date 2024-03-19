package de.slimecloud.slimeball.features.level.commands;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.option.Option;
import de.slimecloud.slimeball.features.level.Level;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

@ApplicationCommand(name = "remove", description = "Entfernt Xp/Level")
public class RemoveCommand {
	@ApplicationCommand(name = "level", description = "Entfernt Level")
	public static class LevelCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, von dem Level entfernt werden") Member target,
		                           @Option(description = "Die Anzahl an Level", minValue = 1) int amount
		) {
			//Load level
			Level level = bot.getLevel().getLevel(target);

			//Check for valid input
			if (amount > level.getLevel()) {
				event.reply(target.getAsMention() + " hat nur **" + level.getLevel() + "** Level!").setEphemeral(true).queue();
				return;
			}

			//Update level
			level.withLevel(level.getLevel() - amount).upsert();
			event.reply(target.getAsMention() + " hat jetzt **" + level.getLevel() + " Level** und **" + level.getXp() + " Xp**").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "xp", description = "Entfernt Xp")
	public static class XpCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "Der Nutzer, von dem Xp entfernt werden") Member target,
		                           @Option(description = "Die Anzahl an Xp", minValue = 1) int amount
		) {
			//Load level
			Level level = bot.getLevel().getLevel(target);

			//Check for valid input
			if (amount > level.getXp()) {
				event.reply(target.getAsMention() + " hat nur **" + level.getXp() + "** Xp!").setEphemeral(true).queue();
				return;
			}

			//Update xp
			level.withXp(level.getXp() - amount).upsert();
			event.reply(target.getAsMention() + " hat jetzt **" + level.getLevel() + " Level** und **" + level.getXp() + " Xp**").setEphemeral(true).queue();
		}
	}
}
