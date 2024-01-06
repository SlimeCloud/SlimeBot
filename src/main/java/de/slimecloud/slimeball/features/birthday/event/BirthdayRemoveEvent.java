package de.slimecloud.slimeball.features.birthday.event;

import de.cyklon.jevent.CancellableEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class BirthdayRemoveEvent extends CancellableEvent {

	@NotNull
	private final Member member;

}
