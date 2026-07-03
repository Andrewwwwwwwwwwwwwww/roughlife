package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RoughLife;
import io.github.andrewwwwwwwwwwwwwww.roughlife.entity.Razorwing;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class RazorwingRenderer extends MobRenderer<Razorwing, LivingEntityRenderState, RazorwingModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "razorwing"), "main");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "textures/entity/razorwing.png");

    public RazorwingRenderer(EntityRendererProvider.Context context) {
        super(context, new RazorwingModel(context.bakeLayer(LAYER)), 0.4f);
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
