package com.slimebot.main;

import de.mineking.discord.commands.ContextBase;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class CommandContext extends ContextBase {
	public final SlashCommandInteractionEvent event;
	public final Guild guild;
	public final User user;
	public final Member member;

	public CommandContext(GenericInteractionCreateEvent event) {
		if (event instanceof SlashCommandInteractionEvent evt) {
			this.event = evt;
		}
		else {
			this.event = null;
		}

		this.guild = event.getGuild();
		this.user = event.getUser();
		this.member = event.getMember();
	}
}
