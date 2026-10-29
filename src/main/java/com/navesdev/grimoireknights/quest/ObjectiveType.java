package com.navesdev.grimoireknights.quest;

import com.mojang.serialization.MapCodec;

public record ObjectiveType<T extends Objective>(MapCodec<T> codec) {}
