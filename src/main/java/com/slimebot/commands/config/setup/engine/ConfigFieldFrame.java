package com.slimebot.commands.config.setup.engine;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.commands.config.engine.ConfigCategory;
import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.InstanceProvider;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.MessageFrame;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;
import de.mineking.discord.ui.components.select.EntitySelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigFieldFrame extends MessageFrame {
	private final String name;
	private final ConfigCategory category;

	private final ConfigField info;
	private final Field field;
	private final InstanceProvider instanceProvider;
	private final long guild;

	public ConfigFieldFrame(Menu menu, long guild, ConfigCategory category, Field field, ConfigField info, InstanceProvider instanceProvider, String name, String last, String next) {
		super(menu, () -> {
			String formattedValue = "*Kein Wert*";

			try {
				Object instance = instanceProvider.getInstance(false, GuildConfig.getConfig(guild));
				Object value = instance == null ? null : field.get(instance);

				if(value != null) {
					formattedValue = info.type().formatter.apply(value);
				}
			} catch(Exception e) {
				ConfigCommand.logger.error("Fehler beim auslesen des aktuellen Konfigurationswerts für " + field.getName(), e);
			}

			if(info.title().isEmpty())  System.out.println(field.getName());

			return new EmbedBuilder()
					.setTitle(info.type().emoji + info.title())
					.setColor(GuildConfig.getColor(guild))
					.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
					.setDescription(info.description())
					.addField("Aktueller Wert", formattedValue, false)
					.build();
		});

		this.name = name;
		this.category = category;

		this.info = info;
		this.field = field;
		this.instanceProvider = instanceProvider;
		this.guild = guild;

		switch(info.type()) {
			case ROLE -> addComponents(
					new EntitySelectComponent("role",
							config -> config.setPlaceholder("Rolle festlegen"),
							EntitySelectMenu.SelectTarget.ROLE
					).handle((m, evt) -> setValue(m, evt, evt.getValues().get(0).getIdLong()))
			);
			case CHANNEL -> addComponents(
					new EntitySelectComponent("channel",
							config -> config
									.setPlaceholder("Kanal festlegen")
									.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS),
							EntitySelectMenu.SelectTarget.CHANNEL
					).handle((m, evt) -> setValue(m, evt, evt.getValues().get(0).getIdLong()))
			);
			case STRING -> {
				addComponents(new FrameButton(ButtonColor.BLUE, "Wert festlegen", info.title()));
				menu.addModalFrame(info.title(), "Wert festlegen",
						modal -> modal.addActionRow(
								TextInput.create("value", "Wert", TextInputStyle.SHORT)
										.build()
						),
						(m, evt) -> setValue(m, evt, evt.getValue("value").getAsString())
				);
			}
		}

		List<ButtonComponent> components = new ArrayList<>();

		components.add(
				new ButtonComponent("reset", ButtonColor.RED, "Wert zurücksetzten").handle((m, evt) -> {
					ConfigCommand.updateField(guild, config -> {
						try {
							Object instance = instanceProvider.getInstance(false, GuildConfig.getConfig(guild));

							if (instance != null) {
								field.set(instance, null);
							}

							if(category.updateCommands()) {
								Main.updateGuildCommands(evt.getGuild());
							}

						} catch (Exception e) {
							ConfigCommand.logger.error("Fehler beim zurücksetzten von " + field.getName(), e);
						}
					});

					menu.update();
				})
		);

		components.add(new FrameButton(ButtonColor.GRAY, "Zurück", last));

		if(!last.equals("main") && next != null) {
			components.add(new FrameButton(ButtonColor.GRAY, "Hauptmenü", "main"));
		}

		if(!(last.equals("main") && next == null)) {
			components.add(new FrameButton(ButtonColor.GRAY, "Weiter", next == null ? "main" : next));
		}

		addComponents(ComponentRow.of(components));
	}

	private void setValue(MenuBase menu, IReplyCallback event, Object value) {
		if(!info.verifier().verifier.test(value)) {
			menu.display(name);
			event.getHook().sendMessage("Ungültiger Wert").setEphemeral(true).queue();
			return;
		}

		ConfigCommand.updateField(guild, config -> {
			try {
				field.set(instanceProvider.getInstance(false, config), value);
			} catch (Exception e) {
				ConfigCommand.logger.error("Fehler beim setzten von " + field.getName(), e);
			}
		});

		if(category.updateCommands()) {
			Main.updateGuildCommands(event.getGuild());
		}

		menu.display(name);
	}
}
