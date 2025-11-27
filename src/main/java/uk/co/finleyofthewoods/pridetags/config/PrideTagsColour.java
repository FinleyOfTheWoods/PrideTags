package uk.co.finleyofthewoods.pridetags.config;

import org.jetbrains.annotations.NotNull;

public record PrideTagsColour(String name, String colour) {
    public boolean isEmpty() {
        return this.name == null;
    }

    public @NotNull String toString() {
        return "PrideTagsTeam{name='" + this.name + "', colour='" + this.colour + "'}";
    }
}
