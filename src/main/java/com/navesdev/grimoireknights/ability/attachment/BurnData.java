package com.navesdev.grimoireknights.ability.attachment;

import java.util.Optional;
import java.util.UUID;

/** Fire burn on any living entity: ticks left and who set it burning (credited with the damage). Not saved; it wears off. */
public record BurnData(int ticksLeft, Optional<UUID> owner) {}
