package com.slimebot.graphic;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;

import java.awt.*;

public class UIError {

    /**
     * Parameters: the invalid color
     */
    public static final UIError COLOR_ERROR = new UIError("""
            								Die Farbe *%s* ist ungültig!
            								
            								Bitte nutze eines der Folgenden Formate
            								
            								**hex:** *#543423*
            								**rgb:** *352345*
            								**rgb:** *30,45,64*
            								**rgba:** *40,46,46,200*
            """);

    /**
     * Parameters: the invalid url, example url extension
     */
    public static final UIError URL_ERROR = new UIError("""
            Die URL *%s* ist ungültig!
                        
            Bitte verwende eine Korrekte http url.
            *https://example.org/%s*
            """);

    private final String description;

    private UIError(String description) {
        this.description = description;
    }

    public void send(IDeferrableCallback callback, Object... args) {
        final MessageEmbed embed = new EmbedBuilder()
                .setTitle("Error")
                .setDescription(description.formatted(args))
                .setColor(new Color(200, 50, 50))
                .build();
        callback.getHook().sendMessageEmbeds(embed)
                .setEphemeral(true)
                .queue();
    }

}
