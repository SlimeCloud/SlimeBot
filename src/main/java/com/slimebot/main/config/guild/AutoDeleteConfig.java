package com.slimebot.main.config.guild;

import com.slimebot.util.Util;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AutoDeleteConfig {
	@Getter
	public enum Filter {
		IMAGE("Bilder", m -> m.getContentRaw().isEmpty() && !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isImage)),
		IMAGE_WITH_TEXT("Beschriftete Bilder", m -> !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isImage)),
		VIDEO("Video", m -> m.getContentRaw().isEmpty() && !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isVideo)),
		VIDEO_WITH_TEXT("Beschriftetes Video", m -> !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isVideo)),
		LINK("Links", m -> m.getAttachments().isEmpty() && Util.isValidURL(m.getContentRaw())),
		INTEGER("Zahlen", m -> m.getAttachments().isEmpty() && Util.isInteger(m.getContentRaw()));

		private final String name;
		private final Predicate<Message> filter;

		Filter(String name, Predicate<Message> filter) {
			this.name = name;
			this.filter = filter;
		}
	}

	public Map<String, EnumSet<Filter>> autoDeleteChannels = new HashMap<>();

	public Map<GuildChannel, EnumSet<Filter>> getAutoDeleteChannels() {
		return autoDeleteChannels.entrySet().stream()
				.map(e -> Map.entry(GuildConfig.getChannel(e.getKey(), GuildChannel.class), e.getValue()))
				.filter(e -> e.getKey().isPresent())
				.collect(Collectors.toUnmodifiableMap(
						e -> e.getKey().get(),
						Map.Entry::getValue
				));
	}
}
