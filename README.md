[![discord](https://img.shields.io/discord/1077255218728796192?label=slimecloud&style=plastic)](https://discord.gg/slimecloud)
[![jda-version](https://img.shields.io/badge/JDA--Version-5.0.0--beta.18-blue?style=plastic)](https://github.com/DV8FromTheWorld/JDA/releases/tag/v5.0.0-beta.18)

# 👋 SlimeBall Bot
Diese Repository enthält den Quellcode für den `SlimeBall` Bot auf dem [SlimeCloud Discord Server](https://discord.gg/slimecloud).
Der Bot steht unter ständiger Entwicklung, falls du Interesse daran hast mitzuwirken, lies zunächst bitte aufmerksam die `README.md` Datei, um dich mit unseren Standards vertraut zu machen.
Wenn du Fragen hast oder dich mit anderen Entwicklern austauschen möchtest, kannst du gerne in der [#👾│tüftlerecke](https://discord.com/channels/1077255218728796192/1098707158750724186) auf dem [SlimeCloud Discord Server](https://discord.gg/slimecloud) vorbeischauen.

Dieses Projekt steht unter der [GNU Affero General Public License v3.0](https://github.com/SlimeCloud/java-SlimeBot/blob/master/LICENSE) Lizenz!

# 💻 Hosting
Der SlimeBall Bot benötigt eine Infrastruktur um zu funktionieren. 
Um alle Features nutzen zu können, benötigst du eine PostgreSQL Datenbank. Es wird vorausgesetzt, dass du eine solche bereits zur Verfügung hast.

Der SlimeBall Bot ist in der Programmiersprache Java geschrieben. Du benötigst zum Ausführen also ein JRE 17!

Zum Starten benötigst du 3 Dateien:
- SlimeBot.jar
- config
- credentials

Für jeden Commit wird automatisch eine JAR Datei erstellt. Du kannst diese also [hier](https://github.com/SlimeCloud/java-SlimeBot/actions) herunterladen.
Beispieldateien für `config` und `credentials` befinden sich im Ordner `run_template`. Die `config` Datei kann unverändert bleiben, in `credentials` musst du dein Discord Bot Token sowie optional einige andere Daten eintragen.

Anschließend kannst du die JAR Datei ausführen und der Bot sollte starten.

# 🏡 Entwicklungsumgebung
Als Entwicklungsumgebung (IDE) empfehlen wir IntelliJ (Community Version reicht aus). Andere IDE's funktionieren im Normalfall auch, folgende Erklärungen beziehen sich jedoch ausschließlich auf die Verwendung von IntelliJ.

Beginne damit, das Projekt zu erstellen. Nutze dazu `File->New->Project from Version Control`. Gib dort `https://github.com/SlimeCloud/java-SlimeBot.git` als URL an.
Dadurch wird das Projekt automatisch richtig konfiguriert. Überprüfe zur Sicherheit unter `File->Project Structure` ob die JDK Version auf 17 Eingestellt ist und ändere die Einstellung gegebenenfalls.

Kopiere anschließend den Ordner `run_template` nach `run`. Trage dein Bot Token und andere Daten in `run/credentials` ein.
Zum Ausführen kannst du die Run Configuration `Run` verwenden, die bereits im Projekt enthalten ist.

# 🪞 Style-Guild

# 🧱 Struktur

# 🔧 Konfiguration

# 🗄️ Datenbank

# 🤖 Befehle
