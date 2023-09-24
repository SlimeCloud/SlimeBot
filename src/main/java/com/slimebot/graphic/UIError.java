package com.slimebot.graphic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.awt.*;

public enum UIError {
	/**
	 * Parameters: the invalid color
	 */
	COLOR("""
			Die Farbe *%s* ist ungültig!
			
			Bitte nutze eines der Folgenden Formate
			
			**hex:** *#543423*
			**rgb:** *352345*
			**rgb:** *30,45,64*
			**rgba:** *40,46,46,200*
			"""
	),

	/**
	 * Parameters: the invalid url, example url extension
	 */
	URL("""
			Die URL *%s* ist ungültig!
			            
			Bitte verwende eine Korrekte http url.
			*https://example.org/%s*
			"""
	),

	NUMBER("""
			Die Zahl *%s* is ungültig
			"""
	);

	private final String description;

	UIError(String description) {
		this.description = description;
	}

	public void send(IReplyCallback callback, Object... args) {
		callback.getHook().sendMessageEmbeds(
				new EmbedBuilder()
						.setTitle("Error")
						.setDescription(description.formatted(args))
						.setColor(new Color(200, 50, 50))
						.build()
		).setEphemeral(true).queue();
	}
}
