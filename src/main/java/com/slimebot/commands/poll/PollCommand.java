package com.slimebot.commands.poll;

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

import java.time.Instant;

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
						.setTimestamp(Instant.now());

		for (int i = 0; i < options.length; i++) {
			String option = options[i];
			if (option==null) continue;
			embed.addField("#" + (i+1) + "  " + option, "(0) **0%**  " + createProgressbar(0), false);
		}

		long id = System.currentTimeMillis();

		StringSelectMenu.Builder select = StringSelectMenu.create("poll:%s:select".formatted(id))
						.setPlaceholder("Wähle weise");

		for (int i = 0; i < options.length; i++) {
			String option = options[i];
			if (option==null) continue;
			select.addOption("#" + (i+1) + "  " + option, "#" + (i+1));
		}

		new Poll(id, options.length+1).save();

		event.replyEmbeds(embed.build()).addActionRow(select.build()).queue();
	}

	@Listener(type = StringSelectHandler.class, filter = "poll:\\d+:select")
	public void handle(StringSelectInteractionEvent event) {
		long id = Long.parseLong(event.getComponentId().split(":")[1]);

		Poll poll = Poll.getPoll(id);

		MessageEmbed embed = event.getMessage().getEmbeds().get(0);
		EmbedBuilder builder = new EmbedBuilder(embed).clearFields();

		String msg = null;

		for (int i = 0; i < embed.getFields().size(); i++) {
			MessageEmbed.Field field = embed.getFields().get(i);
			String option = field.getName();
			if (option==null || !option.startsWith("#")) break;
			String optionNum = option.split(" ")[0];
			if (event.getSelectedOptions().get(0).getValue().equals(optionNum)) {
				Poll.Type type = poll.set(i, event.getMember().getIdLong());
				msg = (switch (type) {
					case SET -> "Du hast für option **%s** gestimmt.";
					case REMOVED -> "Du hast deine Stimme von option **%s** entfernt.";
					case REMOVED_SET -> "Du hast deine Stimme zu option **%s** geändert.";
				}).formatted(optionNum);
			}
		}

		poll.save();

		int totalCount = poll.getAll().size();

		int pad = 0;

		for (int i = 0; i < embed.getFields().size(); i++) {
			MessageEmbed.Field field = embed.getFields().get(i);
			String option = field.getName();
			if (option==null || !option.startsWith("#")) break;
			int count = poll.getOption(i).size();
			double percentage = totalCount==0 ? 0 : ((double) count/totalCount);
			pad = Math.max(pad, ("(" + count + ") **" + ((int)(percentage*100)) + "%**  ").length());
		}

		for (int i = 0; i < embed.getFields().size(); i++) {
			MessageEmbed.Field field = embed.getFields().get(i);
			String option = field.getName();
			if (option==null || !option.startsWith("#")) break;
			int count = poll.getOption(i).size();
			double percentage = totalCount==0 ? 0 : ((double) count/totalCount);
			builder.addField(option, Util.padRight("(" + count + ") **" + ((int)(percentage*100)) + "%**  ", '\u1CBC', pad) + createProgressbar(percentage), false);
		}

		event.editMessageEmbeds(builder.build()).queue();
		if (msg!=null) event.getHook().sendMessage(msg).setEphemeral(true).queue();
	}

	private String createProgressbar(double value) {
		value*=400;
		StringBuilder sb = new StringBuilder();
		for (int v = (int) Math.round(value); v >= 10; v-=10) {
			sb.append('░');
		}
		sb.append(value>=0.10 ? "||" : "");
		sb = Util.padRight(sb, '░', 40 + (value>=0.10 ? 2 : 0));
		sb.insert(0, value>=10 ? "\\|||" : "|");
		return sb + "|";
	}
}