package com.minecraft.shiny.scaner;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.ListTag;

import java.util.HashSet;
import java.util.Set;

@Mod(ShinyScanner.MODID)
public class ShinyScanner {

    public static final String MODID = "shinyscanner";
    private static final Set<Integer> trackedEntityIds = new HashSet<>();
    private static final Set<String> LEGENDARY_POKEMON_NAMES = Set.of(
    "Mewtwo",
    "Mew",
    "Artikodin",
    "Électhor",
    "Sulfura",
    "Registeel",
    "Regice",
    "Regirock",
    "Kyogre",
    "Groudon",
    "Rayquaza",
    "Jirachi",
    "Deoxys",
    "Lugia",
    "Ho-Oh",
    "Celebi",
    "Teracristal",
    "Dialga",
    "Palkia",
    "Giratina",
    "Cresselia",
    "Darkrai",
    "Shaymin",
    "Victini",
    "Zekrom",
    "Reshiram",
    "Kyurem",
    "Keldeo",
    "Meloetta",
    "Genesect",
    "Xerneas",
    "Yveltal",
    "Zygarde",
    "Hoopa",
    "Volcanion",
    "Magearna",
    "Marshadow",
    "Zeraora",
    "Meltan",
    "Melmetal",
    "Eternatus",
    "Kubfu",
    "Urshifu",
    "Zarude",
    "Regieleki",
    "Regidrago",
    "Wyrdeer",
    "Enamorus"
);

	 private static final Set<String> FRENCH_STARTER_POKEMON_NAMES = Set.of(
            "bulbizarre", "herbizarre", "florizarre",  // Bulbasaur line
            "salameche", "reptincel", "dracaufeu",  // Charmander line
            "carapuce", "carabaffe", "tortank",  // Squirtle line
            "germignon","héricendre","kaiminus","arcko","poussifeu","gobou","tortipouss",
			"ouisticram","tiplouf","vipélierre","gruikui","moustillon","marisson","feunnec","grenousse"
    );

    private static final Logger LOGGER = LoggerFactory.getLogger(ShinyScanner.class);
    private static final RenderType LINE_RENDER_TYPE = RenderType.debugLineStrip(10.0);

    public ShinyScanner() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(LINE_RENDER_TYPE);

        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(10.0F);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;

            String name = living.getName().getString().toLowerCase();
             boolean matchedName = LEGENDARY_POKEMON_NAMES.stream().anyMatch(name::contains) || FRENCH_STARTER_POKEMON_NAMES.stream().anyMatch(name::contains);

            if (matchedName) { //matchedName
                trackedEntityIds.add(entity.getId());

                Vec3 pos = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(camera);

                LOGGER.debug("Target found. ID: {}, Name: {}, Texture: {}", entity.getId(), name);
                LOGGER.debug("ESP Line to: ({}, {}, {})", pos.x, pos.y, pos.z);



                poseStack.pushPose();
                Matrix4f matrix = poseStack.last().pose();

                builder.vertex(matrix, 0f, 0f, 0f)
                        .color(255, 0, 255, 255)
                        .normal(0, 1, 0)
                        .endVertex();

                builder.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                        .color(255, 0, 255, 255)
                        .normal(0, 1, 0)
                        .endVertex();

                poseStack.popPose();
            }
        }

        buffer.endBatch();
        RenderSystem.enableDepthTest();
    }

    private boolean isShiny(String texturePath) {
        return texturePath.contains("shiny");
    }
	
	
    private boolean isLegendary(String texturePath) {
        for (String legendary : LEGENDARY_POKEMON_NAMES) {
            if (texturePath.contains(legendary.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}

