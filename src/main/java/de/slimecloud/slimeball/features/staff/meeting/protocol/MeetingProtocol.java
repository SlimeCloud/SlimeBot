package de.slimecloud.slimeball.features.staff.meeting.protocol;

import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.slimecloud.slimeball.main.SlimeBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;

public class MeetingProtocol {
	private static AudioReceiver receiver;

	private static void start(VoiceChannel vc) {
		receiver = new AudioReceiver();
		Guild guild = vc.getGuild();
		AudioManager audioManager = guild.getAudioManager();
		audioManager.openAudioConnection(vc);
		audioManager.setReceivingHandler(receiver);
	}

	private static void stop(Guild guild) {
		JDA jda = guild.getJDA();
		for (Long id : receiver.getUsers()) {
			try (AudioInputStream ais = receiver.getAudioStream(id)) {
				AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(String.format("%s.wav", jda.getUserById(id).getName())));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		AudioManager audioManager = guild.getAudioManager();
		audioManager.setReceivingHandler(null);
		audioManager.closeAudioConnection();
		receiver = null;
	}

	@ApplicationCommand(name = "start-recording", description = "test", defer = true)
	public static class StartCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			if (receiver != null) event.getHook().sendMessage("stop meeting recording first").queue();
			GuildVoiceState state = event.getMember().getVoiceState();
			if (state != null) {
				AudioChannelUnion union = state.getChannel();
				if (union != null) {
					start(union.asVoiceChannel());
					event.getHook().sendMessage("meeting recording started").queue();
				} else event.getHook().sendMessage("connect first to a voice channel").queue();
			} else event.getHook().sendMessage("connect first to a voice channel").queue();
		}
	}

	@ApplicationCommand(name = "stop-recording", description = "test", defer = true)
	public static class StopCommand {
		@ApplicationCommandMethod
		public void performCommand(@NotNull SlimeBot bot, @NotNull SlashCommandInteractionEvent event) {
			if (receiver == null) event.getHook().sendMessage("start meeting recording first").queue();
			stop(event.getGuild());
			event.getHook().sendMessage("meeting recording stopped!").queue();
		}
	}
}
