package com.slimebot.main.config.guild;

import com.slimebot.commands.config.engine.ConfigField;
import com.slimebot.commands.config.engine.ConfigFieldType;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

import java.util.Optional;

public class MeetingConfig {
	@ConfigField(type = ConfigFieldType.CHANNEL, title = "Meeting Kanal", description = "Hier wird die Agenda für Teambesprechungen geteilt und abgestimmt wer kommen kann", command = "meeting-channel")
	public Long meetingChannel;

	@ConfigField(type = ConfigFieldType.CHANNEL, title = "ToDo Kanal", description = "Hier werden aus Team Besprechungen resultierende ToDos verwaltet", command = "todo-channel")
	public Long todoChannel;

	@ConfigField(type = ConfigFieldType.VOICE_CHANNEL, title = "Meeting Voice Kanal", description = "Hier werden die Teammeetings abgehalten", command = "meeting-voice-channel")
	public Long voiceChannel;

	public Optional<GuildMessageChannel> getMeetingChannel() {
		return GuildConfig.getChannel(meetingChannel);
	}

	public Optional<GuildMessageChannel> getTodoChannel() {
		return GuildConfig.getChannel(todoChannel);
	}
	public Optional<AudioChannel> getVoiceChannel() {
		return GuildConfig.getChannel(voiceChannel, AudioChannel.class);
	}
}
