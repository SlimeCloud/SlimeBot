[![discord](https://img.shields.io/discord/1077255218728796192?label=slimecloud&style=plastic)](https://discord.gg/slimecloud)
[![jda-version](https://img.shields.io/badge/JDA--Version-5.0.0--beta.18-blue?style=plastic)](https://github.com/DV8FromTheWorld/JDA/releases/tag/v5.0.0-beta.18)

# 👋 SlimeBall Bot

Diese Repository enthält den Quellcode für den `SlimeBall` Bot auf
dem [SlimeCloud Discord Server](https://discord.gg/slimecloud).
Der Bot steht unter ständiger Entwicklung, falls du Interesse daran hast mitzuwirken, lies zunächst bitte aufmerksam
die `README.md` Datei, um dich mit unseren Standards vertraut zu machen.
Wenn du Fragen hast oder dich mit anderen Entwicklern austauschen möchtest, kannst du gerne in
der [#👾│tüftlerecke](https://discord.com/channels/1077255218728796192/1098707158750724186) auf
dem [SlimeCloud Discord Server](https://discord.gg/slimecloud) vorbeischauen.

Dieses Projekt steht unter der [GNU Affero General Public License v3.0](https://github.com/SlimeCloud/java-SlimeBot/blob/master/LICENSE.md) Lizenz!

# 💻 Hosting

Der SlimeBall Bot benötigt eine Infrastruktur um zu funktionieren.
Um alle Features nutzen zu können, benötigst du eine **PostgreSQL Datenbank**. Es wird vorausgesetzt, dass du eine solche
bereits zur Verfügung hast.

Der SlimeBall Bot ist in der Programmiersprache Java geschrieben. Du benötigst zum Ausführen also ein JRE 17!

Zum Starten benötigst du 3 Dateien:

- `SlimeBot.jar`
- `config.json`
- `credentials`

Für jeden Commit wird automatisch eine JAR Datei erstellt. Du kannst diese
also [hier](https://github.com/SlimeCloud/java-SlimeBot/actions) herunterladen.
Beispieldateien für `config.json` und `credentials` befinden sich im Ordner `run_template`. Die `config.json` Datei kann
unverändert bleiben, in `credentials` musst du dein Discord Bot Token sowie optional einige andere Daten eintragen.

Anschließend kannst du die JAR Datei ausführen und der Bot sollte starten.

# 🏡 Entwicklungsumgebung

Als Entwicklungsumgebung (IDE) empfehlen wir IntelliJ (Community Version reicht aus). Andere IDE's funktionieren im
Normalfall auch, folgende Erklärungen beziehen sich jedoch ausschließlich auf die Verwendung von IntelliJ.

Beginne damit, das Projekt zu erstellen. Nutze dazu `File->New->Project from Version Control`. Gib
dort `https://github.com/SlimeCloud/java-SlimeBot.git` als URL an.
Dadurch wird das Projekt automatisch richtig konfiguriert. Überprüfe zur Sicherheit unter `File->Project Structure` ob
die JDK Version auf 17 Eingestellt ist und ändere die Einstellung gegebenenfalls.

Kopiere anschließend den Ordner `run_template` nach `run`. Trage dein Bot Token und andere Daten in `run/credentials`
ein.
Zum Ausführen kannst du die Run Configuration `Run` verwenden, die bereits im Projekt enthalten ist.

# 🧱 Struktur

Der Code des Bots hat eine klare Struktur. Dafür gibt es 4 Haupt-Packages:

- **config**: Enthält Klassen für die Konfiguration des Bots sowie die Engine für die Konfiguration von Servern
- **features**: Enthält weitere packages für die jeweiligen Funktionen des Bots
- **main**: Enthält Klassen, die den Kern des Bots bilden
- **util**: Enthält Klassen, die ausschließlich static Methoden enthalten, die gewisse Prozesse vereinfachen

# 🪞 Style-Guild

- **Formatierung**:
    - **Einrückung**: Code-Blöcke werden mit `TAB` Eingerückt
    - **Leerzeichen**: `if (x == y) y++;`
    - **Klammern**: Bei Statements, die nur eine Zeile lang sind, werden keine geschweiften Klammern um den Block
      gesetzt. Je nach länge des Statements, wird das Statement direkt nach den Kontrollblock oder ohne Klammern in die
      nächste Zeile geschrieben.
- **Methoden**: Zur besseren Übersichtlichkeit und Lesbarkeit sollten Methoden nicht zu lang sein.
  Stattdessen sollten mehrere kleinere Methoden mit aussagekräftigen Namen verwendet werden, auch wenn diese nur einmal
  verwendet werden.
  Zusätzlich sollten Methoden in sich eine gewisse Struktur gaben. Dazu sollen sie in Abschnitte gegliedert sein, die
  mit Leeren Zeilen getrennt sind. Dadurch wird eine "Wall-Of-Text" vermieden.
- **Kommentare & Dokumentation**: Um es anderen Menschen einfacher zu machen, deinen Code zu lesen, sollte er grob (auf
  Englisch!) kommentiert sein. Es reicht, einzelnen Abschnitten kurze überschriften zu geben, um deren Funktionsweise zu
  erläutern.
  Außerdem sollten alle Methoden Parameter und Rückgabewerte mit `@NotNull` oder `@Nullable` annotiert sein, um zu
  kennzeichnen, wie mit `null` umgegangen wird.
- **Optionals & Streams**: Dieses Projekt verwendet die java `Optional` und `Stream` API

# 🤖 Befehle

Um Discord-Befehl zu erstellen und verwalten verwenden wir [Discord Utils](https://github.com/Utils4J/DiscordUtils). Es
folgt eine kurze Erklärung für das Wichtigste, für genauere Informationen kannst
du [hier](https://github.com/Utils4J/DiscordUtils#command-manager) nachlesen.

Jeder Befehl hat seine eigene Klasse im Package der entsprechenden Funktion. Die Klasse muss die
Annotation `@ApplicationCommand` haben. Hier wird auch der Name des Befehls angegeben.
Wenn ein Nutzer den Befehl ausführt, wird die Methode mit `@ApplicationCommandMethod` in dieser Klasse ausgeführt. Der
Befehl muss zusätzlich in der `SlimeBot` Klasse registriert werden.

Beispiel:

```java

@ApplicationCommand(name = "test")
public class TestCommand {
	@ApplicationCommandMethod
	public void performCommand(@NotNull SlashCommandInteractionEvent event) {
		event.reply("test").setEphemeral(true).queue();
	}
}
```

```java
...
manager.registerCommand(TestCommand .class);
...
```

Um Optionen hinzuzufügen, können einfach Parameter mit der `@Option`-Annotation zur Methode hinzugefügt. Diese optionen
werden automatisch registriert und beim Ausführen mit den Werten gefüllt.
Sub-Klassen einer Befehlsklasse werden automatisch als Subcommands registriert, wenn sie ebenfalls
die `@ApplicationCommand`-Annotation haben. Sie müssen dann NICHT separat registriert werden.

# 🔧 Konfiguration

Für die Konfiguration von Servern gibt es die `GuildConfig` Klasse. Sie enthält Felder und Kategorien, die mit `@ConfigField` oder `@ConfigCategory` annotiert sind.
Aus diesen informationen wird automatisch ein `config`-Befehl erstellt.

Um die Konfiguration eines Servers zu laden, kannst du `SlimeBall#loadGuild` verwenden. Um Zugriff zur SlimeBot instanz zu bekommen, solltest du sie per Konstruktor übergeben.

# 🗄️ Datenbank

Für Zugriffe auf die Datenbank verwenden wir [Java Utils](https://github.com/Utils4J/JavaUtils). Es folgt eine kurze
Erklärung für das Wichtigste, für genauere Informationen kannst du [hier](https://github.com/Utils4J/JavaUtils#database)
nachlesen.

Für jede Datenbank-Tabelle gibt es eine Variable in `SlimeBot`. Diese hat im einfachsten Fall den Typ `Table<T>`, in den
meisten Fällen gibt es jedoch ein Wrapper-Interface für die Tabelle, um spezielle Methoden hinzuzfügen.

Beispiel:

```java

@Getter
@AllArgsConstructor
public class Test {
	@Column(key = true)
	private long guild;

	@Column
	public String text;

	public Test() {
		this(0, null);
	}
}
```

```java
public interface TestTable extends Table<Test> {
	default void create(@NotNull Guild guild) {
		insert(new Test(guild.getIdLong(), guild.getName()));
	}
}
```

```java
...
private final TestTable testTable;
...

testTable = (TestTable) database.getTable(TestTable.class, Test.class, Test::new, "test").createTable();
```
