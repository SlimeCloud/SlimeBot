package de.slimecloud.slimeball.config;

import de.mineking.discordutils.console.RedirectTarget;
import de.slimecloud.slimeball.config.engine.Required;

public record LogForwarding(@Required Type type, @Required Long id) {
	enum Type {
		USER {
			@Override
			public RedirectTarget build(long id) {
				return RedirectTarget.directMessage(id);
			}
		},
		CHANNEL {
			@Override
			public RedirectTarget build(long id) {
				return RedirectTarget.channel(id);
			}
		};

		public abstract RedirectTarget build(long id);
	}

	public RedirectTarget build() {
		return type.build(id);
	}
}
