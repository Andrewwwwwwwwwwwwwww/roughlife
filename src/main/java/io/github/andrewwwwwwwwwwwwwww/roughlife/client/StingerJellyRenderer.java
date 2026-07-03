package io.github.andrewwwwwwwwwwwwwww.roughlife.client;

import io.github.andrewwwwwwwwwwwwwww.roughlife.RoughLife;
import io.github.andrewwwwwwwwwwwwwww.roughlife.entity.StingerJelly;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class StingerJellyRenderer extends MobRenderer<StingerJelly, JellyRenderState, StingerJellyModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "stinger_jelly"), "main");
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "textures/entity/stinger_jelly.png");
    private static final Identifier TEXTURE_SULPHUR =
            Identifier.fromNamespaceAndPath(RoughLife.MOD_ID, "textures/entity/stinger_jelly_sulphur.png");

    public StingerJellyRenderer(EntityRendererProvider.Context context) {
        super(context, new StingerJellyModel(context.bakeLayer(LAYER)), 0.35f);
    }

    @Override
    public Identifier getTextureLocation(JellyRenderState state) {
        return state.sulphur ? TEXTURE_SULPHUR : TEXTURE;
    }

    @Override
    public JellyRenderState createRenderState() {
        return new JellyRenderState();
    }

    @Override
    protected RenderType getRenderType(JellyRenderState state, boolean visible, boolean invisible, boolean glowing) {
        // The jelly's texture is semi-transparent; the default cutout render
        // type discards those pixels and mangles it. Translucent is correct.
        return RenderTypes.entityTranslucent(this.getTextureLocation(state));
    }

    @Override
    public void extractRenderState(StingerJelly entity, JellyRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.sulphur = entity.isSulphur();
    }
}
