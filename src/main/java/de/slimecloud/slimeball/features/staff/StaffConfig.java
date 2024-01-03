package de.slimecloud.slimeball.features.staff;

import de.slimecloud.slimeball.config.ConfigCategory;
import de.slimecloud.slimeball.config.engine.ConfigField;
import de.slimecloud.slimeball.config.engine.ConfigFieldType;
import de.slimecloud.slimeball.config.engine.KeyType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Getter
public class StaffConfig extends ConfigCategory {
	@ConfigField(name = "Einträge", command = "entries", description = "Einträge für die Nachricht (key: Rollen ID, value: Beschreibung)", type = ConfigFieldType.STRING)
	@KeyType(ConfigFieldType.STRING)
	private final LinkedHashMap<String, String> roles = new LinkedHashMap<>();

	@ConfigField(name = "Kanal", command = "channel", description = "Kanal, in dem die Team-Rollen erklärt werden", type = ConfigFieldType.MESSAGE_CHANNEL, required = true)
	private Long channel;
	private Long message;

	@Override
	public void enable(@NotNull Guild guild) {
		message = getChannel().map(channel -> channel
				.sendMessage("*Keine Einträge*")
				.complete().getIdLong()
		).orElseThrow();
	}

	@Override
	public void disable(@NotNull Guild guild) {
		if (message == null) return;
		getChannel().ifPresent(channel -> channel.deleteMessageById(message).queue());
	}

	@Override
	public void update(@NotNull Guild guild) {
		if (message == null) enable(guild);
		getChannel().ifPresent(channel -> channel.editMessageById(message, buildMessage(channel.getGuild())).queue());
	}

	@NotNull
	public Optional<GuildMessageChannel> getChannel() {
		return Optional.ofNullable(channel).map(id -> bot.getJda().getChannelById(GuildMessageChannel.class, id));
	}

	@NotNull
	public MessageEditData buildMessage(@NotNull Guild guild) {
		StringBuilder builder = new StringBuilder();

		roles.forEach((roleId, description) -> {
			try {
				Role role = guild.getRoleById(roleId);

				List<Member> members = guild.getMembersWithRoles(role);

				builder.append(role.getAsMention()).append(" **").append(description).append("**\n");

				if (members.isEmpty()) builder.append("*Keine Mitglieder*").append("\n");
				else {
					for (Member member : members) {
						builder.append("> ").append(member.getAsMention()).append("\n");
					}
				}

				builder.append("\n");
			} catch (NumberFormatException e) {
				builder.append(description).append("\n\n");
			}
		});

		if (builder.isEmpty()) builder.append("*Keine Einträge*");

		return MessageEditData.fromContent(builder.toString());
	}
}
