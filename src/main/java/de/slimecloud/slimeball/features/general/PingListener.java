package de.slimecloud.slimeball.features.general;

import de.slimecloud.slimeball.main.SlimeEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PingListener extends ListenerAdapter {
	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (!event.isFromGuild()) return;
		if (!event.getMessage().getMentions().isMentioned(event.getGuild().getSelfMember())) return;

		event.getMessage().addReaction(SlimeEmoji.SUS.getEmoji(event.getGuild())).queue();
	}
}
