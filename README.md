[![discord](https://img.shields.io/discord/1077255218728796192?label=slimecloud&style=plastic)](https://discord.gg/slimecloud)
[![jda-version](https://img.shields.io/badge/JDA--Version-5.0.0--beta.8-blue?style=plastic)](https://github.com/DV8FromTheWorld/JDA/releases/tag/v5.0.0-beta.8)

# SlimeBall Bot

### Der Discord Bot für den SlimeCloud Discord Server

Dieser Discord bot gehört zu dem deutschen Discord "[SlimeCloud](https://discord.gg/slimecloud)". Beachte das dies Projekt unter der [GNU Affero General Public License v3.0](https://github.com/SlimeCloud/java-SlimeBot/blob/master/LICENSE) läuft.

**Bitte beachtet, dass dieser Bot bzw. dieses Projekt noch sehr am Anfang steht, weshalb noch wenig dokumentiert ist und ggf. komplizierter ist als nötig. Aber lasst uns das doch zusammen ändern!**

Der Bot läuft mit der jdk version 17.

## Contributing

### Issues

In den Issues Sammeln wir jegliche ToDos bezüglich dieses Bots von Ideen bis Fehlern. Nutzt gerne die vorgeschlagenen templates um einen übersichtlich und verständlichen Issue zu erstellen.

### Pull requests

Solltet ihr einen Issue bearbeitet haben könnt ihr einen Pull request (PR) erstellen. Bitte beschreibt in diesem kurz, was ihr verändert habt und gebt ggf. an auf welchen Issue ihr euch bezieht.

### Commits

Ein Commit sollte einen kurzen aber verständlichen Titel tragen (fixed a bug ist kein aussagekräftiger Titel). In der Beschreibung könnt ihr dann z.B. den Bug genauer beschreiben, wie er entstanden ist und was der fix dafür war.
Beachtet dass ihr keine Commits doppelt erstellt.

### Sprache

**Im Code**: Wir geben Variablen, Methoden, Classes etc. Englische Namen. Texte die für den Anwender sichtbar sind verfassen wir auf Deutsch

**Kommunikation**: in Issues, PR, Commits etc. ist es euch freigestellt, ob ihr Deutsch oder Englisch nutzt.

### Conventions

**Benennung**: Variablen, IDs, Funktionen schreiben wir im camelCase; Klassen werden mit einem Großbuchstaben am Anfang geschrieben.

Ihr könnt ToDos auch gerne im Code mit `///ToDo` oder `///FixMe` notieren. Beachtet aber bitte das sowas schnell untergeht, wenn zusätzlich kein Issue erstellt wurde.

### Config

Wir nutzen zum speichern von bspw. UserIDs YAML-Files. Um dies einfacher zu gestallten benuzten wir die [Simple-YAML](https://github.com/Carleslc/Simple-YAML/) Lib.
Es gibt zudem eine [Config Class](https://github.com/SlimeCloud/java-SlimeBot/blob/master/src/main/java/com/slimebot/utils/Config.java) in der einige Methoden sind die man häufiger braucht.

Häufige fehler:
- **Das Value ist None**: Vergesst nich die Config zuerst mit `ymlconfig.load()` zu laden.
- **Das Value ist None**: `ymlconfig.get(PATH)` funktioniert nicht immer, nutzt stattdessen `ymlconfig.getString(PATH)` oder `ymlconfig.getInt(PATH)` etc..
- **.set() geht nicht**: Vergesst nicht eure änderungen mit `ymlconfig.save()` zu speichern

### Commands
Wir verwenden [DiscordUtils](https://github.com/MineKingBot/DiscordUtils) als library für das command handling. Um damit einen Befehl zu erstellen, kannst du einfach eine Klasse mit der `@ApplicationCommand`-Annotation erstellen und 
diese in der `Main`-Klasse bei den anderen Befehlen registrieren.<br>
Um das Verhalten des Befehls anzugeben, könnt ihr eine Methode mit der Annotation `@ApplicationCommandMethod` erstellen (sie muss nicht, sollte aber `performCommand` heißen). Um setup-code auszuführen, könnt ihr eine Methode mit der 
`@WhenFinished`-Annotation erstellen, die `setup` heißen sollte. Diese wird dann einmalig ausgeführt, nachdem der Befehl registriert wurde.<br>
Sowohl in einer `@WhenFinished`, also auch `@ApplicationCommand` Methoden können beliebig parameter vom Typ `CommandManager` oder `DiscordUtils` verwendet werden. Diesen wird automatisch die `DiscordUtils` und `CommandManager` Instanz, 
die verwendet wird, zugewiesen.<br>
In der `@ApplicationCommandMethod` Methode haben zusätzlich alle Parameter, die von `SlashCommandInteractionEvent` zuweisbar sind, beim Aufrufen der Methode den Wert des Events der Befehlsinteraktion.<br>
Um Optionen für den Befehl zu erstellen und zu verwenden, können weitere Parameter mit `@Option` in der `@ApplicationCommandMethod`-Methode hinzugefügt werden. Standardmäßig sind diese `required`, mit einem Annotation-Parameter kann 
dies jedoch geändert werden. Optionale Optionen ohne Wert haben beim Aufrufen der `@ApplicationCommandMethod`-Methode den Wert `null`. Bei primitiven typen sollte daher die Wrapper-Klasse verwendet werden (`int` -> `Integer`).
```java
@ApplicationCommand(name = "test", description = "Test-Befehl als Beispiel")
public class TestCommand {
	@ApplicationCommandMethod
    public void performCommand(SlashCommandInteractionEvent event, 
                               @Option(name = "text", description = "Ein text") String text,
                               @Option(name = "Anzahl", description = "Anzahl an Wiederholungen", required = false) Integer amount 
    ) {
		if(amount == null) amount = 1; //Standardwert für den Fall, dass kein Wert übergeben wird
		
		event.reply(text.repeat(amount)).queue();
    }
}
```
```java
//...
        .registerCommand(TestCommand.class)
```
Wenn ein Befehl registriert wurde, musst du dich nicht weiter um das manuelle erstellen von `CommandData` oder ähnlichem kümmern; Basieren auf den Annotationen werden die Befehle automatisch auf Discord erstellt.
Für weitere Informationen kannst du gerne im [DiscordUtils Wiki](https://github.com/MineKingBot/DiscordUtils/wiki/CommandManager) nachschauen.

## Fragen

Bei Fragen wendet euch gerne auf dem Discord im Channel *#👾│tüftlerecke* an die Community oder in einem Ticket direkt an das Team.
