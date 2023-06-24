package com.slimebot.report.buttons;


import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

public class Close extends ListenerAdapter {

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        super.onButtonInteraction(event);

        if (!(event.getButton().getId().equals("close_report"))) {return;}

        String[] nameSplit = event.getButton().getLabel().split(" ");
        String reportID = nameSplit[1].substring(1);


        TextInput reason = TextInput.create("reason", "Wie Wurde mit dem Report Verfahren?", TextInputStyle.SHORT)
                .setRequired(true)
                .setPlaceholder("z. B. Warn, Kick, Mute, Ban, Nichts etc..")
                .build();

        TextInput id = TextInput.create("id", "ID des Reports der geschlossen wird", TextInputStyle.SHORT)
                .setRequired(true)
                .setValue(reportID)
                .setPlaceholder("Dieses Feld wird automatisch ausgefüllt!")
                .build();



        Modal close = Modal.create("close", "Close Report")
                .addActionRow(reason)
                .addActionRow(id)
                .build();


        event.replyModal(close).queue();


    }

}
