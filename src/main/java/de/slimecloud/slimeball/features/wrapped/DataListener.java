package de.slimecloud.slimeball.features.wrapped;

import com.vdurmont.emoji.EmojiParser;
import de.cyklon.jevent.EventHandler;
import de.cyklon.jevent.JEvent;
import de.slimecloud.slimeball.features.fdmds.FdmdsConfig;
import de.slimecloud.slimeball.features.fdmds.FdmdsCreateEvent;
import de.slimecloud.slimeball.features.fdmds.FdmdsSubmitedEvent;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.StringUtil;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.collections4.Bag;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DataListener extends ListenerAdapter {
	private final SlimeBot bot;
	private final Map<Long, Long> voiceUsers = new HashMap<>();

	public DataListener(@NotNull SlimeBot bot) {
		this.bot = bot;
		JEvent.getManager().registerListener(this);
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		//Add current voice users
		bot.getJda().getVoiceChannels().forEach(channel -> channel.getMembers().forEach(m -> {
			if (m.getUser().isBot()) return;
			voiceUsers.put(m.getIdLong(), channel.getIdLong());
		}));

		//Start schedule
		bot.getExecutor().scheduleAtFixedRate(() -> voiceUsers.forEach((user, channel) -> {
			//Read current data
			WrappedData data = bot.getWrappedData().getData(bot.getJda().getVoiceChannelById(channel).getGuild().getIdLong(), UserSnowflake.fromId(user));

			data.getVoice().compute(String.valueOf(channel), (k, v) -> v == null ? 1 : v + 1);

			//Save changes
			data.update();
		}), 0, 1, TimeUnit.MINUTES);
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (!event.isFromGuild()) return;
		if (event.getAuthor().isBot()) return;
		if (event.isWebhookMessage()) return;

		//Load current data
		WrappedData data = bot.getWrappedData().getData(event.getMember());

		//Save message in corresponding channel
		data.getMessages().compute(event.getMessage().getChannelId(), (k, v) -> v == null ? 1 : v + 1);

		//Save custom emoji usages
		Bag<CustomEmoji> emojis = event.getMessage().getMentions().getCustomEmojisBag();
		emojis.uniqueSet().forEach(emoji -> data.getEmotes().compute(emoji.getAsReactionCode(), (k, v) -> v == null ? emojis.getCount(emoji) : v + emojis.getCount(emoji)));

		EmojiParser.extractEmojis(event.getMessage().getContentRaw()).forEach(e -> data.getEmotes().compute(e, (k, v) -> v == null ? 1 : v + 1));

		//Save attachments
		data.setMedia(data.getMedia() + event.getMessage().getAttachments().size());

		//Save links
		data.setLinks(data.getLinks() + (int) Arrays.stream(event.getMessage().getContentRaw().split("\\s")).filter(StringUtil::isValidURL).count());

		//Save word count
		data.getWordCount().add(event.getMessage().getContentRaw().split("\\s").length);

		//Save changes
		data.update();
	}

	@Override
	public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
		if (event.getUser().isBot()) return;
		if (!event.isFromGuild()) return;

		//Load current data
		WrappedData data = bot.getWrappedData().getData(event.getMember());

		//Check if channel is FdmdS channel
		if (!bot.loadGuild(event.getGuild()).getFdmds().map(FdmdsConfig::getChannel)
				.map(channel -> channel.getIdLong() == event.getChannel().getIdLong())
				.orElse(false)) {
			//If not fdmds: count as emoji
			data.getEmotes().compute(event.getEmoji().getAsReactionCode(), (k, v) -> v == null ? 1 : v + 1);
		} else {
			//Add message. Because it is a set, duplicates don't matter
			data.getFdmdsParticipant().add(event.getMessageIdLong());
		}

		//Save changes
		data.update();
	}

	@EventHandler
	public void handleFdmdsSubmitted(@NotNull FdmdsSubmitedEvent event) {
		//Load current data
		WrappedData data = bot.getWrappedData().getData(event.getUser());

		//Update data
		data.setFdmdsSubmitted(data.getFdmdsSubmitted() + 1);

		//Save changes
		data.update();
	}

	@EventHandler
	public void handleFdmdsCreated(@NotNull FdmdsCreateEvent event) {
		//Load current data
		WrappedData data = bot.getWrappedData().getData(event.getTeam().getGuild().getIdLong(), event.getUser());

		//Update data
		data.setFdmdsAccepted(data.getFdmdsAccepted() + 1);

		//Save changes
		data.update();
	}

	@Override
	public void onChannelDelete(@NotNull ChannelDeleteEvent event) {
		bot.getWrappedData().getAll(event.getGuild()).forEach(data -> {
			//Get channel voice data
			Double minutes = data.getVoice().remove(event.getChannel().getId());

			//Add to tempvoice
			if (minutes != null) data.setTempVoice(data.getTempVoice() + (int) (double) minutes);

			//Save changes
			data.update();
		});
	}

	@Override
	public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
		if (event.getMember().getUser().isBot()) return;

		if (event.getChannelJoined() != null && event.getChannelJoined() instanceof VoiceChannel c) voiceUsers.put(event.getMember().getIdLong(), c.getIdLong());
		else voiceUsers.remove(event.getMember().getIdLong());
	}
}
