package de.slimecloud.slimeball.features.staff.meeting.protocol;

import net.dv8tion.jda.api.audio.AudioReceiveHandler;
import net.dv8tion.jda.api.audio.UserAudio;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AudioReceiver implements AudioReceiveHandler
{
	private static final double VOLUME = 1.0;
	private final Map<Long, List<byte[]>> received = new LinkedHashMap<>();

	@Override
	public boolean canReceiveUser() {
		return true;
	}

	@Override
	public void handleUserAudio(UserAudio userAudio) {
		getList(userAudio.getUser().getIdLong()).add(userAudio.getAudioData(VOLUME));
	}

	private List<byte[]> getList(Long id) {
		if (received.containsKey(id)) return received.get(id);
		List<byte[]> list = new LinkedList<>();
		received.put(id, list);
		return list;
	}

	public Long[] getUsers() {
		return received.keySet().toArray(Long[]::new);
	}

	public byte[] getBytes(Long id) {
		int size = 0;
		for (byte[] bytes : received.get(id)) size += bytes.length;
		byte[] data = new byte[size];
		int i = 0;
		for (byte[] bytes : received.get(id)) {
			for (byte b : bytes) {
				data[i++] = b;
			}
		}
		return data;
	}

	public AudioInputStream getAudioStream(Long id) {
		byte[] data = getBytes(id);
		return new AudioInputStream(new ByteArrayInputStream(data), OUTPUT_FORMAT, data.length);
	}

}
