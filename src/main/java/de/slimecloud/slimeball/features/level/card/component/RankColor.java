package de.slimecloud.slimeball.features.level.card.component;

import org.jetbrains.annotations.NotNull;

public enum RankColor {
	FONT {
		@Override
		public String toString() {
			return "Schriftfarbe";
		}
	},
	ROLE {
		@Override
		public String toString() {
			return "Rollenfarbe";
		}
	};

	@NotNull
	public static RankColor ofId(int id) {
		return values()[id % values().length];
	}
}
