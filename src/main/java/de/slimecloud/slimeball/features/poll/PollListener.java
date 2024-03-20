package de.slimecloud.slimeball.features.poll;

import de.slimecloud.slimeball.main.SlimeBot;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@RequiredArgsConstructor
public class PollListener extends ListenerAdapter {
	private final SlimeBot bot;

	@Override
	public void onMessageDelete(@NotNull MessageDeleteEvent event) {
		//Delete poll. The database will just ignore this if no poll was found, so this should not cause any problems
		bot.getPolls().delete(event.getMessageIdLong());
	}

	@Override
	//Synchronized to avoid conflicts
	public synchronized void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
		if (!event.getComponentId().equals("poll:select")) return;

		event.deferEdit().queue();

		//Parse options
		List<Integer> options = event.getSelectedOptions().stream()
				.map(SelectOption::getValue)
				.map(Integer::parseInt)
				.filter(i -> i >= 0)
				.toList();

		//Handle poll
		bot.getPolls().getPoll(event.getMessageIdLong()).ifPresent(poll -> {
			//Update database, -1 is considered a reset
			poll.updateSelection(event.getUser().getId(), options).update();

			//Update message
			MessageEmbed old = event.getMessage().getEmbeds().get(0);
			String[] temp = old.getDescription().split("## Ergebnisse\n\n", 2);
			event.getHook().editOriginalEmbeds(new EmbedBuilder(old)
					.clearFields() //To keep old polls intact
					.setDescription((old.getFields().isEmpty() ? temp[0] : old.getDescription() + "\n") + "## Ergebnisse\n\n" + poll.buildChoices(event.getGuild()))
					.build()
			).setActionRow(poll.buildMenu(event.getGuild())).queue();
		});
	}
}
