[![discord](https://img.shields.io/discord/1077255218728796192?label=slimecloud&style=plastic)](https://discord.gg/slimecloud)
[![jda-version](https://img.shields.io/badge/JDA--Version-5.0.0--beta.11-blue?style=plastic)](https://github.com/DV8FromTheWorld/JDA/releases/tag/v5.0.0-beta.11)

# 👋 SlimeBall Bot
Diese Repository enthält den Quellcode für den `SlimeBall` Bot auf dem [SlimeCloud Discord Server](https://discord.gg/slimecloud).
Der Bot steht unter ständiger Entwicklung, falls du Interesse daran hast mitzuwirken, schau dir doch die [Contributing-Sektion](#-contributing) an.

Dieses Projekt steht unter der [GNU Affero General Public License v3.0](https://github.com/SlimeCloud/java-SlimeBot/blob/master/LICENSE) Lizenz!

1. [Issues](#-issues)
2. [Mitmachen](#-contributing)
3. [Style-Guide](#-style-guide)
4. [Datenbank](#-datenbank)
5. [Befehle](#-befehle-und-zugehörige-events)

## ❗ Issues
Wir verwenden GitHub issues um Fehler und Feature-Anfragen zu verwalten.
Auch wenn du nicht selbst programmieren kannst, kannst du gerne [einen Issue erstellen](hhttps://github.com/SlimeCloud/java-SlimeBot/issues/new/choose), wenn du
eine Idee für ein Feature hast oder einen Fehler melden möchtest.
Die Entwickler suchen sich regelmäßig die dringendsten Issues, um diese umzusetzen.

## 💻 Contributing
Wenn du die Programmiersprache Java selbst beherrschst und dich mit der [JDA-Bibliothek](https://github.com/discord-jda/JDA) auskennst, kannst du gerne neue Features programmieren oder Fehler reparieren.<br>
Dazu kannst du [die Repository forken](https://github.com/SlimeCloud/java-SlimeBot/fork) und in deiner eigenen Kopie einen neuen Branch für deine Änderungen anlegen (Halte dich dabei an die [Styleguides](#-style-guide)!).
Sobald du mit deinen Änderungen begonnen hast, kannst du [eine Pull-request erstellen](https://github.com/SlimeCloud/java-SlimeBot/compare).<br>
Wenn die Änderungen noch nicht fertig sind, solltest du sie als Draft erstellen, um zu zeigen, dass du noch nicht fertig bist.
Im Text der Pull-request oder des Drafts gibst du an, was du verändert hast.
Durch der erstellen der Pull-request zeigst du anderen Entwicklern woran du arbeitest und die Maintainer können dir Hinweise geben, wenn du etwas anders angehen solltest.
<br>
Sobald deine Änderungen fertig sind, kannst du den Draft als "Ready for Review" markieren, um einen Maintainer der Repository darum zu bitten, deine Änderungen zu verifizieren und letztendlich in den `master`-Branch zu übernehmen.

## 🏡 Entwicklungsumgebung


## 🪞 Style-Guide

## 🔧 Konfiguration
Allgemeine Konfiguration für den Bot wird in der `config`-Datei im gleichen Ordner wie der Bot durchgeführt.
Eine Vorlage für die Konfiguration ist in der `cnfig_preset`-Datei zu finden.<br>
Zum Lesen der Konfiguration verwenden wir [Gson](https://github.com/google/gson).
Im code sind die Konfigurationsfelder in der `Config`-Klasse lesbar.
Eine Instanz dieser Klasse, die verwendet werden sollte befindet sich in `Main.config`.<br>
Um selbst Konfigurationsfelder hinzuzufügen, kann einfach eine Variable in der `Config`-Klasse erstellt werden.
Beim Lesen der Konfiguration wird das Feld automatisch mitgelesen, ohne dass du zusätzlichen code schreiben musst.<br>
Bei Konfigurationsfeldern, die für neue Funktionen im bot benötigt werden, sollte vor dem Initialisieren der Funktion überprüft werden, ob das Konfigurationsfeld in der `Config`-Klasse einen Wert hat, und falls kein Wert vorhanden ist,
statt dem Initialisieren der Funktion eine Warnung ausgeben.

## 🗄️ Datenbank
Wir verwenden eine [PostgreSQL](https://www.postgresql.org/) Datenbank um Server-Konfigurationen und andere Daten zu speichern.
Zur Interaktion mit der Datenbank verwenden wir [JDBI-Bibliothek](https://jdbi.org/).<br>
In der `Database` Klasse werden die Tabellen erstellt, mit denen dann später mit den `handle` und `run` Methoden in der gleichen Klasse interagiert wird.
Die `handle`-Methode hat dabei einen Rückgabewert, und sollte daher für `select` SQL Befehle verwendet werden, während `run` keinen Rückgabewert hat und ist daher eher für `insert` oder `update` Befehle geeignet.

## 🤖 Befehle und zugehörige Events
Discord Befehle erstellen und verarbeiten wir mit der [DiscordUtils Bibliothek](https://github.com/MineKingBot/DiscordUtils).
Die Bibliothek wird zum Registrieren und Bearbeiten der Befehle verwendet, aber auch um zugehörige Events zu handhaben.
Im Folgenden werden die für diesen Bot nötigen Grundlagen erklärt, für detaillierte Informationen kannst du im [DiscordUtils Wiki](https://github.com/MineKingBot/DiscordUtils/wiki/CommandManager) nachschauen.

### Befehle
Einen einfachen Befehl zu erstellen ist recht einfach. So sieht due Grundstruktur aus:
```java
@ApplicationCommand(name = "test", description = "Test Command")
public class TestCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		//Diese Methode wird bei Interaktion mit den Befehl aufgerufen
	}
}
```
Der Befehl muss anschließend im `CommandManager` registriert werden:
```java
.useCommandManager(
		...,
		config -> {
			//...
			config.registerCommand(TestCommand.class);
		}
)
```
Um Optionen hinzuzufügen, können parameter mit der `@Option`-Annotation zur `performCommand`-Methode hinzugefügt werden. Wenn Optionen optional sind und kein Wert angegeben wurde, haben sie beim Aufrufen der Methode der Wert `null`.
Optionen mit primitiven typen (wie `int` oder `boolean`) muss bei optionalen Optionen daher die Wrapper Klasse verwendet werden (`int` -> `Integer`, `boolean` -> `Boolean`).
```java
@ApplicationCommand(name = "test", description = "Test Command")
public class TestCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event,
                               @Option(name = "text", description = "Ein Text") String text,
                               @Option(name = "anzahl", description = "Anzahl der Wiederholungen", required = false) Integer amount //Optionale Option
	) {
		if(amount == null) {
			amount = 1;
		}

		event.reply(text.repeat(amount)).setEphemeral(true).queue();
	}
}
```
Wenn dein Befehl code benötigt, der einmalig als Einrichtung benötigt wird, kann dieser code in die `setup`-Methode mit `@WhenFinished`-Annotation geschrieben werden.
Diese Methode wird ausgeführt, sobald der Befehl erfolgreich registriert wurde.
Ein Beispiel kann in der [Events-Sektion](#events) gefunden werden.

Sowohl in der `performCommand`-Methode, als auch in der `setup`-Methode haben Parameter mit den typen `DiscordUtils` und `CommandManager` Werte mit den aktuellen Instanzen dieser Klassen.
In der `performCommand`-Methode können zusätzlich Parameter mit den Typen `SlashCommandInteractionEvent`, `CommandContext` sowie Parameter mit der `@Option`-Annotation verwendet werden.
Der CommandContext wird in diesem Projekt jedoch aktuell nicht verwendet.

### Events
Um Events, die z.B. Components oder Modals von Befehlen zu handhaben, werden die Handler in der Klasse des Befehls registriert. Dazu wird aber kein ListenerAdapter verwendet, sondern der `EventManager` von DiscordUtils.<br>
Beispiel:
```java
@ApplicationCommand(name = "test", description = "Test Command")
public class TestCommand {
	@ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event) {
		event.replyModal(
				Modal.create("test:modal", "Titel")
                        .addActionRow(
								TextInput.create("test", "Test", TextInputStyle.SHORT)
                                        .build()
                        )
                        .build()
        ).queue();
    }
	
	@WhenFinished
    public void setup(DiscordUtils manager) { //Wird ausgeführt, sobald der Befehl registriert wurde
		manager.getEventManager().registerHandler(new ModalHandler("test:modal", event -> //Handler registrieren, der auf Modals mit der ID 'test:modal' hört
			event.reply(event.getValue("test").getAsString()).setEphemeral(true).queue()
		));
    }
}
```
