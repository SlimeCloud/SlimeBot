package com.slimebot.commands;

import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.util.Util;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.events.Listener;
import de.mineking.discord.events.interaction.StringSelectHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

@ApplicationCommand(name = "poll", description = "")
public class PollCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
	                           @Option(name = "question") String question,
	                           @Option(name = "choice1") String choice1,
	                           @Option(name = "choice2") String choice2,
	                           @Option(name = "choice3", required = false) String choice3,
	                           @Option(name = "choice4", required = false) String choice4,
	                           @Option(name = "choice5", required = false) String choice5,
	                           @Option(name = "choice6", required = false) String choice6,
	                           @Option(name = "choice7", required = false) String choice7,
	                           @Option(name = "choice8", required = false) String choice8,
	                           @Option(name = "choice9", required = false) String choice9,
	                           @Option(name = "choice10", required = false) String choice10
	) {
		String[] options = new String[] {choice1, choice2, choice3, choice4, choice5, choice6, choice7, choice8, choice9, choice10};


		EmbedBuilder embed =
				new EmbedBuilder()
						.setTitle("Abstimmung")
						.setDescription(question)
						.setColor(GuildConfig.getColor(event.getGuild()))
						.setTimestamp(Util.timestamp());

		for (int i = 0; i < options.length; i++) {
			String option = options[i];
			if (option==null) continue;
			embed.addField("#" + (i+1) + "  " + option, "(0) **0%**  " + createProgressbar(0), false);
		}


		StringSelectMenu.Builder select = StringSelectMenu.create("poll:select")
						.setPlaceholder("Wähle weise");

		for (int i = 0; i < options.length; i++) {
			String option = options[i];
			if (option==null) continue;
			select.addOption("#" + (i+1) + "  " + option, "#" + (i+1));
		}

		event.replyEmbeds(embed.build()).addActionRow(select.build()).queue();
	}

	@Listener(type = StringSelectHandler.class, filter = "poll:select")
	public void handle(StringSelectInteractionEvent event) {
		MessageEmbed embed = event.getMessage().getEmbeds().get(0);
		EmbedBuilder builder = new EmbedBuilder(embed).clearFields();

		int totalCount = 1;
		for (MessageEmbed.Field field : embed.getFields()) {
			if (!field.getName().startsWith("#")) continue;
			totalCount += Integer.parseInt(field.getValue().split("\\)")[0].replace("(", ""));
		}
		for (int i = 0; i < embed.getFields().size(); i++) {
			MessageEmbed.Field field = embed.getFields().get(i);
			String option = field.getName();
			if (option==null || !option.startsWith("#")) break;
			int count = Integer.parseInt(field.getValue().split("\\)")[0].replace("(", ""));
			if (event.getSelectedOptions().get(0).getValue().equals(option.split(" ")[0])) count++;
			double percentage = totalCount==0 ? 0 : ((double) count /totalCount);
			builder.addField(option, "(" + count + ") **" + Util.padRight(((int)(percentage*100)) + "%**  ", 8) + createProgressbar(percentage), false);
		}

		event.editMessageEmbeds(builder.build()).queue();
	}

	private String createProgressbar(double value) {
		value*=400;
		StringBuilder sb = new StringBuilder(value>=10 ? "\\|||" : "|");
		for (int v = (int) Math.round(value); v >= 10; v-=10) {
			sb.append('░');
		}
		sb.append(value>=0.10 ? "||" : "");
		return Util.padRight(sb.toString(), '░', 40) + "|";
	}
}