package de.slimecloud.slimeball.features.level.card;

public enum Style {
	ROUND {
		@Override
		public String toString() {
			return "Rund";
		}
	},
	SQUARE {
		@Override
		public String toString() {
			return "Eckig";
		}
	}
}
