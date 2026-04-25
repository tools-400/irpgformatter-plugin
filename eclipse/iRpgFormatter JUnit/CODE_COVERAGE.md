# Code Coverage für iRpgFormatter JUnit Tests

Code Coverage kann nicht direkt in RDi ermittelt werden, da EclEmma (die Eclipse
Code Coverage Lösung) nicht mit RDi kompatibel ist. RDi registriert die
erforderliche Eclipse Feature-Group `org.eclipse.rcp.feature.group` nicht, weshalb
EclEmma in keiner Version installierbar ist.

Als Workaround wird eine separate Eclipse-Installation verwendet.

## Voraussetzungen

- **RDi 9.8** installiert unter `C:\IBM\RDi_098` oder
  **RDi 9.6** installiert unter `C:\IBM\RDi_096` (mit Shared-Verzeichnis `C:\IBM\SDPShared`)
- **Eclipse 2026-03-R** (oder neuer) — [Download](https://www.eclipse.org/downloads/)

Das Verzeichnis `C:\IBM\*` ist nur ein Beispiel. Je nach Installation müssen die
Pfade angepasst werden.

## 1. Eclipse installieren

Eclipse IDE for Eclipse Committers (oder eine andere Edition mit PDE-Support)
in ein separates Verzeichnis installieren, z.B.:

```text
C:\workspaces\rdp_code_coverage\eclipse
```

Einen Workspace-Ordner anlegen:

```text
C:\workspaces\rdp_code_coverage\ws
```

## 2. EclEmma installieren

In Eclipse: **Help → Eclipse Marketplace → "EclEmma"** suchen und installieren.
Eclipse anschließend neu starten.

## 3. Java 8 JRE einrichten

Die Tests müssen mit Java 8 Syntax kompiliert werden (Compiler Compliance 1.8).

Über *Window → Preferences → Java → Installed JREs → Add → Standard VM*
den Pfad zur IBM J9 JRE aus der älteren RDi-Installation angeben:

```text
C:\IBM\RDi_096\jdk
```

Als **Default** setzen.

## 4. Java 11 JRE einrichten (für Testausführung)

Die Eclipse-Platform-Bundles in RDi 9.8 sind für Java 11 kompiliert. Daher
müssen die Tests mit einer Java-11-Runtime **ausgeführt** werden, obwohl sie
mit Java 8 Syntax kompiliert sind.

Über *Window → Preferences → Java → Installed JREs → Add → Standard VM*
den Pfad zum IBM Semeru JDK 11 aus RDi 9.8 angeben:

```text
C:\IBM\RDi_098\plugins\com.ibm.semeru.certified.jdk.x64.windows_9.8.6.202508070900\jdk
```

Dieses JDK **nicht** als Default setzen — es wird nur für die Testausführung
verwendet.

## 5. Target Platform konfigurieren

Die Projekte benötigen IBM-spezifische Bundles (LPEX, iSeries Tools, RSE, etc.),
die nur in RDi vorhanden sind. Diese werden über eine Target Platform bereitgestellt.

Über *Window → Preferences → Plug-in Development → Target Platform*:

1. **Add → Nothing: Start with an empty target definition → Next**
2. **Add → Directory** → Pfad je nach RDi-Version (siehe unten)
3. Name vergeben (z.B. `RDi 9.8` oder `RDi 9.6`)
4. **Finish**
5. Die neue Target Platform als **aktiv** markieren (Checkbox)
6. **Apply and Close**

### Target Platform für RDi 9.8

Ein Verzeichnis reicht — alle Plugins liegen direkt in der Installation:

| Verzeichnis        |
|--------------------|
| `C:\IBM\RDi_098`   |

### Target Platform für RDi 9.6

RDi 9.6 lagert einen Teil der Plugins (u.a. die IBM Toolbox mit der
`AS400`-Klasse) in ein separates Shared-Verzeichnis aus. Es müssen **zwei**
Verzeichnisse als Directory-Einträge hinzugefügt werden:

| Verzeichnis        | Inhalt                                    |
|--------------------|-------------------------------------------|
| `C:\IBM\RDi_096`   | RDi-Kerninstallation                      |
| `C:\IBM\SDPShared` | Shared Plugins (IBM Toolbox, JT400, etc.) |

## 6. Projekte importieren

Über *File → Import → General → Existing Projects into Workspace*
folgendes Root-Verzeichnis auswählen:

```text
c:\workspaces\rdp_098\workspace\irpgformatter-plugin\eclipse\
```

Folgende drei Projekte auswählen:

| Projekt                       | Typ              | Beschreibung                  |
|-------------------------------|------------------|-------------------------------|
| Lpex Menu Extension Plugin    | Eclipse Plugin   | Abhängigkeit des Core Plugins |
| iRpgFormatter Core Plugin     | Eclipse Plugin   | Formatter-Logik               |
| iRpgFormatter JUnit           | Java Projekt     | JUnit Tests                   |

**"Copy projects into workspace"** aktivieren, um das Original nicht zu verändern.

## 7. Compiler Compliance Level prüfen

Alle drei Projekte müssen mit Compiler Compliance Level **1.8** kompiliert werden.

Für jedes Projekt über *Rechtsklick → Properties → Java Compiler*:

- [x] Enable project specific settings
- Compiler compliance level: **1.8**

Falls nach dem Import Compile-Fehler auftreten: **Project → Clean → alle drei
Projekte auswählen → Clean**.

## 8. Tests mit Code Coverage ausführen

1. Rechtsklick auf das Projekt **iRpgFormatter JUnit**
2. **Coverage As → Coverage Configurations...**
3. Tab **JRE** → **Alternate JRE** auswählen → **IBM Semeru JDK 11**
4. **Coverage** klicken

> **Wichtig:** Code Coverage wird nur über **Coverage As** ermittelt, nicht über
> **Run As → JUnit Test**. EclEmma instrumentiert den Bytecode über den
> JaCoCo-Agent — ohne diese Instrumentierung werden keine Coverage-Daten erhoben.

## Fehlerbehebung

### `trustAnchors parameter must be non-empty`

Das Semeru JDK enthält möglicherweise eine leere `jssecacerts`-Datei, die den
TrustStore überschattet. Prüfen und ggf. löschen:

```text
C:\IBM\RDi_098\plugins\com.ibm.semeru.certified.jdk.x64.windows_9.8.6.202508070900\jdk\lib\security\jssecacerts
```

In Java hat `jssecacerts` Vorrang vor `cacerts`. Ist die Datei leer (32 Bytes =
leerer JKS-Header, 0 Zertifikate), schlagen alle SSL/TLS-Verbindungen fehl.

### `UnsupportedClassVersionError` (class file version 65.0 / 55.0)

- **Version 65.0 (Java 21):** Ein Projekt wurde mit einem zu neuen Compiler
  gebaut. Compiler Compliance Level auf 1.8 setzen und **Project → Clean**
  ausführen.
- **Version 55.0 (Java 11):** Die Tests laufen mit Java 8, aber die
  Eclipse-Platform-JARs erfordern Java 11. In der Run/Coverage Configuration
  unter **JRE → Alternate JRE** das Semeru JDK 11 auswählen.

### `StringConcatFactory cannot be resolved`

Ein Projekt verwendet den Java-8-Compiler, referenziert aber Klassen, die mit
Java 11+ kompiliert wurden und `StringConcatFactory` (ab Java 9) nutzen. Compiler
Compliance Level und **Project → Clean** prüfen.
