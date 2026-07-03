package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RoughLife;
import io.github.andrewwwwwwwwwwwwwww.roughlife.entity.WailingSkull;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class WailingSkullRenderer extends MobRenderer<WailingSkull, LivingEntityRenderState, WailingSkullModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "wailing_skull"), "main");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "textures/entity/wailing_skull.png");

    public WailingSkullRenderer(EntityRendererProvider.Context context) {
        super(context, new WailingSkullModel(context.bakeLayer(LAYER)), 0.3f);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
