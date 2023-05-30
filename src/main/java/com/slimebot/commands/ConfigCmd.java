package com.slimebot.commands;

import com.slimebot.utils.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ConfigCmd extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);

        if (event.getName().equalsIgnoreCase("config")) {
            if (event.getInteraction().getMember().hasPermission(Permission.ADMINISTRATOR)) {
                try {
                    OptionMapping type = event.getOption("type");
                    OptionMapping field = event.getOption("field");
                    OptionMapping value = event.getOption("value");

                    switch (type.getAsString()) {
                        case "config":
                            Config.changeProperty(Config.botPath + event.getGuild().getId() + "/config.yml", field.getAsString(), value.getAsString());
                            break;
                    }

                    event.reply("Du hast erfolgreich Anpassungen getätigt!").setEphemeral(true).queue();
                } catch (Exception e) {
                    event.reply("Bei deinen Anpassungen sind fehler aufgetreten...").setEphemeral(true).queue();
                }
            } else {
                event.reply("Auf diesen Command hast du keinen Zugriff.").setEphemeral(true).queue();
            }
        }
    }
}
