package de.slimecloud.slimeball.features.level.card;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.types.Component;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.modal.ModalMenu;
import de.mineking.discordutils.ui.modal.TextComponent;
import de.mineking.javautils.database.Column;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.ValidationException;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.ColorUtil;
import de.slimecloud.slimeball.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ApplicationCommand(name = "card", description = "Verwaltet deine Rank-Card", scope = Scope.GUILD)
public class CardCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	@ApplicationCommand(name = "info", description = "Zeigt deine aktuellen Einstellungen an", defer = true)
	public static class InfoCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) throws Exception {
			CardProfile profile = bot.getLevelProfiles().getProfile(event.getMember()).render();

			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("RankCard Einstellungen")
					.setColor(bot.getColor(event.getGuild()))
					.setImage("attachment://image.png");

			for (Field f : CardProfile.class.getDeclaredFields()) {
				if (!f.isAnnotationPresent(Column.class) || f.getAnnotation(Column.class).key()) continue;

				f.setAccessible(true);

				Object value = f.get(profile);
				Object def = f.get(CardProfile.DEFAULT);

				if (Objects.equals(value, def)) continue;

				if (f.getType().isAssignableFrom(Color.class)) value = ColorUtil.toString((Color) value);

				embed.addField(StringUtil.prettifyCamelCase(f.getName()), value == null ? "*Nicht gesetzt*" : value.toString(), false);
			}

			event.getHook().editOriginalEmbeds(embed.build()).setFiles(profile.getFile()).queue();
		}
	}

	@ApplicationCommand(name = "edit", description = "Öffnet ein Menü um deine Einstellungen zu verändern", defer = true)
	public static class EditCommand {
		private final MessageMenu menu;

		public EditCommand(@NotNull SlimeBot bot, @NotNull UIManager manager) {
			//Modal input menu
			ModalMenu input = manager.createModal(
					"card.edit.modal",
					state -> StringUtil.prettifyCamelCase(state.getState("field")),
					List.of(new TextComponent("value", "Der neue Wert für diese Eigenschaft", TextInputStyle.SHORT)
							.setPlaceholder(s -> s.<String>getState("field").contains("Color") ? "Hex-Code, z.B. #00ff00" : null)
							.setRequired(s -> false)
					),
					(state, response) -> {
						try {
							bot.getLevelProfiles().getProfile(state.event.getMember()).set(state.getState("field"), response.getString("value")).update();
							manager.getMenu("card.edit").display(state.event);
						} catch (ValidationException e) {
							manager.getMenu("card.edit").display(state.event);
							state.event.getHook().sendMessage(":x: Ungültige Eingabe!").setEphemeral(true).queue();
						}
					}
			);

			//Build components
			List<ComponentRow> components = new ArrayList<>();

			List<Component<?>> temp = new ArrayList<>();
			String last = null;

			for (Field field : CardProfile.class.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Column.class) || field.getAnnotation(Column.class).key()) continue;
				field.setAccessible(true);

				String category = StringUtil.parseCamelCase(field.getName())[0];
				if (!category.equals(last) && !temp.isEmpty()) {
					components.add(ComponentRow.of(temp));
					temp = new ArrayList<>();
				}

				last = category;

				if (field.getType().isAssignableFrom(Style.class)) temp.add(0, new StyleComponent(field));
				else temp.add(new ButtonComponent(field.getName(), ButtonColor.GRAY, StringUtil.prettifyCamelCase(field.getName())).appendHandler(s -> {
					input.createState()
							.setState("field", field.getName())
							.display((IModalCallback) s.event);
				}));
			}

			if (!temp.isEmpty()) components.add(ComponentRow.of(temp));

			//Build main menu
			this.menu = manager.createMenu(
					"card.edit",
					MessageRenderer.embed(s -> new EmbedBuilder()
							.setTitle("Aktuelle RankCard")
							.setColor(bot.getColor(s.event.getGuild()))
							.setImage("attachment://image.png")
							.build()
					).withFile(s -> s.<CardProfile>getCache("profile").render().getFile()),
					components
			).cache(state -> state.setCache("profile", bot.getLevelProfiles().getProfile(state.event.getMember())));
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlashCommandInteractionEvent event) {
			menu.display(event);
		}
	}

	@ApplicationCommand(name = "reset", description = "Setzt deine Einstellungen zurück")
	public static class ResetCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			bot.getLevelProfiles().reset(event.getMember());

			//Send confirmation
			event.reply("Einstellungen zurückgesetzt").setEphemeral(true).queue();
		}
	}
}
