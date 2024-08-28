package de.slimecloud.slimeball.features.staff.meeting.protocol;

import ai.picovoice.leopard.Leopard;
import ai.picovoice.leopard.LeopardException;
import ai.picovoice.leopard.LeopardTranscript;
import de.mineking.discordutils.commands.ApplicationCommand;
import de.mineking.discordutils.commands.ApplicationCommandMethod;
import de.slimecloud.slimeball.main.SlimeBot;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MeetingProtocol {

	private static final File protocolDirectory = new File("protocol");

	static {
		protocolDirectory.mkdirs();
	}

	private final String picovoice_access_key;
	private final File leopard_model;
	@Getter
	private AudioReceiver receiver;

	public MeetingProtocol(String picovoice_access_key) {
		this.picovoice_access_key = picovoice_access_key;
		URL url = MeetingProtocol.class.getClassLoader().getResource("leopard_params_de.pv");
		if (url == null || url.getPath().isBlank()) throw new RuntimeException("Leopard model not found");
		this.leopard_model = new File(url.getPath());
	}

	public void start(VoiceChannel vc) {
		receiver = new AudioReceiver();
		Guild guild = vc.getGuild();
		AudioManager audioManager = guild.getAudioManager();
		audioManager.openAudioConnection(vc);
		audioManager.setReceivingHandler(receiver);
	}

	@SneakyThrows(LeopardException.class)
	public void stop(Guild guild) {
		JDA jda = guild.getJDA();

		List<User> users = new ArrayList<>();
		for (Long id : receiver.getUsers()) users.add(jda.getUserById(id));

		for (User user : users) {
			try (AudioInputStream ais = receiver.getAudioStream(user.getIdLong())) {
				AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(protocolDirectory, String.format("%s.wav", user.getName())));
			} catch (IOException e) {
				logger.error("failed to save protocol as wav from " + user.getName(), e);
			}
		}

		Leopard leopard = new Leopard.Builder()
				.setAccessKey(picovoice_access_key)
				.setModelPath(leopard_model.getAbsolutePath())
				.build();

		for (User user : users) {
			try {
				File file = new File(protocolDirectory, String.format("%s.wav", user.getName()));
				if (!file.exists()) {
					logger.debug("skip user " + user.getName() + " for processing because the " + file.getName() + " file is missing");
					continue;
				}
				LeopardTranscript transcript = leopard.processFile(file.getAbsolutePath());
				file.delete();

				try (FileOutputStream fos = new FileOutputStream(new File(protocolDirectory, user.getName() + ".txt"))) {
					fos.write(transcript.getTranscriptString().getBytes());
				} catch (IOException e) {
					logger.error("failed to save protocol transcript from " + user.getName(), e);
				}
			} catch (LeopardException e) {
				logger.error("failed to process protocol from " + user.getName(), e);
			}
		}
		leopard.delete();

		AudioManager audioManager = guild.getAudioManager();
		audioManager.setReceivingHandler(null);
		audioManager.closeAudioConnection();
		receiver = null;
	}
}
