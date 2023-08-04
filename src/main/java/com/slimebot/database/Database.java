package com.slimebot.database;

import com.slimebot.level.Level;
import com.slimebot.main.Main;
import com.slimebot.report.Report;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class Database {

	public Jdbi jdbi;

	public Database() {
		if (Main.config.database == null) {
			return;
		}

		jdbi = Jdbi.create("jdbc:postgresql://" + Main.config.database.host, Main.config.database.user, Main.config.database.password);

		run(handle -> {
			/*
			Hier werden Tabellen erstellt.
			Wenn du selbst eine Tabelle benötigst, kannst du hier einen 'create table if not exists' SQL Befehl ausführen.
			.execute() nicht vergessen!
			 */
			handle.createUpdate("create table if not exists spotify_known(id text)").execute();

			handle.createUpdate("create table if not exists report_blocks(guild bigint, \"user\" bigint)").execute();
			handle.createUpdate("create table if not exists reports(guild bigint, id serial, issuer bigint, target bigint, type text, time timestamp default now(), message text, status text default 'OPEN', closeReason text)").execute();

			handle.createUpdate("create table if not exists levels(guild bigint, \"user\" bigint, level int, xp int, messages int, primary key(guild, \"user\"))").execute();
		});

		/*
		Hier kannst du RowMapper registrieren. Mit diesen gibst du an, wie java objekte aus einer SQL Tabellen Reihe erstellt werden können.
		 */
		jdbi.registerRowMapper(Report.class, new Report.ReportRowMapper());
	}

	/**
	 * Erstellt einen Kontext, in dem du mit der Datenbank interagieren kannst. Dieser wird automatisch geschlossen.
	 * <p><b>Beispiel</b>
	 * <pre>{@code
	 * Main.database.run(handle -> handle.createUpdate("insert into test values('test', 5)").execute());
	 * }</pre>
	 *
	 * @param handler Ein handler, in dem du deinen Datenbank-Code ausführst.
	 */
	public void run(Consumer<Handle> handler) {
		if (jdbi == null) {
			logger.warn("Versuchter Datenbankaufruf nicht möglich: Keine Datenbank konfiguriert");
			return;
		}

		jdbi.useHandle(handler::accept);
	}

	/**
	 * Erstellt einen Kontext, in dem du mit der Datenbank interagieren kannst. Dieser wird automatisch geschlossen.
	 * <p><b>Beispiel</b>
	 * <pre>{@code
	 * Main.database.run(handle -> handle.createUpdate("insert into test values('test', 5)").execute());
	 * }</pre>
	 *
	 * @param handler Eine Funktion, die Daten aus der Datenbank umwandelt.
	 * @return Den Rückgabewert der handler-Funktion
	 */
	public <T> T handle(Function<Handle, T> handler) {
		if (jdbi == null) {
			logger.warn("Versuchter Datenbankaufruf nicht möglich: Keine Datenbank konfiguriert");
			return null;
		}

		return jdbi.withHandle(handler::apply);
	}

	public static Logger getLogger() {
		return logger;
	}
}
