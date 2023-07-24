[![discord](https://img.shields.io/discord/1077255218728796192?label=slimecloud&style=plastic)](https://discord.gg/slimecloud)
[![jda-version](https://img.shields.io/badge/JDA--Version-5.0.0--beta.11-blue?style=plastic)](https://github.com/DV8FromTheWorld/JDA/releases/tag/v5.0.0-beta.11)

# 👋 SlimeBall Bot
Diese Repository enthält den Quellcode für den `SlimeBall` Bot auf dem [SlimeCloud Discord Server](https://discord.gg/slimecloud).
Der Bot steht unter ständiger Entwicklung, falls du Interesse daran hast mitzuwirken, schau dir doch die [Contributing-Sektion](#-contributing) an.
Wenn du Fragen hast oder dich mit anderen Entwicklern austauschen möchtest, kannst du in der [#👾│tüftlerecke](https://discord.com/channels/1077255218728796192/1098707158750724186) auf dem [SlimeCloud Discord Server](https://discord.gg/slimecloud) vorbeischauen.

Dieses Projekt steht unter der [GNU Affero General Public License v3.0](https://github.com/SlimeCloud/java-SlimeBot/blob/master/LICENSE) Lizenz!

1. [Issues](#-issues)
2. [Mitmachen](#-contributing)
3. [Style-Guide](#-style-guide)
4. [Datenbank](#-datenbank)
5. [Befehle](#-befehle-und-zugehörige-events)
   1. [Grundlagen](#befehle)
   2. [Zusätzlicher Registrationscode](#setup)
   3. [Events](#events)

## ❗ Issues
Wir verwenden GitHub issues um Fehler und Feature-Anfragen zu verwalten.
Auch wenn du nicht selbst programmieren kannst, kannst du gerne [einen Issue erstellen](hhttps://github.com/SlimeCloud/java-SlimeBot/issues/new/choose), wenn du
eine Idee für ein Feature hast oder einen Fehler melden möchtest.
Die Entwickler suchen sich regelmäßig die dringendsten Issues, um diese umzusetzen.

In GitHub Issues und Pull-Requests gibt es oft ToDo-Listen. Diese haben folgendes Format:
```markdown
- [ ] Item A
- [ ] Item B
- [x] Item C
```
Achte darauf, zum Abhaken ein einfaches 'x' zwischen die eckigen Klammern zu setzten. Bitte füge keine weiteren Leerzeichen ein, da die Formatierung von GitHub ansonsten nicht korrekt erkannt wird!

## 💻 Contributing
Wenn du die Programmiersprache Java selbst beherrschst und dich mit der [JDA-Bibliothek](https://github.com/discord-jda/JDA) auskennst, kannst du gerne neue Features programmieren oder Fehler reparieren.<br>
Suche dir entweder einen Issue aus, den du bearbeiten möchtest (und gib ihn in der Pull-request an) oder ändere etwas, das du unabhängig von einem bestehenden Issue ändern möchtest. 
Beachte jedoch, dass wenn bei einem Issue bereits eine Person als Beauftragter markiert ist, diese Person für den Issue zuständig ist und du dich nicht um diesen Issue kümmern solltest!
Ein Grund dafür ist zum Beispiel, dass interne Besprechungen mit einem Teammitglied stattgefunden haben, bei deinen Details zur Umsetzung vereinbart wurden.
Du kannst unter solchen Issues oder dazugehörigen Pull-requests trotzdem gerne kommentieren und deine Vorschläge einbringen.
Dazu kannst du [die Repository forken](https://github.com/SlimeCloud/java-SlimeBot/fork) und in deiner eigenen Kopie einen neuen Branch für deine Änderungen anlegen (Halte dich dabei an die [Styleguides](#-style-guide)!).
Sobald du mit deinen Änderungen begonnen hast, kannst du [eine Pull-request erstellen](https://github.com/SlimeCloud/java-SlimeBot/compare).<br>
Wenn die Änderungen noch nicht fertig sind, solltest du sie als Draft erstellen, um zu zeigen, dass du noch nicht fertig bist.
Im Text der Pull-request oder des Drafts gibst du an, was du verändert hast.
Durch der erstellen der Pull-request zeigst du anderen Entwicklern woran du arbeitest und die Maintainer können dir Hinweise geben, wenn du etwas anders angehen solltest.
<br>
Sobald deine Änderungen fertig sind, kannst du den Draft als "Ready for Review" markieren, um einen Maintainer der Repository darum zu bitten, deine Änderungen zu verifizieren und letztendlich in den `master`-Branch zu übernehmen.

## 🏡 Entwicklungsumgebung
Dieser Bot verwendet java 17! Wenn du den bot selber verwenden oder an der Entwicklung teilnehmen möchtest, wird vorausgesetzt, dass du bereits ein JRE/JDK 17 installiert hast, und weißt, wie du es verwendest!<br>

Für die Entwicklung empfehlen wir [IntelliJ](https://www.jetbrains.com/idea/download/) (Community Version reicht aus) als Entwicklungsumgebung. Andere IDE's können auch verwendet werden, folgende Erklärungen beziehen sich jedoch 
ausschließlich auf IntelliJ.

Beginne damit, eines Projekt zu erstellen. Nutze dazu das Menü `File->New->Poject from Version Control`. 
Gib dort als URL die URL deines Forks der Repository an. Dadurch wird ein Projekt erstellt, das den Sourcecode sowie die Buildscripts der aktuellen Version des SlimeBallBots enthält.
In der `Project Structure` des Projekts muss das JDK 17 angeben werden. 

Um den Bot zu starten, musst du zunächst das Token deines Test-Bots in die `.env` Datei als `TEST` token eintragen. Ein Preset für die Datei findest du in der `.env_template` Datei.
Es wird vorausgesetzt, dass du bereits einen Bot Account im [Developer Portal](https://discord.com/developers) erstellt hast, und weißt, wie du das Token kopieren kannst.

Anschließend muss die Datei `config_template` nach `config` kopiert werden. 
In dieser Datei müssen nun die Credentials der Datenbank eingetragen werden, die du zum Testen verwenden möchtest. 
Es wird vorausgesetzt, dass du bereits eine PostgreSQL Datenbank zur Verfügung und einen Nutzer erstellt hast, den du Nutzen kannst.

Wenn du keine Spotify Application hast und die mit Spotify zusammenhängenden Features nicht nutzen möchtest, kannst du das `spotify` Objekt aus der `config` Datei löschen. 
Dadurch wird nicht versucht, die Spotify Listener zu starten und somit werden keine Fehlermeldungen bezüglich ungültiger spotify Tokens ausgegeben.

Du kannst nun die Run Konfiguration `Run` auswählen und starten. Der SlimeBallBot sollte starten und auf deine Befehle reagieren. Wenn du den Bot außerhalb deiner IDE verwenden möchtest, musst du ihn als `jar` exportieren.
Verwende dazu die `Package` Run Konfiguration.

## 🪞 Style-Guide
Um den Code übersichtlich und einheitlich zu halten, sollten sich alle an einen Codestyle halten. Im Folgenden werden die wichtigsten Richtlinien aufgezählt.

- **Einrückung:** Immer wenn ein neuer Codeblock eröffnet wird, wird um einen Tab weiter eingerückt. Diese Einrückungen werden mit `Tab` Zeichen vorgenommen und nicht mit Leerzeichen!
- **Neue Zeilen:** Generell starten Blöcke in der neuen Zeile nach dem Steuerblock. 
  Die geöffnete geschweifte Klammer steht dabei noch in der Zeile des Kontrollblocks. 
  Wenn ein Block nur aus einem weiteren Steuerblock wie `return` oder 
  `break` besteht, wird dieser in er Zeile des Kontrollblocks ohne geschweifte Klammern geschrieben:
  ```java
  if(false) return;
  if(true) {
    System.out.println("Test");
    int x = 0;
  }
  ```
- **Abstände:** Um den Code nicht gequetscht wirken zu lassen, werden zwischen einzelnen Teilen in einer Zeile Leerzeichen eingefügt:
  ```java
  for(int i = 0; i < 10; i++) {
    System.out.println((i + 1) + ". Test");
  }
  ```
- **Java API:** Wir verwenden die Java Stream- und Optional API. Das hat auch die starke Verwendung von Lambda-Ausdrücken zur Folge.
- **Namen & Sprache**: Alle Variablen, Klassen und Methoden im Code werden englisch benannt. Variablen und Methoden in lowerCamelCase und Klassen in UpperCamelCase.
  Alle Texte, die Nutzer sehen, sollen jedoch auf Deutsch verfasst werden.
- **Kommentare:** Mit Kommentare könnt ihr gerne Stellen beschreiben, bei denen nicht sofort klar wird, was der Code warum tut. Ihr müsst jedoch nicht allen Code kommentieren. Wenn ihr markieren möchtet, dass etwas in Zukunft geändert 
  werden soll, könnt ihr `//TODO` oder `//FIXME` Kommentare verwenden.

Als gute Richtlinie lassen sich die intelliJ Standard-Vorgaben verwenden. Wenn du deinen Code mit der intelliJ Funktion "Reformat Code" formatierst, werden die groben Formatierungen bereits automatisch angewendet.

## 🔧 Konfiguration
Allgemeine Konfiguration für den Bot wird in der `config`-Datei im gleichen Ordner wie der Bot durchgeführt.
Eine Vorlage für die Konfiguration ist in der `config_template`-Datei zu finden.<br>
Zum Lesen der Konfiguration verwenden wir [Gson](https://github.com/google/gson).
Im code sind die Konfigurationsfelder in der `Config`-Klasse lesbar.
Eine Instanz dieser Klasse, die verwendet werden sollte befindet sich in `Main.config`.<br>
Um selbst Konfigurationsfelder hinzuzufügen, kann einfach eine Variable in der `Config`-Klasse erstellt werden.
Beim Lesen der Konfiguration wird das Feld automatisch mitgelesen, ohne dass du zusätzlichen code schreiben musst.<br>
Bei Konfigurationsfeldern, die für neue Funktionen im bot benötigt werden, sollte vor dem Initialisieren der Funktion überprüft werden, ob das Konfigurationsfeld in der `Config`-Klasse einen Wert hat, und falls kein Wert vorhanden ist,
statt dem Initialisieren der Funktion eine Warnung ausgeben.

## 🗄️ Datenbank
Wir verwenden eine [PostgreSQL](https://www.postgresql.org/) Datenbank, um große Datenmengen zu speichern.
Zur Interaktion mit der Datenbank verwenden wir [JDBI-Bibliothek](https://jdbi.org/).<br>
In der `Database` Klasse werden die Tabellen erstellt, mit denen dann später mit den `handle` und `run` Methoden in der gleichen Klasse interagiert wird.
Die `handle`-Methode hat dabei einen Rückgabewert, und sollte daher für `select` SQL Befehle verwendet werden, während `run` keinen Rückgabewert hat und ist daher eher für `insert` oder `update` Befehle geeignet.

Um kleinere Datenmengen - wie zum Beispiel für Server Konfigurationen - verwenden wir json-files im `guild` Ordner. 
In diesem Ordner gibt es für jeden Server eine Datei `<server id>.json`. 
Sie enthält jegliche Konfiguration für den Server.
Um die Daten in Java zu verwenden wird - ähnlich wie bei der [Bot Konfiguration](#-konfiguration) - eine Java Klasse mit der gleichen Struktur wie die Datei erstellt, die dann mit den Daten aus der Datei befüllt wird.
Wenn du selbst ein neues Konfigurationsfeld benötigst, kannst du einfach eine Java Variable in der `GuildConfig` Klasse erstellen. 
Zusätzlich sollte eine getter-Methode erstellt werden, die ein Optional zurückgibt. 
Dadurch wird das Handhaben von nicht-gesetzten Konfigurationsfeldern vereinfacht.  

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
		//Diese Methode wird bei Interaktion mit dem Befehl aufgerufen
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

Sowohl in der `performCommand`-Methode, als auch in der `setup`-Methode haben Parameter mit den typen `DiscordUtils` und `CommandManager` Werte mit den aktuellen Instanzen dieser Klassen.
In der `performCommand`-Methode können zusätzlich Parameter mit den Typen `SlashCommandInteractionEvent`, `CommandContext` sowie Parameter mit der `@Option`-Annotation verwendet werden.
Der CommandContext wird in diesem Projekt jedoch aktuell nicht verwendet.

Mit den Annotationsparametern `guildOnly` und `feature` in `@ApplicationCommand` kann bestimmt werden, wann und wo Befehle sichtbar sind. 
Standardmäßig sind alle Befehle `Globalcommands`. 
Das bedeutet, dass sie auf allen Servern und in Privatnachrichten verwendet werden können. 
Mit dem Parameter `guildOnly` werden Befehle auf Server beschränkt und sind in Privatnachrichten Kanälen nicht mehr verwendbar.
Wenn der `feature` parameter einen nicht-leeren String als Wert hat, wird der Befehl als `Guildcommand` erstellt.
Solche Befehle werden für jeden Server einzeln erstellt, und pro Server nur dann, wenn in der Methode `Main#updateGuildCommands` `true` in der Map mit dem Wert des `feature` Parameters als Key hat.
Dadurch werden Befehle auf Servern nur dann aktiviert, wenn die notwendige Konfiguration für diesen Befehl auf dem Server vorhanden ist. 
Wichtig zu beachten ist, dass die Methode `Main#updateGuildCommands` bei `Guildcommands` immer aufgerufen werden muss, wenn die entsprechende Konfiguration geändert wird, 
um daraus hervorgehende Änderungen in der Liste der Befehle auch an Discord zu senden.  

### Setup
Wenn du nach dem Registrieren des Befehls Setup-Code für den Befehl ausführen möchtest, kannst du eine Methode mit der `@WhenFinished`-Annotation (mit dem Namen `setup`) erstellen.
Dies kann zum Beispiel verwendet werden, um `ListComand`s hinzuzufügen. Da diese `inherited` Commands sind, können sie nicht ohne weiteres als Subcommands der `annotaed` Commands hinzugefügt werden, in der setup Methode können sie 
jedoch manuell registriert werden.

### Events
Um Interaktions-Events für deine Befehle zu handhaben, können Methoden mit der `@Listener`-Annotation erstellt werden.
Beispiel:
```java
@ApplicationCommand(name = "test", description = "Test Command")
public class TestCommand {
	@ApplicationCommandMethod
	public void performCommand(SlashCommandInteractionEvent event) {
		event.replyModal(
			Modal.create("test:modal", "Titel")
				.addActionRow(TextInput.create("test", "Test", TextInputStyle.SHORT).build())
				.build()
		).queue();
	}
	
	@Listener(type = ModalHandler.class, filter = "test:modal") //Methode wird ausgeführt, wenn ein Nutzer mit einem Modal mit ID "test:modal" interagiert
	public void handleModal(ModalInteractionEvent event) {
		event.reply(event.getValue("test").getAsString()).setEphemeral(true).queue();
	}
}
```
Für Event Listener, die unabhängig von Befehlen sind, verwenden wir Klassen, die `ListenerAdapter` extenden und in der `Main`-Klasse als Listener registriert werden. 
Für jeden "Themenbereich" wird ein eigener Listener erstellt, in dem die Methoden für die jeweiligen Events überschrieben werden können.
