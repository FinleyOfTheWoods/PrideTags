# PrideTags

PrideTags is a Minecraft Fabric mod that helps players express their identity in-game. It allows server administrators and players to manage and display pronouns and customise player tag colours.

## Features

*   **Pronoun Display**: Configurable system to display pronouns (e.g., He/Him, They/Them, She/Her, Neopronouns) for players.
*   **Colour Customisation**: Support for customising tag colours via `PrideTagsColourConfig`.
*   **Persistence**: Configurations are automatically saved and loaded during the server lifecycle.
*   **Commands**: In-game commands to manage teams and tags.

## Installation

1.  Install [Fabric Loader](https://fabricmc.net/).
2.  Download the latest release of PrideTags.
3.  Place the `.jar` file into your Minecraft `mods` folder.
4.  Launch the game/server.

## Configuration

Configuration files are generated in the `config/pride_tags/` directory after the first launch.

*   **pronouns.json**: Defines the list of available pronouns.
*   **colours.json**: Handles colour definitions for tags.

You can edit these JSON files to add custom pronouns or modify existing ones.

## Commands

The mod registers commands to manage tags (handled by `PrideTagsTeamCommand`).
*   Usage usually involves standard command structures (e.g., `/pridetags ...`). *Check in-game autocomplete for specific syntax.*

## Building from Source

This project uses Gradle. To build the mod locally:

1.  Clone the repository.
2.  Open a terminal in the project root.
3.  Run the build command:

    *   **Windows**: `gradlew.bat build`
    *   **Linux/macOS**: `./gradlew build`

The compiled `.jar` file will be located in `build/libs/`.

## License

This project is licensed under the terms found in `LICENSE.txt`.