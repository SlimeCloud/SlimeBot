package com.slimebot.commands;


import com.slimebot.main.Main;
import com.slimebot.utils.Checks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Objects;

public class BulkAddRole extends ListenerAdapter {
	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
		if(!event.getFullCommandName().equals("role_check")) return;

		if(Checks.hasTeamRole(event.getMember(), event.getGuild())) {
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
				.setTimestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()))
				.setColor(Main.embedColor(event.getGuild().getId()))
				.setTitle(":white_check_mark: Rollen Verteilt")
				.setDescription("Die Rolle " + roleOption.getAsRole().getAsMention() + " wurde " + (long) memberWithout.size() + " Membern gegeben!");

		event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
	}
}
