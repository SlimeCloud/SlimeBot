package com.slimebot.commands.level.remove;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "level", description = "Entferne level von einem Nutzer")
public class RemoveLevelCommand {

	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(description = "Der Nutzer, von dem Level entfernt werden sollen") Member user,
	                           @Option(description = "Die Anzahl an Leveln", minValue = 1) int level
	) {
		Level current = Level.getLevel(user);

		if (level > current.getLevel()) {
			event.reply(user.getAsMention() + "   hat nur " + current.getLevel() + " Level. du kannst ihm also maximal " + current.getLevel() + " Level entfernen!").setEphemeral(true).queue();
			return;
		}

		current = current
				.addXp(-1 * level, 0)
				.save();

		event.reply(user.getAsMention() + " wurden erfolgreich " + level + " level entfernt!\nEr ist jetzt level " + current.getLevel() + "!").setEphemeral(true).queue();
	}

}
