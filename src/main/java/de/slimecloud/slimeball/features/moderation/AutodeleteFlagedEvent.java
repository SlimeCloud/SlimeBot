package de.slimecloud.slimeball.features.moderation;

import de.cyklon.jevent.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;

@Getter
@AllArgsConstructor
public class AutodeleteFlagedEvent extends CancellableEvent {
	private final boolean thread;
	private final Channel channel;
	private final Message message;
}
