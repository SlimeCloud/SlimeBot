package com.slimebot.commands.config.engine;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.main.CommandContext;
import de.mineking.discord.commands.inherited.BaseCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import java.lang.reflect.Field;
import java.util.List;

public class ConfigArrayPropertyCommand extends BaseCommand<CommandContext> {

	private final Field field;
	private final ConfigField info;

	private final ConfigCategory category;

	private final InstanceProvider instanceProvider;
	private final ConfigArrayPropertyCommandType type;


	public ConfigArrayPropertyCommand(Field field, ConfigField info, ConfigCategory category, InstanceProvider instanceProvider, ConfigArrayPropertyCommandType type) {
		this.field = field;
		this.info = info;
		this.category = category;
		this.instanceProvider = instanceProvider;
		this.type = type;

		description = info.description();

		addOption(info.type().getBuilder().apply(info));
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void performCommand(CommandContext ctx, GenericCommandInteractionEvent event) {
		Object value = info.type().getData().apply(event.getOptions().get(0));
		String text = info.type().getFormatter().apply(value);


		switch (type) {
			case ADD -> ConfigCommand.updateField(event.getGuild(), config -> {
				try {
					List field = (List) this.field.get(instanceProvider.getInstance(true, config));

					if (field.contains(value))
						event.replyFormat("%s ist in %s bereits vorhanden", text, info.title()).setEphemeral(true).queue();
					else {
						field.add(value);
						event.replyFormat("%s wurde zu %s hinzugefügt", text, info.title()).setEphemeral(true).queue();
					}


				} catch (Exception e) {
					ConfigCommand.getLogger().error("Fehler beim zugreifen auf die Konfigurationskategorie", e);
				}
			});


			case REMOVE -> ConfigCommand.updateField(event.getGuild(), config -> {
				try {
					if (((List) field.get(instanceProvider.getInstance(true, config))).remove(value))
						event.replyFormat("%s aus %s entfernt", text, info.title()).setEphemeral(true).queue();
					else
						event.replyFormat("%s ist in %s nicht vorhanden", text, info.title()).setEphemeral(true).queue();

				} catch (Exception e) {
					ConfigCommand.getLogger().error("Fehler beim zugreifen auf die Konfigurationskategorie", e);
				}
			});

		}
	}
}
