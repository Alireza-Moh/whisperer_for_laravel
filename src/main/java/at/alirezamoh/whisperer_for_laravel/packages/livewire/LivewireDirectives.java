package at.alirezamoh.whisperer_for_laravel.packages.livewire;

import java.util.Arrays;
import java.util.List;

public class LivewireDirectives {
    private LivewireDirectives() {}

    public static List<String> ACTION_DIRECTIVES = List.of("wire:click", "wire:submit", "wire:navigate");

    public static List<String> DATA_BINDING_DIRECTIVES = List.of("wire:model");

    public static List<String> LOADING_STATE_DIRECTIVES = List.of("wire:loading", "wire:current", "wire:dirty");

    public static List<String> CONFIRMATION_DIRECTIVES = List.of("wire:confirm", "wire:transition");

    public static List<String> INITIALIZATION_DIRECTIVES = List.of("wire:init", "wire:poll");

    public static List<String> NETWORK_DIRECTIVES = List.of("wire:offline");

    public static List<String> DOM_DIRECTIVES = List.of("wire:ignore", "wire:replace");

    public static List<String> STREAM_DIRECTIVES = List.of("wire:stream");

    public static List<List<String>> DIRECTIVES = Arrays.asList(
        ACTION_DIRECTIVES,
        DATA_BINDING_DIRECTIVES,
        LOADING_STATE_DIRECTIVES,
        CONFIRMATION_DIRECTIVES,
        INITIALIZATION_DIRECTIVES,
        NETWORK_DIRECTIVES,
        DOM_DIRECTIVES,
        STREAM_DIRECTIVES
    );
}
