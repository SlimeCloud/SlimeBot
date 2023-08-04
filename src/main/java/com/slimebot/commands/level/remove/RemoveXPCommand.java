package com.slimebot.commands.level.remove;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "xp", description = "Entferne xp von einem Nutzer")
public class RemoveXPCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(description = "Der Nutzer, vom dem XP entfernt werden sollen") Member user,
	                           @Option(description = "Die Anzahl an XP", minValue = 1) int xp
	) {
		Level current = Level.getLevel(user);

		if (xp > current.getXp()) {
			event.reply(user.getAsMention() + " hat nur " + current.getXp() + " XP. du kannst ihm also maximal " + current.getXp() + " XP entfernen!").setEphemeral(true).queue();
			return;
		}

		current = current
				.addXp(0, -1 * xp)
				.save();

		event.reply(user.getAsMention() + " wurden erfolgreich " + xp + " XP entfernt!\nEr hat jetzt " + current.getXp() + " xp!").setEphemeral(true).queue();
	}
}
