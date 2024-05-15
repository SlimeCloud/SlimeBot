package de.slimecloud.slimeball.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.slimecloud.slimeball.config.Config;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.util.Random;

@Slf4j
public class Main {
	public final static Random random = new Random();

	public final static ZoneId timezone = ZoneId.of("Europe/Berlin");

	public final static Gson json = new Gson();
	public final static Gson formattedJson = new GsonBuilder()
			.setPrettyPrinting()
			.create();

	public static void main(String[] args) throws Exception {
		new SlimeBot(Config.readFromFile("config.json"), Dotenv.configure().filename("credentials").load());
	}
}
