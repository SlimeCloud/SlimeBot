package de.slimecloud.slimeball.features.level.card;

import de.mineking.discordutils.DiscordUtils;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.mineking.discordutils.commands.Command;
import de.mineking.discordutils.commands.Setup;
import de.mineking.discordutils.commands.condition.IRegistrationCondition;
import de.mineking.discordutils.commands.condition.Scope;
import de.mineking.discordutils.commands.context.ICommandContext;
import de.mineking.discordutils.commands.option.Autocomplete;
import de.mineking.discordutils.commands.option.Option;
import de.mineking.discordutils.list.ListManager;
import de.mineking.discordutils.ui.MessageMenu;
import de.mineking.discordutils.ui.MessageRenderer;
import de.mineking.discordutils.ui.UIManager;
import de.mineking.discordutils.ui.components.button.ButtonColor;
import de.mineking.discordutils.ui.components.button.ButtonComponent;
import de.mineking.discordutils.ui.components.types.Component;
import de.mineking.discordutils.ui.components.types.ComponentRow;
import de.mineking.discordutils.ui.modal.ModalMenu;
import de.mineking.discordutils.ui.modal.TextComponent;
import de.mineking.discordutils.ui.state.UpdateState;
import de.mineking.javautils.ID;
import de.mineking.javautils.database.Where;
import de.slimecloud.slimeball.config.GuildConfig;
import de.slimecloud.slimeball.config.engine.Info;
import de.slimecloud.slimeball.config.engine.ValidationException;
import de.slimecloud.slimeball.main.SlimeBot;
import de.slimecloud.slimeball.util.ColorUtil;
import de.slimecloud.slimeball.util.StringUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IModalCallback;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ApplicationCommand(name = "card", description = "Verwaltet deine Rank-Card", scope = Scope.GUILD)
public class CardCommand {
	public final IRegistrationCondition<ICommandContext> condition = (manager, guild, cache) -> cache.<GuildConfig>getState("config").getLevel().isPresent();

	@Setup
	public static void setup(@NotNull SlimeBot bot, @NotNull DiscordUtils<?> discordUtils, @NotNull Command<ICommandContext> command, @NotNull ListManager<ICommandContext> manager) {
		command.addSubcommand(manager.createCommand(
				(ctx, state) -> state.setState("filter", ctx.getEvent().getOption("filter").getAsString()),
				state -> bot.getProfileData()
		).withDescription("Zeigt alle Profile an").addOption(new OptionData(OptionType.STRING, "filter", "Ein Filter der angibt, welche Profile angezeigt werden", true).addChoices(
				Arrays.stream(Filter.values())
						.map(f -> new Choice(f.getName(), f.name()))
						.toList()
		)));
	}

	@ApplicationCommand(name = "info", description = "Zeigt deine aktuellen Einstellungen an", defer = true)
	public static class InfoCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) throws Exception {
			CardProfileData profile = bot.getCardProfiles().getProfile(event.getMember()).getData();

			EmbedBuilder embed = new EmbedBuilder()
					.setTitle("RankCard Einstellungen")
					.setColor(bot.getColor(event.getGuild()))
					.setImage("attachment://image.png");

			for (Field f : CardProfileData.class.getDeclaredFields()) {
				if (!f.isAnnotationPresent(Info.class)) continue;

				f.setAccessible(true);

				Object value = f.get(profile);
				Object def = f.get(CardProfileData.DEFAULT);

				if (Objects.equals(value, def)) continue;

				if (f.getType().isAssignableFrom(Color.class)) value = ColorUtil.toString((Color) value);

				embed.addField(StringUtil.prettifyCamelCase(f.getName()), value == null ? "*Nicht gesetzt*" : value.toString(), false);
			}

			if (embed.getFields().isEmpty()) embed.setDescription("*Keine Konfiguration, es werden die Standardwerte verwendet*");

			event.getHook().editOriginalEmbeds(embed.build()).setFiles(profile.render(event.getMember()).getFile()).queue();
		}
	}

	@ApplicationCommand(name = "edit", description = "Öffnet ein Menü um deine Einstellungen zu verändern", defer = true)
	public static class EditCommand {
		private final MessageMenu menu;
		private final MessageMenu confirmation;

		public EditCommand(@NotNull SlimeBot bot, @NotNull UIManager manager) {
			//Modal input menu
			ModalMenu input = manager.createModal(
					"card.edit.modal",
					state -> StringUtil.prettifyCamelCase(state.getState("field", String.class)),
					List.of(new TextComponent("value", "Der neue Wert für diese Eigenschaft", TextInputStyle.SHORT)
							.setValue(s -> bot.getCardProfiles().getProfile(s.getEvent().getMember()).getData().get(s.getState("field", String.class)))
							.setPlaceholder(s -> (s.getState("field", String.class).contains("Color") ? "Hex-Code, z.B. #00ff00. " : "") + "Leer lassen um zurück zusetzten")
							.setRequired(s -> false)
					),
					(state, response) -> {
						try {
							bot.getCardProfiles().getProfile(state.getEvent().getMember()).getData().set(state.getState("field", String.class), response.getString("value")).upsert();
							manager.getMenu("card.edit").display(state.getEvent());
						} catch (ValidationException e) {
							manager.getMenu("card.edit").display(state.getEvent());
							state.getEvent().getHook().sendMessage(":x: Ungültige Eingabe!").setEphemeral(true).queue();
						}
					}
			);

			//Build components
			List<ComponentRow> components = new ArrayList<>();

			List<Component<?>> temp = new ArrayList<>();
			String last = null;

			for (Field field : CardProfileData.class.getDeclaredFields()) {
				if (!field.isAnnotationPresent(Info.class)) continue;
				field.setAccessible(true);

				String[] name = StringUtil.parseCamelCase(field.getName());
				String category = name.length > 1 ? name[0] : "font";

				if (!category.equals(last) && !temp.isEmpty()) {
					components.add(ComponentRow.of(temp));
					temp = new ArrayList<>();
				}

				last = category;

				if (field.getType().isAssignableFrom(Style.class)) temp.add(new StyleComponent(field));
				else temp.add(0, new ButtonComponent(field.getName(), name.length > 1 ? ButtonColor.GRAY : ButtonColor.BLUE, StringUtil.prettifyCamelCase(field.getName())).appendHandler(s ->
						input.createState()
								.setState("field", field.getName())
								.display((IModalCallback) s.getEvent())
				));
			}

			if (!temp.isEmpty()) components.add(ComponentRow.of(temp));

			//Build main menu
			this.menu = manager.createMenu(
					"card.edit",
					MessageRenderer.embed(s -> new EmbedBuilder()
							.setTitle("Aktuelle RankCard (**" + s.<CardProfileData>getCache("profile").getName() + "**, " + s.<CardProfileData>getCache("profile").getId() + ")")
							.setColor(bot.getColor(s.getEvent().getGuild()))
							.setImage("attachment://image.png")
							.build()
					).withFile(s -> s.<CardProfileData>getCache("profile").render(s.getEvent().getMember()).getFile()),
					components
			).cache(state -> state.setCache("profile", bot.getCardProfiles().getProfile(state.getEvent().getMember()).getData()));

			//Build confirmation
			this.confirmation = manager.createMenu(
					"card.confirm",
					MessageRenderer.embed(s -> new EmbedBuilder()
							.setTitle("Keine Rechte zum Bearbeiten")
							.setColor(bot.getColor(s.getEvent().getGuild()))
							.setDescription("Du bist nicht der eigentümer des Profiles, das du aktuell verwendest. Um es zu bearbeiten, musst du eine Kopie des Profils erstellen")
							.build()
					),
					ComponentRow.of(
							new ButtonComponent("confirm", ButtonColor.GREEN, "Profil kopieren").appendHandler(s -> {
								bot.getCardProfiles().getProfile(s.getEvent().getMember()).getData().createCopy(s.getEvent().getMember()).upsert();
								menu.display(s.getEvent());
							}),
							new ButtonComponent("cancel", ButtonColor.RED, "Abbrechen").appendHandler(UpdateState::close)
					)
			);
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			//Load current profile
			GuildCardProfile profile = bot.getCardProfiles().getProfile(event.getMember());
			CardProfileData data = profile.getData();

			//If id is null this is a default profile
			if (data.getId() == null) {
				if (bot.getProfileData().getAll(event.getUser()).size() >= bot.getConfig().getLevel().get().getMaxUserProfiles()) {
					event.getHook().editOriginal(":x: Du hast bereits die maximale Anzahl an Profiles erreicht! Verwende " + bot.getDiscordUtils().getCommandManager().getCommand(DeleteCommand.class).getAsMention(event.getGuild().getIdLong()) + " um ein unbenutztes Profil zu löschen").queue();
					return;
				}

				data.upsert(); //Create new storage, will generate id
				profile.setId(data.getId()).upsert(); //Set newly generated data as current profile
			}

			//Check if member is owner
			if (data.getPermission(event.getMember()).canWrite()) menu.display(event);
			else confirmation.display(event);
		}
	}

	//The description does not describe what actually happens but how it feels for the user. Describing what actually happens would lead to confusion
	@ApplicationCommand(name = "default", description = "Erstellt und lädt ein neues Standard-Profil; dein bisheriges Profil wird NICHT gelöscht!")
	public static class ResetCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			bot.getCardProfiles().reset(event.getMember());

			//Send confirmation
			event.reply("Standardprofil geladen").setEphemeral(true).queue();
		}
	}

	@ApplicationCommand(name = "delete", description = "Löscht ein gespeichertes Profil")
	public static class DeleteCommand {
		@Autocomplete("id")
		public void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			event.replyChoices(
					bot.getProfileData().getAll(event.getUser()).stream()
							.filter(d -> !d.isPublic())
							.filter(d -> d.getPermission(event.getUser()).canWrite())
							.filter(d -> d.getId().asString().contains(event.getFocusedOption().getValue()))
							.map(d -> {
								String id = d.getId().asString();
								Member m = event.getGuild().getMember(d.getOwner());

								return new Choice(id + " (von " + (m != null ? m.getEffectiveName() : "Unbekannt") + ")", id);
							})
							.toList()
			).queue();
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "ID des Profils") ID id
		) {
			//We can pass null as owner here because it is only used when id = null which is impossible here
			bot.getProfileData().getData(id, null).ifPresentOrElse(
					data -> {
						//Cannot delete public profiles for security reasons
						if (data.isPublic()) {
							event.reply(":x: Du kannst kein öffentliches Profil löschen. Stelle es zunächst wieder auf privat!").setEphemeral(true).queue();
							return;
						}

						//Check permission
						if (data.getPermission(event.getMember()).canWrite()) {
							data.delete();

							bot.getCardProfiles().delete(Where.equals("id", data.getId()));

							event.reply("Profil mit ID **" + id + "** gelöscht").setEphemeral(true).queue();
						} else event.reply(":no_entry_sign: Du hast keinen Zugriff auf dieses Profil").setEphemeral(true).queue();
					},
					() -> event.reply(":x: Kein Profil mit der angegebenen ID gefunden").setEphemeral(true).queue()
			);
		}
	}

	@ApplicationCommand(name = "load", description = "Lädt ein bestehendes Profil")
	public static class LoadCommand {
		@Autocomplete("id")
		public void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			event.replyChoices(
					bot.getProfileData().selectAll().stream()
							.filter(d -> d.getPermission(event.getUser()).canRead())
							.filter(d -> d.getId().asString().contains(event.getFocusedOption().getValue()))
							.map(d -> {
								String id = d.getId().asString();
								String name = d.getName();
								Member m = event.getGuild().getMember(d.getOwner());

								return new Choice(name + " (" + id + " von " + (m != null ? m.getEffectiveName() : "Unbekannt") + ")", id);
							})
							.toList()
			).queue();
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "ID des Profils") ID id
		) {
			//We can pass null as owner here because it is only used when id <= 0 which is impossible here
			bot.getProfileData().getData(id, null).ifPresentOrElse(
					data -> {
						//Check permission
						if (data.getPermission(event.getMember()).canRead()) {
							//Reference profile in guild table
							bot.getCardProfiles().getProfile(event.getMember()).setId(data.getId()).upsert();

							event.reply("Profil mit ID **" + id + "** geladen")
									.setFiles(data.render(event.getMember()).getFile())
									.setEphemeral(true).queue();
						} else event.reply(":no_entry_sign: Du hast keinen Zugriff auf dieses Profil").setEphemeral(true).queue();
					},
					() -> event.reply(":x: Kein Profil mit der angegebenen ID gefunden").setEphemeral(true).queue()
			);
		}
	}

	@ApplicationCommand(name = "publish", description = "Macht eines deiner Profile für andere Mitglieder zugänglich")
	public static class PublishCommand {
		@Autocomplete("id")
		public void handleAutocomplete(@NotNull SlimeBot bot, @NotNull CommandAutoCompleteInteractionEvent event) {
			event.replyChoices(
					bot.getProfileData().getAll(event.getUser()).stream()
							.filter(d -> d.getPermission(event.getUser()).canWrite())
							.filter(d -> d.getId().asString().contains(event.getFocusedOption().getValue()))
							.map(d -> {
								String id = d.getId().asString();
								Member m = event.getGuild().getMember(d.getOwner());

								return new Choice(id + " (von " + (m != null ? m.getEffectiveName() : "Unbekannt") + ")", id);
							})
							.toList()
			).queue();
		}

		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event,
		                           @Option(description = "ID des Profils") ID id,
		                           @Option(description = "Ob das Profil öffentlich sein soll", name = "public") boolean isPublic
		) {
			//We can pass null as owner here because it is only used when id = null which is impossible here
			bot.getProfileData().getData(id, null).ifPresentOrElse(
					data -> {
						//Check permission
						if (data.getPermission(event.getMember()).canWrite()) {
							//Set new state
							data.setPublic(isPublic).update();

							if (isPublic) event.reply("Profil mit ID **" + id + "** kann jetzt von anderen Mitgliedern verwendet werden")
									.setFiles(data.render(event.getMember()).getFile())
									.setEphemeral(true).queue();

								//Remove profile from all users that are not the owner
							else {
								event.reply("Profil mit ID **" + id + "** kann nicht mehr von anderen Mitgliedern verwendet werden").setEphemeral(true).queue();

								bot.getCardProfiles().delete(Where.allOf(
										Where.equals("id", data.getId()),
										Where.not(Where.equals("user", data.getOwner()))
								));
							}
						} else event.reply(":no_entry_sign: Du hast keinen Zugriff auf dieses Profil").setEphemeral(true).queue();
					},
					() -> event.reply(":x: Kein Profil mit der angegebenen ID gefunden").setEphemeral(true).queue()
			);
		}
	}
}
