package de.slimecloud.slimeball.features.level;

import de.slimecloud.slimeball.config.LevelConfig;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.attribute.ICategorizableChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
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

	public LevelListener(@NotNull SlimeBot bot) {
		this.bot = bot;
		this.config = bot.getConfig().getLevel().orElseThrow(); //This should always be present when reached
	}

	@Override
	public void onReady(@NotNull ReadyEvent event) {
		bot.getExecutor().scheduleAtFixedRate(
				this::updateVoiceXp,
				0, config.getVoiceLevelingInterval(), TimeUnit.SECONDS
		);
	}

	@Override
	public void onGuildBan(@NotNull GuildBanEvent event) {
		bot.getLevel().reset(event.getGuild(), event.getUser());
	}

	@Override
	public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
		Level level = bot.getLevel().getLevel(event.getMember());
		LevelUpListener.updateLevelRoles(bot, event.getMember(), level.getLevel());
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		//Check for valid environment
		if (!event.isFromGuild() || event.getAuthor().isBot() || isBlacklisted((GuildChannel) event.getChannel())) return;

		//Check timeout
		if (messageTimeout.getOrDefault(event.getAuthor().getIdLong(), 0L) + config.getMessageCooldown() >= System.currentTimeMillis()) return;
		messageTimeout.put(event.getAuthor().getIdLong(), System.currentTimeMillis());

		//Add xp
		bot.getLevel().addMessageXp(event.getMember(), event.getMessage().getContentRaw());
	}

	private void updateVoiceXp() {
		bot.getJda().getVoiceChannels().forEach(channel -> {
			List<Member> members = getValidChannelMembers(channel);

			members.forEach(m -> bot.getLevel().addVoiceXp(m));
		});
	}

	private List<Member> getValidChannelMembers(@Nullable AudioChannel channel) {
		if (channel == null || isBlacklisted(channel)) return Collections.emptyList();

		//Find valid members
		List<Member> validMembers = channel.getMembers().stream()
				.filter(m -> !m.getUser().isBot()) //Ignore bots
				.filter(m -> !m.getVoiceState().isMuted()) //Ignore muted members
				.toList();

		if (validMembers.size() < 2) return Collections.emptyList();
		return validMembers;
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
