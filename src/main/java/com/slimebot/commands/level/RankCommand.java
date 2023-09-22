package com.slimebot.commands.level;

import com.slimebot.level.Level;
import com.slimebot.level.RankCard;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@ApplicationCommand(name = "rank", description = "Zeigt dein Aktuelles Level und XP an", feature = "level")
public class RankCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event, @Option(description = "Der Nutzer, dessen Platzierung die ansehen möchtest", required = false) Member user) {
		if (user == null) user = event.getMember();

		if (user.getUser().isBot()) {
			event.reply("Bots wie " + user.getAsMention() + " können nicht leveln!").queue();
			return;
		}

		event.deferReply().queue();

		event.getHook().sendFiles(new RankCard(Level.getLevel(user)).getFile()).queue();
	}
}
