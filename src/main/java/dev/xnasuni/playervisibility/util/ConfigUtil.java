package dev.xnasuni.playervisibility.util;

import dev.xnasuni.playervisibility.PlayerVisibility;
import dev.xnasuni.playervisibility.types.FilterType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

public class ConfigUtil {
    private static final File filteredPlayersFile = new File(PlayerVisibility.configDirectory.resolve("pv-filtered-players.txt").toUri());
    private static final File filterTypeFile = new File(PlayerVisibility.configDirectory.resolve("pv-filter-type.txt").toUri());
    private static final File filterEnabledFile = new File(PlayerVisibility.configDirectory.resolve("pv-filter-enabled.txt").toUri());

    public static String[] getFilteredPlayers() throws IOException {
        String[] filteredPlayers;
        if (filteredPlayersFile.canRead()) {
            try {
                List<String> allLines = Files.readAllLines(Paths.get(filteredPlayersFile.toURI()));
                filteredPlayers = allLines.toArray(new String[0]);
            } catch (IOException e) {
                throw new IOException(e.getMessage());
            }
        } else {
            throw new IOException("Cannot read file filtered players");
        }
        return filteredPlayers;
    }

    public static FilterType getFilterType() throws IOException {
        FilterType filterType;
        if (filterTypeFile.canRead()) {
            try {
                List<String> allLines = Files.readAllLines(Paths.get(filterTypeFile.toURI()));
                filterType = FilterType.valueOf(allLines.get(0));
            } catch (IOException e) {
                return FilterType.WHITELIST;
            }
        } else {
            throw new IOException("Cannot read file filter type");
        }
        return filterType;
    }

    public static boolean getFilterEnabled() throws IOException {
        boolean toggleState;
        if (filterEnabledFile.canRead()) {
            try {
                List<String> allLines = Files.readAllLines(Paths.get(filterEnabledFile.toURI()));
                toggleState = Objects.equals(allLines.get(0), "true");
            } catch (IOException e) {
                return true;
            }
        } else {
            throw new IOException("Cannot read file toggle state");
        }
        return toggleState;
    }

    public static void save(String[] whitelistedPlayers, FilterType filterType, Boolean toggleState) throws IOException {
        String whitelistedPlayersString = String.join("\n", whitelistedPlayers);
        Files.write(filteredPlayersFile.toPath(), whitelistedPlayersString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.write(filterTypeFile.toPath(), filterType.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        Files.write(filterEnabledFile.toPath(), toggleState.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
