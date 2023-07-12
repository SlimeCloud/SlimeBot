package com.slimebot.commands;


import com.slimebot.main.Checks;
import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

public class BulkAddRole extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if(!event.getFullCommandName().equals("role_check")) return;

		if(Checks.hasTeamRole(event.getMember())) {
			event.reply("Dieser befehl kann nur von einem Teammitglied ausgeführt werden").setEphemeral(true).queue();
			return;
		}

		ArrayList<Member> memberWithout = new ArrayList<>();

		OptionMapping botsOption = event.getOption("bots");
		OptionMapping roleOption = event.getOption("rolle");

		for(Member member : event.getGuild().getMembers()) {
			if(!botsOption.getAsBoolean() && member.getUser().isBot()) {
				continue;
			}

			if(member.getRoles().contains(roleOption.getAsRole())) {
				continue;
			}

			event.getGuild().addRoleToMember(member, Objects.requireNonNull(event.getGuild().getRoleById(roleOption.getAsString()))).queue();
			memberWithout.add(member);
		}

		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setTimestamp(Instant.now())
				.setColor(Main.database.getColor(event.getGuild()))
				.setTitle(":white_check_mark: Rollen Verteilt")
				.setDescription("Die Rolle " + roleOption.getAsRole().getAsMention() + " wurde " + (long) memberWithout.size() + " Membern gegeben!");

		event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
	}
}
