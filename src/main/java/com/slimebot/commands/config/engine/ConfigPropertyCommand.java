package com.slimebot.commands.config.engine;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.main.CommandContext;
import com.slimebot.main.Main;
import de.mineking.discord.commands.inherited.BaseCommand;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.lang.reflect.Field;

public class ConfigPropertyCommand extends BaseCommand<CommandContext> {
	private final Field field;
	private final ConfigField info;

	private final ConfigCategory category;

	private final InstanceProvider instanceProvider;

	public ConfigPropertyCommand(Field field, ConfigField info, ConfigCategory category, InstanceProvider instanceProvider) {
		this.field = field;
		this.info = info;
		this.category = category;
		this.instanceProvider = instanceProvider;

		description = info.description();

		addOption(info.type().builder.apply(info));
	}

	@Override
	public void performCommand(CommandContext context, GenericCommandInteractionEvent event) {
		if(event.getOptions().isEmpty()) {
			ConfigCommand.updateField(event.getGuild(), config -> {
				try {
					Object instance = instanceProvider.getInstance(false, config);

					if(instance == null) return;

					field.set(instanceProvider, null);
				} catch (Exception e) {
					ConfigCommand.logger.error("Fehler beim zugreifen auf die Konfigurationskategorie", e);
				}
			});

			event.reply(info.title() + " zurückgesetzt").setEphemeral(true).queue();

			if(category.updateCommands()) {
				Main.updateGuildCommands(event.getGuild());
			}

			return;
		}

		OptionMapping option = event.getOptions().get(0);

		String text;
		Object value;

		try {
			//Role and Channel
			IMentionable temp = option.getAsMentionable();
			text = temp.getAsMention();
			value = temp.getIdLong();
		} catch(IllegalStateException e) {
			//String
			text = option.getAsString();
			value = text;
		}

		final Object finalValue = value; //Because java

		ConfigCommand.updateField(event.getGuild(), config -> {
			try {
				field.set(instanceProvider.getInstance(true, config), finalValue);
			} catch (Exception e) {
				ConfigCommand.logger.error("Fehler beim zugreifen auf die Konfigurationskategorie", e);
			}
		});

		if(category.updateCommands()) {
			Main.updateGuildCommands(event.getGuild());
		}

		event.replyFormat("%s auf %s gesetzt", info.title(), text).setEphemeral(true).queue();
	}
}
