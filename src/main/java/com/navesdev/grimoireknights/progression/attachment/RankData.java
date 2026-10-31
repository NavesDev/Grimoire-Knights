package com.navesdev.grimoireknights.progression.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.navesdev.grimoireknights.progression.rules.Rank;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** The player's rank. Immutable: change with {@code setData}. */
public record RankData(Rank rank) {
    public static final RankData EMPTY = new RankData(Rank.RECRUIT);

    public static final Codec<Rank> RANK_CODEC = Codec.STRING.comapFlatMap(
            id -> Rank.byId(id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "unknown rank " + id)), Rank::id);

    public static final Codec<RankData> CODEC = RANK_CODEC.fieldOf("rank").codec().xmap(RankData::new, RankData::rank);

    public static final StreamCodec<RegistryFriendlyByteBuf, RankData> STREAM_CODEC = ByteBufCodecs.VAR_INT
            .<RegistryFriendlyByteBuf>cast().map(i -> new RankData(Rank.values()[i]), data -> data.rank().ordinal());
}
