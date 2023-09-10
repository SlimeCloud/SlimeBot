package com.slimebot.commands.config.setup;

import com.slimebot.commands.config.engine.ConfigFieldType;
import com.slimebot.commands.config.setup.engine.CustomSetupFrame;
import com.slimebot.main.config.guild.AutoDeleteConfig;
import com.slimebot.main.config.guild.GuildConfig;
import de.mineking.discord.ui.Menu;
import de.mineking.discord.ui.MenuBase;
import de.mineking.discord.ui.components.ComponentRow;
import de.mineking.discord.ui.components.button.ToggleButton;
import de.mineking.discord.ui.components.button.ToggleHolder;
import de.mineking.discord.ui.components.select.EntitySelectComponent;
import de.mineking.discord.ui.components.select.StringSelectComponent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.*;
import java.util.stream.Collectors;

public class AutoDeleteFrame extends CustomSetupFrame {
	protected AutoDeleteFrame(Menu menu, long guild) {
		super("auto-delete", menu, guild,
				ConfigFieldType.CHANNEL.getEmoji() + "Auto-Delete Kanäle",
				"In diesen Kanälen werden Nachrichten von Nicht-Moderatoren gelöscht, wenn sie bestimmte Bedingungen nicht erfüllen"
		);

		menu.addMessageFrame("auto-delete-config", m ->
						new EmbedBuilder()
								.setColor(GuildConfig.getColor(guild))
								.setTitle("Auto-Delete konfiguration")
								.setDescription("Konfiguriere welche Arten von Nachrichten in " + m.getData("channel", MessageChannel.class).getAsMention() + " erlaubt sind, alle anderen werden gelöscht!")
								.build(),
				config -> config.addComponents(
						ComponentRow.of(
								Arrays.stream(AutoDeleteConfig.Filter.values())
										.map(f -> new ToggleButton(f.toString(), new ToggleHolder() {
											@Override
											public void setState(boolean state, MenuBase menu, ButtonInteractionEvent event) {
												GuildConfig.getConfig(guild).getAutoDeleteConfig().ifPresent(a -> {
													String id = menu.getData("channel", MessageChannel.class).getId();
													if (a.autoDeleteChannels.containsKey(id)) {
														a.autoDeleteChannels.get(id).remove(f);
														if (state) a.autoDeleteChannels.get(id).add(f);
													}
												});
											}

											@Override
											public boolean getState(MenuBase menu) {
												return GuildConfig.getConfig(guild).getAutoDeleteConfig().map(a -> {
													String id = menu.getData("channel", MessageChannel.class).getId();
													return a.autoDeleteChannels.containsKey(id) && a.autoDeleteChannels.get(id).contains(f);
												}).orElse(false);
											}
										}, ToggleButton.redGreen, f.getName()))
										.toList()
						)
				)
		);
	}

	@Override
	public Optional<String> getValue(GuildConfig config) {
		return config.getAutoDeleteConfig().map(AutoDeleteConfig::getAutoDeleteChannels).map(channels -> channels.entrySet().stream()
				.map(e -> e.getKey().getAsMention() + ": " + e.getValue().stream().map(AutoDeleteConfig.Filter::toString).collect(Collectors.joining(",")))
				.collect(Collectors.joining("\n"))
		);
	}

	@Override
	public Collection<ComponentRow> getComponents() {
		return Arrays.asList(
				new EntitySelectComponent("add", config -> config
						.setPlaceholder("Kanal hinzufügen")
						.setChannelTypes(ChannelType.TEXT),
						EntitySelectMenu.SelectTarget.CHANNEL
				).addHandler((m, event) -> {
					m.putData("channel", event.getValues().get(0));
					GuildConfig.getConfig(guild).getOrCreateAutoDelete().autoDeleteChannels.put(event.getValues().get(0).getId(), EnumSet.noneOf(AutoDeleteConfig.Filter.class));
					m.display("auto-delete-config");
				}),
				new StringSelectComponent("remove", config -> {
					config.setPlaceholder("Kanal entfernen");

					Map<MessageChannel, EnumSet<AutoDeleteConfig.Filter>> data = GuildConfig.getConfig(guild).getAutoDeleteConfig().map(AutoDeleteConfig::getAutoDeleteChannels).orElse(Collections.emptyMap());

					if (data.isEmpty()) config.addOption("---", "---").setDisabled(true);
					else config.addOptions(
							data.entrySet().stream()
									.map(e -> SelectOption.of(e.getKey().getName(), e.getKey().getId())
											.withDescription(e.getValue().stream().map(Enum::toString).collect(Collectors.joining(",")))
									)
									.toList()
					);
				}).addHandler((m, event) -> {
					GuildConfig.getConfig(guild).getAutoDeleteConfig().ifPresent(a -> a.autoDeleteChannels.remove(event.getSelectedOptions().get(0).getValue()));
					m.update();
				})
		);
	}
}
