package com.slimebot.commands.level.add;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Füge einem Nutzer level hinzu")
public class AddLevelCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(description = "Der Nutzer, dem Level gegeben werden sollen") Member user,
	                           @Option(description = "Die Anzahl an Leveln", minValue = 1) int level
	) {
		Level newLevel = Level.getLevel(user)
				.addXp(level, 0)
				.save();

		event.reply(user.getAsMention() + " wurden erfolgreich " + level + " level hinzugefügt!\nEr hat jetzt " + newLevel.level() + " level!").setEphemeral(true).queue();
	}
}
