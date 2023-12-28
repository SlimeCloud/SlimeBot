package de.slimecloud.slimeball.events;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class UserQuotedEvent extends CancellableEvent {
	private final UserSnowflake user;
	private final Member target;

	private final String content;

	@Nullable
	private final Message reference;
}
