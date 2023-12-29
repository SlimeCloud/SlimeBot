package de.slimecloud.slimeball.features.moderation;

import de.slimecloud.slimeball.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

import java.util.function.Predicate;

@Getter
@AllArgsConstructor
public enum AutodleteFlag {
	IMAGE("Bilder", m -> m.getContentRaw().isEmpty() && !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isImage)),
	IMAGE_WITH_TEXT("Beschriftete Bilder", m -> !m.getContentRaw().isEmpty() && !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isImage)),
	VIDEO("Video", m -> m.getContentRaw().isEmpty() && !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isVideo)),
	VIDEO_WITH_TEXT("Beschriftetes Video", m -> !m.getContentRaw().isEmpty() && !m.getAttachments().isEmpty() && m.getAttachments().stream().allMatch(Message.Attachment::isVideo)),
	LINK("Links", m -> m.getAttachments().isEmpty() && StringUtil.isValidURL(m.getContentRaw())),
	INTEGER("Zahlen", m -> m.getAttachments().isEmpty() && StringUtil.isInteger(m.getContentRaw()));

	private final String name;
	private final Predicate<Message> filter;
}
