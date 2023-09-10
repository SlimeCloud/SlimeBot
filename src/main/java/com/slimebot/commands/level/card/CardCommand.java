package com.slimebot.commands.level.card;

import com.slimebot.commands.level.card.frame.*;
import com.slimebot.database.DataClass;
import com.slimebot.database.Key;
import com.slimebot.level.Level;
import com.slimebot.level.RankCard;
import com.slimebot.level.profile.CardProfile;
import com.slimebot.level.profile.Style;
import com.slimebot.main.Main;
import com.slimebot.main.config.guild.GuildConfig;
import com.slimebot.util.ColorUtil;
import com.slimebot.util.Util;
import de.mineking.discord.commands.annotated.ApplicationCommand;
import de.mineking.discord.commands.annotated.ApplicationCommandMethod;
import de.mineking.discord.commands.annotated.option.Option;
import de.mineking.discord.events.Listener;
import de.mineking.discord.events.interaction.ButtonHandler;
import de.mineking.discord.ui.CallbackState;
import de.mineking.discord.ui.Menu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.function.Supplier;

import static com.slimebot.util.ColorUtil.parseColor;

@ApplicationCommand(name = "card", description = "passe deine rank card an", feature = "level")
public class CardCommand {

	private static Menu test() {
		return Main.discordUtils.getUIManager().createMenu()
				.addFrame("main", MainFrame::new)
				.addFrame("avatar", AvatarFrame::new)
				.addFrame("background", BackgroundFrame::new)
				.addFrame("background.modal", BackgroundModalFrame::new)
				.addFrame("progressbar", ProgressbarFrame::new)
				.addFrame("progressbar.color", ProgressbarColorFrame::new)
				.addFrame("border", BorderFrame::new);
	}

	private static CardProfile loadProfile(Member member) {
		Supplier<CardProfile> sup = () -> new CardProfile(member.getGuild().getIdLong(), member.getIdLong());
		return DataClass.load(sup, Map.of("guild", member.getGuild().getIdLong(), "user", member.getIdLong())).orElseGet(sup);
	}

	private static void sendResult(IReplyCallback event) {
		sendResult(event, true, true);
	}

	private static void sendResult(IReplyCallback event, boolean deferred, boolean includeMsg) {
		FileUpload file = new RankCard(Level.getLevel(event.getMember())).getFile();
		MessageCreateData data = new MessageCreateBuilder()
				.setContent(includeMsg ? "Deine Änderungen wurden gespeichert!" : null)
				.setFiles(file)
				.build();
		RestAction<?> action = deferred ? event.getHook().sendMessage(data).setEphemeral(true) : event.reply(data).setEphemeral(true);
		action.queue();
	}

	private static boolean validateColor(IReplyCallback event, Color c, String color, boolean deferred) {
		if (c == null) {
			String msg = "**Farbe '_" + color + "_' ist ungültig!**";
			RestAction<?> action = deferred ? event.getHook().sendMessage(msg).setEphemeral(true) : event.reply(msg).setEphemeral(true);
			action.queue();
			return true;
		}
		return false;
	}

	@Listener(type = ButtonHandler.class, filter = "cardprofile:reset")
	public void resetProfile(ButtonInteractionEvent event) {
		event.deferReply(true).queue();
		new CardProfile(event.getGuild().getIdLong(), event.getUser().getIdLong()).save();
		sendResult(event);
	}

	@ApplicationCommand(name = "test")
	public static class TestCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			test().start(new CallbackState(event), "main");
		}

	}

	@ApplicationCommand(name = "info", description = "zeigt deine aktuellen rankcard optionen an")
	public static class InfoCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			CardProfile cp = loadProfile(event.getMember());
			EmbedBuilder builder = new EmbedBuilder();
			Field[] fields = cp.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (!DataClass.isValid(field) || field.isAnnotationPresent(Key.class)) continue;
				try {
					field.setAccessible(true);
					String value;
					if (field.getName().toLowerCase().contains("color") && (field.getType().equals(Integer.class) || field.getType().equals(int.class)))
						value = ColorUtil.toString(new Color(field.getInt(cp)));
					else value = String.valueOf(field.get(cp));
					builder.addField(String.join(" ", Util.parseCamelCase(field.getName().replace("BG", "Background"))), value, false);
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			builder.setColor(GuildConfig.getColor(event.getGuild()));
			event.getHook().sendMessageEmbeds(builder.build()).queue();
		}

	}

	@ApplicationCommand(name = "reset", description = "setze deine rankcard auf standard zurück")
	public static class ResetCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			MessageEmbed embed = new EmbedBuilder()
					.setTitle("Warnung!")
					.setDescription("""
							Bist du sicher das du deine rankcard zurücksetzen möchtest?
							Das zurücksetzen **kann nicht rückgändig gemacht werden!**
														
							**Unwiderruflich zurücksetzen?**
							""")
					.setColor(new Color(218, 55, 60))
					.build();
			event.reply(new MessageCreateBuilder()
					.setEmbeds(embed)
					.setActionRow(Button.danger("cardprofile:reset", Emoji.fromUnicode("✔")))
					.build()).setEphemeral(true).queue();
		}

	}

	@ApplicationCommand(name = "preview", description = "Zeigt deine aktuelle rankcard an")
	public static class PreviewCommand {

		@ApplicationCommandMethod
		public void performCommand(SlashCommandInteractionEvent event) {
			event.deferReply(true).queue();
			sendResult(event, true, false);
		}

	}

	@ApplicationCommand(name = "progressbar", description = "passe deine progressbar an")
	public static class ProgressbarCommand {

		@ApplicationCommand(name = "color", description = "ändere die farbe der progressbar")
		public static class ColorCommand {
			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "color", description = "hex code", required = false) String color) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (color != null) {
					Color c = parseColor(color);
					if (validateColor(event, c, color, true)) return;
					cp.setProgressBarColor(c.getRGB());
				} else cp.reset("progressBarColor");
				cp.save();
				sendResult(event);
			}
		}

		@ApplicationCommand(name = "background-color", description = "ändere die hintergrund farbe der progressbar")
		public static class BackgroundColorCommand {
			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "color", description = "hex code", required = false) String color) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (color != null) {
					Color c = parseColor(color);
					if (validateColor(event, c, color, true)) return;
					cp.setProgressBarBGColor(c.getRGB());
				} else cp.reset("progressBarBGColor");
				cp.save();
				sendResult(event);
			}
		}


		@ApplicationCommand(name = "style", description = "ändere den style der progressbar")
		public static class StyleCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(required = false) Style style) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (style != null) cp.setProgressBarStyle(style);
				else cp.reset("progressBarStyle");
				cp.save();
				sendResult(event);
			}
		}

		@ApplicationCommand(name = "border-color", description = "passe die border farbe der progressbar an")
		public static class BorderColorCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "color", description = "hex code", required = false) String color) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (color != null) {
					Color c = parseColor(color);
					if (validateColor(event, c, color, true)) return;
					cp.setProgressBarBorderColor(c.getRGB());
				} else cp.reset("progressBarBorderColor");
				cp.save();
				sendResult(event);
			}

		}


		@ApplicationCommand(name = "border-width", description = "passe die border größe der progressbar an")
		public static class BorderWidthCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "width", description = "border größe", minValue = 0, required = false) Integer width) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (width != null) cp.setProgressBarBorderWidth(width);
				else cp.reset("progressBarBorderWidth");
				cp.save();
				sendResult(event);
			}

		}
	}

	@ApplicationCommand(name = "avatar", description = "passe deine avatar an")
	public static class AvatarCommand {

		@ApplicationCommand(name = "style", description = "ändere den style deines avatars")
		public static class StyleCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(required = false) Style style) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (style != null) cp.setAvatarStyle(style);
				else cp.reset("avatarStyle");
				cp.save();
				sendResult(event);
			}
		}


		@ApplicationCommand(name = "border-color", description = "passe die border farbe deines avatars an")
		public static class BorderColorCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "color", description = "hex code", required = false) String color) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (color != null) {
					Color c = parseColor(color);
					if (validateColor(event, c, color, true)) return;
					cp.setAvatarBorderColor(c.getRGB());
				} else cp.reset("avatarBorderColor");
				cp.save();
				sendResult(event);
			}

		}


		@ApplicationCommand(name = "border-width", description = "passe die border größe deines avatars an")
		public static class BorderWidthCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "width", description = "border größe", minValue = 0, required = false) Integer width) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (width != null) cp.setAvatarBorderWidth(width);
				else cp.reset("avatarBorderWidth");
				cp.save();
				sendResult(event);
			}

		}
	}


	@ApplicationCommand(name = "background", description = "passe deine hintergrund an")
	public static class BackgroundCommand {

		@ApplicationCommand(name = "color", description = "ändere die farbe deines hintergrundes")
		public static class ColorCommand {
			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "color", description = "hex code", required = false) String color) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (color != null) {
					Color c = parseColor(color);
					if (validateColor(event, c, color, true)) return;
					cp.setBackgroundColor(c.getRGB());
				} else cp.reset("backgroundColor");
				cp.save();
				sendResult(event);
			}
		}

		@ApplicationCommand(name = "image", description = "setze die bild url für den hintergrund")
		public static class ImageCommand {
			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "url", required = false) String url) {
				if (url != null && !url.isBlank() && !Util.isValidURL(url)) {
					event.reply("Die URL '" + url + "' ist ungültig!").setEphemeral(true).queue();
					return;
				}
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (url != null) cp.setBackgroundImageURL(url);
				else cp.reset("backgroundImageURL");
				cp.save();
				sendResult(event);
			}
		}


		@ApplicationCommand(name = "border-color", description = "passe die border farbe deines hintergrundes an")
		public static class BorderColorCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "color", description = "hex code", required = false) String color) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (color != null) {
					Color c = parseColor(color);
					if (validateColor(event, c, color, true)) return;
					cp.setBackgroundBorderColor(c.getRGB());
				} else cp.reset("backgroundBorderColor");
				cp.save();
				sendResult(event);
			}

		}


		@ApplicationCommand(name = "border-width", description = "passe die border größe deines hintergrundes an")
		public static class BorderWidthCommand {

			@ApplicationCommandMethod
			public void performCommand(SlashCommandInteractionEvent event, @Option(name = "width", description = "border größe", minValue = 0, required = false) Integer width) {
				event.deferReply(true).queue();
				CardProfile cp = loadProfile(event.getMember());
				if (width != null) cp.setBackgroundBorderWidth(width);
				else cp.reset("backgroundBorderWidth");
				cp.save();
				sendResult(event);
			}

		}
	}

}
