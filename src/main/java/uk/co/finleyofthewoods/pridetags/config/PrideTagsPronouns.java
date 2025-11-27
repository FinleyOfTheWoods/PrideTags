package uk.co.finleyofthewoods.pridetags.config;

import org.jetbrains.annotations.NotNull;

public record PrideTagsPronouns(String name, String display) {
    public @NotNull String toString() {
        return "PrideTagPronoun{name='" + this.name + "', display='" + this.display + "'}";
    }
}
