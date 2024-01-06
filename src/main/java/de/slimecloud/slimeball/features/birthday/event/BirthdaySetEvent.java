package de.slimecloud.slimeball.features.birthday.event;

import de.cyklon.jevent.CancellableEvent;
import de.slimecloud.slimeball.features.birthday.Birthday;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BirthdaySetEvent extends CancellableEvent {

	@NotNull
	private final Member member;
	@NotNull
	private Birthday newBirthday;

}
