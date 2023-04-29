package com.slimebot.commands;


import com.slimebot.main.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class Ping extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (!(event.getName().equals("ping"))){return;}

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Pong")
                .setDescription(event.getJDA().getGatewayPing() + "ms")
                .setColor(Main.embedColor);


        MessageEmbed em = embed.build();


        event.replyEmbeds(em).setEphemeral(true).queue();

    }
}
