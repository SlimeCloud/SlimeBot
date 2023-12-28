package de.slimecloud.slimeball.features.level;

import de.slimecloud.slimeball.config.LevelConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LevelListener extends ListenerAdapter {
	private final SlimeBot bot;
	private final LevelConfig config;

	private final Map<Long, Long> messageTimeout = new HashMap<>();
	private final Map<Long, Long> voiceUsers = new HashMap<>();

	public LevelListener(@NotNull SlimeBot bot) {
		this.bot = bot;
		this.config = bot.getConfig().getLevel().orElseThrow(); //This should always be present when reached
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		//Update on startup
		event.getJDA().getVoiceChannels().forEach(this::updateChannel);

		//Schedule update
		bot.getExecutor().scheduleAtFixedRate(
				() -> voiceUsers.forEach((user, guild) -> bot.getLevel().addVoiceXp(event.getJDA().getGuildById(guild).getMemberById(user))),
				0, config.getVoiceLevelingInterval(), TimeUnit.SECONDS
		);
	}

	@Override
	public void onGuildBan(@NotNull GuildBanEvent event) {
		bot.getLevel().reset(event.getGuild().getIdLong(), event.getUser());
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		//Check for valid environment
		if (!event.isFromGuild() || event.getAuthor().isBot() || isBlacklisted((GuildChannel) event.getChannel()))
			return;

		//Check timeout
		if (messageTimeout.getOrDefault(event.getAuthor().getIdLong(), 0L) + config.getMessageCooldown() >= System.currentTimeMillis())
			return;
		messageTimeout.put(event.getAuthor().getIdLong(), System.currentTimeMillis());

		//Add xp
		bot.getLevel().addMessageXp(event.getMember(), event.getMessage().getContentRaw());
	}

	@Override
	public void onGenericGuildVoice(@NotNull GenericGuildVoiceEvent event) {
		if (event instanceof GuildVoiceUpdateEvent update) {
			if (update.getChannelLeft() != null && update.getChannelJoined() == null)
				voiceUsers.remove(event.getMember().getIdLong());

			//Update channels
			updateChannel(update.getChannelLeft());
			updateChannel(update.getChannelJoined());
		} else updateChannel(event.getVoiceState().getChannel());
	}

	private void updateChannel(@Nullable AudioChannel channel) {
		if (channel == null) return;

		//Find valid members
		List<Member> validMembers = channel.getMembers().stream()
				.filter(m -> !m.getUser().isBot()) //Ignore bots
				.filter(m -> !m.getVoiceState().isMuted()) //Ignore muted members
				.toList();

		//If channel has 2 valid members and the channel is not blacklisted -> mark channel for leveling
		if (validMembers.size() >= 2 && !isBlacklisted(channel))
			validMembers.forEach(m -> voiceUsers.put(m.getIdLong(), channel.getGuild().getIdLong()));
		else channel.getMembers().forEach(m -> voiceUsers.remove(m.getIdLong()));
	}

	private boolean isBlacklisted(@NotNull GuildChannel channel) {
		//load blacklist
		List<Long> blacklist = bot.loadGuild(channel.getGuild()).getLevel()
				.map(GuildLevelConfig::getChannelBlacklist)
				.orElse(Collections.emptyList());

		//Check blocklist for channel,
		return blacklist.contains(channel.getIdLong())
				//If channel has category: Check category blacklist
				|| (channel instanceof ICategorizableChannel cc && blacklist.contains(cc.getParentCategoryIdLong()))

				//If channel is ThreadChannel check parent channel and parent channel category
				|| (channel instanceof ThreadChannel tc && (blacklist.contains(tc.getParentChannel().getIdLong()) || (tc.getParentChannel() instanceof ICategorizableChannel tpc && blacklist.contains(tpc.getParentCategoryIdLong())))
		);
	}
}
