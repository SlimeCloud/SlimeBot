package de.slimecloud.slimeball.config;

import de.mineking.discordutils.console.RedirectTarget;
import de.slimecloud.slimeball.config.engine.Required;
import org.jetbrains.annotations.NotNull;

public record LogForwarding(@Required Type type, @Required String id) {
	enum Type {
		USER {
			@NotNull
			@Override
			public RedirectTarget build(@NotNull String id) {
				return RedirectTarget.directMessage(Long.parseLong(id));
			}
		},
		CHANNEL {
			@NotNull
			@Override
			public RedirectTarget build(@NotNull String id) {
				return RedirectTarget.channel(Long.parseLong(id));
			}
		},
		WEBHOOK {
			@NotNull
			@Override
			public RedirectTarget build(@NotNull String value) {
				return RedirectTarget.webhook(value);
			}
		};

		@NotNull
		public abstract RedirectTarget build(@NotNull String value);
	}

	@NotNull
	public RedirectTarget build() {
		return type.build(id);
	}
}
