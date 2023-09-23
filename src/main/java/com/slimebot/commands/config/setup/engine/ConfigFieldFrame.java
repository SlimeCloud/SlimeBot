package com.slimebot.commands.config.setup.engine;

import com.slimebot.commands.config.ConfigCommand;
import com.slimebot.commands.config.engine.ConfigCategory;
import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.InstanceProvider;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.MessageFrameBase;
import de.mineking.discord.ui.components.Component;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ButtonColor;
import de.mineking.discord.ui.components.button.ButtonComponent;
import de.mineking.discord.ui.components.button.FrameButton;
import de.mineking.discord.ui.components.select.EntitySelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConfigFieldFrame extends MessageFrameBase {
	private final String name;
	private final ConfigCategory category;

	private final ConfigField info;
	private final Field field;
	private final InstanceProvider instanceProvider;

	private final String last;
	private final String next;

	public ConfigFieldFrame(Menu menu, ConfigCategory category, Field field, ConfigField info, InstanceProvider instanceProvider, String name, String last, String next) {
		super(menu);

		this.name = name;
		this.category = category;

		this.info = info;
		this.field = field;
		this.instanceProvider = instanceProvider;

		this.last = last;
		this.next = next;
	}

	@Override
	public MessageEmbed getEmbed() {
		String formattedValue = "*Kein Wert*";

		try {
			Object instance = instanceProvider.getInstance(false, GuildConfig.getConfig(menu.getGuild()));
			Object value = instance == null ? null : field.get(instance);

			if (value != null) {
				formattedValue = info.type().getFormatter().apply(value);
			}
		} catch (Exception e) {
			ConfigCommand.getLogger().error("Fehler beim auslesen des aktuellen Konfigurationswerts für " + field.getName(), e);
		}

		return new EmbedBuilder()
				.setTitle(info.type().getEmoji() + info.title())
				.setColor(GuildConfig.getColor(menu.getGuild()))
				.setThumbnail(Main.jdaInstance.getSelfUser().getEffectiveAvatarUrl())
				.setDescription(info.description())
				.addField("Aktueller Wert", formattedValue, false)
				.build();
	}

	@Override
	public Collection<ComponentRow> getComponents() {
		Collection<ComponentRow> result = new ArrayList<>();

		switch (info.type()) {
			case ROLE -> result.add(
					new EntitySelectComponent("role",
							config -> config.setPlaceholder("Rolle festlegen"),
							EntitySelectMenu.SelectTarget.ROLE
					).addHandler((m, evt) -> setValue(m, evt, evt.getValues().get(0).getIdLong()))
			);
			case CHANNEL -> result.add(
					new EntitySelectComponent("channel",
							config -> config
									.setPlaceholder("Kanal festlegen")
									.setChannelTypes(ChannelType.TEXT, ChannelType.NEWS),
							EntitySelectMenu.SelectTarget.CHANNEL
					).addHandler((m, evt) -> setValue(m, evt, evt.getValues().get(0).getIdLong()))
			);
			case STRING -> {
				result.add(new FrameButton(ButtonColor.BLUE, "Wert festlegen", info.title()));
				menu.addModalFrame(info.title(), "Wert festlegen",
						modal -> modal.addActionRow(
								TextInput.create("value", "Wert", TextInputStyle.SHORT)
										.build()
						),
						(m, evt) -> setValue(m, evt, evt.getValue("value").getAsString())
				);
			}
		}

		List<Component<?>> temp = new ArrayList<>();

		temp.add(
				new ButtonComponent("reset", ButtonColor.RED, "Wert zurücksetzten").addHandler((m, evt) -> {
					ConfigCommand.updateField(menu.getGuild(), config -> {
						try {
							Object instance = instanceProvider.getInstance(false, GuildConfig.getConfig(menu.getGuild()));

							if (instance != null) {
								field.set(instance, null);
							}

							if (category.updateCommands()) {
								Main.updateGuildCommands(evt.getGuild());
							}

						} catch (Exception e) {
							ConfigCommand.getLogger().error("Fehler beim zurücksetzten von " + field.getName(), e);
						}
					});

					menu.update();
				})
		);

		temp.add(new FrameButton(ButtonColor.GRAY, "Zurück", last));

		if (!last.equals("main") && next != null) {
			temp.add(new FrameButton(ButtonColor.GRAY, "Hauptmenü", "main"));
		}

		if (!(last.equals("main") && next == null)) {
			temp.add(new FrameButton(ButtonColor.GRAY, "Weiter", next == null ? "main" : next));
		}

		result.add(ComponentRow.of(temp));

		return result;
	}

	private void setValue(MenuBase menu, IReplyCallback event, Object value) {
		if (!info.verifier().getVerifier().test(value)) {
			menu.display(name);
			event.getHook().sendMessage("Ungültiger Wert").setEphemeral(true).queue();
			return;
		}

		ConfigCommand.updateField(menu.getGuild(), config -> {
			try {
				Object instance = instanceProvider.getInstance(true, config);

				if (instance == null) return;

				field.set(instance, value);
			} catch (Exception e) {
				ConfigCommand.getLogger().error("Fehler beim setzten von " + field.getName(), e);
			}
		});

		if (category.updateCommands()) {
			Main.updateGuildCommands(event.getGuild());
		}

		menu.display(name);
	}
}
