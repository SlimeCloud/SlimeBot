package de.slimecloud.slimeball.features.level;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import org.jetbrains.annotations.NotNull;

@Getter
public class UserLevelUpEvent extends UserGainXPEvent {
	private final int oldLevel;
	private final int newLevel;

	public UserLevelUpEvent(@NotNull Member user, @NotNull Type type, int oldXp, int newXp, int deltaXp, int oldLevel, int newLevel) {
		super(user, type, oldLevel, oldXp, newXp, deltaXp);
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
	}
}
