package com.slimebot.commands.level.add;

import com.slimebot.level.Level;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "xp", description = "Füge einem Nutzer xp hinzu")
public class AddXPCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(description = "Der Nutzer, dem XP gegeben werden sollen") Member user,
	                           @Option(description = "Die Anzahl an XP", minValue = 1) int xp
	) {
		Level newLevel = Level.getLevel(user)
				.addXp(0, xp)
				.save();

		event.reply(user.getAsMention() + "  wurden erfolgreich " + xp + " xp hinzugefügt!\nEr hat jetzt " + newLevel.getXp() + " xp, und ist level " + newLevel.getLevel() + "!").setEphemeral(true).queue();
	}
}
