package com.slimebot.main;

import com.slimebot.report.Report;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public class Database {
	public final static Logger logger = LoggerFactory.getLogger(Database.class);

	public Jdbi jdbi;

	public Database() {
		if(Main.config.database == null) {
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
		});

		jdbi.registerRowMapper(Report.class, new Report.ReportRowMapper());
	}

	public <T, U> U handle(Class<T> type, Function<T, U> handler) {
		if(jdbi == null) {
			logger.warn("Versuchter Datenbankaufruf nicht möglich: Keine Datenbank konfiguriert");
			return null;
		}

		return jdbi.withExtension(type, handler::apply);
	}

	public <T> void run(Class<T> type, Consumer<T> handler) {
		if(jdbi == null) {
			logger.warn("Versuchter Datenbankaufruf nicht möglich: Keine Datenbank konfiguriert");
			return;
		}

		jdbi.useExtension(type, handler::accept);
	}

	public void run(Consumer<Handle> handler) {
		if(jdbi == null) {
			logger.warn("Versuchter Datenbankaufruf nicht möglich: Keine Datenbank konfiguriert");
			return;
		}

		jdbi.useHandle(handler::accept);
	}

	public <T> T handle(Function<Handle, T> handler) {
		if(jdbi == null) {
			logger.warn("Versuchter Datenbankaufruf nicht möglich: Keine Datenbank konfiguriert");
			return null;
		}

		return jdbi.withHandle(handler::apply);
	}
}
